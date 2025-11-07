package com.alonso.explorersaga.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable // <-- NUEVO IMPORT

@Serializable // <-- NUEVA ANOTACIÓN
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
    // El campo imageResId no vendrá en el JSON, así que le damos un valor por defecto.
    // La librería de serialización lo ignorará si no lo encuentra en el JSON.
    val imageResId: Int? = null 
)
