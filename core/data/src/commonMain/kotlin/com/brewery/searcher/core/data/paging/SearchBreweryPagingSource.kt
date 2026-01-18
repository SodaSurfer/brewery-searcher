package com.brewery.searcher.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.brewery.searcher.core.data.mapper.toDomain
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.SearchType
import com.brewery.searcher.core.network.api.BreweryApiService
import io.github.aakira.napier.Napier

class SearchBreweryPagingSource(
    private val apiService: BreweryApiService,
    private val query: String,
    private val searchType: SearchType,
) : PagingSource<Int, Brewery>() {

    companion object {
        val TAG = SearchBreweryPagingSource::class.simpleName
    }

    override fun getRefreshKey(state: PagingState<Int, Brewery>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Brewery> {
        val page = params.key ?: 1
        Napier.d(tag = TAG) { "load(page=$page, searchType=$searchType, query=$query)" }

        return try {
            val breweryDtos = when (searchType) {
                SearchType.ALL_FIELDS -> apiService.searchBreweries(query, page, params.loadSize)
                SearchType.BY_CITY -> apiService.getBreweriesByCity(query, page, params.loadSize)
                SearchType.BY_COUNTRY -> apiService.getBreweriesByCountry(query, page, params.loadSize)
                SearchType.BY_STATE -> apiService.getBreweriesByState(query, page, params.loadSize)
            }

            val breweries = breweryDtos.map { it.toDomain() }
            Napier.d(tag = TAG) { "load() returned ${breweries.size} results" }

            LoadResult.Page(
                data = breweries,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (breweries.isEmpty()) null else page + 1,
            )
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "load() failed" }
            LoadResult.Error(e)
        }
    }
}
