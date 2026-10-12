package com.alonso.explorersaga.data

import android.content.Context
import com.alonso.explorersaga.data.remote.PlaceResponseDto
import com.alonso.explorersaga.data.remote.RetrofitClient
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

class PlaceRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getPlaces(): List<PlaceEntity> = withContext(Dispatchers.IO) {
        try {
            val remotePlaces = RetrofitClient.api.getPlaces()
            if (remotePlaces.isNotEmpty()) {
                remotePlaces.map { it.toPlaceEntity() }
            } else {
                loadPlacesFromAssets()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadPlacesFromAssets()
        }
    }

    suspend fun getPlaceById(id: Long): PlaceEntity? = withContext(Dispatchers.IO) {
        try {
            val remotePlace = RetrofitClient.api.getPlaceById(id)
            remotePlace.toPlaceEntity()
        } catch (e: Exception) {
            e.printStackTrace()
            val all = loadPlacesFromAssets()
            all.find { it.id.toLong() == id }
        }
    }

    fun getPlacesByCategories(categories: List<String>): Flow<List<PlaceEntity>> = flow {
        val places = getPlaces()
        val filtered = places.filter { place ->
            categories.any { cat -> place.category.contains(cat, ignoreCase = true) }
        }
        emit(filtered)
    }

    fun getPlaceByIdFlow(id: Int): Flow<PlaceEntity?> = flow {
        val place = getPlaceById(id.toLong())
        emit(place)
    }

    private fun loadPlacesFromAssets(): List<PlaceEntity> {
        val monuments = loadAndParseJson<List<Place>>(context, "monuments.json")
        val gastronomy = loadAndParseJson<List<Place>>(context, "gastronomia.json")
        val stores = loadAndParseJson<List<Place>>(context, "tiendas.json")

        val allPlacesModel = monuments + gastronomy + stores
        return allPlacesModel.map { it.toPlaceEntity() }
    }

    private inline fun <reified T> loadAndParseJson(context: Context, fileName: String): T {
        val jsonString = try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return json.decodeFromString("[]")
        }
        return json.decodeFromString(jsonString)
    }
}

fun PlaceResponseDto.toPlaceEntity(): PlaceEntity {
    return PlaceEntity(
        id = this.id.toInt(),
        name = this.name,
        description = this.description ?: "",
        category = this.category?.name ?: "Monument",
        horarios = "",
        direccion = this.address ?: "",
        latitude = this.latitude,
        longitude = this.longitude,
        photo = "",
        website = ""
    )
}

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
