package com.alonso.explorersaga.ui.viewmodels

import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.DataSourceOrigin
import com.alonso.explorersaga.data.PlaceEntity
import com.alonso.explorersaga.data.PlaceRepository
import com.alonso.explorersaga.data.toPlaceModel
import com.alonso.explorersaga.model.Place
import com.alonso.explorersaga.util.MERIDA_CENTER
import com.alonso.explorersaga.util.distanceInMeters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlacesUiState(
    val places: List<Place> = emptyList(),
    val filterState: FilterState = FilterState(),
    val selectedPlace: Place? = null,
    val isLoading: Boolean = false,
    val dataSourceOrigin: DataSourceOrigin = DataSourceOrigin.NONE,
    val errorMessage: String? = null,
    val userLocation: Pair<Double, Double> = MERIDA_CENTER
)

private const val IMPORT_ERROR_MESSAGE =
    "No se pudo importar desde OpenStreetMap. El servicio puede estar saturado: inténtalo de nuevo en unos minutos."

class PlacesViewModel(private val repository: PlaceRepository) : ViewModel() {

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Ubicación real GPS del usuario (si está disponible en el dispositivo)
    private val _userLocation = MutableStateFlow(MERIDA_CENTER)
    val userLocation: StateFlow<Pair<Double, Double>> = _userLocation

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState

    // Cada incremento fuerza una nueva consulta al backend (o al respaldo local)
    private val refreshTrigger = MutableStateFlow(0)

    init {
        observePlaces()
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun observePlaces() {
        val results = refreshTrigger.mapLatest {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getPlacesResult()
        }

        viewModelScope.launch {
            // Los filtros, la búsqueda y la ubicación se aplican en local: no requieren volver a consultar la red
            combine(results, _filterState, _searchQuery.debounce(250), _userLocation) { result, filters, query, location ->
                val places = result.places
                    .filter { filters.matches(it.category) }
                    .filter { it.matchesQuery(query) }
                    .map { it.toPlaceModel() }
                    .sortedBy { distanceInMeters(location.first, location.second, it.latitude, it.longitude) }

                _uiState.update {
                    it.copy(
                        places = places,
                        filterState = filters,
                        isLoading = false,
                        dataSourceOrigin = result.origin,
                        userLocation = location
                    )
                }
            }.collect()
        }
    }

    fun importFromWeb() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            if (repository.importPlacesFromWeb() != null) {
                refresh()
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = IMPORT_ERROR_MESSAGE)
                }
            }
        }
    }

    fun retry() {
        refresh()
    }

    private fun refresh() {
        refreshTrigger.update { it + 1 }
    }

    // Actualiza la ubicación GPS real del usuario desde el sensor del dispositivo
    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
    }

    // Obtiene la última ubicación conocida usando el LocationManager del sistema Android
    fun fetchDeviceLocation(context: Context) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return
            val providers = listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
            )
            val bestLocation = providers
                .filter { locationManager.isProviderEnabled(it) || it == LocationManager.PASSIVE_PROVIDER }
                .firstNotNullOfOrNull { locationManager.getLastKnownLocation(it) }

            bestLocation?.let { updateUserLocation(it.latitude, it.longitude) }
        } catch (e: SecurityException) {
            // Sin permiso de ubicación: se mantiene la ubicación por defecto
            e.printStackTrace()
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun updateFilters(newFilterState: FilterState) {
        _filterState.value = newFilterState
    }

    fun loadPlaceById(id: Int) {
        // Se prioriza el lugar ya cargado en la lista para mostrarlo al instante y evitar
        // confundirlo con otro lugar con el mismo id procedente de una fuente distinta
        val placeFromList = _uiState.value.places.find { it.id == id }
        _uiState.update { it.copy(selectedPlace = placeFromList) }
        if (placeFromList != null) return

        viewModelScope.launch {
            val place = repository.getPlaceById(id.toLong())?.toPlaceModel()
            _uiState.update { it.copy(selectedPlace = place) }
        }
    }

    fun clearSelectedPlace() {
        _uiState.update { it.copy(selectedPlace = null) }
    }
}

private fun PlaceEntity.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    return name.contains(query, ignoreCase = true) ||
            description.contains(query, ignoreCase = true) ||
            direccion?.contains(query, ignoreCase = true) == true
}
