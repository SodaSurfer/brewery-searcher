package com.brewery.searcher.feature.activity.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.feature.activity.ActivityScreen

fun EntryProviderScope<NavKey>.activityEntry() {
    entry<ActivityNavKey> {
        ActivityScreen()
    }
}
