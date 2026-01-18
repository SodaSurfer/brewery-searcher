package com.brewery.searcher.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.feature.activity.navigation.ActivityNavKey
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
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        label = "Explore",
    ),
    ActivityNavKey to TopLevelNavItem(
        selectedIcon = Icons.Filled.Notifications,
        unselectedIcon = Icons.Outlined.Notifications,
        label = "Activity",
    ),
    SettingsNavKey to TopLevelNavItem(
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "Settings",
    ),
)
