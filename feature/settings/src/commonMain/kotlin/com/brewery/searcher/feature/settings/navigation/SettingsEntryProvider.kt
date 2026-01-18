package com.brewery.searcher.feature.settings.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.feature.settings.ui.SettingsScreen

fun EntryProviderScope<NavKey>.settingsEntry() {
    entry<SettingsNavKey> {
        SettingsScreen()
    }
}
