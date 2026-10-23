package com.alonso.explorersaga.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoTest {

    @Test
    fun distanceToSamePoint_isZero() {
        val (lat, lon) = MERIDA_CENTER
        assertEquals(0.0, distanceInMeters(lat, lon, lat, lon), 0.0)
    }

    @Test
    fun distanceBetweenTeatroRomanoAndAcueductoMilagros_isAboutOnePointThreeKm() {
        val meters = distanceInMeters(38.915222, -6.338278, 38.923889, -6.348611)
        assertEquals(1_310.0, meters, 30.0)
    }

    @Test
    fun distance_isSymmetric() {
        val ab = distanceInMeters(38.9, -6.3, 39.0, -6.4)
        val ba = distanceInMeters(39.0, -6.4, 38.9, -6.3)
        assertEquals(ab, ba, 1e-6)
    }
}
