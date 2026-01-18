# Brewery Explorer – Take Home Task

## Features

### 1. Browse & Search Breweries
- Users can browse a paginated list of breweries.
- Search is available including search history.
- Supported search parameters:
    - All Fields
    - Country
    - State
    - City
- implemented using pagination 3.
- Error handling: displaying errors gracefully to the user. can be improved by constantly monitoring the internet connection.

**Extra feature**
- Implemented a **search history** feature.
- Search queries are saved only when the user presses the **“Done”** button on the keyboard.
- Search history is persisted locally using Room db.

---

### 2. Map Experience
- Users can enter the Explore screen and choose whether to grant location permissions.
- If permission is granted, the map searches for breweries near the user’s current location, otherwise fallback to the US.
- Map interactions:
    - Selecting a brewery marker displays its details in a bottom sheet.
    - Selecting a brewery from the list highlights its marker on the map.
- Breweries with missing or invalid coordinates are handled gracefully and are not shown as map markers.

---

### 3. Favorites
- Users can mark breweries as favorites.
- Favorite breweries are persisted locally using **Room**.

---

## Technical Decisions & Architecture

- **Architecture**
    - MVVM + Clean Architecture with a modular design.
    - Clear separation between UI, domain, and data layers.

- **Kotlin Multiplatform (KMP)**
    - The project is structured to support a future iOS implementation.
    - Only Kotlin-based libraries were used (e.g., Koin for DI, Ktor for networking).

- **Dependency Injection**
    - Implemented using **Koin**, compatible with KMP.

- **Reactive ViewModels**
    - ViewModels follow a reactive approach.
    - Data is fetched only in response to user actions (not eagerly in `init` blocks).

- **Parallel Development**
    - Two Git worktrees were created during development.
    - This allowed working with two AI agents in parallel on separate features without interference.

---

## Permissions & Persistence

- **Location Permissions**
    - Managed using the `moko-permissions` library.
    - Used to align the map to the user’s location on first entry.

  **Known UX issue**
    - The map currently re-centers on the user’s location every time the screen is opened.
    - With more time, this would be improved by saving the last viewed map coordinates and restoring them on subsequent visits.

- **DataStore**
    - Used for:
        - Theme selection
        - “Ignore location permission dialog” flag

---

## Navigation & UI

- Built entirely with **Jetpack Compose**.
- Uses **Navigation Compose (Navigation 3)**.
- Due to time constraints:
    - No Compose `@Preview`s were added.
    - No automated tests were implemented.

---

## Documentation & AI Workflow

- The `docs/` folder contains a collection of **larger task definitions** that were created to guide the AI agents during development.
- These documents describe:
    - Feature-level goals
    - Architectural decisions
    - Step-by-step implementation plans
- They were used to:
    - Break down the task into manageable units
    - Coordinate parallel work between multiple AI agents
    - Maintain consistency in architecture and coding patterns
- The `docs/` folder is included for transparency and to provide insight into the development and decision-making process.

---

## Configuration

- The Google Maps API key is stored in `local.properties`.
- It was intentionally committed to the repository **only for evaluation convenience**, so the project can run immediately.

---

## Possible Improvements With More Time

- Add unit tests and UI tests.
- Add Compose previews for easier UI iteration.
- Improve map UX by restoring last viewed coordinates.
- Implement `RemoteMediator` for better paging cache support.
- Enhance offline support beyond favorites.
- Improve search suggestions and filtering options.

---

## Time Constraints

- This solution was implemented within the intended time frame for the task.
- Some trade-offs (testing, previews, extended caching) were consciously made to prioritize core functionality and architecture.

---

## THANK YOU!

---