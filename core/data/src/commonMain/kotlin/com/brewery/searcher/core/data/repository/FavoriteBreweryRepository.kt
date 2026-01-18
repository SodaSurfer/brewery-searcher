package com.brewery.searcher.core.data.repository

import com.brewery.searcher.core.model.Brewery
import kotlinx.coroutines.flow.Flow

interface FavoriteBreweryRepository {

    fun getAllFavorites(): Flow<List<Brewery>>

    fun isFavorite(breweryId: String): Flow<Boolean>

    suspend fun addFavorite(brewery: Brewery)

    suspend fun removeFavorite(breweryId: String)

    suspend fun toggleFavorite(brewery: Brewery): Boolean
}
