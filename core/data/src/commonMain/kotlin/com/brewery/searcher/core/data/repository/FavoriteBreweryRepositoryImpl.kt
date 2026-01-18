package com.brewery.searcher.core.data.repository

import com.brewery.searcher.core.database.dao.FavoriteBreweryDao
import com.brewery.searcher.core.database.entity.FavoriteBreweryEntity
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.BreweryType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class FavoriteBreweryRepositoryImpl(
    private val favoriteBreweryDao: FavoriteBreweryDao,
) : FavoriteBreweryRepository {

    companion object {
        val TAG = FavoriteBreweryRepositoryImpl::class.simpleName
    }

    override fun getAllFavorites(): Flow<List<Brewery>> {
        Napier.d(tag = TAG) { "getAllFavorites()" }
        return favoriteBreweryDao.getAllFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavorite(breweryId: String): Flow<Boolean> {
        return favoriteBreweryDao.isFavorite(breweryId)
    }

    override suspend fun addFavorite(brewery: Brewery) {
        Napier.d(tag = TAG) { "addFavorite(breweryId=${brewery.id})" }
        favoriteBreweryDao.insert(brewery.toEntity())
    }

    override suspend fun removeFavorite(breweryId: String) {
        Napier.d(tag = TAG) { "removeFavorite(breweryId=$breweryId)" }
        favoriteBreweryDao.deleteById(breweryId)
    }

    override suspend fun toggleFavorite(brewery: Brewery): Boolean {
        val isFavorited = favoriteBreweryDao.isFavoriteSync(brewery.id)
        if (isFavorited) {
            removeFavorite(brewery.id)
            Napier.d(tag = TAG) { "Removed ${brewery.name} from favorites" }
        } else {
            addFavorite(brewery)
            Napier.d(tag = TAG) { "Added ${brewery.name} to favorites" }
        }
        return !isFavorited
    }

    private fun FavoriteBreweryEntity.toDomain(): Brewery {
        return Brewery(
            id = breweryId,
            name = name,
            breweryType = try {
                BreweryType.valueOf(breweryType)
            } catch (e: IllegalArgumentException) {
                BreweryType.UNKNOWN
            },
            address = null,
            city = city,
            stateProvince = stateProvince,
            postalCode = null,
            country = country,
            longitude = null,
            latitude = null,
            phone = null,
            websiteUrl = null,
        )
    }

    private fun Brewery.toEntity(): FavoriteBreweryEntity {
        return FavoriteBreweryEntity(
            breweryId = id,
            name = name,
            breweryType = breweryType.name,
            city = city,
            stateProvince = stateProvince,
            country = country,
            addedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }
}
