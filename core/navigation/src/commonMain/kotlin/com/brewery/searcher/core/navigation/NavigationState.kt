package com.brewery.searcher.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

typealias NavBackStack = SnapshotStateList<NavKey>

/**
 * Manages navigation state with support for top-level destinations and nested navigation.
 */
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack,
    val subStacks: Map<NavKey, NavBackStack>,
) {
    /**
     * The currently selected top-level navigation key.
     */
    val currentTopLevelKey: NavKey
        get() = topLevelStack.lastOrNull() ?: startKey

    /**
     * The current navigation key (could be nested within a top-level destination).
     */
    val currentKey: NavKey
        get() = currentSubStack.lastOrNull() ?: currentTopLevelKey

    /**
     * The back stack for the current top-level destination.
     */
    val currentSubStack: NavBackStack
        get() = subStacks[currentTopLevelKey] ?: error("No sub-stack for $currentTopLevelKey")

    /**
     * All available top-level navigation keys.
     */
    val topLevelKeys: Set<NavKey>
        get() = subStacks.keys
}

@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState {
    val topLevelStack = remember { mutableStateListOf(startKey) }
    val subStacks = remember {
        topLevelKeys.associateWith { key -> mutableStateListOf(key) }
    }

    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
}
