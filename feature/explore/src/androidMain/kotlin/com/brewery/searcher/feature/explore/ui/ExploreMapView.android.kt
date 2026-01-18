package com.brewery.searcher.feature.explore.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.brewery.searcher.core.model.Brewery
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

private const val CAMERA_DEBOUNCE_MS = 500L

@OptIn(FlowPreview::class)
@Composable
actual fun ExploreMapView(
    breweries: List<Brewery>,
    selectedBreweryId: String?,
    onCameraMoved: (latitude: Double, longitude: Double, zoom: Float) -> Unit,
    onBrewerySelected: (Brewery) -> Unit,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        // Default to US center
        position = CameraPosition.fromLatLngZoom(LatLng(39.8283, -98.5795), 4f)
    }

    // Debounce camera movements in the UI layer (500ms)
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position }
            .debounce(CAMERA_DEBOUNCE_MS)
            .distinctUntilChanged()
            .collect { position ->
                onCameraMoved(position.target.latitude, position.target.longitude, position.zoom)
            }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        breweries.forEach { brewery ->
            val lat = brewery.latitude
            val lng = brewery.longitude
            if (lat != null && lng != null) {
                val isSelected = brewery.id == selectedBreweryId
                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    title = brewery.name,
                    snippet = brewery.city?.let { city ->
                        brewery.stateProvince?.let { state -> "$city, $state" } ?: city
                    },
                    icon = if (isSelected) {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    } else {
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    },
                    zIndex = if (isSelected) 1f else 0f,
                    onClick = {
                        onBrewerySelected(brewery)
                        true // Return true to consume the event and prevent map from moving
                    }
                )
            }
        }
    }
}
