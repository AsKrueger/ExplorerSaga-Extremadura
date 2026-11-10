package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.LatLon
import com.alonso.explorersaga.data.LocationProvider
import com.alonso.explorersaga.data.content.ContentImporter
import com.alonso.explorersaga.data.content.ContentRepository
import com.alonso.explorersaga.data.content.RouteStopDto
import com.alonso.explorersaga.data.db.ContentDao
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.data.db.RouteEntity
import com.alonso.explorersaga.util.distanceInMeters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de carga explícito: así la ficha distingue "cargando" de "ya no existe". */
sealed interface Loadable<out T> {
    data object Loading : Loadable<Nothing>
    data object Missing : Loadable<Nothing>
    data class Ready<T>(val value: T) : Loadable<T>
}

class PlaceDetailViewModel(
    private val placeId: String,
    dao: ContentDao,
    private val repository: ContentRepository
) : ViewModel() {

    val place: StateFlow<Loadable<Place>> = dao.place(placeId)
        .map { it?.let { place -> Loadable.Ready(place) } ?: Loadable.Missing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loadable.Loading)

    val isFavorite: StateFlow<Boolean> = dao.isFavorite(placeId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleFavorite() {
        viewModelScope.launch { repository.setFavorite(placeId, !isFavorite.value) }
    }
}

data class RouteDetail(val route: RouteEntity, val stops: List<RouteStopDto>)

class RouteViewModel(routeId: String, dao: ContentDao) : ViewModel() {
    val route: StateFlow<Loadable<RouteDetail>> = dao.route(routeId)
        .map { route ->
            route?.let { Loadable.Ready(RouteDetail(it, ContentImporter.decodeStops(it.stopsJson))) }
                ?: Loadable.Missing
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loadable.Loading)
}

data class TripItem(val place: Place, val day: Int)

class TripViewModel(
    private val dao: ContentDao,
    private val repository: ContentRepository
) : ViewModel() {

    /** Favoritos con su lugar, ordenados por día del plan (los "sin día" al final). */
    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<TripItem>?> = dao.favorites()
        .mapLatest { favorites ->
            val places = dao.placesByIds(favorites.map { it.placeId }).associateBy { it.id }
            favorites.mapNotNull { favorite -> places[favorite.placeId]?.let { TripItem(it, favorite.day) } }
                .sortedWith(compareBy({ if (it.day == 0) Int.MAX_VALUE else it.day }, { it.place.name }))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun remove(placeId: String) {
        viewModelScope.launch { repository.setFavorite(placeId, false) }
    }

    fun setDay(placeId: String, day: Int) {
        viewModelScope.launch { repository.setFavoriteDay(placeId, day) }
    }
}

data class ServiceGroup(val category: String, val nearest: List<PlaceItem>)

/** Farmacia, centro de salud, cargador y oficina de turismo más cercanos. */
class ServicesViewModel(dao: ContentDao, private val locationProvider: LocationProvider) : ViewModel() {

    val location: StateFlow<LatLon?> = locationProvider.location

    val services: StateFlow<List<ServiceGroup>> = combine(
        dao.placesInGroups(listOf("SERVICES")), locationProvider.location
    ) { places, location ->
        if (location == null) return@combine emptyList()
        CATEGORIES.map { category ->
            ServiceGroup(
                category,
                places.asSequence()
                    .filter { it.category == category }
                    .map { PlaceItem(it, distanceInMeters(location.latitude, location.longitude, it.latitude, it.longitude)) }
                    .sortedBy { it.distanceMeters }
                    .take(3)
                    .toList()
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refreshLocation() = locationProvider.refresh()

    private companion object {
        val CATEGORIES = listOf("pharmacy", "health", "tourist_info", "charging")
    }
}
