package com.alonso.explorersaga.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val description: String,
    val category: String,
    val horarios: String?,
    val direccion: String?,
    val latitude: Double,
    val longitude: Double,
    val photo: String? // <-- CAMPO ACTUALIZADO
)
