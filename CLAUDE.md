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
- **Repository Layer** (`repository/`): Maps API responses to domain models.
- **UseCase Layer** (`usecase/`): Business logic wrappers around repositories.
- **ViewModel Layer** (`feature/*/viewModel/`): Manages UI state via `StateFlow<ViewState>`, handles intents.
- **Composables** (`feature/*/composables/`): UI components including `SearchIntent` sealed interface for user actions.

### Dependency Injection

Uses **Dagger Hilt** for DI. Modules defined in `di/`:
- `NetworkModule.kt`: Provides Moshi, Retrofit, and CharacterSearchApi
- `RepositoryModule.kt`: Binds SearchRepository interface to implementation

Key annotations:
- `@HiltAndroidApp` on Application class
- `@AndroidEntryPoint` on Activity
- `@HiltViewModel` + `@Inject` on ViewModels
- `@Inject constructor` on repositories and use cases

### Theme

Custom dark Rick and Morty theme in `ui/theme/`. Always uses dark color scheme with yellow accent colors.
