package com.brewery.searcher.core.network.api

import com.brewery.searcher.core.network.dto.BreweryDto

interface BreweryApiService {
    suspend fun searchBreweries(query: String, page: Int, perPage: Int = 50): List<BreweryDto>
    suspend fun getBreweriesByCity(city: String, page: Int, perPage: Int = 50): List<BreweryDto>
    suspend fun getBreweriesByCountry(country: String, page: Int, perPage: Int = 50): List<BreweryDto>
    suspend fun getBreweriesByState(state: String, page: Int, perPage: Int = 50): List<BreweryDto>
    suspend fun getBreweriesByDistance(latitude: Double, longitude: Double, perPage: Int = 50): List<BreweryDto>
    suspend fun getBreweryById(id: String): BreweryDto
}
