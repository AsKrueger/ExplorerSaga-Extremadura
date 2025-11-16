package com.alonso.explorersaga

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.alonso.explorersaga.data.PlaceRepository
import com.alonso.explorersaga.ui.viewmodels.PlacesViewModel

/**
 * Un contenedor simple para las dependencias de la aplicación.
 */
interface AppContainer {
    val placesRepository: PlaceRepository
    val placesViewModelFactory: ViewModelProvider.Factory
}

/**
 * Implementación del contenedor de dependencias.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    override val placesRepository: PlaceRepository by lazy {
        PlaceRepository(context)
    }

    override val placesViewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(PlacesViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return PlacesViewModel(placesRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
