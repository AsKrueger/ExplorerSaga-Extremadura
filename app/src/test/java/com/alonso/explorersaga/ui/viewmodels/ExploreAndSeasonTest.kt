package com.alonso.explorersaga.ui.viewmodels

import com.alonso.explorersaga.data.LatLon
import com.alonso.explorersaga.data.db.EventEntity
import com.alonso.explorersaga.data.db.Place
import com.alonso.explorersaga.ui.model.PlaceGroup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ExploreAndSeasonTest {

    private fun place(id: String, muni: String, prov: String, lat: Double, lon: Double, score: Int = 0) = Place(
        id = id, name = id, category = "museum", groupKey = "MUSEUMS", latitude = lat, longitude = lon,
        municipality = muni, province = prov, score = score
    )

    private val merida = place("merida", "Mérida", "BA", 38.916, -6.344, score = 2)
    private val caceres = place("caceres", "Cáceres", "CC", 39.474, -6.371, score = 9)
    private val badajoz = place("badajoz", "Badajoz", "BA", 38.879, -6.970, score = 5)
    private val all = listOf(merida, caceres, badajoz)

    @Test
    fun withoutLocationSortsByRelevance() {
        val items = ExploreViewModel.refine(all, ExploreFilters(), location = null)
        assertEquals(listOf("caceres", "badajoz", "merida"), items.map { it.place.id })
        assertNull(items.first().distanceMeters)
    }

    @Test
    fun withLocationSortsByDistance() {
        val items = ExploreViewModel.refine(all, ExploreFilters(), LatLon(38.92, -6.34))
        assertEquals("merida", items.first().place.id)
        assertTrue(items.zipWithNext().all { (a, b) -> a.distanceMeters!! <= b.distanceMeters!! })
    }

    @Test
    fun filtersByProvinceAndMunicipality() {
        assertEquals(
            listOf("badajoz", "merida"),
            ExploreViewModel.refine(all, ExploreFilters(province = "BA"), null).map { it.place.id }
        )
        assertEquals(
            listOf("caceres"),
            ExploreViewModel.refine(all, ExploreFilters(municipality = "Cáceres"), null).map { it.place.id }
        )
    }

    @Test
    fun allGroupExcludesServices() {
        assertFalse(PlaceGroup.SERVICES in ExploreFilters().groups)
        assertEquals(listOf(PlaceGroup.SERVICES), ExploreFilters(group = PlaceGroup.SERVICES).groups)
    }

    @Test
    fun seasonPutsActiveEventsFirstThenUpcoming() {
        fun event(id: String, from: String, to: String) =
            EventEntity(id, "nature", from, to, "x", 0.0, 0.0, id, id, "", "")
        val sorted = HomeViewModel.sortSeason(
            listOf(event("autumn", "10-01", "10-31"), event("spring", "03-15", "04-20"), event("winter", "11-15", "02-28")),
            LocalDate.of(2027, 1, 10)
        )
        assertEquals(listOf("winter", "spring", "autumn"), sorted.map { it.event.id })
        assertTrue(sorted.first().active)
        assertEquals(64, sorted[1].daysUntil)
    }

    @Test
    fun featuredKeepsOrderButCapsEachTown() {
        val places = listOf(merida, merida.copy(id = "m2"), merida.copy(id = "m3"), merida.copy(id = "m4"), caceres, badajoz)
        val featured = HomeViewModel.diversify(places, perTown = 3, total = 5)
        assertEquals(listOf("merida", "m2", "m3", "caceres", "badajoz"), featured.map { it.id })
    }

    @Test
    fun placeFallsBackToOtherLanguageDescription() {
        val spanishOnly = merida.copy(descriptionEs = "Texto", nameEn = "Merida")
        assertEquals("Texto" to true, spanishOnly.description(english = true))
        assertEquals("Texto" to false, spanishOnly.description(english = false))
        assertEquals("Merida", spanishOnly.displayName(english = true))
        assertEquals(
            "https://es.wikipedia.org/wiki/Teatro_romano_de_M%C3%A9rida",
            merida.copy(wikipediaEs = "Teatro romano de Mérida").wikipediaUrl(english = true)
        )
    }
}
