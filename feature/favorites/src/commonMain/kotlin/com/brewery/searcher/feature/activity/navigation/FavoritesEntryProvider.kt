package com.brewery.searcher.feature.activity.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.core.navigation.Navigator
import com.brewery.searcher.feature.activity.FavoritesScreen
import com.brewery.searcher.feature.home.navigation.BreweryDetailNavKey

fun EntryProviderScope<NavKey>.favoritesEntry(
    navigator: Navigator,
) {
    entry<FavoritesNavKey> {
        FavoritesScreen(
            onBreweryClick = { breweryId ->
                navigator.navigate(BreweryDetailNavKey(breweryId))
            },
        )
    }
}
