package com.alonso.explorersaga

import android.app.Application
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import okhttp3.OkHttpClient
import org.osmdroid.config.Configuration
import java.io.File

/**
 * Custom Application class para inicializar y mantener el AppContainer.
 */
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
        configureMap()
        // Caché JSON de versiones anteriores, sustituida por Room
        File(filesDir, "saved_places.json").delete()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OfflineImagesInterceptor { settings.value.offlineImages })
                add(OkHttpNetworkFetcherFactory(callFactory = { OkHttpClient() }))
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(File(cacheDir, StorageManager.IMAGE_CACHE_DIRECTORY))
                    .maxSizeBytes(IMAGE_CACHE_BYTES)
                    .build()
            }
            .crossfade(true)
            .build()

    // Los mapas descargados van a la caché de la app para que "Borrar caché" también los elimine
    private fun configureMap() {
        Configuration.getInstance().apply {
            userAgentValue = "ExplorerSaga/${BuildConfig.VERSION_NAME} (Android)"
            osmdroidBasePath = File(cacheDir, StorageManager.MAP_CACHE_DIRECTORY)
            osmdroidTileCache = File(osmdroidBasePath, "tiles")
        }
    }

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
