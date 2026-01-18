package com.brewery.searcher.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.datastore.UserSettingsDataSource
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.network.api.ApiException
import com.brewery.searcher.feature.explore.model.CameraPosition
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    init {
        viewModelScope.launch {
            val settings = userSettingsDataSource.userData.first()
            if (!settings.locationDoNotAsk) {
                _uiState.update { it.copy(showLocationRationaleDialog = true) }
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

    fun onPermissionRequestCompleted() {
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

    fun onCameraMoved(latitude: Double, longitude: Double, zoom: Float) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val perPage = calculatePerPage(zoom)
            try {
                val breweries = breweryRepository.getBreweriesByDistance(latitude, longitude, perPage)
                _uiState.update { it.copy(breweries = breweries, isLoading = false) }
                Napier.d(tag = TAG) { "Loaded ${breweries.size} breweries (perPage=$perPage, zoom=$zoom)" }
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
