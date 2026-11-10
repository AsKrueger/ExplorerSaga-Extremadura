package com.alonso.explorersaga.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alonso.explorersaga.data.db.ContentDao
import com.alonso.explorersaga.data.db.ContentMeta
import com.alonso.explorersaga.data.db.EventEntity
import com.alonso.explorersaga.data.db.Favorite
import com.alonso.explorersaga.data.db.Municipality
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.data.db.PlaceFts
import com.alonso.explorersaga.data.db.RouteEntity

@Database(
    entities = [
        Place::class, PlaceFts::class, Favorite::class, RouteEntity::class,
        EventEntity::class, Municipality::class, ContentMeta::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contentDao(): ContentDao

    companion object {
        const val NAME = "explorer_saga_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Las versiones 1 y 2 solo guardaban una caché de lugares de Mérida que se
                // reconstruye desde el paquete de contenido; aún no existían los favoritos
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    NAME
                )
                    .fallbackToDestructiveMigrationFrom(true, 1, 2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
