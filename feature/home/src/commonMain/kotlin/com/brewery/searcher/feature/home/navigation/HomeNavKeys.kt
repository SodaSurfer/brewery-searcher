package com.brewery.searcher.feature.home.navigation

import com.brewery.searcher.core.navigation.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavKey : NavKey

@Serializable
data object SearchNavKey : NavKey

@Serializable
data class BreweryDetailNavKey(val breweryId: String) : NavKey
