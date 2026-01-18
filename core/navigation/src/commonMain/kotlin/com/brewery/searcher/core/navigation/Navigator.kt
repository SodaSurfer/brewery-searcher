package com.brewery.searcher.core.navigation

/**
 * Navigator class that provides navigation actions.
 */
class Navigator(val state: NavigationState) {
    /**
     * Navigate to the specified key.
     * - If key is the current top-level: clears nested navigation
     * - If key is a different top-level: switches to that tab
     * - Otherwise: pushes onto current tab's back stack
     */
    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    /**
     * Navigate back.
     * - If at a nested screen: pops from current tab's stack
     * - If at a tab root: goes back to previous tab or start
     * - Returns false if cannot go back further (at start)
     */
    fun goBack(): Boolean {
        return when (state.currentKey) {
            state.startKey -> false
            state.currentTopLevelKey -> {
                state.topLevelStack.removeLastOrNull()
                true
            }
            else -> {
                state.currentSubStack.removeLastOrNull()
                true
            }
        }
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            remove(key)
            add(key)
        }
    }

    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) {
                subList(1, size).clear()
            }
        }
    }
}
