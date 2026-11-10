package com.alonso.explorersaga.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContentDao {

    // --- Lugares ---

    @Query("SELECT * FROM places WHERE groupKey IN (:groups)")
    abstract fun placesInGroups(groups: List<String>): Flow<List<Place>>

    @Query(
        "SELECT places.* FROM places JOIN places_fts ON places_fts.placeId = places.id " +
            "WHERE places_fts MATCH :query AND places.groupKey IN (:groups)"
    )
    abstract fun search(query: String, groups: List<String>): Flow<List<Place>>

    @Query("SELECT * FROM places WHERE id = :id")
    abstract fun place(id: String): Flow<Place?>

    @Query("SELECT * FROM places WHERE id IN (:ids)")
    abstract suspend fun placesByIds(ids: List<String>): List<Place>

    @Query(
        "SELECT * FROM places WHERE groupKey IN ('HERITAGE','MUSEUMS','NATURE','BATHING') " +
            "AND imageUrl IS NOT NULL ORDER BY score DESC, id LIMIT :limit"
    )
    abstract fun featured(limit: Int): Flow<List<Place>>

    @Query(
        "SELECT * FROM places WHERE latitude BETWEEN :south AND :north " +
            "AND longitude BETWEEN :west AND :east AND groupKey IN (:groups)"
    )
    abstract suspend fun placesInBox(
        south: Double, north: Double, west: Double, east: Double, groups: List<String>
    ): List<Place>

    @Query("SELECT COUNT(*) FROM places")
    abstract fun placeCount(): Flow<Int>

    // --- Rutas, agenda y municipios ---

    @Query("SELECT * FROM routes ORDER BY position")
    abstract fun routes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE id = :id")
    abstract fun route(id: String): Flow<RouteEntity?>

    @Query("SELECT * FROM events")
    abstract fun events(): Flow<List<EventEntity>>

    @Query("SELECT * FROM municipalities ORDER BY name")
    abstract fun municipalities(): Flow<List<Municipality>>

    // --- Favoritos y plan de viaje ---

    @Query("SELECT * FROM favorites ORDER BY day, addedAt")
    abstract fun favorites(): Flow<List<Favorite>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE placeId = :placeId)")
    abstract fun isFavorite(placeId: String): Flow<Boolean>

    @Upsert
    abstract suspend fun upsertFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE placeId = :placeId")
    abstract suspend fun deleteFavorite(placeId: String)

    @Query("UPDATE favorites SET day = :day WHERE placeId = :placeId")
    abstract suspend fun setFavoriteDay(placeId: String, day: Int)

    // --- Versión del contenido ---

    @Query("SELECT * FROM content_meta WHERE id = 0")
    abstract suspend fun meta(): ContentMeta?

    @Query("SELECT * FROM content_meta WHERE id = 0")
    abstract fun metaFlow(): Flow<ContentMeta?>

    // --- Importación ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertPlaces(places: List<Place>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertFts(rows: List<PlaceFts>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertRoutes(routes: List<RouteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertEvents(events: List<EventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertMunicipalities(municipalities: List<Municipality>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertMeta(meta: ContentMeta)

    @Query("DELETE FROM places") protected abstract suspend fun deletePlaces()
    @Query("DELETE FROM places_fts") protected abstract suspend fun deleteFts()
    @Query("DELETE FROM routes") protected abstract suspend fun deleteRoutes()
    @Query("DELETE FROM events") protected abstract suspend fun deleteEvents()
    @Query("DELETE FROM municipalities") protected abstract suspend fun deleteMunicipalities()
    @Query("DELETE FROM content_meta") protected abstract suspend fun deleteMeta()
    @Query("DELETE FROM favorites") protected abstract suspend fun deleteFavorites()

    /** Sustituye todo el contenido en una transacción. Los favoritos no se tocan. */
    @Transaction
    open suspend fun replaceContent(
        places: List<Place>,
        fts: List<PlaceFts>,
        routes: List<RouteEntity>,
        events: List<EventEntity>,
        municipalities: List<Municipality>,
        meta: ContentMeta
    ) {
        deletePlaces(); deleteFts(); deleteRoutes(); deleteEvents(); deleteMunicipalities()
        places.chunked(CHUNK).forEach { insertPlaces(it) }
        fts.chunked(CHUNK).forEach { insertFts(it) }
        insertRoutes(routes)
        insertEvents(events)
        insertMunicipalities(municipalities)
        insertMeta(meta)
    }

    /** "Borrar datos guardados": contenido y favoritos. */
    @Transaction
    open suspend fun clearAll() {
        deletePlaces(); deleteFts(); deleteRoutes(); deleteEvents(); deleteMunicipalities()
        deleteMeta(); deleteFavorites()
    }

    private companion object {
        const val CHUNK = 500
    }
}
