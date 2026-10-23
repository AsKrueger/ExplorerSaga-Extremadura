package com.alonso.explorersaga.ui.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterStateTest {

    @Test
    fun defaultState_matchesOnlyMonuments() {
        val filters = FilterState()

        assertTrue(filters.matches("monumento"))
        assertTrue(filters.matches("Historical Monument"))
        assertFalse(filters.matches("restaurante"))
        assertFalse(filters.matches("iglesia"))
    }

    @Test
    fun historicoPreset_matchesLocalAndBackendCategories() {
        val filters = FilterState.HISTORICO

        listOf("monumento", "iglesia", "museo", "Religious Site", "Tourism Attraction").forEach {
            assertTrue("Debería incluir $it", filters.matches(it))
        }
        assertFalse(filters.matches("bar"))
    }

    @Test
    fun gastronomiaPreset_matchesFoodAndDrinkCategories() {
        val filters = FilterState.GASTRONOMIA

        listOf("restaurante", "cafeteria", "bar", "heladeria", "Gastronomía").forEach {
            assertTrue("Debería incluir $it", filters.matches(it))
        }
        assertFalse(filters.matches("monumento"))
        assertFalse(filters.matches("supermercado"))
    }

    @Test
    fun tiendasPreset_matchesShopCategories() {
        val filters = FilterState.TIENDAS

        listOf("alimentos", "tienda_alimentos", "souvenir", "supermercado", "libreria", "ropa", "Tiendas", "tiendasGenerales").forEach {
            assertTrue("Debería incluir $it", filters.matches(it))
        }
        assertFalse(filters.matches("cafeteria"))
    }

    @Test
    fun singleSubFilter_excludesSiblingCategories() {
        val onlySupermarkets = FilterState.TIENDAS.copy(
            tiendaAlimentos = false, souvenirs = false, libreria = false, ropa = false, tiendasGenerales = false
        )

        assertTrue(onlySupermarkets.matches("supermercado"))
        assertFalse(onlySupermarkets.matches("libreria"))
        assertFalse(onlySupermarkets.matches("souvenir"))
    }

    @Test
    fun noActiveFilters_matchesNothing() {
        val none = FilterState(monuments = false)

        assertEquals(emptyList<String>(), none.activeCategoryKeywords())
        assertFalse(none.matches("monumento"))
        assertFalse(none.matches(""))
    }

    @Test
    fun allPreset_matchesEveryLocalCategory() {
        listOf(
            "monumento", "iglesia", "museo", "restaurante", "cafeteria", "bar",
            "heladeria", "alimentos", "libreria", "souvenir", "supermercado"
        ).forEach {
            assertTrue("Debería incluir $it", FilterState.ALL.matches(it))
        }
    }
}
