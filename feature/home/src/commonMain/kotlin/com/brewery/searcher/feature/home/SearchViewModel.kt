package com.brewery.searcher.feature.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SearchUiState(
    val isLoading: Boolean = false,
)

class SearchViewModel : ViewModel() {

    companion object {
        val TAG = SearchViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
}
