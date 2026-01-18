package com.brewery.searcher.feature.explore.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.feature.explore.ExploreScreen

fun EntryProviderScope<NavKey>.exploreEntry() {
    entry<ExploreNavKey> {
        ExploreScreen()
    }
}
