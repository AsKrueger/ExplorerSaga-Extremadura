# Historial de Trabajo: Issues Completados

Este documento sirve como un registro del trabajo de desarrollo completado, documentado en formato de "issues" para la memoria del TFG.

---

### **EPIC: 🏗️ Estructura Inicial y Navegación**

-   **Issue #1: [Creación de la Estructura de Pantallas] - CERRADO ✅**
    -   **Descripción:** Crear los archivos Composable iniciales para cada una de las 6 pantallas principales de la aplicación (`HomeScreen`, `InformacionScreen`, `MapScreen`, `FilterScreen`, `PlacesScreen`, `DetailedListScreen`).
    -   **Resultado:** Todos los archivos fueron creados y organizados en el paquete `ui.screens`, estableciendo la arquitectura base de la UI.

-   **Issue #2: [Configuración de la Navegación] - CERRADO ✅**
    -   **Descripción:** Añadir la dependencia de Jetpack Navigation para Compose y configurar el `NavHost` para permitir la navegación entre las 6 pantallas principales.
    -   **Resultado:** Se creó un `AppNavigation.kt` que centraliza el grafo de navegación. `MainActivity` se actualizó para usar este sistema, permitiendo un flujo de usuario funcional.

-   **Issue #3: [Corrección de Navegación de Listados] - CERRADO ✅**
    -   **Descripción:** El diseño inicial llevaba todas las categorías a una única pantalla de listado genérica. Se detectó como un error de diseño y se procedió a su corrección.
    -   **Resultado:** Se crearon 3 pantallas de listado específicas (`MonumentsListScreen`, `RestaurantsListScreen`, `ShopsListScreen`). Se actualizó el `NavHost` y la `PlacesScreen` para que cada categoría navegue a su pantalla correcta, solucionando el error.

---

### **EPIC: 🎨 Diseño de UI e Identidad Visual**

-   **Issue #4: [Implementación del Diseño de la HomeScreen] - CERRADO ✅**
    -   **Descripción:** Aplicar el diseño visual a la pantalla de inicio, incluyendo el fondo de imagen del campo extremeño, la paleta de colores y la tipografía especificada.
    -   **Resultado:** La `HomeScreen` se actualizó con un `Image` de fondo, `Button` y `Text` estilizados para crear una bienvenida profesional y atractiva.

-   **Issue #5: [Implementación del Diseño de la InformacionScreen] - CERRADO ✅**
    -   **Descripción:** Aplicar el diseño visual a la pantalla de información, incluyendo la imagen panorámica del Teatro Romano con un degradado superpuesto para legibilidad del texto.
    -   **Resultado:** La `InformacionScreen` ahora muestra una imagen de cabecera y una tarjeta de información, siguiendo la guía de estilo del proyecto.

-   **Issue #6: [Implementación del Diseño de PlacesScreen y Listados] - CERRADO ✅**
    -   **Descripción:** Diseñar la `PlacesScreen` con tarjetas de categoría clicables y las pantallas de listado con un diseño de item individual (imagen, nombre, descripción).
    -   **Resultado:** Las pantallas de `PlacesScreen`, `MonumentsListScreen`, `RestaurantsListScreen` y `ShopsListScreen` fueron implementadas con datos de ejemplo y una UI limpia y funcional.

-   **Issue #7: [Configuración del Icono de la App] - CERRADO ✅**
    -   **Descripción:** Reemplazar el icono por defecto de Android por el logo personalizado de la aplicación (`ic_explorersaga-playstore.png`).
    -   **Resultado:** Se utilizó la herramienta "Image Asset Studio" para generar los iconos adaptativos (foreground y background) y se actualizó el `AndroidManifest.xml` para usarlos. Se solucionó un problema de configuración en los XML de los iconos (`mipmap-anydpi-v26`) que apuntaban a la carpeta incorrecta (`@drawable` en lugar de `@mipmap`).

---

### **EPIC: 🗺️ Implementación de Mapas**

-   **Issue #8: [Migración a OpenStreetMap] - CERRADO ✅**
    -   **Descripción:** Tomar la decisión estratégica de no usar Google Maps para garantizar la sostenibilidad del proyecto a largo plazo sin costes. El objetivo fue migrar toda la funcionalidad de mapas a una alternativa gratuita y de código abierto.
    -   **Resultado:** Se eliminaron todas las dependencias y configuraciones de Google Maps. Se añadió y configuró la librería **`osmdroid-android`**. La `MapScreen` fue reescrita usando un `AndroidView` para mostrar un mapa funcional de OpenStreetMap.

-   **Issue #9: [Resolución de Fallos de Compilación y Ejecución] - CERRADO ✅**
    -   **Descripción:** Después de múltiples cambios en las dependencias (Google Maps, Firebase, OpenStreetMap), el proyecto entró en un estado de errores de compilación persistentes (`IncompatibleClassChangeError`, `Failed to resolve`, etc.).
    -   **Resultado:** Se diagnosticó el problema como un conflicto de versiones en el archivo `libs.versions.toml`. Se guió al usuario para que **reemplazara el contenido de `libs.versions.toml` por una configuración 100% estable y probada**, lo que finalmente solucionó todos los errores y devolvió el proyecto a un estado funcional.

-   **Issue #10: [Configuración Inicial de `osmdroid`] - CERRADO ✅**
    -   **Descripción:** La aplicación se cerraba al abrir el mapa debido a que `osmdroid` requiere una configuración explícita del `User-Agent` para prevenir abusos.
    -   **Resultado:** Se añadió el bloque de configuración de `osmdroid` en el método `onCreate` de la `MainActivity`, solucionando el cierre inesperado.

---

### **EPIC: 📦 Release Management, Validación Final y Cierre de MVP**

-   **Issue #20: [Preparación de la Versión Candidata v1.0.0-rc1] - CERRADO ✅**
    -   **Descripción:** Documentar los procedimientos de instalación y puesta en marcha del sistema completo (PostgreSQL, Spring Boot 3, Android Retrofit/Room) y verificar la configuración de desarrollo.
    -   **Resultado:** Se elaboró el documento [docs/SETUP.md](docs/SETUP.md) detallando paso a paso el despliegue con Docker Compose, ejecución de suites de pruebas, importación desde Overpass API y pruebas de resiliencia.

-   **Issue #21: [Validación Final, Etiquetado y Publicación v1.0.0-rc1] - CERRADO ✅**
    -   **Descripción:** Ejecutar la suite completa de pruebas sobre el Backend y Android, verificar que el repositorio está limpio y sin datos sensibles, formalizar la etiqueta Git `v1.0.0-rc1`, publicar las notas de release y actualizar el seguimiento del proyecto.
    -   **Resultado:** Se validó satisfactoriamente el Backend (36/36 tests OK), las pruebas unitarias de Android y el ensamblado Debug (`app:assembleDebug`). Se creó la etiqueta Git anotada `v1.0.0-rc1`, se subió al repositorio remoto y se generó el documento de notas de publicación `docs/RELEASE_NOTES_v1.0.0-rc1.md`.

-   **Issue #22: [Publicación de v1.0.0-rc1 y Cierre Formal del MVP] - CERRADO ✅**
    -   **Descripción:** Verificar la disponibilidad de la versión candidata `v1.0.0-rc1` y sus notas de release, auditar la documentación desde la perspectiva de un evaluador externo, verificar los 10 criterios de evaluación del MVP y formalizar la declaración de cierre del MVP.
    -   **Resultado:** Se confirmó que toda la documentación (`README.md`, `docs/SETUP.md`, `docs/RELEASE_NOTES_v1.0.0-rc1.md`) es portátil, independiente de entornos locales y libre de dependencias de pago. Se evaluó satisfactoriamente la matriz de 10/10 criterios de aceptación del MVP y se declaró el cierre formal del MVP en `PROJECT_STATUS.md`.

-   **Issue #23: [Corrección de Configuración Local de Android SDK (`local.properties`)] - CERRADO ✅**
    -   **Descripción:** Resolver el error de compilación `SDK location not found` producido cuando se ejecuta Gradle desde la línea de comandos o en un entorno recién clonado sin la variable `ANDROID_HOME` o el archivo `local.properties`.
    -   **Resultado:** Se creó una plantilla `local.properties.example` en la raíz del proyecto, se configuró `local.properties` con la ruta válida del SDK de Android escapada correctamente para Windows/Linux/macOS y se documentó detalladamente la solución en la Sección 8 de [docs/SETUP.md](docs/SETUP.md).
