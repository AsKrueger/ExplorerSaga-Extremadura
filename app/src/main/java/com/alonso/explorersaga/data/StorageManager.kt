package com.alonso.explorersaga.data

import android.content.Context
import coil3.SingletonImageLoader
import com.alonso.explorersaga.data.content.ContentRepository
import com.alonso.explorersaga.util.deleteContents
import com.alonso.explorersaga.util.sizeOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class StorageUsage(
    // Imágenes y teselas vistas: se vuelven a descargar al usarlas
    val cacheBytes: Long = 0,
    // Lugares, rutas, favoritos y mapa descargado para usar la app sin conexión
    val dataBytes: Long = 0
)

class StorageManager(
    private val context: Context,
    private val database: AppDatabase,
    private val contentRepository: ContentRepository,
    private val offlineMap: OfflineMapManager
) {
    private val _usage = MutableStateFlow(StorageUsage())
    val usage: StateFlow<StorageUsage> = _usage.asStateFlow()

    suspend fun refresh() = withContext(Dispatchers.IO) {
        _usage.value = StorageUsage(
            cacheBytes = sizeOf(context.cacheDir),
            dataBytes = (databaseFiles() + mapFiles()).sumOf(::sizeOf)
        )
    }

    /** Borra solo las imágenes guardadas (al desactivar "Guardar imágenes sin conexión"). */
    suspend fun clearImages() = withContext(Dispatchers.IO) {
        SingletonImageLoader.get(context).diskCache?.clear()
        refresh()
    }

    /** Borra las imágenes y las teselas vistas. Devuelve los bytes liberados. */
    suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        val before = sizeOf(context.cacheDir) + mapFiles().sumOf(::sizeOf)
        offlineMap.clearAmbientCache()
        SingletonImageLoader.get(context).let { loader ->
            loader.memoryCache?.clear()
            loader.diskCache?.clear()
        }
        // La carpeta de Coil ya se ha vaciado con su propia API, que mantiene su diario coherente
        context.cacheDir.listFiles()
            ?.filter { it.name != IMAGE_CACHE_DIRECTORY }
            ?.forEach { if (it.isDirectory) deleteContents(it) else it.delete() }
        refresh()
        (before - _usage.value.cacheBytes - mapFiles().sumOf(::sizeOf)).coerceAtLeast(0)
    }

    /**
     * Borra favoritos, contenido descargado, mapa sin conexión y caché. Los ajustes
     * (idioma, tema) se conservan y se vuelve al contenido incluido en la app.
     */
    suspend fun clearSavedData() = withContext(Dispatchers.IO) {
        offlineMap.delete()
        contentRepository.clearAndReimport()
        // DELETE no reduce el fichero de SQLite: VACUUM devuelve el espacio al sistema
        database.openHelper.writableDatabase.execSQL("VACUUM")
        clearCache()
    }

    private fun databaseFiles() = context.getDatabasePath(AppDatabase.NAME).let { db ->
        listOf(db, java.io.File("${db.path}-wal"), java.io.File("${db.path}-shm"))
    }

    // Base de datos de MapLibre: mapa descargado y caché automática de teselas
    private fun mapFiles() = context.filesDir.listFiles { file -> file.name.startsWith("mbgl-offline") }
        ?.toList().orEmpty()

    companion object {
        const val IMAGE_CACHE_DIRECTORY = "image_cache"
    }
}
