# Estado del Proyecto: ExplorerSaga Extremadura

Este documento sirve como una auditoría completa del estado actual de la aplicación, su documentación para el TFG, y una hoja de ruta para futuras mejoras.

---

## 1. ✅ Características Implementadas y Funcionales

La aplicación se encuentra en un estado **estable y funcional**. Se puede compilar y ejecutar sin errores. Las siguientes características están completadas:

-   **Estructura de Navegación Completa:**
    -   Las 6 pantallas principales (`HomeScreen`, `InformacionScreen`, `PlacesScreen`, etc.) están creadas.
    -   Se utiliza **Jetpack Navigation para Compose** para navegar entre todas las pantallas, siguiendo el flujo de usuario definido.

-   **Diseño de UI Moderno:**
    -   Todas las pantallas tienen una interfaz de usuario estilizada y atractiva, basada en las especificaciones de diseño (paleta de colores, tipografía, etc.).
    -   Se usan componentes de **Material Design 3** (`Card`, `Button`, `Scaffold`, etc.).

-   **Mapa Funcional (OpenStreetMap):**
    -   Se ha migrado con éxito de Google Maps a **OpenStreetMap**, asegurando que la app sea 100% gratuita y sostenible a largo plazo.
    -   La `MapScreen` muestra un mapa interactivo centrado en Mérida.
    -   Se ha añadido un marcador de ejemplo en el Teatro Romano.

-   **Lógica de Listados (Datos de Ejemplo):**
    -   Las pantallas de categorías (`MonumentsListScreen`, `RestaurantsListScreen`, etc.) muestran listas de lugares.
    -   Los datos son actualmente **listas estáticas** (hardcodeadas) como paso previo a la integración de una base de datos.

---

## 2. 📝 Issues y Tareas Pendientes (Hoja de Ruta)

Esta es la lista de tareas pendientes, divididas como "Issues" para una gestión de proyecto clara.

### **EPIC: ⚙️ Backend e Infraestructura**

-   **Issue #2: [Infraestructura Base Spring Boot] - COMPLETADO ✅**
    -   Estructura de proyecto Maven con Spring Boot 3.3.0.
    -   Configuración de Maven Wrapper.
    -   Endpoint de Health Check funcional.
    -   Configuración de perfiles (`dev`, `test`).

-   **Issue #3: [Investigación y diseño de fuentes] - COMPLETADO ✅**
    -   Investigación de viabilidad de Turismo Mérida y Google Places.
    -   Diseño del modelo de dominio multi-fuente.
    -   Definición de estrategias de deduplicación y caché.
    -   Actualización de documentación técnica (Architecture, Database, API).

-   **Issue #4: [Persistencia con PostgreSQL + JPA] - COMPLETADO ✅**
    -   Configuración de Spring Data JPA y driver PostgreSQL.
    -   Infraestructura reproducible con Docker Compose.
    -   Implementación de entidades JPA (`Place`, `Category`, `Source`, `PlaceSource`).
    -   Creación de repositorios y tests de integración de persistencia.

-   **Issue #5: [Capa de aplicación y lógica de dominio] - COMPLETADO ✅**
    -   Implementación de DTOs para desacoplar el dominio de la infraestructura.
    -   Creación de servicios de negocio (`Category`, `Source`, `Place`, `PlaceSource`).
    -   Validación de reglas de negocio (coordenadas, existencia, duplicados).
    -   Estrategia de tests unitarios (Mockito) e integración (E2E Service-to-DB).

-   **Issue #1: [Instalar Room]** - Instalar de forma segura las dependencias de Room y KSP, usando una configuración de versiones estable para evitar los conflictos de compilación anteriores.
-   **Issue #2: [Poblar la Base de Datos]** - Crear un sistema que, la primera vez que se abre la app, lea los datos de un archivo local (ej. un `JSON` en los assets) y los inserte en la base de datos Room.
-   **Issue #3: [Conectar Vistas a la Base de Datos]** - Refactorizar todas las pantallas de listado (`MonumentsListScreen`, etc.) para que obtengan sus datos desde un `ViewModel` conectado a la base de datos, en lugar de usar las listas estáticas.

### **EPIC: 🗺️ Mejoras del Mapa**

-   **Issue #4: [Marcadores Dinámicos]** - Modificar la `MapScreen` para que lea todos los lugares de la base de datos y muestre un marcador para cada uno, en lugar de solo uno de ejemplo.
-   **Issue #5: [Funcionalidad de Filtrado]** - Implementar la lógica del botón "Filtrar" para que actualice los marcadores visibles en el mapa según la categoría seleccionada.
-   **Issue #6: [Mi Ubicación]** - Implementar el botón "Mi Ubicación", lo que requerirá:
    -   Solicitar permisos de geolocalización (`ACCESS_FINE_LOCATION`) al usuario.
    -   Obtener la ubicación actual del dispositivo y centrar el mapa en ella.
-   **Issue #7: [Panel de Detalles del Marcador]** - Diseñar e implementar el panel informativo (BottomSheet) que debe aparecer cuando el usuario toca un marcador en el mapa.

### **EPIC: ✨ UI/UX y Contenido**

-   **Issue #8: [Contenido Real]** - Reemplazar todas las imágenes de placeholder (ej. el icono 📷 en las listas) por imágenes reales de los lugares, que se deben añadir a la carpeta `res/drawable`.
-   **Issue #9: [Pantalla de Rutas]** - Diseñar e implementar la pantalla de Rutas, que actualmente es un `TODO`.
-   **Issue #10: [Transiciones y Animaciones]** - Añadir animaciones de transición entre pantallas para mejorar la experiencia de usuario y darle un toque más pulido a la app.

---

## 3. ⚠️ Puntos Débiles y Carencias Actuales (Dónde puede "cojear")

Un análisis crítico del estado actual del proyecto revela las siguientes debilidades a tener en cuenta para el TFG:

1.  **Gestión de Datos (El punto más débil):**
    -   El uso de listas estáticas "hardcodeadas" es la mayor carencia actual. Es inflexible, difícil de mantener y no escalable. La **prioridad número 1** del proyecto es implementar la base de datos Room (Issues #1, #2, #3).

2.  **Falta de Gestión de Estado Robusta:**
    -   Actualmente, el estado de la UI es muy simple. Cuando implementemos funcionalidades como el filtrado, necesitaremos usar `StateFlow` o `collectAsStateWithLifecycle()` en los ViewModels para gestionar el estado de forma eficiente y segura ante cambios de ciclo de vida (como girar la pantalla).

3.  **Gestión de Permisos Inexistente:**
    -   La app no solicita ningún permiso en tiempo de ejecución. Para implementar la funcionalidad "Mi Ubicación" (Issue #6), es **crítico** añadir un flujo de solicitud de permisos correcto que maneje los casos en que el usuario los deniega.

4.  **Manejo de Errores y Casos Límite:**
    -   La aplicación no tiene ningún tipo de manejo de errores. Por ejemplo, ¿qué pasa si el usuario abre la pantalla del mapa sin conexión a Internet? La app podría comportarse de forma inesperada o incluso cerrarse. Se necesita añadir comprobaciones de estado de red.

5.  **Rendimiento Potencial:**
    -   Con pocos datos, el rendimiento es bueno. Sin embargo, si la base de datos creciera a cientos de lugares, se debería optimizar la carga de marcadores en el mapa (ej. cargando solo los que están en la zona visible - *clustering*).

