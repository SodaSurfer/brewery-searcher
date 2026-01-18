package com.brewery.searcher.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.feature.home.HomeScreen

fun EntryProviderScope<NavKey>.homeEntry() {
    entry<HomeNavKey> {
        HomeScreen()
    }
}
