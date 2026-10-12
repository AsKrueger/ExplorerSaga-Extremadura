package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.DataSourceOrigin
import com.alonso.explorersaga.data.PlaceEntity
import com.alonso.explorersaga.data.PlaceRepository
import com.alonso.explorersaga.data.PlacesResult
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlacesUiState(
    val places: List<Place> = emptyList(),
    val filterState: FilterState = FilterState(),
    val selectedPlace: Place? = null,
    val isLoading: Boolean = false,
    val dataSourceOrigin: DataSourceOrigin = DataSourceOrigin.NONE,
    val errorMessage: String? = null
)

// El estado de filtro definitivo con todas las subcategorías
data class FilterState(
    val monuments: Boolean = true,
    val iglesias: Boolean = false,
    val museos: Boolean = false,
    val restaurants: Boolean = false,
    val cafeterias: Boolean = false,
    val bar: Boolean = false,
    val helados: Boolean = false,
    val tiendaAlimentos: Boolean = false,
    val souvenirs: Boolean = false,
    val supermercados: Boolean = false,
    val libreria: Boolean = false,
    val ropa: Boolean = false,
    val tiendasGenerales: Boolean = false,
    val popularFirst: Boolean = false
)

class PlacesViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState(monuments = true, iglesias = true, museos = true, restaurants = true, cafeterias = true, bar = true, helados = true, tiendaAlimentos = true, souvenirs = true, supermercados = true, libreria = true, ropa = true, tiendasGenerales = true))
    val filterState: StateFlow<FilterState> = _filterState

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState

    init {
        loadPlaces()
        viewModelScope.launch {
            updateFilters(FilterState(monuments = true))
        }
    }

    fun loadPlaces() {
        viewModelScope.launch {
            _filterState.flatMapLatest { filters ->
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                val activeCategories = mutableListOf<String>()
                if (filters.monuments) {
                    activeCategories.add("monumento")
                    activeCategories.add("monument")
                    activeCategories.add("historical")
                }
                if (filters.iglesias) {
                    activeCategories.add("iglesia")
                    activeCategories.add("religious")
                    activeCategories.add("worship")
                }
                if (filters.museos) {
                    activeCategories.add("museo")
                    activeCategories.add("museum")
                    activeCategories.add("attraction")
                    activeCategories.add("tourism")
                }
                if (filters.restaurants) {
                    activeCategories.add("restaurante")
                    activeCategories.add("restaurant")
                }
                if (filters.cafeterias) {
                    activeCategories.add("cafeteria")
                    activeCategories.add("cafe")
                }
                if (filters.bar) {
                    activeCategories.add("bar")
                }
                if (filters.helados) {
                    activeCategories.add("heladeria")
                    activeCategories.add("ice_cream")
                }
                if (filters.tiendaAlimentos) {
                    activeCategories.add("tienda_alimentos")
                    activeCategories.add("food")
                }
                if (filters.souvenirs) {
                    activeCategories.add("souvenir")
                }
                if (filters.supermercados) {
                    activeCategories.add("supermercado")
                    activeCategories.add("supermarket")
                }
                if (filters.libreria) {
                    activeCategories.add("libreria")
                    activeCategories.add("book")
                }
                if (filters.ropa) {
                    activeCategories.add("ropa")
                    activeCategories.add("clothes")
                }
                if (filters.tiendasGenerales) {
                    activeCategories.add("tienda_general")
                    activeCategories.add("shop")
                    activeCategories.add("store")
                }

                if (activeCategories.isEmpty()) {
                    flowOf(PlacesResult(emptyList(), DataSourceOrigin.NONE))
                } else {
                    repository.getPlacesByCategories(activeCategories)
                }
            }.collect { result ->
                _uiState.update { currentState ->
                    currentState.copy(
                        places = result.places.map { it.toPlaceUiModel() },
                        filterState = _filterState.value,
                        isLoading = false,
                        dataSourceOrigin = result.origin
                    )
                }
            }
        }
    }

    fun retry() {
        loadPlaces()
    }

    fun updateFilters(newFilterState: FilterState) {
        _filterState.value = newFilterState
    }

    fun loadPlaceById(id: Int) {
        viewModelScope.launch {
            repository.getPlaceByIdFlow(id).collect { placeEntity ->
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
