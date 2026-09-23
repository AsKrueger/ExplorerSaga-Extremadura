# Arquitectura Hexagonal y Estrategia de Evolución (ExplorerSaga Backend)

Este documento define la estrategia de diseño, los límites arquitectónicos y el plan de migración incremental hacia **Ports & Adapters / Hexagonal Architecture** para el backend de **ExplorerSaga Extremadura**, basado en los hallazgos de la auditoría técnica de la Issue #7.

---

## 1. Estado Arquitectónico Actual vs. Objetivo

### Estado Actual (Layered Architecture - Verificado en Issue #7)
```text
HTTP Request
     ↓
REST Controllers (CategoryController, SourceController, PlaceController, PlaceSourceController)
     ↓
Application Services (CategoryService, SourceService, PlaceService, PlaceSourceService)
     ↓
Spring Data Repositories
     ↓
JPA / Hibernate Entities
     ↓
PostgreSQL 15 (Flyway)
```

### Arquitectura Objetivo (Ports & Adapters)
```text
                 ┌────────────────────────────────┐
                 │          REST Adapter          │
                 └───────────────┬────────────────.
                                 │
                                 ▼
                     Input Ports (Use Cases)
                                 │
                                 ▼
                         Application Layer
                                 │
                                 ▼
                           Domain Model
                                 │
                                 ▼
                        Output Ports (SPI)
                         /              \
                        /                \
                       ▼                  ▼
             Persistence Adapter    External Adapter
                       │                  │
                       ▼                  ▼
                  PostgreSQL         External APIs (OSM/OpenData)
```

---

## 2. Matriz de Estado de Componentes

Para garantizar rigor técnico y evitar presuponer componentes no implementados, la siguiente tabla clasifica cada elemento del sistema:

| Componente | Estado | Descripción Actual |
| :--- | :--- | :--- |
| **REST Controllers** | **Implementado** | Endpoints HTTP `/api/v1/...` activos y documentados con OpenAPI. |
| **Application Services** | **Implementado** | Servicios que contienen lógica de aplicación y transaccionalidad. |
| **Spring Data Repositories** | **Implementado** | Repositorios JPA estándar para acceso a PostgreSQL. |
| **PostgreSQL / Flyway** | **Implementado** | Persistencia relacional con esquema inicial (`V1__Initial_Schema.sql`). |
| **Input Ports (Use Cases)** | **Propuesto** | Interfaces de casos de uso para desacoplar controladores de servicios. |
| **Output Ports (Repositories)** | **Propuesto** | Interfaces de puertos de salida para aislar la persistencia. |
| **Persistence Adapters** | **Propuesto** | Adaptadores JPA que implementan los Output Ports. |
| **External Adapters** | **Futuro** | Clientes para integraciones con OpenStreetMap, Overpass, OpenData, etc. |
| **Domain independiente de JPA** | **A evaluar** | Separación gradual del modelo de dominio puro frente a entidades de persistencia. |
| **Location Value Object** | **A evaluar** | Objeto de valor para coordenadas geográficas con validación de invariantes. |

---

## 3. Problemas y Acoplamientos Identificados (Issue #7)

1. **Acoplamiento Directo Controller ↔ Service**: Los controladores dependen directamente de clases concretas de servicio.
2. **Acoplamiento Directo Service ↔ Spring Data**: Los servicios dependen de repositorios acoplados a Spring Data JPA.
3. **Entidades JPA Híbridas**: Las entidades actuales actúan simultáneamente como objetos de base de datos y modelos de dominio.

---

## 4. Definición de Fronteras y Componentes

### 4.1. Input Ports (Puertos de Entrada / Casos de Use)
Representan las intenciones de usuario o cliente expuestas por la aplicación:
* `CreateCategoryUseCase`, `GetCategoryUseCase`, `ListCategoriesUseCase`
* `CreateSourceUseCase`, `GetSourceUseCase`, `ListSourcesUseCase`
* `CreatePlaceUseCase`, `GetPlaceUseCase`, `ListPlacesUseCase`
* `CreatePlaceSourceUseCase`

### 4.2. Output Ports (Puertos de Salida)
Contratos puros requeridos por la aplicación para persistir o consultar datos:
* `CategoryRepositoryPort`
* `SourceRepositoryPort`
* `PlaceRepositoryPort`
* `PlaceSourceRepositoryPort`

### 4.3. Adapters (Adaptadores)
* **Driving Adapters (Entrada)**: `CategoryController`, `SourceController`, `PlaceController`, `PlaceSourceController`.
* **Driven Adapters (Salida)**: `JpaCategoryRepositoryAdapter`, `JpaPlaceRepositoryAdapter`, etc. (que implementan los Output Ports utilizando Spring Data).

---

## 5. Domain & Application Boundaries

* **Domain (Núcleo)**: Contendrá reglas de negocio puras, entidades de dominio desacopladas de Spring/JPA y Value Objects (como `Location` para coordenadas `[-90, 90]` y `[-180, 180]`).
* **Application Layer**: Coordinará flujos de transacciones y casos de uso utilizando exclusivamente puertos de entrada y salida.

---

## 6. Estrategia de Persistencia e Integración Externa

* **Persistencia**: Los adaptadores de persistencia traducirán entre el modelo de dominio y las entidades JPA (`@Entity`), evitando que las anotaciones de base de datos contaminen la lógica de negocio.
* **Integración Externa (Futuro)**: Se implementará mediante **Adapters** que consuman APIs externas y traduzcan sus respuestas a contratos internos (`TourismDataProvider`), aplicando principios de Adapter en lugar de Strategy a menos que se requiera selección dinámica en runtime.

---

## 7. Estructura de Paquetes Propuesta (Objetivo)

```text
com.alonso.explorersaga/
├── domain/                    # Lógica pura y Value Objects
│   ├── model/
│   └── exception/
├── application/               # Casos de uso y puertos
│   ├── port/
│   │   ├── in/                # Use cases (Input ports)
│   │   └── out/               # Repository ports (Output ports)
│   └── service/               # Implementación de casos de uso
└── adapters/                  # Adaptadores de entrada y salida
    ├── in/
    │   └── web/               # REST Controllers y DTOs HTTP
    └── out/
        ├── persistence/       # Adaptadores JPA y entidades de BD
        └── external/          # Clientes HTTP externos (futuro)
```

---

## 8. Estrategia de Testing

* **Domain & Application Tests**: Pruebas unitarias puras sin levantar Spring Context ni bases de datos.
* **Adapter Tests**: Pruebas de controladores con `@WebMvcTest` y adaptadores JPA con `@DataJpaTest`.
* **Integration/E2E Tests**: Pruebas de integración reales contra PostgreSQL/H2 y flujos REST completos.

---

## 9. Plan de Migración Incremental

1. **Paso 1 (Siguiente Issue)**: Definir **Input Ports** (interfaces de casos de uso) e inyectarlas en los controladores actuales, manteniendo los servicios como implementaciones.
2. **Paso 2**: Definir **Output Ports** (interfaces de repositorios) para desacoplar la capa de aplicación de Spring Data JPA.
3. **Paso 3**: Introducir **Persistence Adapters** explícitos.
4. **Paso 4**: Evaluar la separación entre modelo de dominio puro y entidades JPA, introduciendo Value Objects (`Location`).
5. **Paso 5**: Incorporar adaptadores de integración externa (OSM / OpenData).

---

## 10. Conclusión y Primer Cambio Real (Definido para la Siguiente Issue)

Tras la conclusión de la Issue #8, el diseño establece que **el primer cambio real de implementación para la Issue #9** será:

> **Crear los Input Ports (Interfaces de Casos de Uso) para aislar los REST Controllers de las implementaciones de servicio, preservando 100% la compatibilidad de la API REST y manteniendo los 33 tests en verde.**
