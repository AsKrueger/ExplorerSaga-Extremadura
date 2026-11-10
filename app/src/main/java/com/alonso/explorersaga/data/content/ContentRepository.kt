package com.alonso.explorersaga.data.content

import android.content.Context
import android.util.Log
import com.alonso.explorersaga.data.db.ContentDao
import com.alonso.explorersaga.data.db.Favorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayInputStream
import java.io.IOException

enum class SyncResult { UP_TO_DATE, UPDATED, FAILED }

/**
 * Contenido de la app: el paquete incluido en el APK para el primer arranque
 * sin red y las actualizaciones publicadas en GitHub Releases.
 */
class ContentRepository(
    private val context: Context,
    private val dao: ContentDao,
    private val http: OkHttpClient,
    private val baseUrl: String = CONTENT_BASE_URL,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private val importMutex = Mutex()

    val meta = dao.metaFlow()

    /** Importa el paquete incluido en la app si es más nuevo que el instalado (o no hay ninguno). */
    suspend fun ensureContent() = withContext(Dispatchers.IO) {
        importMutex.withLock {
            val bundled = context.assets.open(BUNDLED_MANIFEST).use {
                ContentImporter.json.decodeFromString<ContentManifest>(it.readBytes().decodeToString())
            }
            val installed = dao.meta()?.version ?: 0
            if (bundled.version > installed) {
                val pack = context.assets.open(BUNDLED_PACK).use(ContentImporter::readPack)
                import(pack)
            }
        }
    }

    /** Descarga el paquete publicado si su versión es mayor que la instalada. */
    suspend fun sync(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val manifest = ContentImporter.json.decodeFromString<ContentManifest>(
                download(baseUrl + MANIFEST).decodeToString()
            )
            if (manifest.schema != ContentImporter.SUPPORTED_SCHEMA) return@withContext SyncResult.UP_TO_DATE
            if (manifest.version <= (dao.meta()?.version ?: 0)) return@withContext SyncResult.UP_TO_DATE

            val bytes = download(baseUrl + manifest.file)
            if (bytes.size.toLong() != manifest.size || ContentImporter.sha256(bytes) != manifest.sha256) {
                Log.w(TAG, "Paquete descartado: tamaño o hash no coinciden")
                return@withContext SyncResult.FAILED
            }
            importMutex.withLock {
                import(ContentImporter.readPack(ByteArrayInputStream(bytes)))
            }
            SyncResult.UPDATED
        } catch (e: IOException) {
            Log.w(TAG, "No se pudo actualizar el contenido", e)
            SyncResult.FAILED
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Paquete de contenido no válido", e)
            SyncResult.FAILED
        }
    }

    suspend fun setFavorite(placeId: String, favorite: Boolean) {
        if (favorite) dao.upsertFavorite(Favorite(placeId, clock())) else dao.deleteFavorite(placeId)
    }

    suspend fun setFavoriteDay(placeId: String, day: Int) = dao.setFavoriteDay(placeId, day)

    /** Borra contenido y favoritos y vuelve a importar el paquete incluido en la app. */
    suspend fun clearAndReimport() = withContext(Dispatchers.IO) {
        importMutex.withLock { dao.clearAll() }
        ensureContent()
    }

    private suspend fun import(pack: ContentPack) {
        val entities = ContentImporter.toEntities(pack, clock())
        dao.replaceContent(
            entities.places, entities.fts, entities.routes,
            entities.events, entities.municipalities, entities.meta
        )
    }

    private fun download(url: String): ByteArray {
        val request = Request.Builder().url(url).build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code} en $url")
            return response.body?.bytes() ?: throw IOException("Respuesta vacía en $url")
        }
    }

    companion object {
        private const val TAG = "ContentRepository"
        const val CONTENT_BASE_URL =
            "https://github.com/AsKrueger/ExplorerSaga-Extremadura/releases/download/content-pack/"
        private const val MANIFEST = "manifest.json"
        private const val BUNDLED_MANIFEST = "content/manifest.json"
        private const val BUNDLED_PACK = "content/pack.bin"
    }
}
