package com.brewery.searcher.core.data.repository

import com.brewery.searcher.core.database.dao.SearchHistoryDao
import com.brewery.searcher.core.database.entity.SearchHistoryEntity
import com.brewery.searcher.core.model.SearchHistory
import com.brewery.searcher.core.model.SearchType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class SearchHistoryRepositoryImpl(
    private val searchHistoryDao: SearchHistoryDao,
) : SearchHistoryRepository {

    companion object {
        val TAG = SearchHistoryRepositoryImpl::class.simpleName
    }

    override fun getRecentSearches(): Flow<List<SearchHistory>> {
        Napier.d(tag = TAG) { "getRecentSearches()" }
        return searchHistoryDao.getRecentSearches().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveSearch(query: String, searchType: SearchType) {
        Napier.d(tag = TAG) { "saveSearch(query=$query, searchType=$searchType)" }

        // Skip if same as last search
        val lastSearch = searchHistoryDao.getLastSearch()
        if (lastSearch?.query == query && lastSearch.searchType == searchType.name) {
            Napier.d(tag = TAG) { "Skipping duplicate search" }
            return
        }

        searchHistoryDao.insert(
            SearchHistoryEntity(
                query = query,
                searchType = searchType.name,
                timestamp = Clock.System.now().toEpochMilliseconds(),
            )
        )

        // Prune old entries (keep only last 20)
        searchHistoryDao.pruneOldEntries()
    }

    override suspend fun deleteSearch(id: Long) {
        Napier.d(tag = TAG) { "deleteSearch(id=$id)" }
        searchHistoryDao.deleteById(id)
    }

    override suspend fun clearAll() {
        Napier.d(tag = TAG) { "clearAll()" }
        searchHistoryDao.clearAll()
    }

    private fun SearchHistoryEntity.toDomain(): SearchHistory {
        return SearchHistory(
            id = id,
            query = query,
            searchType = SearchType.valueOf(searchType),
            timestamp = timestamp,
        )
    }
}
