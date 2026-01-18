package com.brewery.searcher.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brewery.searcher.core.datastore.model.DarkThemeConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        ThemeOption(
            label = "Follow System",
            selected = uiState.darkThemeConfig == DarkThemeConfig.FOLLOW_SYSTEM,
            onClick = { viewModel.setDarkThemeConfig(DarkThemeConfig.FOLLOW_SYSTEM) },
        )

        ThemeOption(
            label = "Light",
            selected = uiState.darkThemeConfig == DarkThemeConfig.LIGHT,
            onClick = { viewModel.setDarkThemeConfig(DarkThemeConfig.LIGHT) },
        )

        ThemeOption(
            label = "Dark",
            selected = uiState.darkThemeConfig == DarkThemeConfig.DARK,
            onClick = { viewModel.setDarkThemeConfig(DarkThemeConfig.DARK) },
        )
    }
}

@Composable
private fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
