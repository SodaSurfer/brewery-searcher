package com.brewery.searcher.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.core.navigation.Navigator
import com.brewery.searcher.feature.home.ui.brewerydetail.BreweryDetailScreen
import com.brewery.searcher.feature.home.ui.brewerydetail.BreweryDetailViewModel
import com.brewery.searcher.feature.home.ui.home.HomeScreen
import com.brewery.searcher.feature.home.ui.search.SearchScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

fun EntryProviderScope<NavKey>.homeEntry(
    navigator: Navigator,
) {
    entry<HomeNavKey> {
        HomeScreen(onNavigate = { navigator.navigate(it) })
    }

    entry<SearchNavKey> {
        SearchScreen(
            onBackClick = { navigator.goBack() },
            onBreweryClick = { breweryId ->
                navigator.navigate(BreweryDetailNavKey(breweryId))
            },
        )
    }

    entry<BreweryDetailNavKey> { navKey ->
        val viewModel: BreweryDetailViewModel = koinViewModel(
            key = navKey.breweryId,
            parameters = { parametersOf(navKey.breweryId) }
        )
        BreweryDetailScreen(
            onBackClick = { navigator.goBack() },
            viewModel = viewModel,
        )
    }
}