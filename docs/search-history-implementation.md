# Search History Feature Implementation Plan

## Scope

**In Scope:** Android implementation only
**Out of Scope:** iOS implementation (stub files only for compilation)

---

## Overview

Add search history functionality to BrewerySearcher using Room database with:
- Save query when user presses "done" key on keyboard
- Display history list when search input is empty
- Delete via X button or swipe gesture
- "Clear all" button to remove entire history
- Auto-limit to last 20 searches
- Skip duplicate consecutive searches

---

## Implementation Phases

### Phase 1: Gradle Dependencies
**Goal:** Add Room database dependencies to the project.

**Files to modify:**
| File | Change |
|------|--------|
| `gradle/libs.versions.toml` | Add Room, KSP, SQLite, kotlinx-datetime versions and libraries |
| `build-logic/convention/build.gradle.kts` | Add KSP and Room plugin dependencies |

**Dependencies to add in `gradle/libs.versions.toml`:**
```toml
[versions]
room = "2.7.1"
ksp = "2.3.0-1.0.31"
sqlite = "2.5.1"
kotlinx-datetime = "0.6.2"

[libraries]
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```

**End state:** Gradle sync succeeds with new dependencies available.

---

### Phase 2: Database Layer
**Goal:** Set up Room database with SearchHistory entity and DAO.

**Files to create:**
| File | Purpose |
|------|---------|
| `core/database/build.gradle.kts` | Module setup with Room, KSP |
| `core/database/.../entity/SearchHistoryEntity.kt` | Room entity for search history |
| `core/database/.../dao/SearchHistoryDao.kt` | DAO with CRUD operations |
| `core/database/.../BrewerySearcherDatabase.kt` | Room database class |
| `core/database/.../DatabaseProvider.kt` | Expect declaration |
| `core/database/.../DatabaseProvider.android.kt` | Android implementation |
| `core/database/.../DatabaseProvider.ios.kt` | iOS stub (throws NotImplementedError) |
| `core/database/.../di/DatabaseModule.kt` | Koin DI module |

**Key patterns:**
```kotlin
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val searchType: String,
    val timestamp: Long,
)

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert
    suspend fun insert(searchHistory: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastSearch(): SearchHistoryEntity?

    @Query("DELETE FROM search_history WHERE id NOT IN (SELECT id FROM search_history ORDER BY timestamp DESC LIMIT 20)")
    suspend fun pruneOldEntries()
}
```

**End state:** Database module compiles. `./gradlew :core:database:assemble` succeeds.

---

### Phase 3: Domain Model
**Goal:** Create the domain model for search history.

**Files to create:**
| File | Purpose |
|------|---------|
| `core/model/.../SearchHistory.kt` | Domain model using existing SearchType enum |

**Key pattern:**
```kotlin
data class SearchHistory(
    val id: Long,
    val query: String,
    val searchType: SearchType,
    val timestamp: Long,
)
```

**End state:** Domain model compiles and can be imported by other modules.

---

### Phase 4: Repository Layer
**Goal:** Create repository to abstract database operations.

**Files to create:**
| File | Purpose |
|------|---------|
| `core/data/.../repository/SearchHistoryRepository.kt` | Repository interface |
| `core/data/.../repository/SearchHistoryRepositoryImpl.kt` | Implementation with duplicate detection |

**Files to modify:**
| File | Change |
|------|--------|
| `core/data/build.gradle.kts` | Add `implementation(projects.core.database)` |
| `core/data/.../di/DataModule.kt` | Register SearchHistoryRepository |

**Key patterns:**
```kotlin
interface SearchHistoryRepository {
    fun getRecentSearches(): Flow<List<SearchHistory>>
    suspend fun saveSearch(query: String, searchType: SearchType)
    suspend fun deleteSearch(id: Long)
    suspend fun clearAll()
}

class SearchHistoryRepositoryImpl(
    private val searchHistoryDao: SearchHistoryDao,
) : SearchHistoryRepository {

    override suspend fun saveSearch(query: String, searchType: SearchType) {
        // Skip if same as last search
        val lastSearch = searchHistoryDao.getLastSearch()
        if (lastSearch?.query == query && lastSearch.searchType == searchType.name) {
            return
        }
        searchHistoryDao.insert(SearchHistoryEntity(query = query, searchType = searchType.name, timestamp = Clock.System.now().toEpochMilliseconds()))
        searchHistoryDao.pruneOldEntries()
    }
}
```

**End state:** Repository compiles. `./gradlew :core:data:assemble` succeeds.

---

### Phase 5: ViewModel Layer
**Goal:** Update SearchViewModel with history management.

**File to modify:** `feature/home/.../SearchViewModel.kt`

**Changes:**
1. Inject `SearchHistoryRepository`
2. Add `searchHistory: StateFlow<List<SearchHistory>>`
3. Add `onSearchSubmit()` - saves to history when "done" pressed
4. Add `onHistoryItemClick(item)` - populates query and type
5. Add `onDeleteHistoryItem(id)` - deletes single item
6. Add `onClearHistory()` - clears all history

**Key patterns:**
```kotlin
class SearchViewModel(
    private val breweryRepository: BreweryRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {

    val searchHistory: StateFlow<List<SearchHistory>> = searchHistoryRepository
        .getRecentSearches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchSubmit() {
        val query = _searchQuery.value.trim()
        if (query.length >= 3) {
            viewModelScope.launch {
                searchHistoryRepository.saveSearch(query, _searchType.value)
            }
        }
    }

    fun onHistoryItemClick(item: SearchHistory) {
        _searchQuery.value = item.query
        _searchType.value = item.searchType
    }

    fun onDeleteHistoryItem(id: Long) {
        viewModelScope.launch { searchHistoryRepository.deleteSearch(id) }
    }

    fun onClearHistory() {
        viewModelScope.launch { searchHistoryRepository.clearAll() }
    }
}
```

**End state:** ViewModel compiles with history support. `./gradlew :feature:home:assemble` succeeds.

---

### Phase 6: UI Layer
**Goal:** Add search history UI with keyboard done action, history list, delete options.

**File to modify:** `feature/home/.../SearchScreen.kt`

**Changes:**
1. Collect `searchHistory` from ViewModel
2. Add `keyboardActions` with `onDone` to OutlinedTextField
3. Show history list when query is empty AND history exists
4. Create `SearchHistoryList` composable with header + clear all button
5. Create `SearchHistoryItem` with X button and SwipeToDismissBox

**Key patterns:**
```kotlin
// SearchBar with keyboard done action
OutlinedTextField(
    value = query,
    onValueChange = onQueryChange,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(onDone = { onSearchSubmit() }),
)

// History item with delete options
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchHistoryItem(
    item: SearchHistory,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false }
    )
    SwipeToDismissBox(state = dismissState, backgroundContent = { /* Delete icon */ }) {
        Row {
            Icon(Icons.Default.History)
            Text(item.query)
            AssistChip(label = { Text(item.searchType.displayName) })
            IconButton(onClick = onDelete) { Icon(Icons.Default.Close) }
        }
    }
}
```

**End state:** UI shows history list when query empty, saves on "done", deletes via X or swipe.

---

### Phase 7: DI Integration
**Goal:** Wire up database module to app.

**Files to modify:**
| File | Change |
|------|--------|
| `feature/home/.../di/HomeModule.kt` | Add SearchHistoryRepository to SearchViewModel |
| `composeApp/.../di/AppModule.kt` | Add `databaseModule` to module list |
| `composeApp/.../BrewerySearcherApplication.kt` | Call `initializeDatabase(this)` before `initKoin` |

**End state:** App compiles and runs with all dependencies injected.

---

## Verification Checklist

- [ ] App builds without errors: `./gradlew build`
- [ ] App launches on Android emulator
- [ ] Navigate to Search screen
- [ ] Type "Stone" and press Done → history entry saved
- [ ] Clear search input → history list appears with "Stone" entry
- [ ] Tap history item → query and type populated, search executed
- [ ] Tap X on history item → item deleted
- [ ] Swipe left on history item → item deleted
- [ ] Tap "Clear all" → all history removed, placeholder returns
- [ ] Close and reopen app → history persists
- [ ] Search same query twice → no duplicate entry created

---

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        feature:home                              │
│  ┌─────────────────┐     ┌──────────────────────────────────┐   │
│  │  SearchScreen   │────▶│        SearchViewModel           │   │
│  │  (Compose UI)   │     │  - searchQuery: StateFlow        │   │
│  │                 │     │  - searchType: StateFlow         │   │
│  │  - TextField    │     │  - searchHistory: StateFlow      │   │
│  │  - HistoryList  │     │  - searchResults: Flow<Paging>   │   │
│  │  - SwipeToDel   │     └──────────────┬───────────────────┘   │
│  └─────────────────┘                    │                       │
└─────────────────────────────────────────┼───────────────────────┘
                                          │
┌─────────────────────────────────────────┼───────────────────────┐
│                        core:data        │                        │
│  ┌──────────────────────────────────────▼───────────────────┐   │
│  │            SearchHistoryRepository                        │   │
│  │  - getRecentSearches(): Flow<List<SearchHistory>>        │   │
│  │  - saveSearch(query, type)                               │   │
│  │  - deleteSearch(id) / clearAll()                         │   │
│  └──────────────────────────────────────┬───────────────────┘   │
└─────────────────────────────────────────┼───────────────────────┘
                                          │
┌─────────────────────────────────────────┼───────────────────────┐
│                      core:database      │                        │
│  ┌──────────────────────────────────────▼───────────────────┐   │
│  │              SearchHistoryDao                             │   │
│  │  - getRecentSearches(): Flow<List<Entity>>               │   │
│  │  - insert() / deleteById() / clearAll()                  │   │
│  │  - getLastSearch() / pruneOldEntries()                   │   │
│  └──────────────────────────────────────┬───────────────────┘   │
│                                         │                       │
│  ┌──────────────────────────────────────▼───────────────────┐   │
│  │           BrewerySearcherDatabase (Room)                 │   │
│  │              search_history table                         │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Files Summary

| Action | File Path |
|--------|-----------|
| Update | `gradle/libs.versions.toml` |
| Update | `build-logic/convention/build.gradle.kts` |
| Update | `core/database/build.gradle.kts` |
| Create | `core/database/src/commonMain/.../entity/SearchHistoryEntity.kt` |
| Create | `core/database/src/commonMain/.../dao/SearchHistoryDao.kt` |
| Create | `core/database/src/commonMain/.../BrewerySearcherDatabase.kt` |
| Create | `core/database/src/commonMain/.../DatabaseProvider.kt` |
| Create | `core/database/src/androidMain/.../DatabaseProvider.android.kt` |
| Create | `core/database/src/iosMain/.../DatabaseProvider.ios.kt` |
| Create | `core/database/src/commonMain/.../di/DatabaseModule.kt` |
| Create | `core/model/src/commonMain/.../SearchHistory.kt` |
| Create | `core/data/src/commonMain/.../repository/SearchHistoryRepository.kt` |
| Create | `core/data/src/commonMain/.../repository/SearchHistoryRepositoryImpl.kt` |
| Update | `core/data/build.gradle.kts` |
| Update | `core/data/src/commonMain/.../di/DataModule.kt` |
| Update | `feature/home/src/commonMain/.../SearchViewModel.kt` |
| Update | `feature/home/src/commonMain/.../SearchScreen.kt` |
| Update | `feature/home/src/commonMain/.../di/HomeModule.kt` |
| Update | `composeApp/src/commonMain/.../di/AppModule.kt` |
| Update | `composeApp/src/androidMain/.../BrewerySearcherApplication.kt` |

---

## Dependencies Summary

```toml
# gradle/libs.versions.toml additions

[versions]
room = "2.7.1"
ksp = "2.3.0-1.0.31"
sqlite = "2.5.1"
kotlinx-datetime = "0.6.2"

[libraries]
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "sqlite" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
```
