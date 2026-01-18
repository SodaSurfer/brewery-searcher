package com.brewery.searcher.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brewery.searcher.core.datastore.UserSettingsDataSource
import com.brewery.searcher.core.datastore.model.DarkThemeConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
)

class SettingsViewModel(
    private val userSettingsDataSource: UserSettingsDataSource,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = userSettingsDataSource.userData
        .map { SettingsUiState(darkThemeConfig = it.darkThemeConfig) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun setDarkThemeConfig(config: DarkThemeConfig) {
        viewModelScope.launch {
            userSettingsDataSource.setDarkThemeConfig(config)
        }
    }
}
