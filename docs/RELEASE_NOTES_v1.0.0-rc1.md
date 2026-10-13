# Release Notes — ExplorerSaga Extremadura (v1.0.0-rc1)

**Versión:** `v1.0.0-rc1` (Release Candidate 1)  
**Fecha de Publicación:** 12 de Octubre de 2026  
**Rama:** `feature/issue21` / `main`  
**Estado:** Candidata validada para presentación en portfolio y evaluación de TFG.

---

## 🚀 Resumen de Capacidades Disponibles

ExplorerSaga Extremadura v1.0.0-rc1 es una aplicación guía turística nativa para la ciudad de Mérida (Extremadura) construida con una arquitectura desacoplada y resiliente:

1. **Navegación e Interfaz Móvil Nativa (Android Jetpack Compose):**
   - 6 pantallas principales (`HomeScreen`, `InformacionScreen`, `PlacesScreen`, `PlacesListScreen`, `PlaceDetailScreen`, `MapScreen`).
   - Componentes modernos basados en Material Design 3.
   - Listados detallados con tarjetas interactivas y filtrado por subcategorías (monumentos, gastronomía, tiendas).
   - Mapa interactivo OpenStreetMap (`osmdroid`) centrado en Mérida con marcadores geolocalizados codificados por color y panel desplegable BottomSheet.

2. **Backend Hexagonal Spring Boot 3 & PostgreSQL:**
   - Persistencia relacional en PostgreSQL 15 con migraciones automáticas mediante Flyway (`V1__Initial_Schema.sql`).
   - API REST v1 (`/api/v1/places`, `/api/v1/categories`, `/api/v1/sources`, `/api/v1/place-sources`, `/api/v1/health`).
   - Adaptador de ingesta idempotente desde OpenStreetMap (Overpass API).
   - Documentación OpenAPI/Swagger interactiva accesible en `/swagger-ui/index.html`.

3. **Resiliencia y Modo Offline Transparente:**
   - Cliente Android Retrofit con indicación visual de estado de conexión (`🟢 Conectado al Backend` vs `🟡 Modo Local (Respaldo)`).
   - Respaldo automático transparente mediante base de datos Room y assets JSON locales si el servidor o la red fallan.
   - Acción de reintento de conexión desde la interfaz sin interrupción de la experiencia del usuario.

---

## 🛠️ Tecnologías Principales Utilizadas

- **Android App:** Kotlin 2.0.21, Jetpack Compose, Navigation Compose, Room (SQLite), Retrofit 2, OkHttp 4, `osmdroid` (OpenStreetMap).
- **Backend:** Java 17/21, Spring Boot 3.3.0, Spring Data JPA, Hibernate, Flyway, Swagger/OpenAPI.
- **Base de Datos e Infraestructura:** PostgreSQL 15 (Docker Compose), H2 (Base de datos en memoria para tests de integración).
- **Construcción y Automatización:** Gradle Wrapper, Apache Maven Wrapper (`mvnw`).

---

## 📖 Instrucciones de Instalación y Ejecución

Para consultar los requisitos completos, paso a paso y escenarios de prueba de aceptación:
- 📖 [Guía de Instalación y Puesta en Marcha (docs/SETUP.md)](SETUP.md)
- 📄 [Documentación General del Proyecto (README.md)](../README.md)

### Resumen rápido de ejecución:

```bash
# 1. Iniciar PostgreSQL con Docker
cd backend
docker compose up -d

# 2. Ejecutar pruebas y arrancar Backend Spring Boot
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run

# 3. Importar datos desde Overpass API
curl -X POST http://localhost:8080/api/v1/places/import

# 4. Compilar y ejecutar App Android
cd ..
.\gradlew.bat test
.\gradlew.bat app:assembleDebug
```

---

## 🧪 Pruebas Ejecutadas y Resultados Reales

Se ha ejecutado la validación completa sobre el código exacto de la versión candidata:

| Componente | Comando de Validación | Resultado Real |
| :--- | :--- | :--- |
| **Backend Spring Boot** | `.\mvnw.cmd clean verify` | **36/36 tests PASSED (`BUILD SUCCESS`)** (Unitarios, Controllers, Servicios, JPA e Integración REST/DB). |
| **Android Pruebas Unitarias** | `.\gradlew.bat test` | **PASSED** (`:app:testReleaseUnitTest` 100% exitoso). |
| **Android Compilación Debug** | `.\gradlew.bat app:assembleDebug` | **BUILD SUCCESSFUL** (APK generado correctamente). |
| **Pruebas E2E y Resiliencia** | Verificación manual de conmutación Backend $\leftrightarrow$ Local JSON | **PASSED** (Indicadores visuales verde/amarillo y reintento funcionales). |

---

## ⚠️ Limitaciones Conocidas

1. **Importación manual desde Overpass:** La sincronización inicial con OpenStreetMap se ejecuta bajo demanda mediante un endpoint HTTP POST (`/api/v1/places/import`), posponiendo las tareas programadas automáticas para versiones posteriores.
2. **Dependencia de disponibilidad de Overpass API:** La importación de datos depende de la API pública de OpenStreetMap/Overpass, la cual está sujeta a límites de frecuencia (HTTP 429) o demoras ocasionales.
3. **Imágenes de lugares:** Se han asociado imágenes representativas disponibles en la aplicación; la inclusión de galerías multimedia extendidas queda planificada para futuras entregas.
4. **Permisos de geolocalización en tiempo de ejecución:** El mapa está centrado por defecto en la zona monumental de Mérida; el centrado dinámico mediante GPS del dispositivo se abordará en la versión estable v1.0.0.

---

## 🌐 Atribución de Datos y Dependencias Externas

- **OpenStreetMap & Overpass API:** Los datos de puntos de interés turístico provienen de OpenStreetMap y sus colaboradores, distribuidos bajo la licencia [Open Database License (ODbL)](https://www.openstreetmap.org/copyright).
- **osmdroid:** Librería de renderizado de mapas OpenSource para Android.

---

## ℹ️ Aclaración de Versión Candidata

Esta publicación corresponde a la etiqueta **`v1.0.0-rc1`** (Release Candidate 1). Es una versión de evaluación totalmente funcional y probada que consolida el trabajo del TFG para su presentación y demostración. No constituye una release de producción pública comercial.
