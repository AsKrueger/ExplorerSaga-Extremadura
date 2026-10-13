# Estado del Proyecto: ExplorerSaga Extremadura

Este documento sirve como una auditoría completa del estado actual de la aplicación, su documentación para el TFG, y una hoja de ruta para futuras mejoras.

---

## 1. ✅ Características Implementadas y Funcionales

La aplicación se encuentra en un estado **estable, probado y formalizado como versión candidata `v1.0.0-rc1`**. Se puede compilar, ejecutar y probar sin errores. Las siguientes características están completadas:

-   **Navegación e Interfaz Móvil Nativa:**
    -   Las 6 pantallas principales (`HomeScreen`, `InformacionScreen`, `PlacesScreen`, `PlacesListScreen`, `PlaceDetailScreen`, `MapScreen`) están operativas.
    -   Se utiliza **Jetpack Navigation para Compose** para navegar entre todas las pantallas.
    -   Diseño UI basado en **Material Design 3**.

-   **Mapa Funcional (OpenStreetMap):**
    -   Mapa interactivo con `osmdroid` centrado en Mérida.
    -   Marcadores geolocalizados de lugares turísticos codificados por color según su categoría y desplegable informativo BottomSheet.

-   **Backend Hexagonal Spring Boot & PostgreSQL:**
    -   Persistencia relacional en PostgreSQL 15 con migraciones Flyway (`V1__Initial_Schema.sql`).
    -   API REST v1 (`/api/v1/places`) con documentación interactiva Swagger/OpenAPI.
    -   Adaptador de ingesta e importación desde OpenStreetMap (Overpass API).

-   **Resiliencia y Modo Offline Transparente:**
    -   Integración con Retrofit, base de datos Room y respaldo en assets JSON.
    -   Indicador visual claro del origen de datos (`🟢 Conectado al Backend` vs `🟡 Modo Local (Respaldo)`) y botón de reintento.

---

## 2. 📝 Issues y Tareas Completadas (Historial de Release)

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

### **EPIC: 📦 Release Management & Validación Final**

-   **Issue #20: [Preparación de la Versión Candidata v1.0.0-rc1] - COMPLETADO ✅**
    -   Documentación de procedimientos de instalación y ejecución en `docs/SETUP.md`.
    -   Verificación de configuración de desarrollo, puertos y perfiles.

-   **Issue #21: [Validación Final, Etiquetado y Publicación v1.0.0-rc1] - COMPLETADO ✅**
    -   Verificación técnica completa: Backend (36/36 tests OK), Android Unit Tests (1/1 OK), Android Assemble Debug (OK).
    -   Auditoría de seguridad y credenciales (sin datos sensibles ni tokens en el repo).
    -   Creación y publicación de la etiqueta Git anotada `v1.0.0-rc1`.
    -   Publicación de notas de versión en `docs/RELEASE_NOTES_v1.0.0-rc1.md`.

---

## 3. 📌 Estado de la Release v1.0.0-rc1

- **Etiqueta Git:** `v1.0.0-rc1`
- **Fecha de Validación:** 12 de Octubre de 2026
- **Pruebas Ejecutadas:**
  - Backend Spring Boot: `.\mvnw.cmd clean verify` $\rightarrow$ 36/36 tests PASSED.
  - Android: `.\gradlew.bat test` $\rightarrow$ PASSED.
  - Android Build: `.\gradlew.bat app:assembleDebug` $\rightarrow$ BUILD SUCCESSFUL.
- **Defectos Pendientes Bloqueantes:** Ninguno.
- **Decisión de Siguiente Paso:** Aceptar la versión candidata `v1.0.0-rc1` para presentación del TFG.

---

## 4. 🔮 Líneas de Trabajo Futuro (Post v1.0.0-rc1)

1. **Gestión de Permisos de Ubicación en Tiempo de Ejecución (GPS):**
   - Implementar la solicitud de permisos `ACCESS_FINE_LOCATION` para centrado dinámico en la posición actual del usuario.

2. **Automatización de Ingesta Sincronizada:**
   - Incorporar un planificador periódico (Spring `@Scheduled`) para sincronizar datos con Overpass API en segundo plano.

3. **Optimización del Mapa (Clustering):**
   - Agrupar marcadores para mejorar el rendimiento gráfico en mapas con alta densidad de puntos.

4. **Funcionalidad de Rutas Turísticas:**
   - Diseñar la pantalla de rutas personalizadas por tiempo disponible o temática.
