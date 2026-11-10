package com.alonso.explorersaga.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alonso.explorersaga.data.LatLon
import com.alonso.explorersaga.data.LocationProvider
import com.alonso.explorersaga.data.content.SeasonWindow
import com.alonso.explorersaga.data.db.ContentDao
import com.alonso.explorersaga.data.db.EventEntity
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.data.db.RouteEntity
import com.alonso.explorersaga.ui.model.PlaceGroup
import com.alonso.explorersaga.util.distanceInMeters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class SeasonalEvent(val event: EventEntity, val active: Boolean, val daysUntil: Long)

class HomeViewModel(
    dao: ContentDao,
    private val locationProvider: LocationProvider,
    today: () -> LocalDate = LocalDate::now
) : ViewModel() {

    val location: StateFlow<LatLon?> = locationProvider.location

    /** Toda la agenda: primero lo que está en curso y después lo próximo. */
    val agenda: StateFlow<List<SeasonalEvent>> = dao.events()
        .map { events -> sortSeason(events, today()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** "Ahora en temporada": lo que está en curso o empieza en los próximos 45 días. */
    val season: StateFlow<List<SeasonalEvent>> = agenda
        .map { list -> list.filter { it.active || it.daysUntil <= 45 }.ifEmpty { list.take(3) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Imprescindibles: los más relevantes (Mérida primero), como mucho 3 por municipio. */
    val featured: StateFlow<List<Place>> = dao.featured(200)
        .map { places -> diversify(places, perTown = 3, total = 16) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val routes: StateFlow<List<RouteEntity>> = dao.routes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val placeCount: StateFlow<Int> = dao.placeCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val nearby: StateFlow<List<PlaceItem>?> = locationProvider.location
        .flatMapLatest { location -> if (location == null) flowOf(null) else nearbyFlow(dao, location) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refreshLocation() = locationProvider.refresh()

    private fun nearbyFlow(dao: ContentDao, at: LatLon): Flow<List<PlaceItem>> = flowOf(at).map {
        val box = NEARBY_DEGREES
        dao.placesInBox(
            at.latitude - box, at.latitude + box, at.longitude - box, at.longitude + box,
            PlaceGroup.SIGHTS.map(PlaceGroup::key)
        )
            .map { PlaceItem(it, distanceInMeters(at.latitude, at.longitude, it.latitude, it.longitude)) }
            // Cerca y relevante: cada punto de relevancia "acerca" el lugar 2 km
            .sortedBy { (it.distanceMeters ?: 0.0) - it.place.score * 2_000 }
            .take(12)
    }

    companion object {
        private const val NEARBY_DEGREES = 0.35

        fun diversify(places: List<Place>, perTown: Int, total: Int): List<Place> {
            val counts = mutableMapOf<String, Int>()
            return places.filter { place ->
                val count = counts.getOrDefault(place.municipality, 0)
                (count < perTown).also { if (it) counts[place.municipality] = count + 1 }
            }.take(total)
        }

        fun sortSeason(events: List<EventEntity>, date: LocalDate): List<SeasonalEvent> =
            events.map {
                val window = SeasonWindow.parse(it.fromDay, it.toDay)
                SeasonalEvent(it, window.contains(date), window.daysUntil(date))
            }.sortedWith(compareBy({ !it.active }, { it.daysUntil }, { it.event.id }))
    }
}
