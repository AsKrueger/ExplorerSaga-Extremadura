package com.alonso.explorersaga.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alonso.explorersaga.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@Database(entities = [PlaceEntity::class], version = 2, exportSchema = true) // VERSIÓN INCREMENTADA
abstract class AppDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "explorer_saga_database"
                )
                .addCallback(DatabaseCallback(context))
                .fallbackToDestructiveMigration() // AÑADIDO PARA LA MIGRACIÓN
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(context, database.placeDao())
                }
            }
        }

        suspend fun populateDatabase(context: Context, placeDao: PlaceDao) {
            placeDao.deleteAll()

            // Preparamos el parser de JSON
            val json = Json { ignoreUnknownKeys = true }

            // Leemos cada archivo de assets y lo parseamos
            val monuments = json.decodeFromString<List<PlaceEntity>>(
                context.assets.open("monuments.json").bufferedReader().use { it.readText() }
            )
            val gastronomy = json.decodeFromString<List<PlaceEntity>>(
                context.assets.open("gastronomia.json").bufferedReader().use { it.readText() }
            )
            val stores = json.decodeFromString<List<PlaceEntity>>(
                context.assets.open("tiendas.json").bufferedReader().use { it.readText() }
            )

            // Combinamos todas las listas en una sola
            val allPlaces = monuments + gastronomy + stores

            // Insertamos todos los datos en la base de datos de una vez
            placeDao.insertAll(allPlaces)
        }
    }
}
