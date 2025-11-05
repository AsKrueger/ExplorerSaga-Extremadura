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

@Database(entities = [PlaceEntity::class], version = 1, exportSchema = true)
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
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database.placeDao())
                }
            }
        }

        suspend fun populateDatabase(placeDao: PlaceDao) {
            val monuments = listOf(
                PlaceEntity(id = 1, name = "Teatro Romano", description = "Espectacular teatro del siglo I a.C. usado para representaciones teatrales.", category = "monumento", horarios = "10:00 - 18:00", direccion = "Plaza Margarita Xirgú, s/n, 06800 Mérida", latitude = 38.9157, longitude = -6.3386, imageResId = R.drawable.teatro_merida),
                PlaceEntity(id = 2, name = "Anfiteatro Romano", description = "Lugar de antiguas luchas de gladiadores y espectáculos públicos.", category = "monumento", horarios = "10:00 - 18:00", direccion = "Plaza Margarita Xirgú, s/n, 06800 Mérida", latitude = 38.9165, longitude = -6.3375, imageResId = R.drawable.teatro_merida),
                PlaceEntity(id = 3, name = "Acueducto de los Milagros", description = "Impresionante obra de ingeniería romana para traer agua a la ciudad.", category = "monumento", horarios = "Abierto 24h", direccion = "Av. Vía de la Plata, 06800 Mérida", latitude = 38.923, longitude = -6.348, imageResId = R.drawable.teatro_merida)
            )

            val restaurants = listOf(
                PlaceEntity(id = 4, name = "Restaurante A de Arco", description = "Cocina extremeña moderna junto al Arco de Trajano.", category = "restaurante", horarios = "13:00 - 16:00, 20:00 - 23:00", direccion = "C. Trajano, 5, 06800 Mérida", latitude = 38.9175, longitude = -6.3458, imageResId = R.drawable.teatro_merida),
                PlaceEntity(id = 5, name = "Sybarit", description = "Tapas y platos creativos en un ambiente acogedor.", category = "restaurante", horarios = "12:30 - 16:30, 20:00 - 00:00", direccion = "C. John Lennon, 15, 06800 Mérida", latitude = 38.915, longitude = -6.347, imageResId = R.drawable.teatro_merida),
                PlaceEntity(id = 6, name = "De Tripas Corazón", description = "Gastronomía local con un toque diferente y original.", category = "restaurante", horarios = "13:30 - 16:00, 20:30 - 23:00", direccion = "C. de Arcos, 11, 06800 Mérida", latitude = 38.915, longitude = -6.346, imageResId = R.drawable.teatro_merida)
            )

            val shops = listOf(
                PlaceEntity(id = 7, name = "Terracota Mérida", description = "Artesanía y cerámica típica de la región.", category = "tienda", horarios = "10:00 - 14:00, 17:00 - 20:00", direccion = "C. José Ramón Mélida, 30, 06800 Mérida", latitude = 38.916, longitude = -6.339, imageResId = R.drawable.teatro_merida),
                PlaceEntity(id = 8, name = "Emérita Souvenirs", description = "Recuerdos variados de la Mérida romana y de Extremadura.", category = "tienda", horarios = "09:30 - 21:00", direccion = "C. José Ramón Mélida, 15, 06800 Mérida", latitude = 38.917, longitude = -6.338, imageResId = R.drawable.teatro_merida)
            )

            monuments.forEach { placeDao.insertPlace(it) }
            restaurants.forEach { placeDao.insertPlace(it) }
            shops.forEach { placeDao.insertPlace(it) }
        }
    }
}
