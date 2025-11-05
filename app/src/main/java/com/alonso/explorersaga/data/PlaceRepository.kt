package com.alonso.explorersaga.data

import kotlinx.coroutines.flow.Flow

/**
 * El Repositorio. Actúa como el único intermediario entre la base de datos (DAO)
 * y el resto de la aplicación (los ViewModels).
 * Recibe el DAO como una dependencia para poder acceder a los datos.
 */
class PlaceRepository(private val placeDao: PlaceDao) {

    /**
     * Esta función simplemente obtiene el Flow de lugares por categoría desde el DAO.
     * En un futuro, aquí podríamos añadir lógica más compleja, como por ejemplo:
     * 1. Comprobar si hay datos en la base de datos.
     * 2. Si no los hay, pedirlos a una API de internet.
     * 3. Guardar los datos de internet en la base de datos.
     * 4. Devolver los datos desde la base de datos.
     * Pero por ahora, simplemente pasamos la petición al DAO.
     */
    fun getPlacesByCategory(category: String): Flow<List<PlaceEntity>> {
        return placeDao.getPlacesByCategory(category)
    }
}
