package com.brewery.searcher.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.feature.home.HomeScreen
import com.brewery.searcher.feature.home.SearchScreen

fun EntryProviderScope<NavKey>.homeEntry(
    onNavigate: (NavKey) -> Unit,
) {
    entry<HomeNavKey> {
        HomeScreen(onNavigate = onNavigate)
    }
}

fun EntryProviderScope<NavKey>.searchEntry() {
    entry<SearchNavKey> {
        SearchScreen()
    }
}
