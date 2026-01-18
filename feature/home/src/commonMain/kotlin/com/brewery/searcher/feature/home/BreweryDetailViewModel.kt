package com.brewery.searcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.network.api.ApiException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BreweryDetailUiState {
    data object Loading : BreweryDetailUiState
    data class Success(val brewery: Brewery) : BreweryDetailUiState
    data class Error(val message: String) : BreweryDetailUiState
}

class BreweryDetailViewModel(
    private val breweryRepository: BreweryRepository,
) : ViewModel() {

    companion object {
        val TAG = BreweryDetailViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow<BreweryDetailUiState>(BreweryDetailUiState.Loading)
    val uiState: StateFlow<BreweryDetailUiState> = _uiState.asStateFlow()

    private var loadedBreweryId: String? = null

    fun loadBrewery(breweryId: String) {
        if (loadedBreweryId == breweryId && _uiState.value is BreweryDetailUiState.Success) {
            Napier.d(tag = TAG) { "Brewery $breweryId already loaded, skipping" }
            return
        }

        Napier.d(tag = TAG) { "loadBrewery(breweryId=$breweryId)" }
        loadedBreweryId = breweryId
        _uiState.value = BreweryDetailUiState.Loading

        viewModelScope.launch {
            try {
                val brewery = breweryRepository.getBreweryById(breweryId)
                Napier.d(tag = TAG) { "Brewery loaded: ${brewery.name}" }
                _uiState.value = BreweryDetailUiState.Success(brewery)
            } catch (e: ApiException) {
                Napier.e(tag = TAG, throwable = e) { "Failed to load brewery" }
                _uiState.value = BreweryDetailUiState.Error(e.userMessage)
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Failed to load brewery" }
                _uiState.value = BreweryDetailUiState.Error("Failed to load brewery details")
            }
        }
    }

    fun retry() {
        loadedBreweryId?.let { loadBrewery(it) }
    }
}
