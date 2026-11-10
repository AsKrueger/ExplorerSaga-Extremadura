package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.LatLon
import com.alonso.explorersaga.data.LocationProvider
import com.alonso.explorersaga.data.db.ContentDao
import com.alonso.explorersaga.data.db.Municipality
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.ui.model.PlaceGroup
import com.alonso.explorersaga.util.distanceInMeters
import com.alonso.explorersaga.util.ftsQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ExploreFilters(
    // null = "Todo" (todo menos servicios)
    val group: PlaceGroup? = null,
    val province: String? = null,
    val municipality: String? = null,
    val query: String = ""
) {
    val groups: List<PlaceGroup> get() = group?.let(::listOf) ?: PlaceGroup.DISCOVER
    val isDefault: Boolean get() = this == ExploreFilters()
}

data class PlaceItem(val place: Place, val distanceMeters: Double?)

data class ExploreResults(
    val items: List<PlaceItem> = emptyList(),
    val loading: Boolean = true
)

/** Filtros, búsqueda y resultados compartidos por Explorar y el mapa. */
class ExploreViewModel(
    private val dao: ContentDao,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _filters = MutableStateFlow(ExploreFilters())
    val filters: StateFlow<ExploreFilters> = _filters.asStateFlow()

    val location: StateFlow<LatLon?> = locationProvider.location

    /** Lugar que el mapa debe centrar al abrirse desde una ficha o una lista. */
    private val _mapFocus = MutableStateFlow<String?>(null)
    val mapFocus: StateFlow<String?> = _mapFocus.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val focusedPlace: StateFlow<Place?> = _mapFocus
        .flatMapLatest { id -> if (id == null) kotlinx.coroutines.flow.flowOf(null) else dao.place(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Última vista del mapa, para recuperarla al volver a la pestaña (lat, lon, zoom). */
    var mapCamera: Triple<Double, Double, Double>? = null

    val municipalities: StateFlow<List<Municipality>> = dao.municipalities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val results: StateFlow<ExploreResults> = run {
        val source = _filters
            .map { it.groups.map(PlaceGroup::key) to it.query }
            .distinctUntilChanged()
            .debounce(150)
            .flatMapLatest { (groups, query) ->
                val fts = ftsQuery(query)
                if (fts == null) dao.placesInGroups(groups) else dao.search(fts, groups)
            }
        combine(source, _filters, locationProvider.location) { places, filters, location ->
            ExploreResults(items = refine(places, filters, location), loading = false)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExploreResults())
    }

    fun setQuery(query: String) = _filters.update { it.copy(query = query) }
    fun setGroup(group: PlaceGroup?) = _filters.update { it.copy(group = group) }
    fun setProvince(province: String?) = _filters.update { it.copy(province = province, municipality = null) }
    fun setMunicipality(municipality: String?) = _filters.update { it.copy(municipality = municipality) }
    fun clearFilters() = _filters.update { ExploreFilters() }

    /** Abre Explorar con un grupo y, opcionalmente, un municipio (desde Inicio). */
    fun showGroup(group: PlaceGroup?, municipality: String? = null) {
        _filters.value = ExploreFilters(group = group, municipality = municipality)
    }

    fun focusOnMap(placeId: String?) {
        _mapFocus.value = placeId
    }

    fun refreshLocation() = locationProvider.refresh()

    companion object {
        /** Filtra por provincia y municipio y ordena por cercanía o, sin ubicación, por relevancia. */
        fun refine(places: List<Place>, filters: ExploreFilters, location: LatLon?): List<PlaceItem> =
            places.asSequence()
                .filter { filters.province == null || it.province == filters.province }
                .filter { filters.municipality == null || it.municipality == filters.municipality }
                .map { place ->
                    PlaceItem(place, location?.let {
                        distanceInMeters(it.latitude, it.longitude, place.latitude, place.longitude)
                    })
                }
                .sortedWith(
                    if (location != null) compareBy({ it.distanceMeters }, { it.place.id })
                    else compareByDescending<PlaceItem> { it.place.score }.thenBy { it.place.name }
                )
                .toList()
    }
}
