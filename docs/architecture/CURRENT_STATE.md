# Estado Actual de ExplorerSaga Extremadura

> Este documento describe la arquitectura vigente tras el MVP (`v1.0.0-rc1`) y las mejoras de las Issues #24–#28. El análisis inicial, previo al backend, que justificó su creación se conserva al final como contexto histórico.

## 🏗️ Visión General

ExplorerSaga se compone de tres piezas:

1. **Backend Spring Boot** (`backend/`): arquitectura hexagonal con PostgreSQL 15 y migraciones Flyway. Importa lugares de Mérida desde OpenStreetMap/Overpass API y los expone mediante una API REST (`docs/api/API.md`).
2. **Base de datos PostgreSQL**: lugares, categorías, fuentes y la trazabilidad de cada lugar respecto a sus fuentes externas (`place_sources`).
3. **App Android** (`app/`): Kotlin y Jetpack Compose con patrón MVVM. Consume la API REST y mantiene una caché local en Room para funcionar sin conexión.

## 📱 App Android

### Componentes Clave
- **UI**: Jetpack Compose. Pantallas principales: `HomeScreen`, `InformacionScreen`, `PlacesScreen`, `PlacesListScreen`, `PlaceDetailScreen`, `MapScreen` y `FilterScreen`.
- **Navegación**: `androidx.navigation.compose`, con rutas en `AppScreens.kt` y grafo en `AppNavigation.kt`.
- **Estado**: `PlacesViewModel` expone un único `StateFlow<PlacesUiState>`. La red solo se consulta al iniciar, reintentar o importar; los filtros (`FilterState`), la búsqueda y la ordenación por cercanía GPS se aplican en local.
- **Inyección de dependencias**: manual, a través de `AppContainer` (`ExplorerSagaApplication`).
- **Mapas**: osmdroid (OpenStreetMap), sin coste.
- **Red**: Retrofit + kotlinx.serialization (`RetrofitClient`), con un cliente de timeout largo reservado a la importación.

### Flujo de Datos
1. `PlaceRepository` solicita los lugares al backend (`GET /api/v1/places`).
2. **Con respuesta:** la caché de Room se sustituye en una única transacción (`PlaceDao.replaceAll`) por los lugares recibidos. La UI muestra el indicador `🟢 Conectado al Backend`.
3. **Sin respuesta** (sin red, backend caído o lista vacía): se leen los lugares de Room, es decir, los de la última sincronización correcta. La UI muestra `🟡 Modo Local (Respaldo)`.
4. **Primer arranque sin conexión:** si la caché está vacía, se puebla antes de leerla con los lugares incluidos en la app (`assets/monuments.json`, `gastronomia.json`, `tiendas.json`) mediante `BundledPlacesLoader`.
5. El detalle de un lugar usa el lugar ya cargado en la lista; si no está, se consulta al backend y, sin conexión, a Room.

Room es la **única** persistencia local. La caché JSON (`saved_places.json`) de versiones anteriores se elimina al arrancar.

### Categorías
Backend, seed y datos locales comparten un único vocabulario en español: `monumento`, `iglesia`, `museo`, `restaurante`, `cafeteria`, `bar`, `heladeria`, `tienda_alimentos`, `souvenir`, `supermercado`, `libreria`, `ropa`, `tienda_general`. `FilterState` relaciona cada filtro de la UI con sus categorías.

## 🧪 Pruebas
- **Backend**: `mvn clean verify`. Incluye pruebas unitarias, de controladores, de persistencia, de integración (con una importación repetida sobre JPA real para verificar la idempotencia) y del adaptador Overpass con `MockRestServiceServer`.
- **Android**: `./gradlew testDebugUnitTest lintDebug`. Incluye pruebas de filtrado (`FilterStateTest`), distancias (`GeoTest`), mapeo de datos (`PlaceMappingTest`) y repositorio y caché offline (`PlaceRepositoryTest`).

## ⚠️ Limitaciones Conocidas
- La importación desde Overpass es manual (`POST /api/v1/places/import`, o el botón "Buscar en la Web" de la app). No hay sincronización periódica.
- Los lugares importados de OSM suelen carecer de descripción, horarios y foto.
- La URL del backend está fijada para el emulador (`10.0.2.2:8080`). En un dispositivo físico hay que usar `adb reverse` o cambiarla.
- El permiso de ubicación, el tema y el modo offline aún no se han verificado en un emulador como parte de las Issues #25–#28.

---

## 📜 Contexto Histórico: Análisis Inicial (Pre-Backend)

Antes del backend, la app era "estática": cargaba los lugares de los JSON de `assets` en cada sesión y Room existía solo como estructura preliminar. Aquel análisis detectó estas limitaciones, que motivaron el backend centralizado:

1. **Datos estáticos**: cualquier cambio exigía publicar una nueva versión de la app.
2. **Escalabilidad geográfica limitada**: los JSON locales no escalan a una región entera.
3. **Mantenimiento manual** de horarios y descripciones.
4. **Sin detección de duplicados** entre fuentes.
5. **Categorización rígida** en el `ViewModel` y el repositorio.
6. **Sin capacidad offline real** basada en sincronización.

Los puntos 1, 2, 4, 5 y 6 quedan resueltos por el backend, la importación desde OpenStreetMap con deduplicación (Issue #27), el vocabulario de categorías unificado y la caché de Room (Issue #28). El punto 3 sigue parcialmente abierto, porque OSM no siempre incluye esa información.
