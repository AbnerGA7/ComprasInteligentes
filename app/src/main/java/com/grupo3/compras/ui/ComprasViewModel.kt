package com.grupo3.compras.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.compras.data.Catalogo
import com.grupo3.compras.data.ComprasDatabase
import com.grupo3.compras.data.Ejemplos
import com.grupo3.compras.data.FavoritoEntity
import com.grupo3.compras.data.InteraccionEntity
import com.grupo3.compras.data.MostradoEntity
import com.grupo3.compras.data.aDominio
import com.grupo3.compras.domain.Actividad
import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Contexto
import com.grupo3.compras.domain.MotorRecomendador
import com.grupo3.compras.domain.Producto
import com.grupo3.compras.domain.ResultadoMotor
import com.grupo3.compras.domain.TipoInteraccion
import com.grupo3.compras.sensor.DetectorActividad
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Hora simulada para las demostraciones (el modo "Ahora" usa el reloj real). */
enum class ModoHora(val etiqueta: String) {
    AUTOMATICO("Ahora"),
    LABORAL("💼 Laboral"),
    FIN_DE_SEMANA("🎉 Fin de semana"),
    NOCHE("🌙 Noche")
}

enum class ModoActividad(val etiqueta: String) {
    SENTADO("🪑 Sentado"),
    CAMINANDO("🚶 Caminando"),
    AUTO("📡 Auto")
}

@OptIn(ExperimentalCoroutinesApi::class)
class ComprasViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = ComprasDatabase.obtener(app).dao()
    private val motor = MotorRecomendador()
    private val detector = DetectorActividad(app)

    val acelerometroDisponible: Boolean = detector.disponible()

    // ---------------- Contexto (hora + actividad) ----------------
    private val _modoHora = MutableStateFlow(ModoHora.AUTOMATICO)
    val modoHora: StateFlow<ModoHora> = _modoHora.asStateFlow()

    private val _modoActividad = MutableStateFlow(ModoActividad.SENTADO)
    val modoActividad: StateFlow<ModoActividad> = _modoActividad.asStateFlow()

    private val reloj: Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(30_000)
        }
    }

    private val actividadActual: Flow<Actividad> = _modoActividad.flatMapLatest { modo ->
        when (modo) {
            ModoActividad.SENTADO -> flowOf(Actividad.SENTADO)
            ModoActividad.CAMINANDO -> flowOf(Actividad.CAMINANDO)
            ModoActividad.AUTO -> detector.actividad()
        }
    }.distinctUntilChanged()

    val contexto: StateFlow<Contexto> = combine(_modoHora, reloj, actividadActual) { modo, ahora, actividad ->
        Contexto(fechaSegunModo(modo, ahora), actividad)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        Contexto(LocalDateTime.now(), Actividad.SENTADO)
    )

    // ---------------- Datos locales (Room) ----------------
    private val favoritosIds: Flow<Set<String>> = dao.favoritosFlow().map { lista -> lista.map { it.productoId }.toSet() }

    val historial: StateFlow<List<InteraccionEntity>> = dao.interaccionesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoritos: StateFlow<Set<String>> = favoritosIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val favoritosProductos: StateFlow<List<Producto>> = dao.favoritosFlow()
        .map { lista -> lista.mapNotNull { Catalogo.porId(it.productoId) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---------------- Recomendación ----------------
    /** Productos de los últimos lotes mostrados. Solo cambia al iniciar y al pedir "Ver otras". */
    private val penalizados = MutableStateFlow<List<String>?>(null)
    private var ultimoLote: List<String> = emptyList()

    private val claveContexto = contexto.map { it.franja to it.actividad }.distinctUntilChanged()

    val resultado: StateFlow<ResultadoMotor> = combine(
        dao.interaccionesFlow().map { lista -> lista.map { it.aDominio() } },
        favoritosIds,
        claveContexto,
        penalizados.filterNotNull()
    ) { historialDominio, favoritosActuales, clave, recientes ->
        motor.recomendar(
            catalogo = Catalogo.productos,
            historial = historialDominio,
            favoritos = favoritosActuales,
            franja = clave.first,
            actividad = clave.second,
            ahora = System.currentTimeMillis(),
            mostradosRecientes = recientes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultadoMotor(emptyList(), emptyList()))

    // ---------------- Producto abierto ----------------
    private val _productoAbierto = MutableStateFlow<Producto?>(null)
    val productoAbierto: StateFlow<Producto?> = _productoAbierto.asStateFlow()

    init {
        viewModelScope.launch { penalizados.value = dao.ultimosMostrados(LOTES_PENALIZADOS * 3) }
    }

    fun elegirModoHora(modo: ModoHora) {
        _modoHora.value = modo
    }

    fun elegirModoActividad(modo: ModoActividad) {
        _modoActividad.value = modo
    }

    /** Guarda en Room qué productos se mostraron (para rotarlos en la próxima visita). */
    fun registrarLote(ids: List<String>) {
        if (ids.isEmpty() || ids == ultimoLote) return
        ultimoLote = ids
        viewModelScope.launch {
            val ahora = System.currentTimeMillis()
            dao.insertarMostrados(ids.map { MostradoEntity(productoId = it, fecha = ahora) })
        }
    }

    /** "Ver otras": lo que se ve ahora baja de posición y aparecen nuevas sugerencias. */
    fun mostrarOtras() {
        val actuales = resultado.value.recomendaciones.map { it.producto.id }
        if (actuales.isEmpty()) return
        penalizados.value = (actuales + (penalizados.value ?: emptyList())).take(LOTES_PENALIZADOS * 3)
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

    private var ultimaBusqueda = ""

    /** Aprende de lo que se busca: registra una búsqueda por cada categoría que aparece en los resultados. */
    fun registrarBusqueda(texto: String) {
        val limpio = Catalogo.normalizar(texto)
        if (limpio.length < 3 || limpio == ultimaBusqueda) return
        val encontrados = Catalogo.buscar(texto)
        if (encontrados.isEmpty()) return
        ultimaBusqueda = limpio
        encontrados.groupBy { it.categoria }.entries
            .sortedByDescending { it.value.size }
            .take(2)
            .forEach { registrar(null, it.key, TipoInteraccion.BUSQUEDA, texto.trim()) }
    }

    // ---------------- Privacidad y escenarios ----------------
    fun borrarTodo() {
        viewModelScope.launch { limpiar() }
    }

    fun borrarInteraccion(id: Long) {
        viewModelScope.launch { dao.borrarInteraccion(id) }
    }

    fun cargarHistorialEjemplo() = cargarEscenario { Ejemplos.historialEjemplo(it) }

    fun cargarCambioDeGustos() = cargarEscenario { Ejemplos.cambioDeGustos(it) }

    private fun cargarEscenario(generar: (Long) -> List<InteraccionEntity>) {
        viewModelScope.launch {
            limpiar()
            val filas = generar(System.currentTimeMillis())
            dao.insertarInteracciones(filas)
            dao.agregarFavoritos(Ejemplos.favoritosDe(filas))
        }
    }

    private suspend fun limpiar() {
        dao.borrarInteracciones()
        dao.borrarFavoritos()
        dao.borrarMostrados()
        penalizados.value = emptyList()
        ultimoLote = emptyList()
        ultimaBusqueda = ""
    }

    private fun registrar(
        producto: Producto?,
        categoria: Categoria,
        tipo: TipoInteraccion,
        termino: String? = null
    ) {
        val ctx = contexto.value
        val fila = InteraccionEntity(
            productoId = producto?.id,
            categoria = categoria.name,
            interaccion = tipo.name,
            fecha = System.currentTimeMillis(),
            actividad = ctx.actividad.name,
            hora = ctx.hora,
            franja = ctx.franja.name,
            peso = tipo.peso,
            termino = termino
        )
        viewModelScope.launch { dao.insertarInteraccion(fila) }
    }

    private fun fechaSegunModo(modo: ModoHora, real: LocalDateTime): LocalDateTime = when (modo) {
        ModoHora.AUTOMATICO -> real
        ModoHora.LABORAL ->
            real.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY)).atTime(10, 30)
        ModoHora.FIN_DE_SEMANA ->
            real.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY)).atTime(20, 0)
        ModoHora.NOCHE ->
            real.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.WEDNESDAY)).atTime(21, 0)
    }

    private companion object {
        const val LOTES_PENALIZADOS = 3
    }
}
