package com.alonso.explorersaga.data

import android.content.Context
import com.alonso.explorersaga.data.remote.ImportResultDto
import com.alonso.explorersaga.data.remote.PlaceResponseDto
import com.alonso.explorersaga.data.remote.RetrofitClient
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.Dispatchers
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
            // Los lugares importados se descargan y guardan en local en la siguiente llamada a getPlacesResult()
            RetrofitClient.api.importPlaces()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPlacesResult(): PlacesResult = withContext(Dispatchers.IO) {
        try {
            // 1. ONLINE: Busca automáticamente en internet / backend
            val remotePlaces = RetrofitClient.api.getPlaces()
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
                PlacesResult(getLocalPlacesFromPersistentJson(), DataSourceOrigin.LOCAL_FALLBACK)
            }
        } catch (e: Exception) {
            // 2. OFFLINE (Sin Internet): Carga únicamente la última actualización guardada en el JSON local
            PlacesResult(getLocalPlacesFromPersistentJson(), DataSourceOrigin.LOCAL_FALLBACK)
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
    private fun getLocalPlacesFromPersistentJson(): List<PlaceEntity> {
        return try {
            val jsonFile = File(context.filesDir, "saved_places.json")
            if (jsonFile.exists()) {
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
        } catch (e: Exception) {
            e.printStackTrace()
            loadPlacesFromAssets()
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
        description = this.description.orEmpty(),
        category = this.category?.name ?: "Monument",
        // El backend todavía no expone horarios, fotos ni web: se dejan a null para que la UI los oculte
        horarios = null,
        direccion = this.address?.takeIf { it.isNotBlank() },
        latitude = this.latitude,
        longitude = this.longitude,
        photo = null,
        website = null
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
