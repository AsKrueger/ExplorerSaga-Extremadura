package com.alonso.explorersaga.data

import android.content.Context
import com.alonso.explorersaga.data.remote.ImportResultDto
import com.alonso.explorersaga.data.remote.PlaceResponseDto
import com.alonso.explorersaga.data.remote.RetrofitClient
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
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

class PlaceRepository(
    private val context: Context,
    private val placeDao: PlaceDao = AppDatabase.getDatabase(context).placeDao()
) {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    suspend fun importPlacesFromWeb(): ImportResultDto? = withContext(Dispatchers.IO) {
        try {
            val result = RetrofitClient.api.importPlaces()
            // Tras la importación web, recupera los nuevos lugares de internet y actualiza el JSON local
            val remotePlaces = RetrofitClient.api.getPlaces()
            if (remotePlaces.isNotEmpty()) {
                val entities = remotePlaces.map { it.toPlaceEntity() }
                placeDao.insertAll(entities)
                saveAndSyncToLocalJson(entities)
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPlacesResult(category: String? = null): PlacesResult = withContext(Dispatchers.IO) {
        try {
            // 1. ONLINE: Busca automáticamente en internet / backend
            val remotePlaces = RetrofitClient.api.getPlaces(category)
            if (remotePlaces.isNotEmpty()) {
                val entities = remotePlaces.map { it.toPlaceEntity() }
                // Guarda y actualiza los resultados de internet en la BD local y en el archivo JSON local
                try {
                    placeDao.insertAll(entities)
                    saveAndSyncToLocalJson(entities)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                PlacesResult(entities, DataSourceOrigin.REMOTE)
            } else {
                // Si no hay respuesta online, carga la última versión guardada en el JSON local
                val localPlaces = getLocalPlacesFromPersistentJson(category)
                PlacesResult(localPlaces, DataSourceOrigin.LOCAL_FALLBACK)
            }
        } catch (e: Exception) {
            // 2. OFFLINE (Sin Internet): Carga únicamente la última actualización guardada en el JSON local
            val localPlaces = getLocalPlacesFromPersistentJson(category)
            PlacesResult(localPlaces, DataSourceOrigin.LOCAL_FALLBACK)
        }
    }

    // Guarda y actualiza automáticamente los lugares recuperados de la web dentro del archivo JSON local persistente
    private fun saveAndSyncToLocalJson(newPlaces: List<PlaceEntity>) {
        try {
            val jsonFile = File(context.filesDir, "saved_places.json")
            val existingPlaces: MutableList<Place> = if (jsonFile.exists()) {
                val text = jsonFile.readText()
                if (text.isNotBlank()) {
                    try {
                        json.decodeFromString<List<Place>>(text).toMutableList()
                    } catch (e: Exception) {
                        loadAllPlacesFromAssetsModel().toMutableList()
                    }
                } else {
                    loadAllPlacesFromAssetsModel().toMutableList()
                }
            } else {
                loadAllPlacesFromAssetsModel().toMutableList()
            }

            // Fusionar y deduplicar por nombre y coordenadas los lugares obtenidos de la web
            for (entity in newPlaces) {
                val exists = existingPlaces.any {
                    it.name.equals(entity.name, ignoreCase = true) ||
                    (Math.abs(it.latitude - entity.latitude) < 0.0001 && Math.abs(it.longitude - entity.longitude) < 0.0001)
                }
                if (!exists) {
                    existingPlaces.add(entity.toPlaceModel())
                }
            }

            // Escribir y guardar el archivo JSON actualizado en el almacenamiento local del dispositivo
            val updatedJson = json.encodeToString(existingPlaces)
            jsonFile.writeText(updatedJson)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Carga la última versión guardada del archivo JSON local (o cae a assets si aún no se ha creado)
    private fun getLocalPlacesFromPersistentJson(category: String? = null): List<PlaceEntity> {
        return try {
            val jsonFile = File(context.filesDir, "saved_places.json")
            val places = if (jsonFile.exists()) {
                val text = jsonFile.readText()
                if (text.isNotBlank()) {
                    val placesModel = json.decodeFromString<List<Place>>(text)
                    placesModel.map { it.toPlaceEntity() }
                } else {
                    loadPlacesFromAssets()
                }
            } else {
                loadPlacesFromAssets()
            }

            if (category != null) {
                places.filter { it.category.contains(category, ignoreCase = true) }
            } else {
                places
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val localPlaces = loadPlacesFromAssets()
            if (category != null) localPlaces.filter { it.category.contains(category, ignoreCase = true) } else localPlaces
        }
    }

    suspend fun getPlaceById(id: Long): PlaceEntity? = withContext(Dispatchers.IO) {
        try {
            val remotePlace = RetrofitClient.api.getPlaceById(id)
            val entity = remotePlace.toPlaceEntity()
            try {
                placeDao.insertPlace(entity)
                saveAndSyncToLocalJson(listOf(entity))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            entity
        } catch (e: Exception) {
            e.printStackTrace()
            val persistentPlaces = getLocalPlacesFromPersistentJson()
            persistentPlaces.find { it.id.toLong() == id }
        }
    }

    fun getPlacesByCategories(categories: List<String>): Flow<PlacesResult> = flow {
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

    private fun loadAllPlacesFromAssetsModel(): List<Place> {
        val monuments = loadAndParseJson<List<Place>>(context, "monuments.json")
        val gastronomy = loadAndParseJson<List<Place>>(context, "gastronomia.json")
        val stores = loadAndParseJson<List<Place>>(context, "tiendas.json")
        return monuments + gastronomy + stores
    }

    private fun loadPlacesFromAssets(): List<PlaceEntity> {
        return loadAllPlacesFromAssetsModel().map { it.toPlaceEntity() }
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

fun PlaceEntity.toPlaceModel(): Place {
    return Place(
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
