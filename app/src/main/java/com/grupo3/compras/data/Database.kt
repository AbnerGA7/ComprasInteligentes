package com.grupo3.compras.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [InteraccionEntity::class, FavoritoEntity::class, MostradoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ComprasDatabase : RoomDatabase() {
    abstract fun dao(): ComprasDao

    companion object {
        @Volatile
        private var instancia: ComprasDatabase? = null

        fun obtener(context: Context): ComprasDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    ComprasDatabase::class.java,
                    "compras.db"
                ).fallbackToDestructiveMigration().build().also { instancia = it }
            }
    }
}
