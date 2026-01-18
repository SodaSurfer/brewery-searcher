package com.brewery.searcher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewery.searcher.core.data.repository.BreweryRepository
import com.brewery.searcher.core.data.repository.FavoriteBreweryRepository
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.network.api.ApiException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface BreweryDetailUiState {
    data object Loading : BreweryDetailUiState
    data class Success(val brewery: Brewery) : BreweryDetailUiState
    data class Error(val message: String) : BreweryDetailUiState
}

class BreweryDetailViewModel(
    private val breweryId: String,
    private val breweryRepository: BreweryRepository,
    private val favoriteBreweryRepository: FavoriteBreweryRepository,
) : ViewModel() {

    companion object {
        val TAG = BreweryDetailViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow<BreweryDetailUiState>(BreweryDetailUiState.Loading)
    val uiState: StateFlow<BreweryDetailUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = favoriteBreweryRepository
        .isFavorite(breweryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    init {
        Napier.d(tag = TAG) { "Initializing BreweryDetailViewModel(breweryId=$breweryId)" }
        loadBrewery()
    }

    private fun loadBrewery() {
        viewModelScope.launch {
            _uiState.value = BreweryDetailUiState.Loading
            Napier.d(tag = TAG) { "loadBrewery: Fetching brewery with id=$breweryId" }
            try {
                val brewery = breweryRepository.getBreweryById(breweryId)
                Napier.d(tag = TAG) { "loadBrewery: Found brewery '${brewery.name}'" }
                _uiState.value = BreweryDetailUiState.Success(brewery)
            } catch (e: ApiException) {
                Napier.e(tag = TAG, throwable = e) { "loadBrewery: Failed to load brewery" }
                _uiState.value = BreweryDetailUiState.Error(e.userMessage)
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "loadBrewery: Failed to load brewery" }
                _uiState.value = BreweryDetailUiState.Error("Failed to load brewery details")
            }
        }
    }

    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState !is BreweryDetailUiState.Success) return

        viewModelScope.launch {
            try {
                favoriteBreweryRepository.toggleFavorite(currentState.brewery)
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Failed to toggle favorite" }
            }
        }
    }

    fun retry() {
        Napier.d(tag = TAG) { "retry()" }
        loadBrewery()
    }
}
