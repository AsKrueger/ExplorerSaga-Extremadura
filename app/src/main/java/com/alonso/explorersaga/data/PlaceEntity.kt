package com.alonso.explorersaga.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val description: String,
    val category: String,

    // ¡Tu idea! Columna para guardar los horarios.
    val horarios: String,

    // ¡Tu idea! Columna para la dirección física.
    val direccion: String,

    // ¡Tu idea! Columna para la latitud geográfica.
    val latitude: Double,

    // ¡Tu idea! Columna para la longitud geográfica.
    val longitude: Double,
    
    // ¡Tu idea! Columna para la imagen de portada.
    // Guardará el ID del recurso drawable (ej: R.drawable.mi_imagen).
    val imageResId: Int
)
