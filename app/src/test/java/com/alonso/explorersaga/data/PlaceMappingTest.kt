package com.alonso.explorersaga.data

import com.alonso.explorersaga.data.remote.CategoryResponseDto
import com.alonso.explorersaga.data.remote.PlaceResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaceMappingTest {

    @Test
    fun remotePlace_mapsMissingOptionalFieldsToNull() {
        val dto = PlaceResponseDto(
            id = 7,
            name = "Teatro Romano",
            description = null,
            address = "  ",
            latitude = 38.915,
            longitude = -6.338,
            category = CategoryResponseDto(id = 1, name = "monumento")
        )

        val entity = dto.toPlaceEntity()

        assertEquals(7, entity.id)
        assertEquals("", entity.description)
        assertEquals("monumento", entity.category)
        assertNull(entity.direccion)
        assertNull(entity.horarios)
        assertNull(entity.photo)
        assertNull(entity.website)
    }

    @Test
    fun remotePlaceWithoutCategory_usesMonumentoFallback() {
        val dto = PlaceResponseDto(id = 1, name = "Sin categoría", latitude = 0.0, longitude = 0.0)

        assertEquals("monumento", dto.toPlaceEntity().category)
    }

    @Test
    fun entityToModel_preservesAllFields() {
        val entity = PlaceEntity(
            id = 3, name = "Bar", description = "Tapas", category = "bar",
            horarios = "12:00–16:00", direccion = "Calle Mayor, 1",
            latitude = 38.9, longitude = -6.3, photo = "elbar", website = "https://example.com"
        )

        val model = entity.toPlaceModel()

        assertEquals(
            listOf(entity.id, entity.name, entity.description, entity.category, entity.horarios,
                entity.direccion, entity.latitude, entity.longitude, entity.photo, entity.website),
            listOf(model.id, model.name, model.description, model.category, model.horarios,
                model.direccion, model.latitude, model.longitude, model.photo, model.website)
        )
    }
}
