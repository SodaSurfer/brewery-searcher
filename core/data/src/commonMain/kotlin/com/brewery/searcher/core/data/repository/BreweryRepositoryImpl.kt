package com.brewery.searcher.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.brewery.searcher.core.data.mapper.toDomain
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

    override suspend fun getBreweriesByDistance(
        latitude: Double,
        longitude: Double,
        perPage: Int
    ): List<Brewery> {
        Napier.d(tag = TAG) { "getBreweriesByDistance(lat=$latitude, lng=$longitude, perPage=$perPage)" }
        return apiService.getBreweriesByDistance(latitude, longitude, perPage)
            .map { it.toDomain() }
            .filter { it.latitude != null && it.longitude != null }
    }

    override suspend fun getBreweryById(id: String): Brewery {
        Napier.d(tag = TAG) { "getBreweryById(id=$id)" }
        return apiService.getBreweryById(id).toDomain()
    }
}
