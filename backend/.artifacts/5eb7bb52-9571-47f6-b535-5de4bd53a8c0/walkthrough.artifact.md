# Walkthrough - Issue #17: Filtros básicos de lugares por categoría

Successfully implemented category filtering for places in both the Spring Boot backend and the Android application (`ExplorerSaga-Extremadura`), maintaining robust compatibility and seamless fallback handling.

## Changes Made

### 1. Backend Category Filtering (`PlaceRepository.java`, `PlaceService.java`, `PlaceController.java`)
- **`PlaceRepository`**: Added Spring Data query method `findByCategoryNameIgnoreCase(String name)`.
- **`PlaceRepositoryPort` & `JpaPlaceRepositoryAdapter`**: Exposed category filtering through output ports and persistence adapters.
- **`PlaceUseCase` & `PlaceService`**: Overloaded `listPlaces(String category)` to support optional category filtering efficiently at the database level.
- **`PlaceController`**: Updated `GET /api/v1/places` to accept an optional `@RequestParam(required = false) String category` query parameter.

### 2. Android Retrofit & Repository Integration (`ExplorerSagaApi.kt`, `PlaceRepository.kt`)
- **`ExplorerSagaApi`**: Added `@Query("category") category: String? = null` to `getPlaces()`.
- **`PlaceRepository`**: Updated `getPlacesResult(category)` and `getPlacesByCategories` to forward category queries to the backend API, preserving local JSON asset filtering as a resilient fallback.

## Verification Results

### Automated Tests & Build
- Executed `.\mvnw.cmd clean verify` successfully.
- **BUILD SUCCESS**
- **36/36 backend tests passed successfully**.
