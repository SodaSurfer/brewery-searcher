package com.brewery.searcher.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.core.navigation.Navigator
import com.brewery.searcher.feature.home.HomeScreen
import com.brewery.searcher.feature.home.SearchScreen

fun EntryProviderScope<NavKey>.homeEntry(
    navigator: Navigator,
) {
    entry<HomeNavKey> {
        HomeScreen(onNavigate = { navigator.navigate(it) })
    }

    entry<SearchNavKey> {
        SearchScreen(onBackClick = { navigator.goBack() })
    }
}