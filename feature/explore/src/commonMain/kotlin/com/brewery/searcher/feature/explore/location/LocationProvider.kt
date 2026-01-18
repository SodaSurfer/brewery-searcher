package com.brewery.searcher.feature.explore.location

data class LatLng(val latitude: Double, val longitude: Double)

expect class LocationProvider {
    suspend fun getCurrentLocation(): LatLng?
}
