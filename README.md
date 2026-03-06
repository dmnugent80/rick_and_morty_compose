# Rick and Morty Compose

A modern Android app for browsing Rick and Morty characters, built with Jetpack Compose and following MVI architecture.

<img width="264" height="600" alt="Screenshot" src="https://github.com/user-attachments/assets/0774aa70-66ca-467e-bd4e-31c1de1676da" />

## Features

- Browse all characters with infinite scrolling
- Search characters by name
- Character detail view with shared element transitions
- Offline support with local caching
- Dark theme inspired by the show

## Architecture

### Module Structure

```
rick_and_morty_compose/
├── app/                              # Application entry point
│   ├── RickAndMortyApplication.kt    # @HiltAndroidApp, ImageLoaderFactory
│   ├── MainActivity.kt               # @AndroidEntryPoint, Navigation host
│   └── navigation/
│       └── NavRoutes.kt              # Type-safe navigation routes
│
├── core/
│   ├── domain/                       # Pure Kotlin module (no Android dependencies)
│   │   ├── model/
│   │   │   └── Character.kt          # Domain model
│   │   ├── repository/
│   │   │   └── SearchRepository.kt   # Repository interface
│   │   ├── network/
│   │   │   └── ConnectivityObserver.kt  # Connectivity interface
│   │   └── usecase/
│   │       ├── GetAllCharactersUseCase.kt
│   │       ├── GetCharacterByIdUseCase.kt
│   │       └── SearchCharactersUseCase.kt
│   │
│   ├── data/                         # Data layer implementation
│   │   ├── api/
│   │   │   ├── CharacterSearchApi.kt # Retrofit interface
│   │   │   └── RetryInterceptor.kt   # OkHttp retry logic
│   │   ├── db/
│   │   │   ├── AppDatabase.kt        # Room database
│   │   │   ├── dao/
│   │   │   │   ├── CharacterDao.kt
│   │   │   │   └── RemoteKeyDao.kt
│   │   │   └── entity/
│   │   │       ├── CharacterEntity.kt
│   │   │       └── RemoteKeyEntity.kt
│   │   ├── network/
│   │   │   └── NetworkConnectivityObserver.kt
│   │   ├── paging/
│   │   │   ├── CharacterPagingSource.kt     # Network-only paging
│   │   │   └── CharacterRemoteMediator.kt   # Offline-capable paging
│   │   ├── repository/
│   │   │   └── SearchRepositoryImpl.kt
│   │   └── di/
│   │       ├── NetworkModule.kt
│   │       ├── DatabaseModule.kt
│   │       └── RepositoryModule.kt
│   │
│   └── ui/                           # Shared UI components
│       ├── components/
│       │   └── ShimmerEffect.kt
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
│
└── feature/
    ├── search/                       # Search feature
    │   ├── SearchScreen.kt
    │   ├── SearchViewModel.kt
    │   └── SearchViewState.kt
    │
    └── detail/                       # Detail feature
        ├── DetailScreen.kt
        ├── DetailViewModel.kt
        ├── DetailViewState.kt
        └── DetailShimmerContent.kt
```

### Module Dependencies

```
                    ┌─────────────┐
                    │     app     │
                    └──────┬──────┘
                           │
           ┌───────────────┼───────────────┐
           │               │               │
           ▼               ▼               ▼
    ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
    │   feature/  │ │   feature/  │ │   core/ui   │
    │   search    │ │   detail    │ │             │
    └──────┬──────┘ └──────┬──────┘ └─────────────┘
           │               │
           └───────┬───────┘
                   │
                   ▼
           ┌─────────────┐
           │  core/data  │
           └──────┬──────┘
                  │
                  ▼
          ┌─────────────┐
          │ core/domain │
          └─────────────┘
```

### Data Flow (MVI)

```
Intent → ViewModel → UseCase → Repository → API/Database
                 ↓
          ViewState → Composable UI
```

## Data Layer Implementation

### Networking

The app uses **Retrofit** with **OkHttp** and **Moshi** for JSON parsing:

- **Base URL**: `https://rickandmortyapi.com/api/`
- **OkHttpClient** configured with:
  - 50MB HTTP cache for offline image/response caching
  - `RetryInterceptor`: Exponential backoff with 3 retries, 1s base delay + jitter
  - 15s connect timeout, 30s read/write timeouts

### Infinite Scrolling with Paging 3

The app implements **Paging 3** with a dual strategy for offline support:

#### "See All" Mode (Offline-capable)

Uses `RemoteMediator` pattern with Room as single source of truth:

```
┌──────────────┐     ┌───────────────────────┐     ┌──────────────┐
│   Pager      │────▶│ CharacterRemoteMediator│────▶│    API       │
└──────┬───────┘     └───────────┬───────────┘     └──────────────┘
       │                         │
       │                         ▼ (writes to)
       │             ┌───────────────────────┐
       │             │    Room Database      │
       │             │  ┌─────────────────┐  │
       │             │  │ CharacterEntity │  │
       └────────────▶│  │ RemoteKeyEntity │  │
         (reads from)│  └─────────────────┘  │
                     └───────────────────────┘
```

- `CharacterRemoteMediator` fetches pages from API and writes to Room
- Room `PagingSource` emits data to UI
- Data persists across app restarts
- Works offline after initial load (graceful degradation)

#### Search Mode (Network-only)

Uses direct `PagingSource` for transient search results:

```
┌──────────────┐     ┌───────────────────────┐     ┌──────────────┐
│   Pager      │────▶│  CharacterPagingSource │────▶│    API       │
└──────────────┘     └───────────────────────┘     └──────────────┘
```

- `CharacterPagingSource` fetches directly from API
- No local caching (search results are ephemeral)

### Offline Support

**Connectivity Monitoring:**
- `ConnectivityObserver` interface in `core/domain`
- `NetworkConnectivityObserver` implementation using Android `ConnectivityManager` with `NetworkCallback`
- Exposes `isOnline: StateFlow<Boolean>` for reactive UI updates

**Offline Behavior:**
- "See All" mode works offline using cached Room data
- Detail page loads from Room cache when offline (if previously viewed)
- Search disabled offline (shows offline banner)
- Images served from OkHttp cache when offline (via Coil integration)

### Image Loading

Uses **Coil** with shared OkHttpClient:
- `RickAndMortyApplication` implements `ImageLoaderFactory`
- Shares the same OkHttpClient (with 50MB cache) used for API requests
- Images cached automatically via HTTP cache headers

## Tech Stack

- **UI**: Jetpack Compose, Material 3
- **Architecture**: MVI (Model-View-Intent)
- **DI**: Dagger Hilt
- **Networking**: Retrofit, OkHttp, Moshi
- **Database**: Room
- **Pagination**: Paging 3
- **Image Loading**: Coil
- **Navigation**: Compose Navigation with type-safe routes
- **Async**: Kotlin Coroutines & Flow

## Building

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## API

Data provided by the [Rick and Morty API](https://rickandmortyapi.com/).
