package com.grupo3.compras.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.compras.data.Catalogo
import com.grupo3.compras.data.ComprasDatabase
import com.grupo3.compras.data.FavoritoEntity
import com.grupo3.compras.data.InteraccionEntity
import com.grupo3.compras.data.MostradoEntity
import com.grupo3.compras.data.aDominio
import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Contexto
import com.grupo3.compras.domain.Franja
import com.grupo3.compras.domain.Producto
import com.grupo3.compras.domain.Recomendacion
import com.grupo3.compras.domain.Recomendador
import com.grupo3.compras.domain.TipoInteraccion
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Permite probar la app a cualquier hora: "Hora real" usa el reloj del teléfono. */
enum class ModoHora(val etiqueta: String) {
    REAL("Hora real"),
    LABORAL("Horario laboral"),
    FIN_DE_SEMANA("Fin de semana")
}

class ComprasViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = ComprasDatabase.obtener(app).dao()
    private val motor = Recomendador()

    // ---------------- Contexto: día y hora ----------------
    private val _modoHora = MutableStateFlow(ModoHora.REAL)
    val modoHora: StateFlow<ModoHora> = _modoHora.asStateFlow()

    private val reloj: Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(30_000)
        }
    }

    val ahora: StateFlow<LocalDateTime> = combine(_modoHora, reloj) { modo, real -> fechaSegunModo(modo, real) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, LocalDateTime.now())

    val franja: StateFlow<Franja> = ahora.map { Franja.de(it) }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, Franja.de(LocalDateTime.now()))

    /** Día y momento actuales: lo que usa Naive Bayes. */
    val contexto: StateFlow<Contexto> = ahora.map { Contexto.de(it) }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, Contexto.de(LocalDateTime.now()))

    // ---------------- Datos guardados en el teléfono (Room) ----------------
    private val interacciones = dao.interaccionesFlow().map { lista -> lista.map { it.aDominio() } }
    private val favoritosIds: Flow<Set<String>> =
        dao.favoritosFlow().map { lista -> lista.map { it.productoId }.toSet() }

    val favoritos: StateFlow<Set<String>> = favoritosIds
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    val favoritosProductos: StateFlow<List<Producto>> = dao.favoritosFlow()
        .map { lista -> lista.mapNotNull { Catalogo.porId(it.productoId) } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Lo que la app ha aprendido: puntos por categoría, de mayor a menor. */
    val gustos: StateFlow<List<Pair<Categoria, Double>>> = interacciones
        .map { historial ->
            motor.puntosPorCategoria(historial, System.currentTimeMillis())
                .toList()
                .filter { it.second > 0.0 }
                .sortedByDescending { it.second }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Naive Bayes: probabilidad de cada categoría ahora. Vacío mientras no haya historial. */
    val probabilidades: StateFlow<List<Pair<Categoria, Double>>> = combine(interacciones, contexto) { historial, ctx ->
        if (historial.isEmpty()) {
            emptyList<Pair<Categoria, Double>>()
        } else {
            motor.probabilidadesPorCategoria(historial, ctx, System.currentTimeMillis())
                .toList()
                .sortedByDescending { it.second }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---------------- Recomendaciones ----------------
    /** Productos mostrados en los últimos lotes. Solo cambia al iniciar y con "Ver otras". */
    private val recientes = MutableStateFlow<List<String>?>(null)

    val recomendaciones: StateFlow<List<Recomendacion>> = combine(
        interacciones,
        favoritosIds,
        franja,
        contexto,
        recientes.filterNotNull()
    ) { historial, favs, franjaActual, contextoActual, mostrados ->
        motor.recomendar(
            catalogo = Catalogo.productos,
            historial = historial,
            favoritos = favs,
            franja = franjaActual,
            contexto = contextoActual,
            ahora = System.currentTimeMillis(),
            mostradosRecientes = mostrados
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Productos nuevos parecidos a lo que buscaste, abriste o guardaste. Cambian al instante con cada acción. */
    val relacionados: StateFlow<List<Recomendacion>> = combine(
        interacciones,
        favoritosIds,
        recomendaciones
    ) { historial, favs, principales ->
        motor.relacionados(
            catalogo = Catalogo.productos,
            historial = historial,
            favoritos = favs,
            excluir = principales.map { it.producto.id }.toSet(),
            ahora = System.currentTimeMillis()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _productoAbierto = MutableStateFlow<Producto?>(null)
    val productoAbierto: StateFlow<Producto?> = _productoAbierto.asStateFlow()

    private var ultimaBusqueda = ""

    init {
        viewModelScope.launch {
            recientes.value = dao.ultimosMostrados(LOTES_RECIENTES * 3)
            // Cada lote que se muestra queda guardado para rotarlo en la próxima visita.
            recomendaciones
                .map { lista -> lista.map { it.producto.id } }
                .filter { it.isNotEmpty() }
                .distinctUntilChanged()
                .collect { ids ->
                    val ahoraMs = System.currentTimeMillis()
                    dao.insertarMostrados(ids.map { MostradoEntity(productoId = it, fecha = ahoraMs) })
                }
        }
    }

    fun elegirModoHora(modo: ModoHora) {
        _modoHora.value = modo
    }

    /** "Ver otras": lo que se ve ahora baja de posición y aparecen otros productos. */
    fun verOtras() {
        val actuales = recomendaciones.value.map { it.producto.id }
        if (actuales.isEmpty()) return
        recientes.value = (actuales + (recientes.value ?: emptyList())).take(LOTES_RECIENTES * 3)
    }

    fun abrirProducto(producto: Producto) {
        _productoAbierto.value = producto
        registrar(producto, producto.categoria, TipoInteraccion.CLIC)
    }

    fun cerrarProducto() {
        _productoAbierto.value = null
    }

    fun alternarFavorito(producto: Producto) {
        viewModelScope.launch {
            if (producto.id in favoritos.value) {
                dao.quitarFavorito(producto.id)
            } else {
                dao.agregarFavorito(FavoritoEntity(producto.id, System.currentTimeMillis()))
                registrar(producto, producto.categoria, TipoInteraccion.FAVORITO)
            }
        }
    }

    /** Aprende de lo que se busca: guarda una búsqueda sobre el producto que mejor responde al texto. */
    fun registrarBusqueda(texto: String) {
        val limpio = Catalogo.normalizar(texto)
        if (limpio.length < 3 || limpio == ultimaBusqueda) return
        val mejor = Catalogo.mejorCoincidencia(texto) ?: return
        ultimaBusqueda = limpio
        registrar(mejor, mejor.categoria, TipoInteraccion.BUSQUEDA, texto.trim())
    }

    fun borrarMisDatos() {
        viewModelScope.launch {
            dao.borrarInteracciones()
            dao.borrarFavoritos()
            dao.borrarMostrados()
            recientes.value = emptyList()
            ultimaBusqueda = ""
        }
    }

    private fun registrar(producto: Producto?, categoria: Categoria, tipo: TipoInteraccion, termino: String? = null) {
        val fila = InteraccionEntity(
            productoId = producto?.id,
            categoria = categoria.name,
            interaccion = tipo.name,
            fecha = System.currentTimeMillis(),
            hora = ahora.value.hour,
            diaSemana = ahora.value.dayOfWeek.value,
            peso = tipo.peso,
            termino = termino
        )
        viewModelScope.launch { dao.insertarInteraccion(fila) }
    }

    private fun fechaSegunModo(modo: ModoHora, real: LocalDateTime): LocalDateTime = when (modo) {
        ModoHora.REAL -> real
        ModoHora.LABORAL ->
            real.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY)).atTime(10, 30)
        ModoHora.FIN_DE_SEMANA ->
            real.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)).atTime(20, 0)
    }

    private companion object {
        const val LOTES_RECIENTES = 6
    }
}
