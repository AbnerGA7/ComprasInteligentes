package com.grupo3.compras.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.grupo3.compras.domain.Actividad
import com.grupo3.compras.domain.Categoria
import com.grupo3.compras.domain.Franja
import com.grupo3.compras.domain.Interaccion
import com.grupo3.compras.domain.TipoInteraccion

/** Diapositiva 13: categoría, interacción, fecha, actividad, hora y peso. */
@Entity(tableName = "interacciones", indices = [Index("fecha")])
data class InteraccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: String?,
    val categoria: String,
    val interaccion: String,
    val fecha: Long,
    val actividad: String,
    val hora: Int,
    val franja: String,
    val peso: Int,
    val termino: String? = null
)

@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey val productoId: String,
    val fecha: Long
)

/** Productos que ya se le mostraron al usuario, para no repetir siempre lo mismo. */
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
    epochMillis = fecha,
    actividad = Actividad.valueOf(actividad),
    franja = Franja.valueOf(franja),
    peso = peso
)
