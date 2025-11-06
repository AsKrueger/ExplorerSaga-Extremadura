package com.alonso.explorersaga.data

import kotlinx.coroutines.flow.Flow

class PlaceRepository(private val placeDao: PlaceDao) {

    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>> {
        return placeDao.getPlacesByCategory(category)
    }

    fun getPlacesByCategories(categories: List<String>): Flow<List<PlaceEntity>> {
        return placeDao.getPlacesByCategories(categories)
    }

    // ¡NUEVA FUNCIÓN! Obtiene un solo lugar por su ID.
    fun getPlaceById(id: Int): Flow<PlaceEntity?> {
        return placeDao.getPlaceById(id)
    }
}
