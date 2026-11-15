package com.grupo3.compras.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Interaccion
import com.grupo3.compras.domain.TipoInteraccion

/** Cada búsqueda, clic o favorito: categoría, interacción, fecha, hora y peso. */
@Entity(tableName = "interacciones")
data class InteraccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: String?,
    val categoria: String,
    val interaccion: String,
    val fecha: Long,
    val hora: Int,
    val peso: Int,
    val termino: String? = null
)

@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey val productoId: String,
    val fecha: Long
)

/** Productos que ya se mostraron, para no repetir siempre los mismos. */
@Entity(tableName = "mostrados")
data class MostradoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: String,
    val fecha: Long
)

fun InteraccionEntity.aDominio(): Interaccion = Interaccion(
    productoId = productoId,
    categoria = Categoria.valueOf(categoria),
    tipo = TipoInteraccion.valueOf(interaccion),
    epochMillis = fecha
)
