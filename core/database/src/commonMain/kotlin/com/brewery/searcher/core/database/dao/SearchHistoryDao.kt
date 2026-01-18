package com.brewery.searcher.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.brewery.searcher.core.database.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert
    suspend fun insert(searchHistory: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSearch(): SearchHistoryEntity?

    @Query(
        """
        DELETE FROM search_history
        WHERE id NOT IN (
            SELECT id FROM search_history
            ORDER BY timestamp DESC
            LIMIT 20
        )
        """
    )
    suspend fun pruneOldEntries()
}
