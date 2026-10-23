package com.alonso.explorersaga.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(places: List<PlaceEntity>)

    @Query("SELECT * FROM places")
    suspend fun getAllPlacesList(): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE category LIKE '%' || :categoryName || '%'")
    suspend fun getPlacesByCategoryList(categoryName: String): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE category = :categoryName")
    fun getPlacesByCategory(categoryName: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE category IN (:categories)")
    fun getPlacesByCategories(categories: List<String>): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id")
    fun getPlaceById(id: Int): Flow<PlaceEntity?>

    @Query("DELETE FROM places")
    suspend fun deleteAll()
}
