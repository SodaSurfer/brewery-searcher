package com.brewery.searcher.core.network.api

import com.brewery.searcher.core.network.dto.BreweryDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BreweryApiServiceImpl(
    private val httpClient: HttpClient,
) : BreweryApiService {

    companion object {
        val TAG = BreweryApiServiceImpl::class.simpleName
        private const val BASE_URL = "https://api.openbrewerydb.org/v1/breweries"
    }

    private suspend fun HttpResponse.parseErrorMessage(): String {
        return try {
            val body = bodyAsText()
            val json = Json.parseToJsonElement(body).jsonObject
            json["message"]?.jsonPrimitive?.content ?: "Unknown error"
        } catch (e: Exception) {
            "Request failed"
        }
    }

    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        if (!status.isSuccess()) {
            throw ApiException(parseErrorMessage())
        }
        return body()
    }

    override suspend fun searchBreweries(query: String, page: Int, perPage: Int): List<BreweryDto> {
        Napier.d(tag = TAG) { "searchBreweries(query=$query, page=$page, perPage=$perPage)" }
        return try {
            httpClient.get("$BASE_URL/search") {
                parameter("query", query)
                parameter("page", page)
                parameter("per_page", perPage)
            }.bodyOrThrow()
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
            }.bodyOrThrow()
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
            }.bodyOrThrow()
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
            }.bodyOrThrow()
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "getBreweriesByState failed" }
            throw e
        }
    }

    override suspend fun getBreweriesByDistance(
        latitude: Double,
        longitude: Double,
        perPage: Int
    ): List<BreweryDto> {
        Napier.d(tag = TAG) { "getBreweriesByDistance(lat=$latitude, lng=$longitude, perPage=$perPage)" }
        return try {
            httpClient.get(BASE_URL) {
                parameter("by_dist", "$latitude,$longitude")
                parameter("per_page", perPage)
            }.bodyOrThrow()
        } catch (e: Exception) {
            Napier.e(tag = TAG, throwable = e) { "getBreweriesByDistance failed" }
            throw e
        }
    }
}
