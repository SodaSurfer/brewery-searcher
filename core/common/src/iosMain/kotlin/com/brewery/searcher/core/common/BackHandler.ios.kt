package com.brewery.searcher.core.common

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS - back navigation handled by iOS gestures
}
