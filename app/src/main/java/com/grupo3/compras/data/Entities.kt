package com.grupo3.compras.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val categoria: String // OFICINA, ENTRETENIMIENTO, OTROS
)

@Entity(tableName = "interacciones")
data class InteraccionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productoId: String,
    val tipo: String, // BUSQUEDA, CLIC, FAVORITO
    val fechaHora: Long // epoch millis
)
