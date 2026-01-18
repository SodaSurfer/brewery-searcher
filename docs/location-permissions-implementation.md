# Location Permissions Implementation Plan

## Scope

**In Scope:** Android implementation only
**Out of Scope:** iOS implementation (stub only for compilation), background location tracking

---

## Overview

Request location permission when user enters Explore screen and center map on their location if granted.

**Libraries:**
- [moko-permissions](https://github.com/icerockdev/moko-permissions) (0.20.1) - Multiplatform permissions
- [moko-geo](https://github.com/icerockdev/moko-geo) (0.8.0) - Multiplatform location tracking

**Permission Flow:**
1. User navigates to Explore screen
2. Check if location permission already granted:
   - **Yes** -> Get location and center map
   - **No** -> Check if "do not ask again" preference is set:
     - **Set** -> Use default US center
     - **Not set** -> Show rationale dialog explaining why we need location
3. User responds to rationale dialog:
   - **Agree** -> Request system permission
   - **Cancel** -> Ask ONE more time (second dialog), then give up
4. Rationale dialog includes checkbox: "Do not ask me again"
   - If checked and user cancels -> Save preference to DataStore, never ask again

**Map Centering:**
- Permission granted: Animate to user location (zoom 12)
- Permission denied: Use default US center (39.8283, -98.5795, zoom 4)

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           ExploreScreen                                  │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  LaunchedEffect(uiState.shouldRequestPermission)                │    │
│  │    1. Request permission via PermissionsController               │    │
│  │    2. If granted, get location via LocationTracker               │    │
│  │    3. Update ViewModel with result                               │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  LocationPermissionDialog                                        │    │
│  │    - Explains why location is needed                            │    │
│  │    - "Do not ask me again" checkbox                             │    │
│  │    - Allow / Cancel buttons                                      │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                              │                                           │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  ExploreMapView                                                  │    │
│  │    - initialCameraPosition (from ViewModel state)               │    │
│  │    - Animates to new position when changed                      │    │
│  └─────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         ExploreViewModel                                 │
│  ┌─────────────────────────────────────────────────────────────────┐    │
│  │  ExploreUiState                                                  │    │
│  │    - initialCameraPosition: CameraPosition                       │    │
│  │    - showLocationRationaleDialog: Boolean                        │    │
│  │    - locationPermissionDenialCount: Int                         │    │
│  │    - shouldRequestPermission: Boolean                           │    │
│  └─────────────────────────────────────────────────────────────────┘    │
│                                                                          │
│  Dependencies: BreweryRepository, UserSettingsDataSource                │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                    moko-permissions / moko-geo                          │
│  ┌──────────────────────────┐  ┌──────────────────────────────────┐    │
│  │  PermissionsController   │  │  LocationTracker                  │    │
│  │    - providePermission() │  │    - startTracking()              │    │
│  │    - isPermissionGranted │  │    - stopTracking()               │    │
│  └──────────────────────────┘  │    - getExtendedLocation()        │    │
│                                 └──────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Implementation Phases

### Phase 1: Add Dependencies

**Goal:** Add moko-permissions and moko-geo libraries to the project.

| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add moko-permissions (0.20.1), moko-geo (0.8.0) |
| `feature/explore/build.gradle.kts` | Add moko deps to commonMain |

**Dependencies to add in `gradle/libs.versions.toml`:**
```toml
[versions]
moko-permissions = "0.20.1"
moko-geo = "0.8.0"

[libraries]
moko-permissions = { module = "dev.icerock.moko:permissions", version.ref = "moko-permissions" }
moko-permissions-compose = { module = "dev.icerock.moko:permissions-compose", version.ref = "moko-permissions" }
moko-geo = { module = "dev.icerock.moko:geo", version.ref = "moko-geo" }
moko-geo-compose = { module = "dev.icerock.moko:geo-compose", version.ref = "moko-geo" }
```

**In `feature/explore/build.gradle.kts`:**
```kotlin
commonMain.dependencies {
    // existing deps...
    implementation(libs.moko.permissions)
    implementation(libs.moko.permissions.compose)
    implementation(libs.moko.geo)
    implementation(libs.moko.geo.compose)
}
```

**End state:** `./gradlew build` succeeds with moko libraries available.

---

### Phase 2: Android Manifest Permissions

**Goal:** Add required location permissions to Android manifest.

| File | Change |
|------|--------|
| `composeApp/src/androidMain/AndroidManifest.xml` | Add location permissions |

**AndroidManifest.xml (add before `<application>`):**
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

**End state:** Android app declares required location permissions.

---

### Phase 3: DataStore Preference for "Do Not Ask Again"

**Goal:** Persist user's "do not ask again" preference.

| File | Change |
|------|--------|
| `core/datastore/src/commonMain/proto/user_settings.proto` | Add `location_permission_do_not_ask` field |
| `core/datastore/.../model/UserSettings.kt` | Add `locationDoNotAsk` field |
| `core/datastore/.../UserSettingsSerializer.kt` | Update mapper |
| `core/datastore/.../UserSettingsDataSource.kt` | Add setter method |

**Proto file:**
```protobuf
message UserSettings {
  DarkThemeConfigProto dark_theme_config = 1;
  bool location_permission_do_not_ask = 2;  // NEW
}
```

**Domain model:**
```kotlin
data class UserSettings(
    val darkThemeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
    val locationDoNotAsk: Boolean = false,  // NEW
)
```

**Mapper update:**
```kotlin
fun ProtoUserSettings.toUserSettings(): UserSettings {
    return UserSettings(
        darkThemeConfig = when (dark_theme_config) { ... },
        locationDoNotAsk = location_permission_do_not_ask,
    )
}
```

**DataSource method:**
```kotlin
suspend fun setLocationDoNotAsk(value: Boolean) {
    dataStore.updateData { prefs ->
        prefs.copy(location_permission_do_not_ask = value)
    }
}
```

**End state:** "Do not ask again" preference can be read and written via DataStore.

---

### Phase 4: CameraPosition Model & Map Updates

**Goal:** Support dynamic initial camera position for the map.

| File | Change |
|------|--------|
| `feature/explore/.../model/CameraPosition.kt` | Create data class |
| `feature/explore/.../ui/ExploreMapView.kt` | Add `initialCameraPosition` param |
| `feature/explore/.../ui/ExploreMapView.android.kt` | Animate to initial position |
| `feature/explore/.../ui/ExploreMapView.ios.kt` | Update signature (stub) |

**CameraPosition.kt:**
```kotlin
package com.brewery.searcher.feature.explore.model

data class CameraPosition(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float,
) {
    companion object {
        val DEFAULT_US_CENTER = CameraPosition(
            latitude = 39.8283,
            longitude = -98.5795,
            zoom = 4f,
        )
    }
}
```

**ExploreMapView expect:**
```kotlin
@Composable
expect fun ExploreMapView(
    breweries: List<Brewery>,
    selectedBreweryId: String?,
    initialCameraPosition: CameraPosition,
    onCameraMoved: (latitude: Double, longitude: Double, zoom: Float) -> Unit,
    onBrewerySelected: (Brewery) -> Unit,
    modifier: Modifier = Modifier,
)
```

**Android implementation update:**
```kotlin
val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(
        LatLng(initialCameraPosition.latitude, initialCameraPosition.longitude),
        initialCameraPosition.zoom
    )
}

LaunchedEffect(initialCameraPosition) {
    cameraPositionState.animate(
        CameraUpdateFactory.newLatLngZoom(
            LatLng(initialCameraPosition.latitude, initialCameraPosition.longitude),
            initialCameraPosition.zoom
        )
    )
}
```

**End state:** Map can receive and animate to a dynamic initial position.

---

### Phase 5: Rationale Dialog Component

**Goal:** Create dialog that explains why location is needed with "do not ask" checkbox.

| File | Change |
|------|--------|
| `feature/explore/.../ui/LocationPermissionDialog.kt` | Create dialog component |

**LocationPermissionDialog.kt:**
```kotlin
@Composable
fun LocationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: (doNotAskAgain: Boolean) -> Unit,
) {
    var doNotAskAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss(doNotAskAgain) },
        title = { Text("Location Access") },
        text = {
            Column {
                Text("BrewerySearcher needs your location to show breweries near you.")
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = doNotAskAgain,
                        onCheckedChange = { doNotAskAgain = it }
                    )
                    Text("Do not ask me again")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Allow") }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss(doNotAskAgain) }) { Text("Cancel") }
        }
    )
}
```

**End state:** Rationale dialog component ready for use in ExploreScreen.

---

### Phase 6: ViewModel State Management

**Goal:** Add permission flow state management to ExploreViewModel.

| File | Change |
|------|--------|
| `feature/explore/.../ExploreViewModel.kt` | Add dialog state, denial tracking |
| `feature/explore/.../di/ExploreModule.kt` | Add UserSettingsDataSource to ViewModel |

**Updated ExploreUiState:**
```kotlin
data class ExploreUiState(
    // existing fields...
    val initialCameraPosition: CameraPosition = CameraPosition.DEFAULT_US_CENTER,
    val showLocationRationaleDialog: Boolean = false,
    val locationPermissionDenialCount: Int = 0,
    val shouldRequestPermission: Boolean = false,
)
```

**Updated ViewModel:**
```kotlin
class ExploreViewModel(
    private val breweryRepository: BreweryRepository,
    private val userSettingsDataSource: UserSettingsDataSource,
) : ViewModel() {

    init {
        viewModelScope.launch {
            userSettingsDataSource.userData.collect { settings ->
                if (!settings.locationDoNotAsk) {
                    checkAndRequestLocationPermission()
                }
            }
        }
    }

    fun checkAndRequestLocationPermission() {
        _uiState.update { it.copy(showLocationRationaleDialog = true) }
    }

    fun onRationaleDialogConfirm() {
        _uiState.update {
            it.copy(showLocationRationaleDialog = false, shouldRequestPermission = true)
        }
    }

    fun onRationaleDialogDismiss(doNotAskAgain: Boolean) {
        viewModelScope.launch {
            if (doNotAskAgain) {
                userSettingsDataSource.setLocationDoNotAsk(true)
            }
        }
        _uiState.update {
            val newDenialCount = it.locationPermissionDenialCount + 1
            it.copy(
                showLocationRationaleDialog = false,
                locationPermissionDenialCount = newDenialCount,
            )
        }
        if (_uiState.value.locationPermissionDenialCount < 2 && !doNotAskAgain) {
            _uiState.update { it.copy(showLocationRationaleDialog = true) }
        }
    }

    fun onPermissionRequestCompleted() {
        _uiState.update { it.copy(shouldRequestPermission = false) }
    }

    fun onUserLocationReceived(latitude: Double, longitude: Double) {
        _uiState.update {
            it.copy(initialCameraPosition = CameraPosition(latitude, longitude, 12f))
        }
    }
}
```

**End state:** ViewModel manages complete permission dialog flow with denial tracking.

---

### Phase 7: ExploreScreen Integration

**Goal:** Wire up moko-permissions, moko-geo, and dialog in ExploreScreen.

| File | Change |
|------|--------|
| `feature/explore/.../ExploreScreen.kt` | Add permission + location logic |

**Key changes:**
```kotlin
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // moko-permissions controller
    val permissionsFactory = rememberPermissionsControllerFactory()
    val permissionsController = remember(permissionsFactory) {
        permissionsFactory.createPermissionsController()
    }
    BindEffect(permissionsController)

    // moko-geo location tracker
    val locationTrackerFactory = rememberLocationTrackerFactory()
    val locationTracker = remember(locationTrackerFactory, permissionsController) {
        locationTrackerFactory.createLocationTracker(permissionsController)
    }

    // Handle system permission request when triggered by ViewModel
    LaunchedEffect(uiState.shouldRequestPermission) {
        if (uiState.shouldRequestPermission) {
            try {
                permissionsController.providePermission(Permission.LOCATION)
                locationTracker.startTracking()
                val location = locationTracker.getExtendedLocation()
                locationTracker.stopTracking()
                viewModel.onUserLocationReceived(location.latitude, location.longitude)
            } catch (e: DeniedException) {
                Napier.w(tag = TAG) { "Location permission denied" }
            } catch (e: DeniedAlwaysException) {
                Napier.w(tag = TAG) { "Location permission denied permanently" }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Error getting location" }
            }
            viewModel.onPermissionRequestCompleted()
        }
    }

    // Show rationale dialog
    if (uiState.showLocationRationaleDialog) {
        LocationPermissionDialog(
            onConfirm = viewModel::onRationaleDialogConfirm,
            onDismiss = viewModel::onRationaleDialogDismiss,
        )
    }

    // ... existing UI with updated ExploreMapView call ...
    ExploreMapView(
        breweries = uiState.breweries,
        selectedBreweryId = uiState.selectedBrewery?.id,
        initialCameraPosition = uiState.initialCameraPosition,
        onCameraMoved = viewModel::onCameraMoved,
        onBrewerySelected = viewModel::onBrewerySelected,
        modifier = Modifier.fillMaxSize(),
    )
}
```

**End state:** Complete working permission flow integrated into ExploreScreen.

---

## Files Summary

| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add moko-permissions and moko-geo deps |
| `feature/explore/build.gradle.kts` | Add moko deps to commonMain |
| `composeApp/src/androidMain/AndroidManifest.xml` | Add location permissions |
| `core/datastore/src/commonMain/proto/user_settings.proto` | Add `location_permission_do_not_ask` field |
| `core/datastore/.../model/UserSettings.kt` | Add `locationDoNotAsk` field |
| `core/datastore/.../UserSettingsSerializer.kt` | Update mapper for new field |
| `core/datastore/.../UserSettingsDataSource.kt` | Add `setLocationDoNotAsk()` method |
| `feature/explore/.../model/CameraPosition.kt` | Create new data class |
| `feature/explore/.../ui/LocationPermissionDialog.kt` | Create rationale dialog with checkbox |
| `feature/explore/.../ExploreViewModel.kt` | Add dialog state, denial tracking, DataStore |
| `feature/explore/.../di/ExploreModule.kt` | Add UserSettingsDataSource to ViewModel |
| `feature/explore/.../ui/ExploreMapView.kt` | Add initialCameraPosition param |
| `feature/explore/.../ui/ExploreMapView.android.kt` | Animate to initial position |
| `feature/explore/.../ui/ExploreMapView.ios.kt` | Update signature (stub only) |
| `feature/explore/.../ExploreScreen.kt` | Add dialog + permission flow |

---

## Verification Checklist

- [ ] Build succeeds: `.\gradlew.bat build`
- [ ] Navigate to Explore tab on Android emulator/device
- [ ] Rationale dialog appears explaining why location is needed
- [ ] Dialog has checkbox "Do not ask me again"
- [ ] Click "Allow" -> system permission dialog appears
- [ ] If system permission granted: map animates to current location (zoom ~12)
- [ ] If system permission denied: rationale dialog appears ONE more time
- [ ] If denied twice: map shows US center (zoom 4), no more dialogs
- [ ] If checkbox checked and "Cancel" clicked: preference saved, no future dialogs
- [ ] Re-enter Explore tab: no dialog if "do not ask" was checked
- [ ] Project compiles for iOS target (stub only, no functionality)

---

## Dependencies Summary

```toml
# gradle/libs.versions.toml additions

[versions]
moko-permissions = "0.20.1"
moko-geo = "0.8.0"

[libraries]
moko-permissions = { module = "dev.icerock.moko:permissions", version.ref = "moko-permissions" }
moko-permissions-compose = { module = "dev.icerock.moko:permissions-compose", version.ref = "moko-permissions" }
moko-geo = { module = "dev.icerock.moko:geo", version.ref = "moko-geo" }
moko-geo-compose = { module = "dev.icerock.moko:geo-compose", version.ref = "moko-geo" }
```
