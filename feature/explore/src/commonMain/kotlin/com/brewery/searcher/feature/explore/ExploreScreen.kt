package com.brewery.searcher.feature.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brewery.searcher.feature.explore.ui.BreweryListBottomSheet
import com.brewery.searcher.feature.explore.ui.ExploreMapView
import com.brewery.searcher.feature.explore.ui.SelectedBreweryBottomSheet
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short,
            )
            viewModel.clearError()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(20.dp),
            textAlign = TextAlign.Center,
            text = "Explore Breweries",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
        Box(modifier = Modifier.fillMaxSize()) {
            ExploreMapView(
                breweries = uiState.breweries,
                selectedBreweryId = uiState.selectedBrewery?.id,
                onCameraMoved = viewModel::onCameraMoved,
                onBrewerySelected = viewModel::onBrewerySelected,
                modifier = Modifier.fillMaxSize(),
            )

            if (uiState.isLoading) {
                LoadingOverlay()
            }

            if (uiState.breweries.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = viewModel::onShowBottomSheet,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text("View List (${uiState.breweries.size})")
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                )
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Show brewery list bottom sheet
    if (uiState.showBottomSheet) {
        BreweryListBottomSheet(
            breweries = uiState.breweries,
            onDismiss = viewModel::onDismissBottomSheet,
        )
    }

    // Show selected brewery detail bottom sheet
    uiState.selectedBrewery?.let { brewery ->
        SelectedBreweryBottomSheet(
            brewery = brewery,
            onDismiss = viewModel::onClearSelection,
        )
    }
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}
