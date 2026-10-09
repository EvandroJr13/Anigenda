# Project Plan

Crie uma agenda de animes, separadas por temporada e dias da semana, fácil de selecionar e visualizar. Deve utilizar uma API pública de animes (como a Jikan API v4 / MyAnimeList) para carregar dados atualizados de lançamentos por temporada (Inverno, Primavera, Verão, Outono) e por dia da semana (Segunda, Terça, Quarta, Quinta, Sexta, Sábado, Domingo). A interface deve ser moderna, limpa, intuitiva e fluida com Jetpack Compose.

## Project Brief

# Project Brief: Agenda de Animes (Anime Schedule App)

## Features
1. **Schedule by Day of the Week**: View anime releases organized by day of the week (Monday through Sunday) for convenient weekly tracking.
2. **Seasonal Anime Catalog**: Browse seasonal releases categorized by year and season (Winter, Spring, Summer, Fall).
3. **Anime Details Screen**: View essential details for any selected anime, including synopsis, genres, broadcast times, episode count, and studio.
4. **Search & Quick Filter**: Search for specific titles and filter by genre or status within the current season.

## High-Level Technical Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **Navigation**: Jetpack Navigation 3 (State-driven navigation)
- **Adaptive Strategy**: Compose Material Adaptive library (for multi-pane and responsive layouts across phones and tablets)
- **Asynchronous Execution & Data Streams**: Kotlin Coroutines & Flow
- **Architecture**: MVVM / Clean Architecture with `ViewModel`
- **Networking**: Retrofit with Kotlinx Serialization (for integration with Jikan API v4)
- **Image Loading**: Coil for Compose

## Implementation Steps
**Total Duration:** 23h 55m 38s

### Task_1_SetupNetworkAndDataLayer: Configure Retrofit with Kotlinx Serialization, Coil, and repository layers for Jikan API v4 endpoints (schedules, seasonal anime, search, and anime details).
- **Status:** COMPLETED
- **Updates:** Successfully configured Retrofit with Moshi, Coil Compose, Jikan API v4 endpoints (schedules, seasonal anime, details, search), domain models, and AnimeRepositoryImpl with Flow<Resource<T>>. Confirmed INTERNET permission and verified assembleDebug build succeeds.
- **Acceptance Criteria:**
  - Jikan API endpoints for schedules, seasonal anime, details, and search are defined
  - Data models and Retrofit service interfaces are implemented
  - Repository and data layer setup compiles without errors
- **Duration:** 31m 17s

### Task_2_ScheduleAndSeasonScreens: Build UI screens for Weekly Schedule (by day of week) and Seasonal Anime Catalog using Jetpack Compose and Coil image loading.
- **Status:** COMPLETED
- **Updates:** Created ScheduleViewModel and SeasonalViewModel, AnimeCard component with Coil SubcomposeAsyncImage, ScheduleScreen with day-of-week selection chips, SeasonalScreen with season/year selection, adaptive grid layouts for phone/tablet, shimmer loading skeletons, error views, and unit tests. Verified clean build with assembleDebug.
- **Acceptance Criteria:**
  - Weekly schedule screen lists anime by day of the week with Coil images
  - Seasonal catalog screen allows browsing anime by season and year
  - UI responsive layout is implemented for phone/tablet
- **Duration:** 17m 18s

### Task_3_DetailsAndSearchFeatures: Build Details Screen and Search/Filter functionality with Jetpack Compose, integrating state-driven navigation between lists and details.
- **Status:** COMPLETED
- **Updates:** Implemented DetailsViewModel & DetailsScreen with poster, stats, synopsis, genres, broadcast time, and studio. Created SearchViewModel & SearchScreen with debounced search and genre/status filters. Built MainApp navigation structure connecting Schedule, Seasonal, Search, and Details screens. Updated MainActivity. All 15 unit tests passed and assembleDebug built successfully.
- **Acceptance Criteria:**
  - Details screen displays anime synopsis, genres, broadcast time, episode count, and studio
  - Search bar and quick filters by genre or status work accurately with Jikan API
  - Navigation between Schedule, Seasonal, Search, and Details screens functions smoothly
- **Duration:** 21h 49m 41s

### Task_4_RunAndVerify: Build project, execute app, and verify application stability, requirement alignment, and UI quality. Instruct critic_agent to verify stability and report any issues.
- **Status:** COMPLETED
- **Updates:** Successfully verified compilation, executed all unit tests (100% passing), installed debug variant on device via Wi-Fi, and launched MainActivity successfully using Jikan API v4.
- **Acceptance Criteria:**
  - build pass
  - app does not crash
  - make sure all existing tests pass
  - critic_agent verifies application stability, user requirements alignment, and reports UI issues
- **Duration:** 1h 17m 22s

