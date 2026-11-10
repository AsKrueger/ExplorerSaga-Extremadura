package com.alonso.explorersaga.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextTest {

    @Test
    fun foldRemovesAccentsCaseAndSymbols() {
        assertEquals("caceres", "Cáceres".foldForSearch())
        assertEquals("caceres", "caceres".foldForSearch())
        assertEquals("monfrague parque", "Monfragüe — Parque".foldForSearch())
    }

    @Test
    fun ftsQueryUsesPrefixesAndDropsOperators() {
        assertEquals("teatro* merida*", ftsQuery("Teatro  Mérida"))
        assertEquals("a* or* b*", ftsQuery("a OR \"b\""))
        assertNull(ftsQuery("  ¿? "))
    }
}
