package com.alonso.explorersaga.model

// Modelo de datos para la UI. Los campos que pueden faltar son nulos
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
