package com.brewery.searcher.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.brewery.searcher.core.data.paging.SearchBreweryPagingSource
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.SearchType
import com.brewery.searcher.core.network.api.BreweryApiService
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow

class BreweryRepositoryImpl(
    private val apiService: BreweryApiService,
) : BreweryRepository {

    companion object {
        val TAG = BreweryRepositoryImpl::class.simpleName
        private const val PAGE_SIZE = 50
    }

    override fun searchBreweries(query: String, searchType: SearchType): Flow<PagingData<Brewery>> {
        Napier.d(tag = TAG) { "searchBreweries(query=$query, searchType=$searchType)" }
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = { SearchBreweryPagingSource(apiService, query, searchType) }
        ).flow
    }
}
