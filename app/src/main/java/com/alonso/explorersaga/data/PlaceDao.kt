package com.alonso.explorersaga.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class PlaceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(places: List<PlaceEntity>)

    @Query("SELECT * FROM places")
    abstract suspend fun getAll(): List<PlaceEntity>

    @Query("SELECT * FROM places WHERE id = :id")
    abstract suspend fun getById(id: Int): PlaceEntity?

    @Query("SELECT COUNT(*) FROM places")
    abstract suspend fun count(): Int

    @Query("DELETE FROM places")
    abstract suspend fun deleteAll()

    // Sustituye la caché completa en una única transacción para no dejarla nunca a medias
    @Transaction
    open suspend fun replaceAll(places: List<PlaceEntity>) {
        deleteAll()
        insertAll(places)
    }
}
