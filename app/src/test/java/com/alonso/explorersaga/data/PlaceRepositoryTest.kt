package com.alonso.explorersaga.data

import com.alonso.explorersaga.data.remote.CategoryResponseDto
import com.alonso.explorersaga.data.remote.ExplorerSagaApi
import com.alonso.explorersaga.data.remote.ImportResultDto
import com.alonso.explorersaga.data.remote.PlaceResponseDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.IOException

class PlaceRepositoryTest {

    private val dao = InMemoryPlaceDao()
    private var bundledLoads = 0
    private val bundled = listOf(entity(1, "Teatro Romano"), entity(2, "Templo de Diana"))

    private fun repository(api: ExplorerSagaApi) = PlaceRepository(
        placeDao = dao,
        api = api,
        importApi = api,
        loadBundledPlaces = { bundledLoads++; bundled }
    )

    @Test
    fun online_replacesTheWholeCacheWithRemotePlaces() = runBlocking {
        dao.insertAll(listOf(entity(99, "Lugar obsoleto")))
        val api = FakeApi(places = listOf(dto(10, "Acueducto de los Milagros")))

        val result = repository(api).getPlacesResult()

        assertEquals(DataSourceOrigin.REMOTE, result.origin)
        assertEquals(listOf("Acueducto de los Milagros"), result.places.map { it.name })
        assertEquals(listOf("Acueducto de los Milagros"), dao.getAll().map { it.name })
        assertEquals(0, bundledLoads)
    }

    @Test
    fun offline_readsLastSyncedPlacesFromCache() = runBlocking {
        repository(FakeApi(places = listOf(dto(10, "Acueducto de los Milagros")))).getPlacesResult()

        val result = repository(FakeApi(failure = IOException("sin red"))).getPlacesResult()

        assertEquals(DataSourceOrigin.LOCAL_FALLBACK, result.origin)
        assertEquals(listOf("Acueducto de los Milagros"), result.places.map { it.name })
        assertEquals(0, bundledLoads)
    }

    @Test
    fun offlineOnFirstLaunch_populatesCacheFromBundledPlacesOnce() = runBlocking {
        val repository = repository(FakeApi(failure = IOException("sin red")))

        val first = repository.getPlacesResult()
        val second = repository.getPlacesResult()

        assertEquals(DataSourceOrigin.LOCAL_FALLBACK, first.origin)
        assertEquals(bundled.map { it.name }, first.places.map { it.name })
        assertEquals(bundled.map { it.name }, second.places.map { it.name })
        assertEquals(1, bundledLoads)
    }

    @Test
    fun emptyRemoteResponse_keepsCacheAndFallsBack() = runBlocking {
        dao.insertAll(listOf(entity(5, "Lugar guardado")))

        val result = repository(FakeApi(places = emptyList())).getPlacesResult()

        assertEquals(DataSourceOrigin.LOCAL_FALLBACK, result.origin)
        assertEquals(listOf("Lugar guardado"), result.places.map { it.name })
    }

    @Test
    fun placeDetailOffline_isReadFromCache() = runBlocking {
        dao.insertAll(listOf(entity(7, "Casa del Mitreo")))
        val repository = repository(FakeApi(failure = IOException("sin red")))

        assertEquals("Casa del Mitreo", repository.getPlaceById(7)?.name)
        assertNull(repository.getPlaceById(8))
    }

    @Test
    fun importFailure_returnsNullAndSuccessReturnsMetrics() = runBlocking {
        assertNull(repository(FakeApi(failure = IOException("502"))).importPlacesFromWeb())
        assertEquals(1, repository(FakeApi()).importPlacesFromWeb()?.inserted)
    }

    private class InMemoryPlaceDao : PlaceDao() {
        private val places = linkedMapOf<Int, PlaceEntity>()

        override suspend fun insertAll(places: List<PlaceEntity>) {
            places.forEach { this.places[it.id] = it }
        }

        override suspend fun getAll(): List<PlaceEntity> = places.values.toList()

        override suspend fun getById(id: Int): PlaceEntity? = places[id]

        override suspend fun count(): Int = places.size

        override suspend fun deleteAll() = places.clear()
    }

    private class FakeApi(
        private val places: List<PlaceResponseDto> = emptyList(),
        private val failure: Exception? = null
    ) : ExplorerSagaApi {
        override suspend fun getPlaces(category: String?): List<PlaceResponseDto> {
            failure?.let { throw it }
            return places
        }

        override suspend fun getPlaceById(id: Long): PlaceResponseDto {
            failure?.let { throw it }
            return places.first { it.id == id }
        }

        override suspend fun importPlaces(): ImportResultDto {
            failure?.let { throw it }
            return ImportResultDto(processed = 1, inserted = 1)
        }
    }

    private companion object {
        fun entity(id: Int, name: String) = PlaceEntity(
            id = id, name = name, description = "", category = "monumento",
            horarios = null, direccion = null, latitude = 38.9, longitude = -6.3, photo = null, website = null
        )

        fun dto(id: Long, name: String) = PlaceResponseDto(
            id = id, name = name, latitude = 38.9, longitude = -6.3,
            category = CategoryResponseDto(id = 1, name = "monumento")
        )
    }
}
