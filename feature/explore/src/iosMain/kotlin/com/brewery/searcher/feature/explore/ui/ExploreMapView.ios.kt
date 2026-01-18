package com.brewery.searcher.feature.explore.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.brewery.searcher.core.model.Brewery

@Composable
actual fun ExploreMapView(
    breweries: List<Brewery>,
    selectedBreweryId: String?,
    onCameraMoved: (latitude: Double, longitude: Double) -> Unit,
    onBrewerySelected: (Brewery) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Map view coming soon for iOS")
    }
}
