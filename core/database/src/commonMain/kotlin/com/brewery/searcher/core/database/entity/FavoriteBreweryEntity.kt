package com.brewery.searcher.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_breweries")
data class FavoriteBreweryEntity(
    @PrimaryKey
    val breweryId: String,
    val name: String,
    val breweryType: String,
    val city: String?,
    val stateProvince: String?,
    val country: String?,
    val addedAt: Long,
)
