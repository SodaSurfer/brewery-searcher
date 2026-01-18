package com.brewery.searcher.feature.explore.ui.explore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.feature.explore.model.CameraPosition
import com.brewery.searcher.feature.explore.model.VisibleBounds
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition

private const val CAMERA_DEBOUNCE_MS = 500L

@OptIn(FlowPreview::class)
@Composable
actual fun ExploreMapView(
    breweries: List<Brewery>,
    selectedBreweryId: String?,
    initialCameraPosition: CameraPosition,
    onCameraMoved: (latitude: Double, longitude: Double, zoom: Float, bounds: VisibleBounds?) -> Unit,
    onBrewerySelected: (Brewery) -> Unit,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = GoogleCameraPosition.fromLatLngZoom(
            LatLng(initialCameraPosition.latitude, initialCameraPosition.longitude),
            initialCameraPosition.zoom
        )
    }

    // Animate to new position when initialCameraPosition changes
    LaunchedEffect(initialCameraPosition) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(initialCameraPosition.latitude, initialCameraPosition.longitude),
                initialCameraPosition.zoom
            )
        )
    }

    // Debounce camera movements in the UI layer (500ms)
    LaunchedEffect(cameraPositionState) {
        snapshotFlow {
            val position = cameraPositionState.position
            val latLngBounds = cameraPositionState.projection?.visibleRegion?.latLngBounds
            position to latLngBounds
        }
            .debounce(CAMERA_DEBOUNCE_MS)
            .distinctUntilChanged()
            .collect { (position, latLngBounds) ->
                val bounds = latLngBounds?.let {
                    VisibleBounds(
                        northEastLat = it.northeast.latitude,
                        northEastLng = it.northeast.longitude,
                        southWestLat = it.southwest.latitude,
                        southWestLng = it.southwest.longitude,
                    )
                }
                onCameraMoved(position.target.latitude, position.target.longitude, position.zoom, bounds)
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
