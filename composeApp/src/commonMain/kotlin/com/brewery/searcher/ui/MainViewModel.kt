package com.brewery.searcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewery.searcher.core.datastore.UserSettingsDataSource
import com.brewery.searcher.core.datastore.model.DarkThemeConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    userSettingsDataSource: UserSettingsDataSource,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = userSettingsDataSource.userData.map {
        MainUiState.Success(darkThemeConfig = it.darkThemeConfig)
    }.stateIn(
        scope = viewModelScope,
        initialValue = MainUiState.Loading,
        started = SharingStarted.WhileSubscribed(5_000),
    )
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Success(val darkThemeConfig: DarkThemeConfig) : MainUiState
}
