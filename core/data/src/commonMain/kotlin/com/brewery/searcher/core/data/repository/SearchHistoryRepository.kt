package com.brewery.searcher.core.data.repository

import com.brewery.searcher.core.model.SearchHistory
import com.brewery.searcher.core.model.SearchType
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {

    fun getRecentSearches(): Flow<List<SearchHistory>>

    suspend fun saveSearch(query: String, searchType: SearchType)

    suspend fun deleteSearch(id: Long)

    suspend fun clearAll()
}
