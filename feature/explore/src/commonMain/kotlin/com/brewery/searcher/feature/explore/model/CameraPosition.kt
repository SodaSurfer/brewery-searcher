package com.brewery.searcher.feature.explore.model

data class CameraPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float,
) {
    companion object {
        val DEFAULT_US_CENTER = CameraPosition(
            latitude = 39.8283,
            longitude = -98.5795,
            zoom = 4f,
        )
    }
}
