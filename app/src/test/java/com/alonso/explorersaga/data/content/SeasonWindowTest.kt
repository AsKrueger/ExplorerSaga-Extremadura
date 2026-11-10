package com.alonso.explorersaga.data.content

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SeasonWindowTest {

    @Test
    fun windowWithinTheYear() {
        val cherry = SeasonWindow.parse("03-15", "04-20")
        assertTrue(cherry.contains(LocalDate.of(2027, 3, 15)))
        assertTrue(cherry.contains(LocalDate.of(2027, 4, 20)))
        assertFalse(cherry.contains(LocalDate.of(2027, 4, 21)))
    }

    @Test
    fun windowCrossingNewYear() {
        val cranes = SeasonWindow.parse("11-15", "02-28")
        assertTrue(cranes.contains(LocalDate.of(2026, 12, 31)))
        assertTrue(cranes.contains(LocalDate.of(2027, 1, 10)))
        assertFalse(cranes.contains(LocalDate.of(2027, 6, 1)))
    }

    @Test
    fun daysUntilNextStart() {
        val merida = SeasonWindow.parse("07-01", "08-31")
        assertEquals(0, merida.daysUntil(LocalDate.of(2027, 7, 10)))
        assertEquals(30, merida.daysUntil(LocalDate.of(2027, 6, 1)))
        assertEquals(LocalDate.of(2026, 9, 1).until(LocalDate.of(2027, 7, 1), java.time.temporal.ChronoUnit.DAYS),
            merida.daysUntil(LocalDate.of(2026, 9, 1)))
    }
}
