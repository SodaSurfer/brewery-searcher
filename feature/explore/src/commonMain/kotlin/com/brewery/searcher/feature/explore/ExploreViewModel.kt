package com.brewery.searcher.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.data.repository.FavoriteBreweryRepository
import com.brewery.searcher.core.datastore.UserSettingsDataSource
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.network.api.ApiException
import com.brewery.searcher.feature.explore.model.CameraPosition
import com.brewery.searcher.feature.explore.model.VisibleBounds
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val breweries: List<Brewery> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showBottomSheet: Boolean = false,
    val selectedBrewery: Brewery? = null,
    val initialCameraPosition: CameraPosition = CameraPosition.DEFAULT_US_CENTER,
    val showLocationRationaleDialog: Boolean = false,
    val showPermissionDeniedDialog: Boolean = false,
    val shouldRequestPermission: Boolean = false,
)

class ExploreViewModel(
    private val breweryRepository: BreweryRepository,
    private val userSettingsDataSource: UserSettingsDataSource,
    favoriteBreweryRepository: FavoriteBreweryRepository,
) : ViewModel() {

    companion object {
        val TAG = ExploreViewModel::class.simpleName

        fun calculatePerPage(zoom: Float): Int = when {
            zoom >= 16f -> 10
            zoom >= 13f -> 20
            zoom >= 10f -> 30
            zoom >= 7f -> 40
            else -> 50
        }
    }

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    val favoriteBreweryIds: StateFlow<Set<String>> = favoriteBreweryRepository
        .getAllFavorites()
        .map { breweries -> breweries.map { it.id }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    init {
        viewModelScope.launch {
            val settings = userSettingsDataSource.userData.first()
            if (!settings.locationDoNotAsk) {
                // Small delay to let screen check if permission is already granted
                delay(200)
                // Only show dialog if location wasn't already received
                if (_uiState.value.initialCameraPosition == CameraPosition.DEFAULT_US_CENTER) {
                    _uiState.update { it.copy(showLocationRationaleDialog = true) }
                }
            }
        }
    }

    fun onRationaleDialogConfirm() {
        _uiState.update {
            it.copy(showLocationRationaleDialog = false, shouldRequestPermission = true)
        }
    }

    fun onRationaleDialogDismiss(doNotAskAgain: Boolean) {
        _uiState.update { it.copy(showLocationRationaleDialog = false) }

        if (doNotAskAgain) {
            viewModelScope.launch {
                userSettingsDataSource.setLocationDoNotAsk(true)
                Napier.d(tag = TAG) { "User selected 'do not ask again' for location permission" }
            }
        }
    }

    fun onPermissionRequestCompleted(locationDoNotAsk: Boolean) {
        if (locationDoNotAsk) {
            viewModelScope.launch {
                userSettingsDataSource.setLocationDoNotAsk(true)
                Napier.d(tag = TAG) { "User selected 'do not ask again' for location permission" }
            }
        }
        _uiState.update { it.copy(shouldRequestPermission = false) }
    }

    fun onPermissionDeniedPermanently() {
        _uiState.update { it.copy(showPermissionDeniedDialog = true) }
    }

    fun onDismissPermissionDeniedDialog() {
        _uiState.update { it.copy(showPermissionDeniedDialog = false) }
    }

    fun onUserLocationReceived(latitude: Double, longitude: Double) {
        Napier.d(tag = TAG) { "User location received: ($latitude, $longitude)" }
        _uiState.update {
            it.copy(initialCameraPosition = CameraPosition(latitude, longitude, 12f))
        }
    }

    fun onCameraMoved(latitude: Double, longitude: Double, zoom: Float, bounds: VisibleBounds?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val perPage = calculatePerPage(zoom)
            try {
                val breweries = breweryRepository.getBreweriesByDistance(latitude, longitude, perPage)
                val filteredBreweries = if (bounds != null) {
                    breweries.filter { brewery ->
                        val lat = brewery.latitude ?: return@filter false
                        val lng = brewery.longitude ?: return@filter false
                        lat in bounds.southWestLat..bounds.northEastLat &&
                            lng in bounds.southWestLng..bounds.northEastLng
                    }
                } else {
                    breweries
                }
                _uiState.update { it.copy(breweries = filteredBreweries, isLoading = false) }
                Napier.d(tag = TAG) { "Loaded ${filteredBreweries.size}/${breweries.size} breweries (perPage=$perPage, zoom=$zoom)" }
            } catch (e: ApiException) {
                Napier.e(tag = TAG, throwable = e) { "API error fetching breweries" }
                _uiState.update { it.copy(error = e.userMessage, isLoading = false) }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Failed to load breweries" }
                _uiState.update { it.copy(error = "Failed to load breweries", isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onShowBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = true) }
    }

    fun onDismissBottomSheet() {
        _uiState.update { it.copy(showBottomSheet = false, selectedBrewery = null) }
    }

    fun onBrewerySelected(brewery: Brewery) {
        _uiState.update { it.copy(selectedBrewery = brewery) }
    }

    fun onClearSelection() {
        _uiState.update { it.copy(selectedBrewery = null) }
    }
}
