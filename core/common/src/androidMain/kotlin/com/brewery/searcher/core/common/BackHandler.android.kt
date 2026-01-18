package com.brewery.searcher.core.common

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val backState = rememberNavigationEventState(currentInfo = NavigationEventInfo.None)
    NavigationBackHandler(
        state = backState,
        isBackEnabled = enabled,
        onBackCompleted = onBack
    )
}
