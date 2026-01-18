package com.brewery.searcher.core.data.repository

import androidx.paging.PagingData
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.SearchType
import kotlinx.coroutines.flow.Flow

interface BreweryRepository {
    fun searchBreweries(query: String, searchType: SearchType): Flow<PagingData<Brewery>>
    suspend fun getBreweriesByDistance(latitude: Double, longitude: Double): List<Brewery>
}
