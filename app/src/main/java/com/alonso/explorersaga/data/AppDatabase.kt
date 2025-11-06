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
            placeDao.deleteAll()

            val places = listOf(
                // --- MONUMENTOS ---
                PlaceEntity(
                    name = "Teatro Romano de Mérida",
                    description = "Uno de los teatros mejor conservados del Imperio Romano, construido entre los años 16 y 15 a.C.",
                    category = "monumento",
                    horarios = "10:00 - 18:00",
                    direccion = "Plaza Margarita Xirgú, s/n",
                    latitude = 38.9157, 
                    longitude = -6.3386,
                    imageResId = R.drawable.teatro_merida
                ),
                PlaceEntity(
                    name = "Anfiteatro Romano",
                    description = "Inaugurado en el 8 a.C., este recinto acogía luchas de gladiadores y espectáculos con fieras.",
                    category = "monumento",
                    horarios = "10:00 - 18:00",
                    direccion = "Plaza Margarita Xirgú, s/n",
                    latitude = 38.9163,
                    longitude = -6.3379,
                    imageResId = R.drawable.teatro_merida // Placeholder
                ),

                // --- RESTAURANTES Y BARES ---
                PlaceEntity(
                    name = "A de Arco",
                    description = "Restaurante de cocina extremeña moderna ubicado junto al Arco de Trajano.",
                    category = "restaurante",
                    horarios = "13:00 - 16:00, 20:00 - 23:00",
                    direccion = "C. Trajano, 5",
                    latitude = 38.9175, 
                    longitude = -6.3444,
                    imageResId = R.drawable.teatro_merida // Placeholder
                ),
                PlaceEntity(
                    name = "Sybarit",
                    description = "Tapas y raciones con un toque moderno en un local acogedor.",
                    category = "restaurante",
                    horarios = "12:30 - 16:30, 20:00 - 00:00",
                    direccion = "Plaza de España, 15",
                    latitude = 38.9155, 
                    longitude = -6.3456,
                    imageResId = R.drawable.teatro_merida // Placeholder
                ),
                PlaceEntity(
                    name = "La Carbonería",
                    description = "Asador tradicional famoso por sus carnes a la brasa y su ambiente rústico.",
                    category = "restaurante",
                    horarios = "13:00 - 16:00, 20:30 - 23:30",
                    direccion = "C. Holguín, 12",
                    latitude = 38.9181, 
                    longitude = -6.3475,
                    imageResId = R.drawable.teatro_merida // Placeholder
                ),
                PlaceEntity(
                    name = "Bar El Rincón",
                    description = "Bar de tapas de toda la vida, perfecto para probar la gastronomía local de forma informal.",
                    category = "restaurante",
                    horarios = "09:00 - 00:00",
                    direccion = "C. de Santa Julia, 3",
                    latitude = 38.9168, 
                    longitude = -6.3431,
                    imageResId = R.drawable.teatro_merida // Placeholder
                ),

                // --- TIENDAS ---
                PlaceEntity(
                    name = "La Alacena de la Extremadureña",
                    description = "Tienda gourmet especializada en productos locales de alta calidad.",
                    category = "tienda",
                    horarios = "10:00 - 14:00, 17:00 - 20:00",
                    direccion = "C. Sta. Eulalia, 22",
                    latitude = 38.9165,
                    longitude = -6.3458,
                    imageResId = R.drawable.teatro_merida // Placeholder
                )
            )

            places.forEach { placeDao.insertPlace(it) }
        }
    }
}
