package com.alonso.explorersaga.ui.viewmodels

import android.content.Context
import android.location.LocationManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.DataSourceOrigin
import com.alonso.explorersaga.data.PlaceEntity
import com.alonso.explorersaga.data.PlaceRepository
import com.alonso.explorersaga.data.PlacesResult
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlacesUiState(
    val places: List<Place> = emptyList(),
    val filterState: FilterState = FilterState(),
    val selectedPlace: Place? = null,
    val isLoading: Boolean = false,
    val dataSourceOrigin: DataSourceOrigin = DataSourceOrigin.NONE,
    val errorMessage: String? = null,
    val userLocation: Pair<Double, Double> = Pair(38.916111, -6.343611) // Coordenadas por defecto (Centro de Mérida)
)

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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // Ubicación real GPS del usuario (si está disponible en el dispositivo)
    private val _userLocation = MutableStateFlow<Pair<Double, Double>>(Pair(38.916111, -6.343611))
    val userLocation: StateFlow<Pair<Double, Double>> = _userLocation

    private val _uiState = MutableStateFlow(PlacesUiState())
    val uiState: StateFlow<PlacesUiState> = _uiState

    init {
        loadPlaces()
        viewModelScope.launch {
            updateFilters(FilterState(monuments = true))
        }
    }

    fun importFromWeb() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.importPlacesFromWeb()
            loadPlaces()
        }
    }

    // Actualiza la ubicación GPS real del usuario desde el sensor del dispositivo
    fun updateUserLocation(latitude: Double, longitude: Double) {
        _userLocation.value = Pair(latitude, longitude)
    }

    // Obtiene la ubicación real del GPS usando el LocationManager del sistema Android
    fun fetchDeviceLocation(context: Context) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager != null) {
                val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                var bestLocation: Pair<Double, Double>? = null
                if (isGpsEnabled) {
                    val gpsLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (gpsLoc != null) {
                        bestLocation = Pair(gpsLoc.latitude, gpsLoc.longitude)
                    }
                }
                if (bestLocation == null && isNetworkEnabled) {
                    val netLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (netLoc != null) {
                        bestLocation = Pair(netLoc.latitude, netLoc.longitude)
                    }
                }
                if (bestLocation == null) {
                    val passiveLoc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    if (passiveLoc != null) {
                        bestLocation = Pair(passiveLoc.latitude, passiveLoc.longitude)
                    }
                }

                bestLocation?.let { (lat, lon) ->
                    updateUserLocation(lat, lon)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    fun loadPlaces() {
        viewModelScope.launch {
            combine(_filterState, _searchQuery, _userLocation) { filters, query, location ->
                Triple(filters, query, location)
            }
            .debounce(250) // Búsqueda reactiva tipo web con 250ms de debounce
            .flatMapLatest { (filters, query, location) ->
                _uiState.update { it.copy(isLoading = true, errorMessage = null, userLocation = location) }
                val activeCategories = mutableListOf<String>()
                if (filters.monuments) {
                    activeCategories.add("monumento")
                    activeCategories.add("monument")
                    activeCategories.add("historical")
                    activeCategories.add("Historical Monument")
                }
                if (filters.iglesias) {
                    activeCategories.add("iglesia")
                    activeCategories.add("religious")
                    activeCategories.add("worship")
                    activeCategories.add("Religious Site")
                }
                if (filters.museos) {
                    activeCategories.add("museo")
                    activeCategories.add("museum")
                    activeCategories.add("attraction")
                    activeCategories.add("tourism")
                    activeCategories.add("Tourism Attraction")
                }
                if (filters.restaurants) {
                    activeCategories.add("restaurante")
                    activeCategories.add("restaurant")
                    activeCategories.add("Gastronomía")
                }
                if (filters.cafeterias) {
                    activeCategories.add("cafeteria")
                    activeCategories.add("cafe")
                }
                if (filters.bar) {
                    activeCategories.add("bar")
                    activeCategories.add("pub")
                }
                if (filters.helados) {
                    activeCategories.add("heladeria")
                    activeCategories.add("ice_cream")
                }
                if (filters.tiendaAlimentos) {
                    activeCategories.add("tienda_alimentos")
                    activeCategories.add("alimentos")
                    activeCategories.add("food")
                    activeCategories.add("Tiendas")
                }
                if (filters.souvenirs) {
                    activeCategories.add("souvenir")
                    activeCategories.add("gift")
                    activeCategories.add("Tiendas")
                }
                if (filters.supermercados) {
                    activeCategories.add("supermercado")
                    activeCategories.add("supermarket")
                    activeCategories.add("Tiendas")
                }
                if (filters.libreria) {
                    activeCategories.add("libreria")
                    activeCategories.add("book")
                    activeCategories.add("Tiendas")
                }
                if (filters.ropa) {
                    activeCategories.add("ropa")
                    activeCategories.add("clothes")
                    activeCategories.add("Tiendas")
                }
                if (filters.tiendasGenerales) {
                    activeCategories.add("tienda_general")
                    activeCategories.add("tiendas")
                    activeCategories.add("shop")
                    activeCategories.add("store")
                    activeCategories.add("Tiendas")
                }

                if (activeCategories.isEmpty()) {
                    flowOf(Triple(PlacesResult(emptyList(), DataSourceOrigin.NONE), query, location))
                } else {
                    repository.getPlacesByCategories(activeCategories).map { Triple(it, query, location) }
                }
            }.collect { (result, query, location) ->
                // Filtrado bidireccional por categorías
                val filteredByCategory = result.places.filter { place ->
                    _filterState.value.let { filters ->
                        val activeCats = mutableListOf<String>()
                        if (filters.monuments) activeCats.addAll(listOf("monumento", "monument", "historical", "Historical Monument"))
                        if (filters.iglesias) activeCats.addAll(listOf("iglesia", "religious", "worship", "Religious Site"))
                        if (filters.museos) activeCats.addAll(listOf("museo", "museum", "attraction", "tourism", "Tourism Attraction"))
                        if (filters.restaurants) activeCats.addAll(listOf("restaurante", "restaurant", "Gastronomía"))
                        if (filters.cafeterias) activeCats.addAll(listOf("cafeteria", "cafe"))
                        if (filters.bar) activeCats.addAll(listOf("bar", "pub"))
                        if (filters.helados) activeCats.addAll(listOf("heladeria", "ice_cream"))
                        if (filters.tiendaAlimentos) activeCats.addAll(listOf("tienda_alimentos", "alimentos", "food"))
                        if (filters.souvenirs) activeCats.addAll(listOf("souvenir", "gift"))
                        if (filters.supermercados) activeCats.addAll(listOf("supermercado", "supermarket"))
                        if (filters.libreria) activeCats.addAll(listOf("libreria", "book"))
                        if (filters.ropa) activeCats.addAll(listOf("ropa", "clothes"))
                        if (filters.tiendasGenerales) activeCats.addAll(listOf("tienda_general", "tiendas", "shop", "store"))

                        activeCats.any { cat ->
                            place.category.contains(cat, ignoreCase = true) || cat.contains(place.category, ignoreCase = true)
                        }
                    }
                }

                // Filtrado reactivo por término de búsqueda (nombre, dirección o descripción)
                val filteredByName = if (query.isNotBlank()) {
                    filteredByCategory.filter {
                        it.name.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        (it.direccion?.contains(query, ignoreCase = true) == true)
                    }
                } else {
                    filteredByCategory
                }

                // Ordenar automáticamente los resultados por cercanía a la localización GPS real del usuario
                val sortedPlaces = filteredByName
                    .map { it.toPlaceUiModel() }
                    .sortedBy { calculateDistanceInMeters(location.first, location.second, it.latitude, it.longitude) }

                _uiState.update { currentState ->
                    currentState.copy(
                        places = sortedPlaces,
                        filterState = _filterState.value,
                        isLoading = false,
                        dataSourceOrigin = result.origin,
                        userLocation = location
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
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

    private fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadiusKm = 6371.0088
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadiusKm * c * 1000.0
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
