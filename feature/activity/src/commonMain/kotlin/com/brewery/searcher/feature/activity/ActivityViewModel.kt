package com.brewery.searcher.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewery.searcher.core.data.repository.FavoriteBreweryRepository
import com.brewery.searcher.core.model.Brewery
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface ActivityUiState {
    data object Loading : ActivityUiState
    data class Success(val favorites: List<Brewery>) : ActivityUiState
    data class Error(val message: String) : ActivityUiState
}

class ActivityViewModel(
    favoriteBreweryRepository: FavoriteBreweryRepository,
) : ViewModel() {

    companion object {
        val TAG = ActivityViewModel::class.simpleName
    }

    val uiState: StateFlow<ActivityUiState> = favoriteBreweryRepository.getAllFavorites()
        .map<List<Brewery>, ActivityUiState> { favorites ->
            ActivityUiState.Success(favorites)
        }
        .catch { e ->
            Napier.e(tag = TAG, throwable = e) { "Failed to load favorites" }
            emit(ActivityUiState.Error("Failed to load favorites"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ActivityUiState.Loading,
        )
}
