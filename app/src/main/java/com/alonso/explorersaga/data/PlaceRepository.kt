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

enum class DataSourceOrigin {
    REMOTE,
    LOCAL_FALLBACK,
    NONE
}

data class PlacesResult(
    val places: List<PlaceEntity>,
    val origin: DataSourceOrigin
)

class PlaceRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getPlacesResult(category: String? = null): PlacesResult = withContext(Dispatchers.IO) {
        try {
            val remotePlaces = RetrofitClient.api.getPlaces(category)
            if (remotePlaces.isNotEmpty()) {
                PlacesResult(remotePlaces.map { it.toPlaceEntity() }, DataSourceOrigin.REMOTE)
            } else {
                val localPlaces = loadPlacesFromAssets()
                val filteredLocal = if (category != null) localPlaces.filter { it.category.contains(category, ignoreCase = true) } else localPlaces
                PlacesResult(filteredLocal, DataSourceOrigin.LOCAL_FALLBACK)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val localPlaces = loadPlacesFromAssets()
            val filteredLocal = if (category != null) localPlaces.filter { it.category.contains(category, ignoreCase = true) } else localPlaces
            PlacesResult(filteredLocal, DataSourceOrigin.LOCAL_FALLBACK)
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

    fun getPlacesByCategories(categories: List<String>): Flow<PlacesResult> = flow {
        // If single category selected, pass it to backend query
        val categoryQuery = if (categories.size == 1) categories.first() else null
        val result = getPlacesResult(categoryQuery)
        val filtered = if (categoryQuery != null) {
            result.places
        } else {
            result.places.filter { place ->
                categories.any { cat -> place.category.contains(cat, ignoreCase = true) }
            }
        }
        emit(PlacesResult(filtered, result.origin))
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
