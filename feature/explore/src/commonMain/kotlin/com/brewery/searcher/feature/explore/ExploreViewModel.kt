package com.brewery.searcher.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.network.api.ApiException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val breweries: List<Brewery> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showBottomSheet: Boolean = false,
    val selectedBrewery: Brewery? = null,
)

class ExploreViewModel(
    private val breweryRepository: BreweryRepository,
) : ViewModel() {

    companion object {
        val TAG = ExploreViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    fun onCameraMoved(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val breweries = breweryRepository.getBreweriesByDistance(latitude, longitude)
                _uiState.update { it.copy(breweries = breweries, isLoading = false) }
                Napier.d(tag = TAG) { "Loaded ${breweries.size} breweries near ($latitude, $longitude)" }
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
