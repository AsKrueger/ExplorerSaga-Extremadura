# ExplorerSaga Extremadura: TFG sobre Desarrollo de Aplicaciones Móviles Android

---

## 📝 Resumen del Proyecto (Abstract)

Este repositorio contiene el código fuente del Trabajo de Fin de Grado (TFG) **ExplorerSaga Extremadura**, una aplicación móvil Android nativa desarrollada en Kotlin con Jetpack Compose. El proyecto consiste en el diseño e implementación de una guía turística interactiva y visual para la ciudad de Mérida (Extremadura).

La aplicación se centra en ofrecer una experiencia de usuario (UX) fluida, moderna y atractiva, permitiendo la consulta de puntos de interés (monumentos, restaurantes, tiendas, etc.) a través de listados y un mapa interactivo. El desarrollo sigue patrones de arquitectura modernos como MVVM y utiliza las últimas herramientas recomendadas del ecosistema de Android Jetpack.

## 🎯 Objetivos del Trabajo

### Objetivo General

Diseñar e implementar una aplicación Android nativa que sirva como guía turística para la ciudad de Mérida, poniendo especial énfasis en la calidad de la interfaz (UI), la experiencia de usuario (UX) y la aplicación de buenas prácticas de desarrollo de software.

### Objetivos Específicos

-   **Aplicar los principios de desarrollo moderno de Android**, utilizando Kotlin como lenguaje y Jetpack Compose para la construcción de la interfaz de usuario.
-   **Implementar un sistema de navegación claro e intuitivo** que guíe al usuario a través de las diferentes secciones de la aplicación.
-   **Integrar un mapa interactivo funcional** utilizando una solución de código abierto (OpenStreetMap) para la geolocalización de puntos de interés.
-   **Estructurar el código de la aplicación** siguiendo un patrón de arquitectura reconocido (MVVM) que garantice la escalabilidad y mantenibilidad del proyecto.
-   **Ofrecer contenido estático relevante** sobre la historia de la ciudad y una clasificación de lugares por categorías como base funcional.

---

## 🛠️ Metodología y Tecnologías Empleadas

*   **Lenguaje de programación**: [Kotlin](https://kotlinlang.org/), por su sintaxis moderna, seguridad y por ser el lenguaje preferido para el desarrollo Android.
*   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose), para la creación de una interfaz de usuario declarativa, reactiva y nativa.
*   **Navegación**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation) para gestionar el flujo de navegación entre las distintas pantallas (Composables).
*   **Mapas**: [OpenStreetMap (OSM)](https://www.openstreetmap.org/) a través de la librería `osmdroid-android`, como alternativa gratuita y de código abierto a otros proveedores de mapas.
*   **Arquitectura**: Estructura simple basada en el patrón **MVVM (Model-View-ViewModel)**. Las Vistas (Views) se implementan con Composables, que observan el estado expuesto por los ViewModels.
*   **Diseño**: [Material Design 3](https://m3.material.io/), utilizando una paleta de colores personalizada y componentes modernos para una estética limpia y coherente.

---

## ✨ Funcionalidades Implementadas (Versión Candidata MVP)

1. **🏠 Pantalla de Bienvenida**: Presentación visual que sirve como punto de entrada a la aplicación.
2. **🏛️ Contexto Histórico**: Pantalla informativa que ofrece al usuario una breve introducción a la historia de Mérida.
3. **🏪 Menú de Categorías**: Selección visual de los tipos de lugares de interés (monumentos, restaurantes, tiendas).
4. **📋 Listados Detallados**: Muestra de lugares en formato de lista con filtros interactivos por subcategorías.
5. **🗺️ Mapa Interactivo**: Mapa dinámico con OpenStreetMap que muestra marcadores geolocalizados de Mérida codificados por color según la categoría y desplegable informativo BottomSheet.
6. **🌐 Backend Spring Boot Hexagonal & PostgreSQL**:
   - Persistencia relacional en PostgreSQL 15 con migraciones automáticas Flyway.
   - API REST v1 (`/api/v1/places`) con filtrado por categorías y documentación OpenAPI/Swagger.
   - Adaptador de ingesta e importación idempotente desde OpenStreetMap (Overpass API) a coste 0 €.
7. **🔄 Resiliencia y Respaldo Local en Android**:
   - Integración remota mediante Retrofit con indicador visual claro de origen de datos (`🟢 Conectado al Backend` vs `🟡 Modo Local (Respaldo)`).
   - Respaldo local transparente basado en assets JSON cuando el servidor o la red no están disponibles.
   - Acción de reintento de conexión desde la interfaz.

---

## 🚀 Guía de Instalación, Configuración y Ejecución

Consulta la documentación detallada de puesta en marcha en [docs/SETUP.md](docs/SETUP.md), la cual incluye:

- Requisitos de entorno (JDK 17/21, Docker/PostgreSQL, Android Studio, SDK 35).
- Despliegue de la base de datos PostgreSQL con Docker Compose.
- Ejecución de la suite de pruebas del backend (`mvnw test`) y Android (`gradlew test`).
- Arranque del backend Spring Boot e importación manual desde Overpass API.
- Configuración y ejecución de la aplicación Android en el emulador.
- Guía de solución de problemas y pruebas de aceptación E2E.

---

## 🔮 Líneas de Trabajo Futuro

Como parte de la evolución natural del proyecto, se proponen las siguientes mejoras:

-   [x] **Implementar mapa funcional con OpenStreetMap.**
-   [x] **Persistencia de Datos**: **Room** como única caché offline: refleja la última sincronización con el backend.
-   [x] **Carga de Datos Inicial**: La caché se puebla con los JSON incluidos en la app en el primer arranque sin conexión.
-   [ ] **Contenido Multimedia**: Reemplazo de las imágenes de marcador de posición (placeholders) por recursos visuales de alta calidad de Mérida.
-   [ ] **Funcionalidad de Rutas**: Implementación de un sistema que sugiera al usuario rutas turísticas personalizadas por días o temáticas.
-   [ ] **Internacionalización (i18n)**: Adaptación de la aplicación para soportar múltiples idiomas (ej. inglés), permitiendo un mayor alcance.
-   [x] **Testing (unitario)**: Pruebas unitarias del backend (48) y de Android (19), incluida la caché offline.
-   [ ] **Testing (instrumentación)**: Pruebas de UI y de instrumentación en dispositivo/emulador.
-   [ ] **Accesibilidad (a11y)**: Aplicación de mejoras para asegurar la usabilidad de la aplicación por parte de personas con diversidad funcional.

---

## 📄 Licencia

Este proyecto está distribuido bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.
""
