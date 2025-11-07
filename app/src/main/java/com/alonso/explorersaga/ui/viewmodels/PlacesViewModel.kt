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
import kotlinx.coroutines.flow.combine

// El estado de la UI ahora incluye el estado de los filtros.
data class PlacesUiState(
    val places: List<Place> = emptyList(),
    val filterState: FilterState = FilterState()
)

// Un data class para mantener el estado de los checkboxes.
data class FilterState(
    val monuments: Boolean = true,
    val restaurants: Boolean = true,
    val shops: Boolean = true,
    val popularFirst: Boolean = false
)

class PlacesViewModel(private val repository: PlaceRepository) : ViewModel() {

    // Estado para los filtros de categoría
    private val _filterState = MutableStateFlow(FilterState())

    // La UI puede observar el estado de los filtros a través de esto.
    val filterState: StateFlow<FilterState> = _filterState

    // Función para que la UI actualice los filtros.
    fun updateFilters(newFilterState: FilterState) {
        _filterState.value = newFilterState
    }

    // El uiState ahora reacciona a los cambios en los filtros.
    val uiState: StateFlow<PlacesUiState> = _filterState
        .flatMapLatest { filters ->
            // 1. Construimos la lista de categorías activas a partir del estado de los filtros.
            val activeCategories = mutableListOf<String>()
            if (filters.monuments) activeCategories.add("monumento")
            if (filters.restaurants) activeCategories.add("restaurante")
            if (filters.shops) activeCategories.add("tienda")
            
            // Si no hay ninguna categoría activa, devolvemos un flujo vacío para no crashear la query
            if (activeCategories.isEmpty()) {
                // Devolvemos un flujo que emite una lista vacía
                kotlinx.coroutines.flow.flowOf(emptyList<PlaceEntity>())
            } else {
                 // 2. Usamos la nueva función del repositorio.
                repository.getPlacesByCategories(activeCategories)
            }
        }
        .map { placesFromDb ->
            // 3. Mapeamos los resultados de la base de datos al estado de la UI.
            val places = placesFromDb.map { it.toPlaceUiModel() }
            // Combinamos los lugares con el estado actual de los filtros.
            PlacesUiState(places = places, filterState = _filterState.value)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlacesUiState()
        )
    
    // Las funciones `setCategory` y `_categoryFilter` ya no son necesarias
    // ya que el nuevo sistema de filtros las reemplaza.
}

// La función de extensión no necesita cambios.
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
