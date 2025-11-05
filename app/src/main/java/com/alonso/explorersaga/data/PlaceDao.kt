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

    @Query("SELECT * FROM places WHERE category = :categoryName")
    fun getPlacesByCategory(categoryName: String): Flow<List<PlaceEntity>>

    @Query("SELECT * FROM places WHERE id = :id")
    fun getPlaceById(id: Int): Flow<PlaceEntity?>
}
