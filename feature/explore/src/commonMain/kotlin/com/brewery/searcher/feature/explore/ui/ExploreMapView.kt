package com.brewery.searcher.feature.explore.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.feature.explore.model.CameraPosition

@Composable
expect fun ExploreMapView(
    breweries: List<Brewery>,
    selectedBreweryId: String?,
    initialCameraPosition: CameraPosition,
    onCameraMoved: (latitude: Double, longitude: Double, zoom: Float) -> Unit,
    onBrewerySelected: (Brewery) -> Unit,
    modifier: Modifier = Modifier,
)
