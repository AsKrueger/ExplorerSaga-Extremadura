# ExplorerSaga Extremadura - Backend

Base backend implementation for the ExplorerSaga Extremadura project using Spring Boot.

## Prerequisites
- Java 17 or higher.
- Maven (or use the provided Maven Wrapper).

## Getting Started

### Build and Test
To build the project and run tests, use:
```powershell
.\mvnw.cmd clean verify
```

### Run the Application
To start the backend server:
```powershell
.\mvnw.cmd spring-boot:run
```
The application will be available at `http://localhost:8080`.

### Health Check
You can verify the application status at:
`GET http://localhost:8080/api/v1/health`

Expected response:
```json
{"status": "UP"}
```

## API Documentation (OpenAPI / Swagger)

The backend provides an interactive OpenAPI / Swagger documentation interface powered by Springdoc.

- **Base Path**: `/api/v1`
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON Spec**: `http://localhost:8080/v3/api-docs`

### Resources & Endpoints
- **Categories** (`/api/v1/categories`): Create, get by ID, and list tourist categories.
- **Sources** (`/api/v1/sources`): Manage external information sources.
- **Places** (`/api/v1/places`): Manage tourist places with geographic coordinates and categories.
- **Place Sources** (`/api/v1/place-sources`): Link internal places with external data provider identifiers (`externalId`).

## Environment Details
- **Spring Boot**: 3.3.0
- **Java Target**: 17
- **Port**: 8080
- **Context Path**: `/` (API prefixed with `/api/v1`)
