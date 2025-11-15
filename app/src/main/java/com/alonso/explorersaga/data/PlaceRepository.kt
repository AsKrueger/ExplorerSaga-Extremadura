package com.alonso.explorersaga.data

import android.content.Context
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import java.io.IOException

class PlaceRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val allPlaces: List<PlaceEntity> by lazy {
        loadPlacesFromAssets()
    }

    private fun loadPlacesFromAssets(): List<PlaceEntity> {
        val monuments = loadAndParseJson<List<Place>>(context, "monuments.json")
        val gastronomy = loadAndParseJson<List<Place>>(context, "gastronomia.json")
        val stores = loadAndParseJson<List<Place>>(context, "tiendas.json")

        val allPlacesModel = monuments + gastronomy + stores
        return allPlacesModel.map { it.toPlaceEntity() }
    }

    fun getPlacesByCategories(categories: List<String>): Flow<List<PlaceEntity>> {
        val filteredPlaces = allPlaces.filter { it.category in categories }
        return flowOf(filteredPlaces)
    }

    fun getPlaceById(id: Int): Flow<PlaceEntity?> {
        val place = allPlaces.find { it.id == id }
        return flowOf(place)
    }

    private inline fun <reified T> loadAndParseJson(context: Context, fileName: String): T {
        val jsonString = try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            // Devolvemos un string JSON vacío si hay un error para evitar que la app crashee
            return json.decodeFromString("[]")
        }
        return json.decodeFromString(jsonString)
    }
}


// Función de extensión para convertir el modelo de la API/JSON a la entidad de la base de datos
fun Place.toPlaceEntity(): PlaceEntity {
    return PlaceEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        category = this.category,
        horarios = this.horarios,
        direccion = this.direccion,
        latitude = this.latitude,
        longitude = this.longitude,
        photo = this.photo,
        website = this.website
    )
}
