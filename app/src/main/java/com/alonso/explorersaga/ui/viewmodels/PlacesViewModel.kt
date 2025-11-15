package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.PlaceEntity
import com.alonso.explorersaga.data.PlaceRepository
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlacesUiState(
    val places: List<Place> = emptyList(),
    val filterState: FilterState = FilterState(),
    val selectedPlace: Place? = null
)

// El estado de filtro definitivo con todas las subcategorías
data class FilterState(
    val monuments: Boolean = true,
    val iglesias: Boolean = true,
    val museos: Boolean = true,
    val restaurants: Boolean = true,
    val cafeterias: Boolean = true,
    val tiendasGenerales: Boolean = true,
    val supermercados: Boolean = true,
    val souvenirs: Boolean = true,
    val popularFirst: Boolean = false
)

class PlacesViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState

    init {
        viewModelScope.launch {
            _filterState.flatMapLatest { filters ->
                val activeCategories = mutableListOf<String>()
                if (filters.monuments) activeCategories.add("monumento")
                if (filters.iglesias) activeCategories.add("iglesia")
                if (filters.museos) activeCategories.add("museo")
                if (filters.restaurants) activeCategories.add("restaurante")
                if (filters.cafeterias) activeCategories.add("cafeteria")
                if (filters.tiendasGenerales) activeCategories.add("tienda_general")
                if (filters.supermercados) activeCategories.add("supermercado")
                if (filters.souvenirs) activeCategories.add("souvenir")
                
                if (activeCategories.isEmpty()) {
                    kotlinx.coroutines.flow.flowOf(emptyList<PlaceEntity>())
                } else {
                    repository.getPlacesByCategories(activeCategories)
                }
            }.collect { placesFromDb ->
                _uiState.update { currentState ->
                    currentState.copy(
                        places = placesFromDb.map { it.toPlaceUiModel() },
                        filterState = _filterState.value
                    )
                }
            }
        }
    }

    fun updateFilters(newFilterState: FilterState) {
        _filterState.value = newFilterState
    }

    fun loadPlaceById(id: Int) {
        viewModelScope.launch {
            repository.getPlaceById(id).collect { placeEntity ->
                _uiState.update { currentState ->
                    currentState.copy(selectedPlace = placeEntity?.toPlaceUiModel())
                }
            }
        }
    }

    fun clearSelectedPlace() {
        _uiState.update { it.copy(selectedPlace = null) }
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
        photo = this.photo,
        website = this.website
    )
}
