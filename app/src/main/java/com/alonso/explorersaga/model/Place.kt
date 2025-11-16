package com.alonso.explorersaga.model

import kotlinx.serialization.Serializable

// Modelo de datos para la UI. Ahora los campos que pueden ser nulos están marcados con ?
@Serializable
data class Place(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val horarios: String?,
    val direccion: String?,
    val latitude: Double,
    val longitude: Double,
    val photo: String?,
    val website: String?
)
