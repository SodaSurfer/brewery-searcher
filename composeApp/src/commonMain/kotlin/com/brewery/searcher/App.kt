package com.brewery.searcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.brewery.searcher.core.designsystem.theme.BrewerySearcherTheme
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.core.navigation.Navigator
import com.brewery.searcher.feature.activity.navigation.activityEntry
import com.brewery.searcher.feature.explore.navigation.exploreEntry
import com.brewery.searcher.feature.home.navigation.SearchNavKey
import com.brewery.searcher.feature.home.navigation.homeEntry
import com.brewery.searcher.feature.home.navigation.searchEntry
import com.brewery.searcher.feature.settings.navigation.settingsEntry
import com.brewery.searcher.navigation.TOP_LEVEL_NAV_ITEMS
import com.brewery.searcher.ui.AppState
import com.brewery.searcher.ui.rememberAppState

@Composable
fun App(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    AppContent(
        modifier = modifier,
        darkTheme = darkTheme,
    )
}

@Composable
private fun AppContent(
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
    appState: AppState = rememberAppState(),
) {
    val navigator = remember(appState.navigationState) {
        Navigator(appState.navigationState)
    }

    BrewerySearcherTheme(darkTheme = darkTheme) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                AnimatedVisibility(
                    visible = appState.isNavBarVisible,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    AppBottomNavigationBar(
                        currentTopLevelKey = appState.navigationState.currentTopLevelKey,
                        onNavigate = { navigator.navigate(it) },
                    )
                }
            },
        ) { paddingValues ->
            val entryProvider = entryProvider {
                homeEntry(onNavigate = { navigator.navigate(it) })
                searchEntry()
                exploreEntry()
                activityEntry()
                settingsEntry()
            }

            Box(
                modifier = Modifier.padding(
                    PaddingValues(
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        top = paddingValues.calculateTopPadding(),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                        bottom = if (appState.isNavBarVisible) paddingValues.calculateBottomPadding() else 0.dp,
                    )
                )
            ) {
                NavDisplay(
                    backStack = appState.navigationState.currentSubStack,
                    entryProvider = entryProvider,
                    onBack = { navigator.goBack() },
                )
            }
        }
    }
}

@Composable
private fun AppBottomNavigationBar(
    currentTopLevelKey: NavKey,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        TOP_LEVEL_NAV_ITEMS.forEach { (navKey, item) ->
            val selected = currentTopLevelKey == navKey
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(navKey) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
            )
        }
    }
}
