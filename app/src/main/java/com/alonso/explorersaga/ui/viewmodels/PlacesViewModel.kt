package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.PlaceEntity
import com.alonso.explorersaga.data.PlaceRepository
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// El estado de la UI ahora incluye el lugar seleccionado
data class PlacesUiState(
    val places: List<Place> = emptyList(),
    val filterState: FilterState = FilterState(),
    val selectedPlace: Place? = null // <-- NUEVO
)

data class FilterState(
    val monuments: Boolean = true,
    val restaurants: Boolean = true,
    val shops: Boolean = true,
    val popularFirst: Boolean = false
)

class PlacesViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    // Estado mutable principal que ahora contiene todo
    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState

    init {
        // El colector principal que reacciona a los cambios de filtro
        viewModelScope.launch {
            _filterState.flatMapLatest { filters ->
                val activeCategories = mutableListOf<String>()
                if (filters.monuments) activeCategories.add("monumento")
                if (filters.restaurants) activeCategories.add("restaurante")
                if (filters.shops) activeCategories.add("tienda")
                
                if (activeCategories.isEmpty()) {
                    kotlinx.coroutines.flow.flowOf(emptyList<PlaceEntity>())
                } else {
                    repository.getPlacesByCategories(activeCategories)
                }
            }.collect { placesFromDb ->
                _uiState.update { currentState ->
                    currentState.copy(places = placesFromDb.map { it.toPlaceUiModel() })
                }
            }
        }
    }

    fun updateFilters(newFilterState: FilterState) {
        _filterState.value = newFilterState
    }

    // ¡NUEVA FUNCIÓN! Carga un lugar por su ID y actualiza el estado
    fun loadPlaceById(id: Int) {
        viewModelScope.launch {
            repository.getPlaceById(id).collect { placeEntity ->
                _uiState.update { currentState ->
                    currentState.copy(selectedPlace = placeEntity?.toPlaceUiModel())
                }
            }
        }
    }
}

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
