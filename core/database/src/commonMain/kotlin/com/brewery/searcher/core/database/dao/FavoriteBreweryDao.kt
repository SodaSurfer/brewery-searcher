package com.brewery.searcher.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brewery.searcher.core.database.entity.FavoriteBreweryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteBreweryDao {

    @Query("SELECT * FROM favorite_breweries ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteBreweryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_breweries WHERE breweryId = :breweryId)")
    fun isFavorite(breweryId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_breweries WHERE breweryId = :breweryId)")
    suspend fun isFavoriteSync(breweryId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteBreweryEntity)

    @Query("DELETE FROM favorite_breweries WHERE breweryId = :breweryId")
    suspend fun deleteById(breweryId: String)

    @Query("DELETE FROM favorite_breweries")
    suspend fun clearAll()
}
