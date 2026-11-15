package com.grupo3.compras.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComprasDao {
    @Query("SELECT * FROM productos")
    fun productos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos")
    suspend fun productosUnaVez(): List<ProductoEntity>

    @Query("SELECT * FROM interacciones ORDER BY fechaHora DESC")
    suspend fun interacciones(): List<InteraccionEntity>

    @Insert suspend fun insertarInteraccion(interaccion: InteraccionEntity)
    @Insert suspend fun insertarProductos(productos: List<ProductoEntity>)

    @Query("SELECT COUNT(*) FROM productos")
    suspend fun contarProductos(): Int
}
