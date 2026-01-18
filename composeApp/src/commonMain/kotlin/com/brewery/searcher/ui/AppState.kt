package com.brewery.searcher.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.brewery.searcher.core.navigation.NavigationState
import com.brewery.searcher.core.navigation.rememberNavigationState
import com.brewery.searcher.feature.home.navigation.HomeNavKey
import com.brewery.searcher.navigation.TOP_LEVEL_NAV_ITEMS

@Composable
fun rememberAppState(
    navigationState: NavigationState = rememberNavigationState(
        startKey = HomeNavKey,
        topLevelKeys = TOP_LEVEL_NAV_ITEMS.keys,
    ),
): AppState {
    return remember(navigationState) {
        AppState(navigationState)
    }
}

class AppState(
    val navigationState: NavigationState,
) {
    val isNavBarVisible: Boolean
        get() = TOP_LEVEL_NAV_ITEMS.keys.contains(navigationState.currentKey)
}
