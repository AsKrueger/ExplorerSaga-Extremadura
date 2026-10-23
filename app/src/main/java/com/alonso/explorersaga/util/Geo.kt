package com.alonso.explorersaga.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Coordenadas por defecto cuando no se dispone de la ubicación GPS del usuario (centro de Mérida)
val MERIDA_CENTER: Pair<Double, Double> = Pair(38.916111, -6.343611)

private const val EARTH_RADIUS_METERS = 6_371_008.8

// Distancia de Haversine entre dos coordenadas, en metros
fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_METERS * c
}
