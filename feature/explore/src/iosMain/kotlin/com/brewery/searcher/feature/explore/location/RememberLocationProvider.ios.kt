package com.brewery.searcher.feature.explore.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberLocationProvider(): LocationProvider {
    return remember { LocationProvider() }
}
