# Search Feature Implementation Plan

## Scope

**In Scope:** Android implementation only
**Out of Scope:** iOS implementation (stub files only for compilation)

---

## Overview

Implement brewery search functionality using the [Open Brewery DB API](https://www.openbrewerydb.org/documentation) with:
- Search input with auto-search (300ms debounce)
- Search type selection via bottom sheet (All Fields, By City, By Country, By State)
- Infinite scroll pagination
- Loading indicators

---

## Implementation Phases

### Phase 1: Domain Models
**Goal:** Define the core data structures used across the app.

**Files to create:**
| File | Purpose |
|------|---------|
| `core/model/.../Brewery.kt` | Domain model for brewery data |
| `core/model/.../BreweryType.kt` | Enum for brewery types |
| `core/model/.../SearchType.kt` | Enum for search filter types |

**End state:** Domain models compile and can be imported by other modules.

---

### Phase 2: Network Layer
**Goal:** Set up HTTP client and API service to fetch breweries from the Open Brewery DB API.

**Files to create:**
| File | Purpose |
|------|---------|
| `core/network/.../dto/BreweryDto.kt` | API response DTO with JSON serialization |
| `core/network/.../api/BreweryApiService.kt` | Interface defining API methods |
| `core/network/.../api/BreweryApiServiceImpl.kt` | Ktor implementation of API service |
| `core/network/.../HttpClientProvider.kt` | Expect declaration for HTTP client |
| `core/network/.../HttpClientProvider.android.kt` | Android implementation (OkHttp engine) |
| `core/network/.../HttpClientProvider.ios.kt` | iOS stub (throws NotImplementedError) |
| `core/network/.../di/NetworkModule.kt` | Koin DI module |

**Dependencies to add in `gradle/libs.versions.toml`:**
```toml
[versions]
ktor = "3.1.1"

[libraries]
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
```

**API Endpoints:**
- Search: `GET /v1/breweries/search?query={query}&page={page}&per_page={perPage}`
- List by filter: `GET /v1/breweries?by_city={city}&page={page}&per_page={perPage}`

**End state:** Can make API calls and receive brewery data. Verify with unit test or manual API call.

---

### Phase 3: Data Layer (Repository + Paging)
**Goal:** Create repository layer with Paging3 support for infinite scroll.

**New module:** `core:data`

**Files to create:**
| File | Purpose |
|------|---------|
| `core/data/build.gradle.kts` | Module setup with dependencies |
| `core/data/.../repository/BreweryRepository.kt` | Repository interface |
| `core/data/.../repository/BreweryRepositoryImpl.kt` | Implementation with Pager |
| `core/data/.../paging/SearchBreweryPagingSource.kt` | PagingSource for search |
| `core/data/.../mapper/BreweryMapper.kt` | DTO to domain mapper |
| `core/data/.../di/DataModule.kt` | Koin DI module |

**Dependencies to add:**
```toml
[versions]
paging = "3.3.6"

[libraries]
paging-common = { module = "androidx.paging:paging-common", version.ref = "paging" }
paging-compose-common = { module = "androidx.paging:paging-compose-common", version.ref = "paging" }
```

**Key patterns:**
```kotlin
// Repository returns Flow<PagingData<Brewery>>
fun searchBreweries(query: String, searchType: SearchType): Flow<PagingData<Brewery>>

// PagingSource handles pagination
class SearchBreweryPagingSource(
    private val apiService: BreweryApiService,
    private val query: String,
    private val searchType: SearchType,
) : PagingSource<Int, BreweryDto>()
```

**End state:** Repository can be injected and returns paginated search results.

---

### Phase 4: Presentation Layer (ViewModel)
**Goal:** Update SearchViewModel with debounced search and Paging3 integration.

**File to modify:** `feature/home/.../SearchViewModel.kt`

**Key patterns (from BreweryFinder):**
```kotlin
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val breweryRepository: BreweryRepository,
) : ViewModel() {

    // Separate StateFlows for each piece of state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchType = MutableStateFlow(SearchType.ALL_FIELDS)
    val searchType: StateFlow<SearchType> = _searchType.asStateFlow()

    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()

    // Declarative debounced search using Flow operators
    val searchResults: Flow<PagingData<Brewery>> = combine(
        _searchQuery.debounce(300),
        _searchType,
    ) { query, type ->
        query to type
    }.flatMapLatest { (query, type) ->
        if (query.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            breweryRepository.searchBreweries(query.trim(), type)
        }
    }.cachedIn(viewModelScope)

    fun onQueryChange(query: String) { ... }
    fun onSearchTypeSelected(type: SearchType) { ... }
    fun onShowBottomSheet() { ... }
    fun onDismissBottomSheet() { ... }
}
```

**End state:** ViewModel compiles and exposes search results as `Flow<PagingData<Brewery>>`.

---

### Phase 5: UI Layer (SearchScreen)
**Goal:** Build the search UI with input, filter button, bottom sheet, and paginated results list.

**File to modify:** `feature/home/.../SearchScreen.kt`

**UI Components:**
1. **Search Bar:** `OutlinedTextField` + filter `IconButton`
2. **Search Type Indicator:** Chip/Text showing current search type
3. **Results List:** `LazyColumn` with Paging Compose
4. **Bottom Sheet:** `ModalBottomSheet` with `RadioButton` list for search type selection
5. **Loading States:** `CircularProgressIndicator` for initial load and load more

**Key patterns:**
```kotlin
@Composable
fun SearchScreen(viewModel: SearchViewModel = koinViewModel()) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchType by viewModel.searchType.collectAsState()
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    // Handle loadState.refresh for initial loading/error
    // Handle loadState.append for load more indicator

    if (showBottomSheet) {
        SearchTypeBottomSheet(...)
    }
}
```

**End state:** Fully functional search screen with all UI interactions working.

---

### Phase 6: DI Integration
**Goal:** Wire up all Koin modules so dependencies are properly injected.

**Files to modify:**
| File | Change |
|------|--------|
| `settings.gradle.kts` | Add `include(":core:data")` |
| `feature/home/.../di/HomeModule.kt` | Inject `BreweryRepository` into `SearchViewModel` |
| `composeApp/.../di/AppModule.kt` | Add `networkModule`, `dataModule` to `appModules()` |

**End state:** App compiles and runs with all dependencies properly injected.

---

## Verification Checklist

- [ ] App builds without errors: `./gradlew build`
- [ ] App launches on Android emulator
- [ ] Navigate to Search screen from Home
- [ ] Type in search input → results appear after 300ms
- [ ] Tap filter button → bottom sheet opens
- [ ] Select different search type → bottom sheet closes, new search triggered
- [ ] Scroll to bottom of results → more results load (pagination)
- [ ] Loading indicator shows during initial search
- [ ] Loading indicator shows at bottom when loading more

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        feature:home                              │
│  ┌─────────────────┐     ┌──────────────────────────────────┐   │
│  │  SearchScreen   │────▶│        SearchViewModel           │   │
│  │  (Compose UI)   │     │  - searchQuery: StateFlow        │   │
│  │                 │     │  - searchType: StateFlow         │   │
│  │  - TextField    │     │  - searchResults: Flow<Paging>   │   │
│  │  - LazyColumn   │     └──────────────┬───────────────────┘   │
│  │  - BottomSheet  │                    │                       │
│  └─────────────────┘                    │                       │
└─────────────────────────────────────────┼───────────────────────┘
                                          │
┌─────────────────────────────────────────┼───────────────────────┐
│                        core:data        │                        │
│  ┌──────────────────────────────────────▼───────────────────┐   │
│  │              BreweryRepository                            │   │
│  │  searchBreweries(query, type): Flow<PagingData<Brewery>> │   │
│  └──────────────────────────────────────┬───────────────────┘   │
│                                         │                       │
│  ┌──────────────────────────────────────▼───────────────────┐   │
│  │           SearchBreweryPagingSource                       │   │
│  │  - Handles pagination (page 1, 2, 3...)                  │   │
│  │  - Switches API based on SearchType                      │   │
│  └──────────────────────────────────────┬───────────────────┘   │
└─────────────────────────────────────────┼───────────────────────┘
                                          │
┌─────────────────────────────────────────┼───────────────────────┐
│                      core:network       │                        │
│  ┌──────────────────────────────────────▼───────────────────┐   │
│  │              BreweryApiService                            │   │
│  │  - searchBreweries(query, page)                          │   │
│  │  - getBreweriesByCity(city, page)                        │   │
│  │  - getBreweriesByCountry(country, page)                  │   │
│  │  - getBreweriesByState(state, page)                      │   │
│  └──────────────────────────────────────┬───────────────────┘   │
│                                         │                       │
│                                         ▼                       │
│                          Open Brewery DB API                    │
│                   https://api.openbrewerydb.org                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Dependencies Summary

```toml
# gradle/libs.versions.toml

[versions]
ktor = "3.1.1"
paging = "3.3.6"

[libraries]
# Ktor HTTP Client
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

# Paging 3
paging-common = { module = "androidx.paging:paging-common", version.ref = "paging" }
paging-compose-common = { module = "androidx.paging:paging-compose-common", version.ref = "paging" }
```
