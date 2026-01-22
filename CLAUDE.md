# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean assembleDebug
```

## Architecture

This is a Jetpack Compose Android app following **MVI (Model-View-Intent)** architecture.

### Data Flow
```
Intent → ViewModel → UseCase → Repository → API
                ↓
            ViewState → Composable UI
```

### Key Layers

- **API Layer** (`api/`): Retrofit interfaces and response DTOs. Uses https://rickandmortyapi.com/ Rick And Morty API.
- **Database Layer** (`db/`): Room database for offline caching of "See All" results.
  - `CharacterEntity`: Room entity matching domain model
  - `RemoteKeyEntity`: Pagination state tracking for RemoteMediator
  - `CharacterDao`: PagingSource + CRUD operations
  - `RemoteKeyDao`: Remote key operations
  - `AppDatabase`: Room database definition
- **Repository Layer** (`repository/`): Maps API responses to domain models, provides `PagingData` flows. Uses dual strategy for offline support.
- **UseCase Layer** (`usecase/`): Business logic wrappers around repositories.
  - `SearchCharactersUseCase`: Query-based character search with paging
  - `GetAllCharactersUseCase`: Fetch all characters with paging
  - `GetCharacterByIdUseCase`: Single character fetch
- **ViewModel Layer** (`feature/*/viewModel/`): Manages UI state via `StateFlow<ViewState>`, handles intents.
- **Composables** (`feature/*/composables/`): UI components with MVI intents.

### Features

**Search Feature** (`feature/search/`)
- `SearchScreen.kt`: Main list UI with search bar, "See All" button, paginated results
- `SearchViewModel.kt`: Uses `flatMapLatest` to switch between modes (Idle, SeeAll, Search)
- `SearchIntent`: Sealed interface with `QueryChanged`, `SubmitSearch`, `SeeAll` intents
- `SearchViewState`: Data class with query state

**Detail Feature** (`feature/detail/`)
- `DetailScreen.kt`: Character details in Info and Location cards
- `DetailViewModel.kt`: Fetches single character by ID via `SavedStateHandle`
- `DetailShimmerContent.kt`: Skeleton loader with shimmer animation
- `DetailViewState`: Sealed interface (Loading, Success, Error)

### Navigation

Type-safe navigation using `@Serializable` sealed classes in `navigation/NavRoutes.kt`:
- `Search`: Object route for search screen
- `CharacterDetail(characterId: Int)`: Data class route for detail screen

NavHost defined in `MainActivity.kt` with `SharedTransitionLayout` wrapper.

### Shared Element Transitions

Uses `SharedTransitionLayout` (experimental API) for character avatar animations:
- Wraps NavHost in MainActivity
- Passed to composables as `sharedTransitionScope` and `animatedVisibilityScope`
- Applied via `sharedElement()` modifier with key `"avatar-${item.id}"`

### Paging & Offline Support

Implements Paging 3 library with dual strategy for offline support:

**"See All" Mode (Offline-capable):**
- Uses `CharacterRemoteMediator` with Room as single source of truth
- `RemoteMediator` fetches from network → writes to Room → Room `PagingSource` emits to UI
- Data persists across app restarts, works offline after initial load

**Search Mode (Network-only):**
- Uses `CharacterPagingSource` for direct API pagination
- No caching (search results are transient)

Key files:
- `CharacterPagingSource.kt`: Network-only PagingSource for search
- `CharacterRemoteMediator.kt`: Coordinates network fetches with Room writes
- `SearchRepositoryImpl`: Dual strategy based on `query == null` (See All) vs `query != null` (Search)
- ViewModels expose `Flow<PagingData<Character>>`

### UI Components

- `ShimmerEffect.kt` (`ui/components/`): Modifier extension for diagonal shimmer animation
- `SearchResultRow`: Character card with avatar and info
- `InfoCard`: Reusable key-value display card

### Dependency Injection

Uses **Dagger Hilt** for DI. Modules defined in `di/`:
- `NetworkModule.kt`: Provides Moshi, Retrofit, and CharacterSearchApi
- `DatabaseModule.kt`: Provides Room database, CharacterDao, and RemoteKeyDao
- `RepositoryModule.kt`: Binds SearchRepository interface to implementation

Key annotations:
- `@HiltAndroidApp` on Application class
- `@AndroidEntryPoint` on Activity
- `@HiltViewModel` + `@Inject` on ViewModels
- `@Inject constructor` on repositories and use cases

### Theme

Custom dark Rick and Morty theme in `ui/theme/`. Always uses dark color scheme:
- Primary: `#01889F` (Teal)
- Secondary: `#DAA520` (Goldenrod/Yellow accent)
- Background: `#0A0A0A` (Near black)
- Surface: `#1A1A1A` (Dark gray)
