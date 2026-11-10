package com.alonso.explorersaga

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alonso.explorersaga.data.AppDatabase
import com.alonso.explorersaga.data.LocationProvider
import com.alonso.explorersaga.data.OfflineMapManager
import com.alonso.explorersaga.data.SettingsStore
import com.alonso.explorersaga.data.StorageManager
import com.alonso.explorersaga.data.content.ContentRepository
import com.alonso.explorersaga.data.content.ContentSyncWorker
import com.alonso.explorersaga.data.db.ContentDao
import com.alonso.explorersaga.ui.viewmodels.ExploreViewModel
import com.alonso.explorersaga.ui.viewmodels.HomeViewModel
import com.alonso.explorersaga.ui.viewmodels.PlaceDetailViewModel
import com.alonso.explorersaga.ui.viewmodels.RouteViewModel
import com.alonso.explorersaga.ui.viewmodels.ServicesViewModel
import com.alonso.explorersaga.ui.viewmodels.SettingsViewModel
import com.alonso.explorersaga.ui.viewmodels.TripViewModel
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/** Dependencias de la app, creadas una sola vez. */
interface AppContainer {
    val contentDao: ContentDao
    val contentRepository: ContentRepository
    val settingsStore: SettingsStore
    val storageManager: StorageManager
    val offlineMap: OfflineMapManager
    val location: LocationProvider
    val httpClient: OkHttpClient
    val viewModelFactory: ViewModelProvider.Factory
    fun placeDetailFactory(placeId: String): ViewModelProvider.Factory
    fun routeFactory(routeId: String): ViewModelProvider.Factory
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }

    override val contentDao: ContentDao by lazy { database.contentDao() }

    override val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        // Wikimedia pide un User-Agent que identifique la app y cómo contactar
                        .header("User-Agent", USER_AGENT)
                        .build()
                )
            }
            .build()
    }

    override val contentRepository: ContentRepository by lazy {
        ContentRepository(context, contentDao, httpClient)
    }

    override val settingsStore: SettingsStore by lazy { SettingsStore(context) }

    override val offlineMap: OfflineMapManager by lazy { OfflineMapManager(context) }

    override val location: LocationProvider by lazy { LocationProvider(context) }

    override val storageManager: StorageManager by lazy {
        StorageManager(context, database, contentRepository, offlineMap)
    }

    override val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { ExploreViewModel(contentDao, location) }
        initializer { HomeViewModel(contentDao, location) }
        initializer { TripViewModel(contentDao, contentRepository) }
        initializer { ServicesViewModel(contentDao, location) }
        initializer {
            SettingsViewModel(settingsStore, storageManager, contentRepository, offlineMap) { wifiOnly ->
                ContentSyncWorker.schedule(context, wifiOnly)
            }
        }
    }

    override fun placeDetailFactory(placeId: String) = viewModelFactory {
        initializer { PlaceDetailViewModel(placeId, contentDao, contentRepository) }
    }

    override fun routeFactory(routeId: String) = viewModelFactory {
        initializer { RouteViewModel(routeId, contentDao) }
    }

    companion object {
        val USER_AGENT = "ExplorerSaga/${BuildConfig.VERSION_NAME} " +
            "(Android; +https://github.com/AsKrueger/ExplorerSaga-Extremadura)"
    }
}
