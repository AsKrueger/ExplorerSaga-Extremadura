package com.alonso.explorersaga

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.alonso.explorersaga.data.AppDatabase
import com.alonso.explorersaga.data.BundledPlacesLoader
import com.alonso.explorersaga.data.PlaceRepository
import com.alonso.explorersaga.data.SettingsStore
import com.alonso.explorersaga.data.StorageManager
import com.alonso.explorersaga.data.remote.RetrofitClient
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel
import com.alonso.explorersaga.ui.viewmodels.SettingsViewModel

/**
 * Un contenedor simple para las dependencias de la aplicación.
 */
interface AppContainer {
    val placesRepository: PlaceRepository
    val settingsStore: SettingsStore
    val storageManager: StorageManager
    val viewModelFactory: ViewModelProvider.Factory
}

/**
 * Implementación del contenedor de dependencias.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy { AppDatabase.getDatabase(context) }

    override val placesRepository: PlaceRepository by lazy {
        PlaceRepository(
            placeDao = database.placeDao(),
            api = RetrofitClient.api,
            importApi = RetrofitClient.importApi,
            loadBundledPlaces = BundledPlacesLoader(context)::load
        )
    }

    override val settingsStore: SettingsStore by lazy { SettingsStore(context) }

    override val storageManager: StorageManager by lazy {
        StorageManager(context, database, placesRepository)
    }

    override val viewModelFactory: ViewModelProvider.Factory = viewModelFactory {
        initializer { PlacesViewModel(placesRepository) }
        initializer { SettingsViewModel(settingsStore, storageManager) }
    }
}
