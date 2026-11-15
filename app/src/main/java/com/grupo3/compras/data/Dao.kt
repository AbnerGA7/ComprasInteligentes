package com.grupo3.compras.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ComprasDao {
    @Query("SELECT * FROM interacciones ORDER BY fecha DESC")
    fun interaccionesFlow(): Flow<List<InteraccionEntity>>

    @Insert
    suspend fun insertarInteraccion(interaccion: InteraccionEntity)

    @Insert
    suspend fun insertarInteracciones(interacciones: List<InteraccionEntity>)

    @Query("DELETE FROM interacciones WHERE id = :id")
    suspend fun borrarInteraccion(id: Long)

    @Query("DELETE FROM interacciones")
    suspend fun borrarInteracciones()

    @Query("SELECT * FROM favoritos ORDER BY fecha DESC")
    fun favoritosFlow(): Flow<List<FavoritoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarFavorito(favorito: FavoritoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarFavoritos(favoritos: List<FavoritoEntity>)

    @Query("DELETE FROM favoritos WHERE productoId = :productoId")
    suspend fun quitarFavorito(productoId: String)

    @Query("DELETE FROM favoritos")
    suspend fun borrarFavoritos()

    @Insert
    suspend fun insertarMostrados(mostrados: List<MostradoEntity>)

    @Query("SELECT productoId FROM mostrados ORDER BY id DESC LIMIT :cantidad")
    suspend fun ultimosMostrados(cantidad: Int): List<String>

    @Query("DELETE FROM mostrados")
    suspend fun borrarMostrados()
}
