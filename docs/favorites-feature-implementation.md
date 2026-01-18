# Favorite Breweries Feature - Implementation Plan

## Scope

- **In Scope**: Android and iOS (KMP cross-platform)
- **In Scope**: Room database storage for favorites
- **In Scope**: Favorite toggle button in brewery detail screen
- **In Scope**: Display favorites list in Activity screen with navigation
- **Out of Scope**: Cloud sync of favorites
- **Out of Scope**: Sorting/filtering/search within favorites

## Overview

This feature enables users to save breweries as favorites. Users can toggle the favorite status from the brewery detail screen via an icon button in the header, and view all their favorited breweries in the Activity screen. The data persists locally using Room database.

---

## Phase 1: Database Layer - FavoriteBrewery Entity and DAO

**Goal**: Create the Room entity and DAO for storing favorite breweries.

### Files to Create/Modify

| File | Purpose |
|------|---------|
| `core/database/src/commonMain/kotlin/com/brewery/searcher/core/database/entity/FavoriteBreweryEntity.kt` | **Create** - New entity storing favorite brewery data |
| `core/database/src/commonMain/kotlin/com/brewery/searcher/core/database/dao/FavoriteBreweryDao.kt` | **Create** - New DAO with CRUD operations |
| `core/database/src/commonMain/kotlin/com/brewery/searcher/core/database/BrewerySearcherDatabase.kt` | **Modify** - Add new entity and DAO, bump version to 2 |
| `core/database/src/commonMain/kotlin/com/brewery/searcher/core/database/di/DatabaseModule.kt` | **Modify** - Register FavoriteBreweryDao in Koin |

### Implementation Details

**FavoriteBreweryEntity.kt**:
```kotlin
package com.brewery.searcher.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_breweries")
data class FavoriteBreweryEntity(
    @PrimaryKey
    val breweryId: String,
    val name: String,
    val breweryType: String,
    val city: String?,
    val stateProvince: String?,
    val country: String?,
    val addedAt: Long,
)
```

**FavoriteBreweryDao.kt**:
```kotlin
package com.brewery.searcher.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.brewery.searcher.core.database.entity.FavoriteBreweryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteBreweryDao {

    @Query("SELECT * FROM favorite_breweries ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteBreweryEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_breweries WHERE breweryId = :breweryId)")
    fun isFavorite(breweryId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_breweries WHERE breweryId = :breweryId)")
    suspend fun isFavoriteSync(breweryId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteBreweryEntity)

    @Query("DELETE FROM favorite_breweries WHERE breweryId = :breweryId")
    suspend fun deleteById(breweryId: String)

    @Query("DELETE FROM favorite_breweries")
    suspend fun clearAll()
}
```

**BrewerySearcherDatabase.kt changes**:
- Add `FavoriteBreweryEntity::class` to entities array
- Bump version from 1 to 2
- Add abstract function `fun favoriteBreweryDao(): FavoriteBreweryDao`

**DatabaseModule.kt changes**:
- Add: `single { get<BrewerySearcherDatabase>().favoriteBreweryDao() }`

### End State

- Database contains `favorite_breweries` table
- `FavoriteBreweryDao` is accessible via Koin DI
- Database migrates cleanly (using existing destructive migration fallback)

---

## Phase 2: Repository Layer - FavoriteBreweryRepository

**Goal**: Create repository interface and implementation to abstract database access and provide domain models.

### Files to Create/Modify

| File | Purpose |
|------|---------|
| `core/data/src/commonMain/kotlin/com/brewery/searcher/core/data/repository/FavoriteBreweryRepository.kt` | **Create** - Interface defining favorite operations |
| `core/data/src/commonMain/kotlin/com/brewery/searcher/core/data/repository/FavoriteBreweryRepositoryImpl.kt` | **Create** - Implementation with DAO and mappers |
| `core/data/src/commonMain/kotlin/com/brewery/searcher/core/data/di/DataModule.kt` | **Modify** - Register repository in Koin |

### Implementation Details

**FavoriteBreweryRepository.kt**:
```kotlin
package com.brewery.searcher.core.data.repository

import com.brewery.searcher.core.model.Brewery
import kotlinx.coroutines.flow.Flow

interface FavoriteBreweryRepository {

    fun getAllFavorites(): Flow<List<Brewery>>

    fun isFavorite(breweryId: String): Flow<Boolean>

    suspend fun addFavorite(brewery: Brewery)

    suspend fun removeFavorite(breweryId: String)

    suspend fun toggleFavorite(brewery: Brewery): Boolean
}
```

**FavoriteBreweryRepositoryImpl.kt**:
```kotlin
package com.brewery.searcher.core.data.repository

import com.brewery.searcher.core.database.dao.FavoriteBreweryDao
import com.brewery.searcher.core.database.entity.FavoriteBreweryEntity
import com.brewery.searcher.core.model.Brewery
import com.brewery.searcher.core.model.BreweryType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class FavoriteBreweryRepositoryImpl(
    private val favoriteBreweryDao: FavoriteBreweryDao,
) : FavoriteBreweryRepository {

    companion object {
        val TAG = FavoriteBreweryRepositoryImpl::class.simpleName
    }

    override fun getAllFavorites(): Flow<List<Brewery>> {
        Napier.d(tag = TAG) { "getAllFavorites()" }
        return favoriteBreweryDao.getAllFavorites().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun isFavorite(breweryId: String): Flow<Boolean> {
        return favoriteBreweryDao.isFavorite(breweryId)
    }

    override suspend fun addFavorite(brewery: Brewery) {
        Napier.d(tag = TAG) { "addFavorite(breweryId=${brewery.id})" }
        favoriteBreweryDao.insert(brewery.toEntity())
    }

    override suspend fun removeFavorite(breweryId: String) {
        Napier.d(tag = TAG) { "removeFavorite(breweryId=$breweryId)" }
        favoriteBreweryDao.deleteById(breweryId)
    }

    override suspend fun toggleFavorite(brewery: Brewery): Boolean {
        val isFavorited = favoriteBreweryDao.isFavoriteSync(brewery.id)
        if (isFavorited) {
            removeFavorite(brewery.id)
            Napier.d(tag = TAG) { "Removed ${brewery.name} from favorites" }
        } else {
            addFavorite(brewery)
            Napier.d(tag = TAG) { "Added ${brewery.name} to favorites" }
        }
        return !isFavorited
    }

    private fun FavoriteBreweryEntity.toDomain(): Brewery {
        return Brewery(
            id = breweryId,
            name = name,
            breweryType = try {
                BreweryType.valueOf(breweryType)
            } catch (e: IllegalArgumentException) {
                BreweryType.UNKNOWN
            },
            address = null,
            city = city,
            stateProvince = stateProvince,
            postalCode = null,
            country = country,
            longitude = null,
            latitude = null,
            phone = null,
            websiteUrl = null,
        )
    }

    private fun Brewery.toEntity(): FavoriteBreweryEntity {
        return FavoriteBreweryEntity(
            breweryId = id,
            name = name,
            breweryType = breweryType.name,
            city = city,
            stateProvince = stateProvince,
            country = country,
            addedAt = Clock.System.now().toEpochMilliseconds(),
        )
    }
}
```

**DataModule.kt changes**:
- Add: `single<FavoriteBreweryRepository> { FavoriteBreweryRepositoryImpl(get()) }`

### End State

- `FavoriteBreweryRepository` is available via Koin for ViewModels
- Mappers convert between Entity and Domain models
- Reactive flows expose favorites list and individual favorite status

---

## Phase 3: Brewery Detail Screen - Favorite Toggle Button

**Goal**: Add favorite icon button to brewery detail screen header and wire up toggle functionality.

### Files to Modify

| File | Purpose |
|------|---------|
| `feature/home/src/commonMain/kotlin/com/brewery/searcher/feature/home/BreweryDetailViewModel.kt` | Add favorite state and toggle action |
| `feature/home/src/commonMain/kotlin/com/brewery/searcher/feature/home/BreweryDetailScreen.kt` | Add favorite icon button to HeaderSection |

### Implementation Details

**BreweryDetailViewModel.kt changes**:

1. Accept `breweryId: String` as constructor parameter (not loaded via LaunchedEffect)
2. Add `FavoriteBreweryRepository` dependency
3. Use `stateIn` for `isFavorite` StateFlow (reactive, separate from uiState)
4. Load brewery in `init` block
5. Add `toggleFavorite()` function

```kotlin
sealed interface BreweryDetailUiState {
    data object Loading : BreweryDetailUiState
    data class Success(val brewery: Brewery) : BreweryDetailUiState
    data class Error(val message: String) : BreweryDetailUiState
}

class BreweryDetailViewModel(
    private val breweryId: String,
    private val breweryRepository: BreweryRepository,
    private val favoriteBreweryRepository: FavoriteBreweryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BreweryDetailUiState>(BreweryDetailUiState.Loading)
    val uiState: StateFlow<BreweryDetailUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = favoriteBreweryRepository
        .isFavorite(breweryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    init {
        loadBrewery()
    }

    private fun loadBrewery() {
        viewModelScope.launch {
            _uiState.value = BreweryDetailUiState.Loading
            try {
                val brewery = breweryRepository.getBreweryById(breweryId)
                _uiState.value = BreweryDetailUiState.Success(brewery)
            } catch (e: ApiException) {
                _uiState.value = BreweryDetailUiState.Error(e.userMessage)
            } catch (e: Exception) {
                _uiState.value = BreweryDetailUiState.Error("Failed to load brewery")
            }
        }
    }

    fun retry() {
        loadBrewery()
    }

    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState !is BreweryDetailUiState.Success) return
        viewModelScope.launch {
            favoriteBreweryRepository.toggleFavorite(currentState.brewery)
        }
    }
}
```

**HomeModule.kt changes**:
```kotlin
viewModel { params ->
    BreweryDetailViewModel(
        breweryId = params.get(),
        breweryRepository = get(),
        favoriteBreweryRepository = get(),
    )
}
```

**HomeEntryProvider.kt changes**:
```kotlin
entry<BreweryDetailNavKey> { navKey ->
    val viewModel: BreweryDetailViewModel = koinViewModel(
        key = navKey.breweryId,
        parameters = { parametersOf(navKey.breweryId) }
    )
    BreweryDetailScreen(
        onBackClick = { navigator.goBack() },
        viewModel = viewModel,
    )
}
```

**BreweryDetailScreen.kt changes**:

1. Remove `breweryId` parameter
2. Receive `viewModel: BreweryDetailViewModel` as parameter (not using `koinViewModel()` inside)
3. Remove `LaunchedEffect` that called `loadBrewery`
4. Collect `isFavorite` separately from `uiState`
5. Add `IconButton` in `HeaderSection` (right side of Row)

```kotlin
@Composable
fun BreweryDetailScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BreweryDetailViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()
    // ... rest of implementation
}
```

### End State

- Brewery detail screen shows heart icon in header
- Icon is filled/red when brewery is favorited
- Icon is outlined/gray when not favorited
- Tapping icon toggles favorite status and persists to database
- No race conditions when navigating between breweries (each ViewModel has its own breweryId)

---

## Phase 4: Activity Screen - Display Favorites List

**Goal**: Replace placeholder Activity screen with a functional favorites list UI.

### Files to Modify

| File | Purpose |
|------|---------|
| `feature/activity/build.gradle.kts` | Add required dependencies |
| `feature/activity/src/commonMain/kotlin/com/brewery/searcher/feature/activity/ActivityViewModel.kt` | Load and expose favorites list |
| `feature/activity/src/commonMain/kotlin/com/brewery/searcher/feature/activity/ActivityScreen.kt` | Display favorites with navigation |
| `feature/activity/src/commonMain/kotlin/com/brewery/searcher/feature/activity/di/ActivityModule.kt` | Update DI registration |
| `feature/activity/src/commonMain/kotlin/com/brewery/searcher/feature/activity/navigation/ActivityEntryProvider.kt` | Add navigation callback |

### Implementation Details

**build.gradle.kts changes**:
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.designsystem)
            implementation(projects.feature.home) // For BreweryDetailNavKey
        }
    }
}
```

**ActivityViewModel.kt** (reactive approach using `stateIn`):
```kotlin
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
```

**ActivityScreen.kt**:
```kotlin
package com.brewery.searcher.feature.activity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brewery.searcher.core.designsystem.component.BreweryListItem
import com.brewery.searcher.core.designsystem.component.BreweryTopBar
import com.brewery.searcher.core.model.Brewery
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ActivityScreen(
    onBreweryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            BreweryTopBar(title = "Favorites")
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        when (val state = uiState) {
            is ActivityUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            is ActivityUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            is ActivityUiState.Success -> {
                if (state.favorites.isEmpty()) {
                    EmptyFavoritesContent(modifier = Modifier.padding(innerPadding))
                } else {
                    FavoritesList(
                        favorites = state.favorites,
                        onBreweryClick = onBreweryClick,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No favorites yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the heart icon on any brewery to add it to your favorites",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FavoritesList(
    favorites: List<Brewery>,
    onBreweryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = favorites,
            key = { it.id },
        ) { brewery ->
            BreweryListItem(
                brewery = brewery,
                onClick = { onBreweryClick(brewery.id) },
            )
        }
    }
}
```

**ActivityEntryProvider.kt changes**:
```kotlin
package com.brewery.searcher.feature.activity.navigation

import androidx.navigation3.runtime.EntryProviderScope
import com.brewery.searcher.core.navigation.NavKey
import com.brewery.searcher.core.navigation.Navigator
import com.brewery.searcher.feature.activity.ActivityScreen
import com.brewery.searcher.feature.home.navigation.BreweryDetailNavKey

fun EntryProviderScope<NavKey>.activityEntry(
    navigator: Navigator,
) {
    entry<ActivityNavKey> {
        ActivityScreen(
            onBreweryClick = { breweryId ->
                navigator.navigate(BreweryDetailNavKey(breweryId))
            },
        )
    }
}
```

### End State

- Activity screen shows "Favorites" title in top bar
- Loading state displays spinner
- Empty state shows icon with helpful message
- Favorites list displays brewery cards using existing `BreweryListItem`
- Tapping a brewery navigates to brewery detail screen

---

## Phase 5: App Integration

**Goal**: Wire up Activity entry provider with the navigator.

### Files to Modify

| File | Purpose |
|------|---------|
| `composeApp/src/commonMain/kotlin/com/brewery/searcher/App.kt` | Update activityEntry call to pass navigator |

### Implementation Details

Update the `entryProvider` block in App.kt:
```kotlin
val entryProvider = entryProvider {
    homeEntry(navigator)
    exploreEntry()
    activityEntry(navigator)  // Changed from activityEntry()
    settingsEntry()
}
```

### End State

- Full navigation flow works: Activity screen -> Brewery Detail -> Back to Activity
- Favorite toggle in detail screen updates Activity screen reactively

---

## Architecture Diagram

```
+------------------+     +-------------------+     +--------------------+
|  BreweryDetail   |     |     Activity      |     |  BreweryDetail     |
|     Screen       |     |      Screen       |     |     Screen         |
|                  |     |                   |     |                    |
| [Heart Toggle]---+---->| [Favorites List]--+---->| (navigated from    |
|                  |     |                   |     |  favorites)        |
+--------+---------+     +---------+---------+     +--------------------+
         |                         |
         v                         v
+--------+---------+     +---------+---------+
|  BreweryDetail   |     |    Activity       |
|   ViewModel      |     |    ViewModel      |
+--------+---------+     +---------+---------+
         |                         |
         +------------+------------+
                      |
                      v
         +------------+------------+
         | FavoriteBreweryRepository|
         +------------+------------+
                      |
                      v
         +------------+------------+
         |   FavoriteBreweryDao    |
         +------------+------------+
                      |
                      v
         +------------+------------+
         |     Room Database       |
         |  (favorite_breweries)   |
         +-------------------------+
```

---

## Dependencies Summary

**No new external dependencies required** - all needed libraries are already in use:
- Room (core/database)
- Koin (DI)
- Napier (logging)
- kotlinx-datetime (timestamps)

**Internal module dependencies to add**:
- `feature/activity` -> `projects.core.data`
- `feature/activity` -> `projects.core.designsystem`
- `feature/activity` -> `projects.feature.home`

---

## Edge Cases Handled

1. **Empty State**: Activity screen displays icon and helpful message when no favorites
2. **Already Favorited**: `toggleFavorite` checks current state via `isFavoriteSync`
3. **Reactive Updates**: Using Flow ensures UI updates immediately when favorites change
4. **Missing Brewery Data**: Entity stores minimal info; full details fetched when navigating to detail
5. **Database Migration**: Using existing destructive migration fallback for simplicity
6. **Error Handling**: Repository catches exceptions; ViewModels expose error states to UI
7. **Duplicate Prevention**: Using `OnConflictStrategy.REPLACE` in DAO

---

## Verification Checklist

- [ ] Project builds successfully: `.\gradlew.bat build`
- [ ] App installs on emulator: `.\gradlew.bat :composeApp:installDebug`
- [ ] Search for a brewery and open detail screen
- [ ] Heart icon appears in header (outlined/gray)
- [ ] Tap heart icon - turns filled/red
- [ ] Navigate to Activity tab - brewery appears in list
- [ ] Tap brewery in list - navigates to detail screen
- [ ] Heart icon shows filled/red (still favorited)
- [ ] Tap heart icon - turns outlined/gray (removed)
- [ ] Navigate back to Activity tab - list shows empty state
- [ ] Close and reopen app - favorites persist correctly
