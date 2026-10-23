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
    fun remotePlaceWithoutCategory_usesMonumentFallback() {
        val dto = PlaceResponseDto(id = 1, name = "Sin categoría", latitude = 0.0, longitude = 0.0)

        assertEquals("Monument", dto.toPlaceEntity().category)
    }

    @Test
    fun entityToModelAndBack_preservesAllFields() {
        val entity = PlaceEntity(
            id = 3, name = "Bar", description = "Tapas", category = "bar",
            horarios = "12:00–16:00", direccion = "Calle Mayor, 1",
            latitude = 38.9, longitude = -6.3, photo = "elbar", website = "https://example.com"
        )

        assertEquals(entity, entity.toPlaceModel().toPlaceEntity())
    }
}
