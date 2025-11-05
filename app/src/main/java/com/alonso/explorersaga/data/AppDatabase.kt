package com.alonso.explorersaga.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
// import androidx.sqlite.db.SupportSQLiteDatabase // Comentamos este import también
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
                // .addCallback(DatabaseCallback(context)) // 1. HEMOS COMENTADO ESTA LÍNEA
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // 2. HEMOS COMENTADO TODA LA CLASE INTERNA DEL CALLBACK
    /*
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
            // ... todo el código de inserción ...
        }
    }
    */
}
