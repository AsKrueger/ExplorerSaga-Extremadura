package com.alonso.explorersaga.data

import android.content.Context
import coil3.SingletonImageLoader
import com.alonso.explorersaga.util.deleteContents
import com.alonso.explorersaga.util.sizeOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class StorageUsage(
    // Imágenes y mapas descargados: se vuelven a descargar al usarlos
    val cacheBytes: Long = 0,
    // Lugares guardados para usar la app sin conexión
    val dataBytes: Long = 0
)

class StorageManager(
    private val context: Context,
    private val database: AppDatabase,
    private val placeRepository: PlaceRepository
) {
    private val _usage = MutableStateFlow(StorageUsage())
    val usage: StateFlow<StorageUsage> = _usage.asStateFlow()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        _usage.value = StorageUsage(
            cacheBytes = sizeOf(context.cacheDir),
            dataBytes = databaseFiles().sumOf(::sizeOf)
        )
    }

    /** Borra solo las imágenes guardadas (al desactivar "Guardar imágenes sin conexión"). */
    suspend fun clearImages() = withContext(Dispatchers.IO) {
        SingletonImageLoader.get(context).diskCache?.clear()
        refresh()
    }

    /** Borra imágenes y mapas descargados. Devuelve los bytes liberados. */
    suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        val before = sizeOf(context.cacheDir)
        SingletonImageLoader.get(context).let { loader ->
            loader.memoryCache?.clear()
            loader.diskCache?.clear()
        }
        // La carpeta de Coil ya se ha vaciado con su propia API, que mantiene su diario coherente
        context.cacheDir.listFiles()
            ?.filter { it.name != IMAGE_CACHE_DIRECTORY }
            ?.forEach { if (it.isDirectory) deleteContents(it) else it.delete() }
        refresh()
        (before - _usage.value.cacheBytes).coerceAtLeast(0)
    }

    /**
     * Borra la caché y los lugares guardados. Los ajustes (idioma, tema) se conservan.
     * Después la app vuelve a cargar los lugares del servidor o, sin conexión, los incluidos en la app.
     */
    suspend fun clearSavedData() = withContext(Dispatchers.IO) {
        placeRepository.clearLocalData()
        // DELETE no reduce el fichero de SQLite: VACUUM devuelve el espacio al sistema
        database.openHelper.writableDatabase.execSQL("VACUUM")
        clearCache()
    }

    private fun databaseFiles() = context.getDatabasePath(AppDatabase.NAME).let { db ->
        listOf(db, java.io.File("${db.path}-wal"), java.io.File("${db.path}-shm"))
    }

    companion object {
        const val IMAGE_CACHE_DIRECTORY = "image_cache"
        const val MAP_CACHE_DIRECTORY = "osmdroid"
    }
}
