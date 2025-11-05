package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.PlaceEntity
import com.alonso.explorersaga.data.PlaceRepository
import com.alonso.explorersaga.ui.screens.Place // Importamos el 'Place' de la UI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Esta es la clase que contendrá el estado de nuestra UI (la lista de lugares).
data class PlacesUiState(
    val places: List<Place> = emptyList()
)

class PlacesViewModel(private val repository: PlaceRepository) : ViewModel() {

    // Este es el 'StateFlow'. Es un flujo de datos que guardará el estado
    // actual de nuestra pantalla. Es privado (_uiState) para que solo
    // el ViewModel pueda modificarlo.
    private val _uiState = MutableStateFlow(PlacesUiState())

    // Esta es la versión pública y de solo lectura del estado. La UI
    // observará este 'uiState' para recibir las actualizaciones.
    val uiState: StateFlow<PlacesUiState> = _uiState.asStateFlow()

    // Esta es la función principal que llamaremos desde la UI.
    fun loadPlaces(category: String) {
        // Usamos 'viewModelScope.launch' para iniciar una corrutina.
        // Esta se cancelará automáticamente si el ViewModel se destruye,
        // evitando fugas de memoria.
        viewModelScope.launch {
            // Le pedimos al repositorio el flujo de datos para una categoría.
            repository.getPlacesByCategory(category)
                // El método '.collect' se suscribirá al Flow. Cada vez
                // que haya un cambio en la base de datos, este bloque
                // de código se ejecutará con la nueva lista.
                .collect { placesFromDb ->
                    // Actualizamos nuestro estado de la UI...
                    _uiState.value = PlacesUiState(
                        // ...convirtiendo la lista de 'PlaceEntity' (de la BD)
                        //     a una lista de 'Place' (de la UI).
                        places = placesFromDb.map { it.toPlaceUiModel() }
                    )
                }
        }
    }
}

// Función de extensión para convertir el modelo de la BD al modelo de la UI.
// Esto mantiene nuestro código limpio y la separación de capas.
fun PlaceEntity.toPlaceUiModel(): Place {
    return Place(
        id = this.id,
        name = this.name,
        description = this.description,
        category = this.category,
        horarios = this.horarios,
        direccion = this.direccion,
        latitude = this.latitude,
        longitude = this.longitude,
        imageResId = this.imageResId
    )
}
