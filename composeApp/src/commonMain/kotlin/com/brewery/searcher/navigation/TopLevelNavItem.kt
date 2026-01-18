package com.brewery.searcher.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.feature.activity.navigation.FavoritesNavKey
import com.brewery.searcher.feature.explore.navigation.ExploreNavKey
import com.brewery.searcher.feature.home.navigation.HomeNavKey
import com.brewery.searcher.feature.settings.navigation.SettingsNavKey

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
)

val TOP_LEVEL_NAV_ITEMS: Map<NavKey, TopLevelNavItem> = linkedMapOf(
    HomeNavKey to TopLevelNavItem(
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        label = "Home",
    ),
    ExploreNavKey to TopLevelNavItem(
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map,
        label = "Explore",
    ),
    FavoritesNavKey to TopLevelNavItem(
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
        label = "Favorites",
    ),
    SettingsNavKey to TopLevelNavItem(
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "Settings",
    ),
)
