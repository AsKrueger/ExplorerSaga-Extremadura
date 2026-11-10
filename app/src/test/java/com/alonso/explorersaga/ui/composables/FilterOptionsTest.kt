package com.alonso.explorersaga.ui.composables

import com.alonso.explorersaga.ui.viewmodels.FilterState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterOptionsTest {

    @Test
    fun everyFilterStateField_hasExactlyOneOption() {
        // Si se añade un filtro a FilterState sin describirlo aquí, ALL tendría una opción sin chip
        val allOn = FilterOption.entries.fold(FilterState.ALL) { state, option -> option.set(state, true) }
        assertEquals(FilterState.ALL, allOn)
        FilterOption.entries.forEach { assertTrue(it.isOn(FilterState.ALL)) }
    }

    @Test
    fun presets_belongToTheirGroup() {
        FilterGroup.entries.forEach { group ->
            assertEquals(group, group.preset.singleGroup())
            FilterOption.of(group).forEach { assertTrue(it.isOn(group.preset)) }
        }
    }

    @Test
    fun singleGroup_isNullWhenSeveralGroupsAreActive() {
        assertNull(FilterState.ALL.singleGroup())
    }

    @Test
    fun onlyOption_keepsJustThatFilter() {
        val state = onlyOption(FilterGroup.FOOD, FilterOption.CAFES)

        assertTrue(state.cafeterias)
        FilterOption.entries.filter { it != FilterOption.CAFES }.forEach { assertFalse(it.isOn(state)) }
        assertTrue(state.matches("cafeteria"))
        assertFalse(state.matches("restaurante"))
    }

    @Test
    fun onlyOption_withoutOption_selectsTheWholeGroup() {
        assertEquals(FilterGroup.SHOPS.preset, onlyOption(FilterGroup.SHOPS, null))
    }
}
