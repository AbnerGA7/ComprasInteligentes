package com.grupo3.compras.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.grupo3.compras.data.ComprasDatabase
import com.grupo3.compras.data.InteraccionEntity
import com.grupo3.compras.data.ProductoEntity
import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Interaccion
import com.grupo3.compras.domain.Producto
import com.grupo3.compras.domain.Recomendacion
import com.grupo3.compras.domain.RecomendadorCompras
import com.grupo3.compras.domain.TipoInteraccion
import com.grupo3.compras.domain.contextoAhora
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ComprasViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = ComprasDatabase.obtener(app).dao()
    private val recomendador = RecomendadorCompras()

    private val _productos = MutableStateFlow<List<ProductoEntity>>(emptyList())
    val productos: StateFlow<List<ProductoEntity>> = _productos.asStateFlow()

    private val _recomendaciones = MutableStateFlow<List<Recomendacion>>(emptyList())
    val recomendaciones: StateFlow<List<Recomendacion>> = _recomendaciones.asStateFlow()

    init {
        viewModelScope.launch {
            if (dao.contarProductos() == 0) dao.insertarProductos(CATALOGO_INICIAL)
            _productos.value = dao.productosUnaVez()
            recalcular()
        }
    }

    fun registrar(productoId: String, tipo: TipoInteraccion) {
        viewModelScope.launch {
            dao.insertarInteraccion(
                InteraccionEntity(productoId = productoId, tipo = tipo.name, fechaHora = System.currentTimeMillis())
            )
            recalcular()
        }
    }

    private suspend fun recalcular() {
        val catalogo = dao.productosUnaVez().map {
            Producto(it.id, it.nombre, Categoria.valueOf(it.categoria))
        }
        val historial = dao.interacciones().map {
            val fecha = contextoAhora(it.fechaHora)
            Interaccion(it.productoId, TipoInteraccion.valueOf(it.tipo), fecha)
        }
        _recomendaciones.value = recomendador.recomendar(catalogo, historial, contextoAhora())
    }

    companion object {
        val CATALOGO_INICIAL = listOf(
            ProductoEntity("P1", "Resma de papel A4", "OFICINA"),
            ProductoEntity("P2", "Teclado mecánico", "OTROS"),
            ProductoEntity("P3", "Audífonos inalámbricos", "ENTRETENIMIENTO"),
            ProductoEntity("P4", "Consola de videojuegos", "ENTRETENIMIENTO"),
            ProductoEntity("P5", "Archivador de oficina", "OFICINA"),
            ProductoEntity("P6", "Silla ergonómica", "OFICINA")
        )
    }
}
