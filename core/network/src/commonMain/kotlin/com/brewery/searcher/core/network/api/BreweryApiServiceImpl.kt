package com.brewery.searcher.core.network.api

import com.brewery.searcher.core.network.dto.BreweryDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class BreweryApiServiceImpl(
    private val httpClient: HttpClient,
) : BreweryApiService {

    companion object {
        val TAG = BreweryApiServiceImpl::class.simpleName
        private const val BASE_URL = "https://api.openbrewerydb.org/v1/breweries"
    }

    override suspend fun searchBreweries(query: String, page: Int, perPage: Int): List<BreweryDto> {
        Napier.d(tag = TAG) { "searchBreweries(query=$query, page=$page, perPage=$perPage)" }
        return try {
            httpClient.get("$BASE_URL/search") {
                parameter("query", query)
                parameter("page", page)
                parameter("per_page", perPage)
            }.body()
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "searchBreweries failed" }
            throw e
        }
    }

    override suspend fun getBreweriesByCity(city: String, page: Int, perPage: Int): List<BreweryDto> {
        Napier.d(tag = TAG) { "getBreweriesByCity(city=$city, page=$page, perPage=$perPage)" }
        return try {
            httpClient.get(BASE_URL) {
                parameter("by_city", city)
                parameter("page", page)
                parameter("per_page", perPage)
            }.body()
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "getBreweriesByCity failed" }
            throw e
        }
    }

    override suspend fun getBreweriesByCountry(country: String, page: Int, perPage: Int): List<BreweryDto> {
        Napier.d(tag = TAG) { "getBreweriesByCountry(country=$country, page=$page, perPage=$perPage)" }
        return try {
            httpClient.get(BASE_URL) {
                parameter("by_country", country)
                parameter("page", page)
                parameter("per_page", perPage)
            }.body()
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "getBreweriesByCountry failed" }
            throw e
        }
    }

    override suspend fun getBreweriesByState(state: String, page: Int, perPage: Int): List<BreweryDto> {
        Napier.d(tag = TAG) { "getBreweriesByState(state=$state, page=$page, perPage=$perPage)" }
        return try {
            httpClient.get(BASE_URL) {
                parameter("by_state", state)
                parameter("page", page)
                parameter("per_page", perPage)
            }.body()
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "getBreweriesByState failed" }
            throw e
        }
    }
}
