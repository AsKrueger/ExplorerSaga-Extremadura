# Estado del Proyecto: ExplorerSaga Extremadura

Este documento sirve como una auditoría completa del estado actual de la aplicación, su documentación para el TFG, y la evaluación formal del cierre del MVP.

---

## 1. 🏆 Estado Global y Cierre Formal del MVP

La versión candidata **`v1.0.0-rc1`** de ExplorerSaga Extremadura ha sido **validada, etiquetada y aprobada como MVP funcional completo**. 

### 🟢 Decisión de Cierre de MVP
Se declara el **cierre formal del MVP de ExplorerSaga Extremadura** bajo la versión candidata **`v1.0.0-rc1`**, cumpliendo el 100% de los criterios de evaluación funcional, técnica y documental requeridos para la presentación y evaluación del Trabajo de Fin de Grado (TFG) y demostración de portfolio.

---

## 2. ✅ Matriz de Evaluación de Criterios del MVP

| Criterio de Evaluación | Estado | Evidencia y Verificación |
| :--- | :---: | :--- |
| **1. Ingesta de Datos Abiertos (OSM/Overpass API)** | ✅ | Adaptador `OverpassAdapter` e importación desde OpenStreetMap operativa (`POST /api/v1/places/import`). |
| **2. Persistencia Relacional & Trazabilidad** | ✅ | PostgreSQL 15 con esquema de 4 entidades (`places`, `categories`, `sources`, `place_sources`) y Flyway. |
| **3. Ingesta Idempotente Sin Duplicados** | ✅ | Verificado: peticiones repetidas actualizan/omitirán registros sin duplicar gracias a claves de fuentes externas. |
| **4. API REST y Filtrado por Categorías** | ✅ | Controladores `/api/v1/places` con filtros y especificación OpenAPI/Swagger interactiva. |
| **5. Consumo Remoto desde Android** | ✅ | `RetrofitClient` integrado consumiendo API REST v1 y mapeando entidades a modelos Composable. |
| **6. Resiliencia & Respaldo Local (Offline)** | ✅ | Conmutación transparente a assets JSON locales y base de datos Room con indicadores de estado (`🟢` / `🟡`). |
| **7. Suite de Pruebas Automatizadas** | ✅ | **36/36 tests Backend PASSED** (`mvnw clean verify`), **Android Unit Tests PASSED**, **Assemble Debug PASSED**. |
| **8. Documentación de Puesta en Marcha** | ✅ | [docs/SETUP.md](docs/SETUP.md) y [README.md](README.md) verificados, reproducibles y portables. |
| **9. Registro de Limitaciones Conocidas** | ✅ | Limitaciones y dependencias registradas explícitamente en [docs/RELEASE_NOTES_v1.0.0-rc1.md](docs/RELEASE_NOTES_v1.0.0-rc1.md). |
| **10. Ausencia de Dependencias de Pago** | ✅ | Stack 100% de código abierto (OpenStreetMap, osmdroid, PostgreSQL, Spring Boot, Retrofit, Room). |

---

## 3. 📝 Historial de Issues Completados

### **EPIC: ⚙️ Backend e Infraestructura**
-   **Issue #2: [Infraestructura Base Spring Boot] - COMPLETADO ✅**
-   **Issue #3: [Investigación y diseño de fuentes] - COMPLETADO ✅**
-   **Issue #4: [Persistencia con PostgreSQL + JPA] - COMPLETADO ✅**
-   **Issue #5: [Capa de aplicación y lógica de dominio] - COMPLETADO ✅**
-   **Issue #6: [API REST del dominio] - COMPLETADO ✅**

### **EPIC: 📱 Persistencia Local & Resiliencia Android**
-   **Issue #1: [Instalar Room] - COMPLETADO ✅**
-   **Issue #2: [Poblar la Base de Datos con Respaldo JSON] - COMPLETADO ✅**
-   **Issue #3: [Conectar Vistas a ViewModel, Retrofit y Room] - COMPLETADO ✅**

### **EPIC: 🗺️ Mejoras del Mapa**
-   **Issue #4: [Marcadores Dinámicos] - COMPLETADO ✅**
-   **Issue #5: [Funcionalidad de Filtrado] - COMPLETADO ✅**
-   **Issue #7: [Panel de Detalles del Marcador (BottomSheet)] - COMPLETADO ✅**

### **EPIC: 📦 Release Management, Validación Final y Cierre de MVP**
-   **Issue #20: [Preparación de la Versión Candidata v1.0.0-rc1] - COMPLETADO ✅**
    -   Elaboración de la guía de instalación [docs/SETUP.md](docs/SETUP.md) y comprobación de perfiles de desarrollo.
-   **Issue #21: [Validación Final, Etiquetado y Publicación v1.0.0-rc1] - COMPLETADO ✅**
    -   Verificación técnica completa (36/36 tests Backend, tests Android, assembleDebug).
    -   Creación de la etiqueta Git `v1.0.0-rc1` y notas de versión en [docs/RELEASE_NOTES_v1.0.0-rc1.md](docs/RELEASE_NOTES_v1.0.0-rc1.md).
-   **Issue #22: [Publicación de v1.0.0-rc1 y Cierre Formal del MVP] - COMPLETADO ✅**
    -   Verificación de accesibilidad y portabilidad de la documentación sin dependencias locales.
    -   Evaluación formal de criterios de aceptación del MVP y declaración de cierre de la release candidata v1.0.0-rc1.

### **EPIC: 🧹 Mantenimiento y Calidad (Post-MVP)**
-   **Issue #24: [Limpieza del Repositorio] - COMPLETADO ✅**
    -   Eliminación de artefactos locales, configuración personal del IDE, código muerto y recursos sin uso.
-   **Issue #25: [Corrección de Defectos y Refactorización Android] - COMPLETADO ✅**
    -   ViewModel con un único flujo reactivo, permiso de ubicación en tiempo de ejecución, paleta corporativa y 13 pruebas unitarias.

---

## 4. 📌 Datos de la Release v1.0.0-rc1

- **Versión:** `v1.0.0-rc1` (Release Candidate 1)
- **Etiqueta Git:** `v1.0.0-rc1`
- **Commit Validado:** `46ffe1e` (Rama `feature/issue21` / `feature/issue22` / `main`)
- **Notas de Publicación:** [docs/RELEASE_NOTES_v1.0.0-rc1.md](docs/RELEASE_NOTES_v1.0.0-rc1.md)
- **Defectos Bloqueantes:** Ninguno.

---

## 5. 🔮 Próximas Etapas (Post-MVP v1.0.0)

Tras la consecución y cierre del MVP, se contemplan las siguientes líneas evolutivas para futuras versiones estables:

1. **Permisos de Geolocalización en Tiempo de Ejecución (GPS):** Solicitud interactiva de `ACCESS_FINE_LOCATION` para centrar el mapa dinámicamente en la ubicación real del usuario.
2. **Sincronización Periódica Automatizada:** Programador en segundo plano (Spring `@Scheduled`) para refrescar periódicamente la información desde Overpass API.
3. **Agrupación de Marcadores en Mapa (Clustering):** Optimización visual mediante agrupación cuando existen múltiples puntos de interés con alta densidad en el mapa.
4. **Pantalla de Rutas Turísticas Temáticas:** Diseño e implementación de itinerarios guiados por días o temáticas históricas en Mérida.
