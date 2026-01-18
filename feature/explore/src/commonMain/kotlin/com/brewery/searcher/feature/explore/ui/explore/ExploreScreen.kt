package com.brewery.searcher.feature.explore.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
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
import com.brewery.searcher.feature.explore.location.rememberLocationProvider
import com.brewery.searcher.feature.explore.ui.bottomsheets.BreweryListBottomSheet
import com.brewery.searcher.feature.explore.ui.dialogs.LocationPermissionDialog
import com.brewery.searcher.feature.explore.ui.dialogs.PermissionDeniedDialog
import com.brewery.searcher.feature.explore.ui.bottomsheets.SelectedBreweryBottomSheet
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import io.github.aakira.napier.Napier
import org.koin.compose.viewmodel.koinViewModel

private const val TAG = "ExploreScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val favoriteBreweryIds by viewModel.favoriteBreweryIds.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // moko-permissions controller
    val permissionsFactory = rememberPermissionsControllerFactory()
    val permissionsController = remember(permissionsFactory) {
        permissionsFactory.createPermissionsController()
    }
    BindEffect(permissionsController)

    // Location provider for getting actual location
    val locationProvider = rememberLocationProvider()

    // Check permission on screen entry and get location if already granted
    LaunchedEffect(Unit) {
        val isGranted = permissionsController.isPermissionGranted(Permission.LOCATION)
        if (isGranted) {
            Napier.d(tag = TAG) { "Location permission already granted on entry, getting location..." }
            val location = locationProvider.getCurrentLocation()
            if (location != null) {
                viewModel.onUserLocationReceived(location.latitude, location.longitude)
            }
        }
    }

    // Handle system permission request when triggered by ViewModel
    LaunchedEffect(uiState.shouldRequestPermission) {
        if (uiState.shouldRequestPermission) {
            var locationDoNotAsk = false
            try {
                permissionsController.providePermission(Permission.LOCATION)
                // Permission granted - get location
                locationDoNotAsk = true
                Napier.d(tag = TAG) { "Location permission granted, getting location..." }
                val location = locationProvider.getCurrentLocation()
                if (location != null) {
                    viewModel.onUserLocationReceived(location.latitude, location.longitude)
                } else {
                    Napier.w(tag = TAG) { "Could not get current location" }
                }
            } catch (_: DeniedAlwaysException) {
                Napier.w(tag = TAG) { "Location permission denied permanently" }
                viewModel.onPermissionDeniedPermanently()
                locationDoNotAsk = true
            } catch (_: DeniedException) {
                Napier.w(tag = TAG) { "Location permission denied" }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Error getting location" }
            }
            viewModel.onPermissionRequestCompleted(locationDoNotAsk = locationDoNotAsk)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short,
            )
            viewModel.clearError()
        }
    }

    // Show rationale dialog
    if (uiState.showLocationRationaleDialog) {
        LocationPermissionDialog(
            onConfirm = viewModel::onRationaleDialogConfirm,
            onDismiss = viewModel::onRationaleDialogDismiss,
        )
    }

    // Show permission denied dialog
    if (uiState.showPermissionDeniedDialog) {
        PermissionDeniedDialog(
            onDismiss = viewModel::onDismissPermissionDeniedDialog,
        )
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
                initialCameraPosition = uiState.initialCameraPosition,
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
            favoriteBreweryIds = favoriteBreweryIds,
            onBreweryClick = { brewery ->
                viewModel.onDismissBottomSheet()
                viewModel.onBrewerySelected(brewery)
            },
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
