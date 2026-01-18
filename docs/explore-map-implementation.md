# Explore Map Feature Implementation Plan

## Scope

**In Scope:** Android implementation only
**Out of Scope:** iOS implementation (stub only for compilation)

---

## Overview

Add a Google Maps-based brewery explorer to the `feature/explore` module that displays breweries based on the current map viewport using the Open Brewery DB `by_dist` API parameter.

**Key Features:**
- Google Map displaying current viewport area
- Markers for breweries with valid coordinates
- Auto-search on camera movement (500ms debounce)
- Loading overlay during API calls
- Error handling via Snackbar

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        feature:explore                           │
│  ┌─────────────────┐     ┌──────────────────────────────────┐   │
│  │  ExploreScreen  │────▶│        ExploreViewModel          │   │
│  │  (Compose UI)   │     │  - breweries: StateFlow          │   │
│  │                 │     │  - isLoading: StateFlow          │   │
│  │  - ExploreMapView     │  - error: StateFlow              │   │
│  │    (expect/actual)    │  - onCameraMoved() [debounced]   │   │
│  │  - LoadingOverlay     └──────────────┬───────────────────┘   │
│  │  - Snackbar     │                    │                       │
│  └─────────────────┘                    │                       │
│                                         │                       │
│  commonMain/ExploreMapView.kt (expect)  │                       │
│  androidMain/ExploreMapView.android.kt (Google Maps)            │
│  iosMain/ExploreMapView.ios.kt (stub)   │                       │
└─────────────────────────────────────────┼───────────────────────┘
                                          │
┌─────────────────────────────────────────┼───────────────────────┐
│                        core:data        │                        │
│  ┌──────────────────────────────────────▼───────────────────┐   │
│  │              BreweryRepository (extended)                 │   │
│  │  + getBreweriesByDistance(lat, lng): List<Brewery>       │   │
│  └──────────────────────────────────────┬───────────────────┘   │
└─────────────────────────────────────────┼───────────────────────┘
                                          │
┌─────────────────────────────────────────┼───────────────────────┐
│                      core:network       │                        │
│  ┌──────────────────────────────────────▼───────────────────┐   │
│  │              BreweryApiService (extended)                 │   │
│  │  + getBreweriesByDistance(lat, lng, perPage)             │   │
│  └──────────────────────────────────────┬───────────────────┘   │
│                                         │                        │
│                          Open Brewery DB API                     │
│              GET /breweries?by_dist={lat},{lng}&per_page=50      │
└─────────────────────────────────────────────────────────────────┘
```

---

## Implementation Phases

### Phase 1: Dependencies & API Key Setup
**Goal:** Add Google Maps dependencies and configure secure API key storage.

| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add maps-compose (6.4.1), play-services-maps (19.0.0) |
| `local.properties` | Add `MAPS_API_KEY=<your_key>` (already in .gitignore) |
| `composeApp/build.gradle.kts` | Read API key, expose via BuildConfig + manifestPlaceholders |
| `composeApp/src/androidMain/AndroidManifest.xml` | Add `<meta-data>` for Maps API key |

**Dependencies to add in `gradle/libs.versions.toml`:**
```toml
[versions]
maps-compose = "6.4.1"
play-services-maps = "19.0.0"

[libraries]
maps-compose = { module = "com.google.maps.android:maps-compose", version.ref = "maps-compose" }
play-services-maps = { module = "com.google.android.gms:play-services-maps", version.ref = "play-services-maps" }
```

**API Key setup in `composeApp/build.gradle.kts`:**
```kotlin
import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    defaultConfig {
        buildConfigField("String", "MAPS_API_KEY", "\"${localProperties.getProperty("MAPS_API_KEY", "")}\"")
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY", "")
    }
    buildFeatures {
        buildConfig = true
    }
}
```

**AndroidManifest.xml:**
```xml
<application ...>
    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="${MAPS_API_KEY}" />
</application>
```

**End state:** `./gradlew build` succeeds, BuildConfig.MAPS_API_KEY accessible.

---

### Phase 2: Network Layer Extension
**Goal:** Add `getBreweriesByDistance` API method.

| File | Change |
|------|--------|
| `core/network/.../api/BreweryApiService.kt` | Add interface method |
| `core/network/.../api/BreweryApiServiceImpl.kt` | Implement with `by_dist` parameter |

**Key patterns:**
```kotlin
// BreweryApiService.kt
interface BreweryApiService {
    // ... existing methods ...
    suspend fun getBreweriesByDistance(
        latitude: Double,
        longitude: Double,
        perPage: Int = 50
    ): List<BreweryDto>
}

// BreweryApiServiceImpl.kt
override suspend fun getBreweriesByDistance(
    latitude: Double,
    longitude: Double,
    perPage: Int
): List<BreweryDto> {
    Napier.d(tag = TAG) { "getBreweriesByDistance(lat=$latitude, lng=$longitude, perPage=$perPage)" }
    return try {
        httpClient.get(BASE_URL) {
            parameter("by_dist", "$latitude,$longitude")
            parameter("per_page", perPage)
        }.bodyOrThrow()
    } catch (e: Exception) {
        Napier.e(tag = TAG, throwable = e) { "getBreweriesByDistance failed" }
        throw e
    }
}
```

**End state:** API service can fetch breweries sorted by distance from coordinates.

---

### Phase 3: Repository Layer Extension
**Goal:** Add repository method with DTO-to-domain mapping.

| File | Change |
|------|--------|
| `core/data/.../repository/BreweryRepository.kt` | Add interface method |
| `core/data/.../repository/BreweryRepositoryImpl.kt` | Implement using existing `toDomain()` mapper |

**Key patterns:**
```kotlin
// BreweryRepository.kt
interface BreweryRepository {
    // ... existing methods ...
    suspend fun getBreweriesByDistance(
        latitude: Double,
        longitude: Double
    ): List<Brewery>
}

// BreweryRepositoryImpl.kt
override suspend fun getBreweriesByDistance(
    latitude: Double,
    longitude: Double
): List<Brewery> {
    Napier.d(tag = TAG) { "getBreweriesByDistance(lat=$latitude, lng=$longitude)" }
    return apiService.getBreweriesByDistance(latitude, longitude)
        .map { it.toDomain() }
        .filter { it.latitude != null && it.longitude != null }
}
```

**Edge case:** Filter out breweries with null lat/lng at repository level.

**End state:** Repository returns `List<Brewery>` with valid coordinates only.

---

### Phase 4: Map Composable (expect/actual)
**Goal:** Create platform-specific map implementations.

| File | Purpose |
|------|---------|
| `feature/explore/src/commonMain/.../ui/ExploreMapView.kt` | Expect declaration |
| `feature/explore/src/androidMain/.../ui/ExploreMapView.android.kt` | Google Maps with markers |
| `feature/explore/src/iosMain/.../ui/ExploreMapView.ios.kt` | Stub: "Map coming soon" |
| `feature/explore/build.gradle.kts` | Add maps dependencies to androidMain |

**Module dependencies in `feature/explore/build.gradle.kts`:**
```kotlin
plugins {
    id("brewerysearcher.feature")
}

android {
    namespace = "com.brewery.searcher.feature.explore"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.model)
            implementation(projects.core.network)
        }
        androidMain.dependencies {
            implementation(libs.maps.compose)
            implementation(libs.play.services.maps)
        }
    }
}
```

**Key patterns:**
```kotlin
// commonMain - ExploreMapView.kt
@Composable
expect fun ExploreMapView(
    breweries: List<Brewery>,
    onCameraMoved: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier = Modifier,
)

// androidMain - ExploreMapView.android.kt
private const val CAMERA_DEBOUNCE_MS = 500L

@OptIn(FlowPreview::class)
@Composable
actual fun ExploreMapView(
    breweries: List<Brewery>,
    onCameraMoved: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(39.8283, -98.5795), 4f) // US center
    }

    // Debounce camera movements in the UI layer (500ms)
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position.target }
            .debounce(CAMERA_DEBOUNCE_MS)
            .distinctUntilChanged()
            .collect { latLng ->
                onCameraMoved(latLng.latitude, latLng.longitude)
            }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        breweries.forEach { brewery ->
            val lat = brewery.latitude
            val lng = brewery.longitude
            if (lat != null && lng != null) {
                Marker(
                    state = MarkerState(position = LatLng(lat, lng)),
                    title = brewery.name,
                    snippet = brewery.city?.let { "$it, ${brewery.stateProvince}" }
                )
            }
        }
    }
}

// iosMain - ExploreMapView.ios.kt
@Composable
actual fun ExploreMapView(
    breweries: List<Brewery>,
    onCameraMoved: (latitude: Double, longitude: Double) -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Map view coming soon for iOS")
    }
}
```

**End state:** Map renders on Android with marker support.

---

### Phase 5: ViewModel & State Management
**Goal:** Implement simple reactive ViewModel that responds to camera movements.

| File | Change |
|------|--------|
| `feature/explore/.../ExploreViewModel.kt` | Simple reactive implementation (no init block) |
| `feature/explore/.../di/ExploreModule.kt` | Inject BreweryRepository |

**Note:** Debouncing is handled in the UI layer (map composable), keeping the ViewModel simple and reactive.

**Key patterns:**
```kotlin
data class ExploreUiState(
    val breweries: List<Brewery> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class ExploreViewModel(
    private val breweryRepository: BreweryRepository,
) : ViewModel() {

    companion object {
        val TAG = ExploreViewModel::class.simpleName
    }

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    fun onCameraMoved(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val breweries = breweryRepository.getBreweriesByDistance(latitude, longitude)
                _uiState.update { it.copy(breweries = breweries, isLoading = false) }
                Napier.d(tag = TAG) { "Loaded ${breweries.size} breweries near ($latitude, $longitude)" }
            } catch (e: ApiException) {
                Napier.e(tag = TAG, throwable = e) { "API error fetching breweries" }
                _uiState.update { it.copy(error = e.userMessage, isLoading = false) }
            } catch (e: Exception) {
                Napier.e(tag = TAG, throwable = e) { "Failed to load breweries" }
                _uiState.update { it.copy(error = "Failed to load breweries", isLoading = false) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
```

**DI Module update:**
```kotlin
// ExploreModule.kt
val exploreModule = module {
    viewModelOf(::ExploreViewModel)
}
```

**End state:** ViewModel is a simple reactive handler - no init block, debouncing in UI layer.

---

### Phase 6: UI Layer (ExploreScreen)
**Goal:** Integrate map, loading overlay, and error Snackbar.

| File | Change |
|------|--------|
| `feature/explore/.../ExploreScreen.kt` | Full UI implementation |

**Key patterns:**
```kotlin
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

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ExploreMapView(
                breweries = uiState.breweries,
                onCameraMoved = viewModel::onCameraMoved,
                modifier = Modifier.fillMaxSize(),
            )

            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
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
```

**End state:** Complete working explore screen with map and error handling.

---

## Error Handling Strategy

| Scenario | Handling |
|----------|----------|
| ApiException | Show `userMessage` in Snackbar |
| Network error | Show "Failed to load breweries" in Snackbar |
| No breweries found | Empty markers (valid state, no error) |
| Null lat/lng in API response | Filtered at repository layer |
| Rapid camera moves | Debounced at 500ms |

---

## Edge Cases

1. **Empty results:** Map displays with no markers (valid state)
2. **Null lat/lng in API response:** Filtered out at repository layer
3. **Offline mode:** ApiException caught, error shown in Snackbar
4. **API rate limiting:** ApiException with server message displayed
5. **Very fast camera movements:** Debounced to prevent API spam
6. **Missing API key:** Map fails to load (handled by Google Maps SDK)

---

## Verification Checklist

- [ ] App builds without errors: `./gradlew build`
- [ ] App launches on Android emulator
- [ ] Navigate to Explore tab - map displays
- [ ] Pan/zoom map - loading indicator appears after 500ms pause
- [ ] Markers appear for nearby breweries
- [ ] Tap marker - shows brewery name and location snippet
- [ ] Disable network - error Snackbar appears
- [ ] iOS compiles with "Map coming soon" stub
- [ ] `local.properties` contains MAPS_API_KEY (not committed to git)
- [ ] Rapid camera movements don't spam API (check Napier logs)

---

## Dependencies Summary

```toml
# gradle/libs.versions.toml additions

[versions]
maps-compose = "6.4.1"
play-services-maps = "19.0.0"

[libraries]
maps-compose = { module = "com.google.maps.android:maps-compose", version.ref = "maps-compose" }
play-services-maps = { module = "com.google.android.gms:play-services-maps", version.ref = "play-services-maps" }
```
