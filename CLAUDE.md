# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build entire project
./gradlew build

# Build Android app only
./gradlew :composeApp:assembleDebug

# Build specific module
./gradlew :core:common:build
./gradlew :feature:home:build

# Run Android app (requires emulator/device)
./gradlew :composeApp:installDebug

# Clean build
./gradlew clean build

# Sync Gradle (after changing dependencies)
./gradlew --refresh-dependencies
```

## Architecture Overview

This is a **Kotlin Multiplatform (KMP)** project using **Compose Multiplatform** for shared UI across Android and iOS.

### Module Structure

```
BrewerySearcher/
├── composeApp/          # Main app entry point (Android Activity, iOS ViewController)
├── core/                # Shared infrastructure
│   ├── common/          # Platform utilities (isDebug expect/actual)
│   ├── navigation/      # Type-safe navigation with dual-stack system
│   ├── designsystem/    # Material3 theme, colors, typography
│   ├── datastore/       # Proto-based user preferences (Wire + DataStore)
│   ├── database/        # Local persistence (empty, ready for Room/SQLite)
│   ├── model/           # Domain models (empty)
│   └── network/         # API client (empty)
├── feature/             # Feature modules following MVVM
│   ├── home/
│   ├── explore/
│   ├── activity/
│   └── settings/
├── build-logic/         # Gradle convention plugins
└── iosApp/              # Xcode project wrapper
```

### Build-Logic Convention Plugins

All modules use convention plugins from `build-logic/convention/` instead of duplicating Gradle config:

- **`brewerysearcher.kmp.library`** - Base KMP library setup (Android + iOS targets, Compose)
- **`brewerysearcher.core`** - For core modules. Adds: Koin, Napier (as `api`)
- **`brewerysearcher.feature`** - For feature modules. Adds: Compose UI, Material3, ViewModel, core:common, core:navigation
- **`brewerysearcher.navigation`** - For navigation module. Adds: navigation3
- **`brewerysearcher.datastore`** - For datastore module. Adds: DataStore, Wire proto compiler

To add dependencies to multiple modules, prefer modifying convention plugins over individual build.gradle.kts files.

### Navigation System

Uses JetBrains Navigation3 with a dual-stack architecture:

- **NavigationState** manages `topLevelStack` (tabs) and `subStacks` (per-tab nested navigation)
- **NavKey** interface: All destinations implement this (e.g., `HomeNavKey`, `ExploreNavKey`)
- **Navigator**: Smart routing via `navigate(key)` and `goBack()`

Each feature module provides:
- `{Feature}NavKey.kt` - Serializable navigation key
- `{Feature}EntryProvider.kt` - Navigation3 DSL builder

### Dependency Injection (Koin)

Initialized in `composeApp/src/commonMain/kotlin/di/AppModule.kt`:
- Platform modules configured via `initKoin { }` block
- Android: `androidContext()` set in `BrewerySearcherApplication`
- ViewModels registered per feature module (e.g., `homeModule`, `settingsModule`)

### Data Layer Patterns

**Datastore (user preferences):**
- Proto definitions in `core/datastore/src/commonMain/proto/`
- Wire compiler generates Kotlin classes
- `UserSettingsDataSource` exposes `Flow<UserSettings>`

**Expect/Actual pattern** for platform-specific code:
- Define `expect` in `commonMain/`
- Implement `actual` in `androidMain/` and `iosMain/`
- Example: `PlatformUtils.kt` for `isDebug()`

### Key Libraries & Versions

| Library | Version | Purpose |
|---------|---------|---------|
| Kotlin | 2.3.0 | Language |
| Compose Multiplatform | 1.10.0 | Shared UI |
| Koin | 4.0.4 | Dependency injection |
| Navigation3 | 1.0.0-alpha05 | Type-safe navigation |
| Wire | 5.4.0 | Protobuf for datastore |
| Napier | 2.7.1 | Multiplatform logging |

### Source Set Organization

Each module has platform source sets:
- `commonMain/` - Shared Kotlin code
- `androidMain/` - Android-specific (BuildConfig, Context)
- `iosMain/` - iOS-specific implementations

### Adding a New Feature Module

1. Create directory: `feature/{name}/`
2. Add `build.gradle.kts`:
   ```kotlin
   plugins {
       id("brewerysearcher.feature")
   }
   android {
       namespace = "com.brewery.searcher.feature.{name}"
   }
   ```
3. Include in `settings.gradle.kts`: `include(":feature:{name}")`
4. Create source sets following existing patterns:
   - `{Name}Screen.kt`, `{Name}ViewModel.kt`
   - `navigation/{Name}NavKey.kt`, `navigation/{Name}EntryProvider.kt`
   - `di/{Name}Module.kt`
5. Register DI module in `composeApp/src/commonMain/kotlin/di/AppModule.kt`
6. Add entry provider to `App.kt` NavDisplay

### Logging

Use Napier (available in all core/feature modules). Always include a TAG for filtering:

```kotlin
import io.github.aakira.napier.Napier

Napier.d(tag = TAG) { "Debug message" }
Napier.e(tag = TAG) { "Error message" }
Napier.i(tag = TAG) { "Info message" }
Napier.w(tag = TAG) { "Warning message" }
```

When creating a new Kotlin class, always add a TAG companion object:

```kotlin
class MyNewClass {
    companion object {
        val TAG = MyNewClass::class.simpleName
    }
}
```

Only outputs in debug builds (controlled by `isDebug()` in Application class).
