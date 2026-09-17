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

## Environment Details
- **Spring Boot**: 3.3.0
- **Java Target**: 17
- **Port**: 8080
- **Context Path**: `/` (API prefixed with `/api/v1`)
