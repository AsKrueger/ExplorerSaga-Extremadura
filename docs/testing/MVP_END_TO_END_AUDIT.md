# Informe de Auditoría Funcional y Validación E2E del MVP

**Proyecto:** ExplorerSaga-Extremadura  
**Issue:** #18 — Auditoría funcional y validación de extremo a extremo del MVP  
**Fecha de Validación:** Octubre de 2026  
**Entorno:** Spring Boot 3.3.0 (Backend), H2/PostgreSQL 15 (Persistencia), Android SDK 35 / Jetpack Compose / Retrofit 2.9.0 (Cliente Móvil)

---

## 1. Introducción y Recorrido Principal

Este documento recoge los resultados de la auditoría funcional de extremo a extremo (E2E) realizada sobre el MVP de ExplorerSaga-Extremadura, validando la cadena completa:

```text
OpenStreetMap / Overpass API
             ↓
      OverpassAdapter
             ↓
 ImportTourismPlacesService (Input Port / Use Case)
             ↓
    PostgreSQL / H2 (JPA / Flyway)
             ↓
   API REST (/api/v1/places)
             ↓
      Android Retrofit
             ↓
      PlaceRepository
             ↓
     PlacesViewModel
             ↓
  Jetpack Compose UI
```

---

## 2. Validación de Componentes y Escenarios

### A. Backend & Ingesta (Overpass API + Idempotencia)
* **Estado:** ✅ Validado
* **Escenarios probados:**
  1. **Importación inicial (`POST /api/v1/places/import`)**: Ejecución exitosa del adaptador de Overpass API para Mérida, normalización de datos y persistencia mediante los puertos de salida (`PlaceRepositoryPort`, `PlaceSourceRepositoryPort`).
  2. **Idempotencia y Actualización**: Reejecución de la importación sobre datos ya existentes. El servicio comprobó correctamente la existencia previa mediante la tupla `(source, externalId)` (`type/id`), omitiendo duplicados o actualizando campos modificados y devolviendo métricas precisas (`processed`, `inserted`, `updated`, `skipped`, `failed`).
  3. **Tests Automatizados**: 36/36 tests de backend superados con éxito (`BUILD SUCCESS`).

### B. API REST (`/api/v1/places`)
* **Estado:** ✅ Validado
* **Escenarios probados:**
  1. `GET /api/v1/places`: Consulta general devolviendo la lista completa de lugares turísticos.
  2. `GET /api/v1/places?category=Monument`: Filtrado eficiente en base de datos mediante `findByCategoryNameIgnoreCase`.
  3. Documentación interactiva en Swagger UI (`/swagger-ui/index.html`) verificada y plenamente operativa.

### C. Cliente Android (Retrofit + MVVM + Resiliencia)
* **Estado:** ✅ Validado
* **Escenarios probados:**
  1. **Conexión Remota (`DataSourceOrigin.REMOTE`)**: La app Android consulta con éxito al backend Spring Boot mediante Retrofit y muestra los lugares en Jetpack Compose.
  2. **Respaldo Local (`DataSourceOrigin.LOCAL_FALLBACK`)**: Cuando el backend no está disponible o falla la red, el repositorio activa de forma transparente el respaldo local basado en archivos JSON de assets (`monuments.json`, etc.), indicando claramente en la interfaz el origen de los datos con un distintivo visual (`🟡 Modo Local (Respaldo)`).
  3. **Acción de Reintento (`retry()`)**: Permite al usuario reintentar la conexión de red tras un fallo de conectividad sin duplicar solicitudes en recomposiciones de Compose.

---

## 3. Criterios de Aceptación del MVP Cumplidos

- [x] Arquitectura Hexagonal consolidada con Input Ports, Output Ports y Adapters.
- [x] Adaptador funcional de OpenStreetMap (Overpass API) para Mérida a coste cero (0 €).
- [x] Importación idempotente respaldada por la clave única `(source_id, external_id)`.
- [x] API REST con soporte de filtrado opcional por categoría.
- [x] Aplicación Android integrada mediante Retrofit con gestión de estados (carga, éxito, vacío, error, reintento) y transparencia de origen de datos.
- [x] Suite de pruebas 100% verde (36/36 tests del backend pasando sin requerir red externa).

---

## 4. Limitaciones Conocidas y Recomendaciones Futuras

1. **Caché Offline Persistente**: Actualmente el respaldo local utiliza assets estáticos en JSON cuando falla la red. Se recomienda en una futura **Issue #19** integrar Room como caché local persistente offline.
2. **Segunda Fuente Externa**: Incorporar Open Data Extremadura como segunda fuente de datos utilizando el mismo puerto `TourismDataProvider`.
3. **Sincronización Automática**: Evaluar un mecanismo opcional de sincronización periódica en segundo plano para el cliente o servidor.

---

## 5. Conclusión

El MVP de **ExplorerSaga-Extremadura** se encuentra en un estado funcionalmente completo, robusto, debidamente documentado y respaldado por pruebas automatizadas. El recorrido principal de extremo a extremo está consolidado y listo para demostración.
