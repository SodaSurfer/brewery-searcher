package com.brewery.searcher.feature.activity.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.core.navigation.Navigator
import com.brewery.searcher.feature.activity.ActivityScreen
import com.brewery.searcher.feature.home.navigation.BreweryDetailNavKey

fun EntryProviderScope<NavKey>.activityEntry(
    navigator: Navigator,
) {
    entry<ActivityNavKey> {
        ActivityScreen(
            onBreweryClick = { breweryId ->
                navigator.navigate(BreweryDetailNavKey(breweryId))
            },
        )
    }
}
