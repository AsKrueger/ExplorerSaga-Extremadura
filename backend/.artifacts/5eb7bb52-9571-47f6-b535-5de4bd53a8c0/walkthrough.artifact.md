# Walkthrough - Issue #13: Implementación del adaptador OpenStreetMap / Overpass API

Successfully implemented the first functional external data adapter for ExplorerSaga Backend, enabling tourism place ingestion from OpenStreetMap via Overpass API while respecting Hexagonal Architecture boundaries and achieving 0 € operating cost.

## Changes Made

### 1. Application Ports & Use Cases
- **`TourismDataProvider.java`** & **`NormalizedPlaceData.java`**: Output port contract and normalized data record in `com.alonso.explorersaga.application.port.out`.
- **`ImportTourismPlacesUseCase.java`** & **`ImportResult.java`**: Input port and result record in `com.alonso.explorersaga.application.port.in`.

### 2. Infrastructure Adapter (Overpass API)
- **`OverpassAdapter.java`**: Implements `TourismDataProvider`, querying Overpass API (`https://overpass-api.de/api/interpreter`) specifically for tourist attractions, monuments, and places of worship in Mérida, converting raw responses into normalized application records.
- **`OverpassResponseDto.java`**, **`OverpassElementDto.java`**, **`OverpassCenterDto.java`**: Isolated Jackson DTOs for JSON deserialization of Overpass responses.

### 3. Application Service & REST Endpoint
- **`ImportTourismPlacesService.java`**: Implements `ImportTourismPlacesUseCase`, coordinating data fetching, source resolution (`Source`), duplicate checking (`PlaceSource`), category resolution (`Category`), and persistence via output ports.
- **`PlaceController.java`**: Added `POST /api/v1/places/import` endpoint to trigger the import flow on-demand.

### 4. Configuration & Testing
- **`application-dev.yml`**: Added Overpass provider configuration parameters (base URL and timeout).
- **`ImportTourismPlacesServiceTest.java`**: Unit tests verifying the import flow and persistence delegation using mocked output ports without requiring live network connections.
- **`HEXAGONAL_ARCHITECTURE.md`**: Updated component status matrix marking **External Adapters** as *Implementado*.

## Verification Results

### Automated Tests & Build
- Executed `.\mvnw.cmd clean verify` successfully.
- **BUILD SUCCESS**
- **35/35 tests passed successfully** (including new import service unit tests and updated controller tests).
