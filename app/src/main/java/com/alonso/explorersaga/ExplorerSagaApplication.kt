package com.alonso.explorersaga

import android.app.Application
import android.util.Log
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.intercept.Interceptor
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import coil3.request.ImageResult
import coil3.request.crossfade
import com.alonso.explorersaga.data.AppSettings
import com.alonso.explorersaga.data.StorageManager
import com.alonso.explorersaga.data.content.ContentSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import java.io.File

class ExplorerSagaApplication : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Ajustes siempre disponibles para el cargador de imágenes, que no puede esperar a DataStore
    private lateinit var settings: StateFlow<AppSettings>

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        settings = container.settingsStore.settings
            .stateIn(scope, SharingStarted.Eagerly, AppSettings())
        MapLibre.getInstance(this)
        // Restos de versiones anteriores: caché JSON y teselas de osmdroid
        File(filesDir, "saved_places.json").delete()
        File(cacheDir, "osmdroid").deleteRecursively()

        scope.launch {
            try {
                // Primer arranque (o app actualizada): el contenido incluido está disponible al momento
                container.contentRepository.ensureContent()
            } catch (e: Exception) {
                Log.e("ExplorerSaga", "No se pudo importar el contenido incluido", e)
            }
            ContentSyncWorker.schedule(this@ExplorerSagaApplication, container.settingsStore.settings.first().syncWifiOnly)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OfflineImagesInterceptor { settings.value.offlineImages })
                add(OkHttpNetworkFetcherFactory(callFactory = { container.httpClient }))
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, StorageManager.IMAGE_CACHE_DIRECTORY))
                    .maxSizeBytes(IMAGE_CACHE_BYTES)
                    .build()
            }
            .crossfade(true)
            .build()

    private companion object {
        const val IMAGE_CACHE_BYTES = 300L * 1024 * 1024
    }
}

/** Con "Guardar imágenes sin conexión" desactivado, las imágenes no se escriben ni leen del disco. */
class OfflineImagesInterceptor(private val enabled: () -> Boolean) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        if (enabled()) return chain.proceed()
        val request = chain.request.newBuilder()
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()
        return chain.withRequest(request).proceed()
    }
}
