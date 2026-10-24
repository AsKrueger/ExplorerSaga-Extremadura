package com.alonso.explorersaga.data

import com.alonso.explorersaga.data.remote.ExplorerSagaApi
import com.alonso.explorersaga.data.remote.ImportResultDto
import com.alonso.explorersaga.data.remote.PlaceResponseDto
import com.alonso.explorersaga.model.Place
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class DataSourceOrigin {
    REMOTE,
    LOCAL_FALLBACK,
    NONE
}

data class PlacesResult(
    val places: List<PlaceEntity>,
    val origin: DataSourceOrigin
)

// Room es la única caché local: refleja la última sincronización correcta con el backend
// o, si nunca la hubo, los lugares incluidos en la app
class PlaceRepository(
    private val placeDao: PlaceDao,
    private val api: ExplorerSagaApi,
    private val importApi: ExplorerSagaApi,
    private val loadBundledPlaces: () -> List<PlaceEntity>
) {

    suspend fun importPlacesFromWeb(): ImportResultDto? = withContext(Dispatchers.IO) {
        try {
            // Los lugares importados se descargan y guardan en local en la siguiente llamada a getPlacesResult()
            importApi.importPlaces()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getPlacesResult(): PlacesResult = withContext(Dispatchers.IO) {
        val remotePlaces = try {
            api.getPlaces()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        if (remotePlaces.isEmpty()) {
            return@withContext PlacesResult(getCachedPlaces(), DataSourceOrigin.LOCAL_FALLBACK)
        }

        val entities = remotePlaces.map { it.toPlaceEntity() }
        try {
            placeDao.replaceAll(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        PlacesResult(entities, DataSourceOrigin.REMOTE)
    }

    suspend fun getPlaceById(id: Long): PlaceEntity? = withContext(Dispatchers.IO) {
        try {
            api.getPlaceById(id).toPlaceEntity()
        } catch (e: Exception) {
            e.printStackTrace()
            placeDao.getById(id.toInt())
        }
    }

    private suspend fun getCachedPlaces(): List<PlaceEntity> {
        // Primer arranque sin conexión: se puebla la caché con los lugares incluidos en la app
        if (placeDao.count() == 0) {
            placeDao.insertAll(loadBundledPlaces())
        }
        return placeDao.getAll()
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
        category = this.category?.name ?: "monumento",
        // El backend todavía no expone horarios, fotos ni web: se dejan a null para que la UI los oculte
        horarios = null,
        direccion = this.address?.takeIf { it.isNotBlank() },
        latitude = this.latitude,
        longitude = this.longitude,
        photo = null,
        website = null
    )
}
