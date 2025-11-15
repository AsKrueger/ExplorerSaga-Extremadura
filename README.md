"""# ExplorerSaga Extremadura: TFG sobre Desarrollo de Aplicaciones Móviles Android

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

## ✨ Funcionalidades Implementadas

1.  **🏠 Pantalla de Bienvenida**: Presentación visual que sirve como punto de entrada a la aplicación.
2.  **🏛️ Contexto Histórico**: Pantalla informativa que ofrece al usuario una breve introducción a la historia de Mérida.
3.  **🏪 Menú de Categorías**: Selección visual de los tipos de lugares de interés (monumentos, restaurantes, tiendas).
4.  **📋 Listados Detallados**: Muestra de lugares en formato de lista, con datos de ejemplo como nombre y descripción.
5.  **🗺️ Mapa Interactivo**: Implementación de un mapa funcional que muestra la ubicación de Mérida y un marcador de ejemplo.

---

## 🚀 Guía de Instalación y Ejecución

1.  Clona este repositorio:
    ```bash
    git clone https://github.com/tu-usuario/ExplorerSaga-Extremadura.git
    ```
2.  Abre el proyecto en la última versión estable de [Android Studio](https://developer.android.com/studio).
3.  Sincroniza el proyecto con Gradle para que se descarguen todas las dependencias.
4.  Ejecuta la aplicación en un emulador o en un dispositivo físico con Android API 26 o superior.

---

## 🔮 Líneas de Trabajo Futuro

Como parte de la evolución natural del proyecto, se proponen las siguientes mejoras:

-   [x] **Implementar mapa funcional con OpenStreetMap.**
-   [x] **Persistencia de Datos**: Integración completa de la biblioteca **Room** para la gestión de una base de datos local (SQLite).
-   [x] **Carga de Datos Inicial**: Desarrollo de un mecanismo para poblar la base de datos a partir de un fichero JSON local en el primer arranque de la app.
-   [ ] **Contenido Multimedia**: Reemplazo de las imágenes de marcador de posición (placeholders) por recursos visuales de alta calidad de Mérida.
-   [ ] **Funcionalidad de Rutas**: Implementación de un sistema que sugiera al usuario rutas turísticas personalizadas por días o temáticas.
-   [ ] **Internacionalización (i18n)**: Adaptación de la aplicación para soportar múltiples idiomas (ej. inglés), permitiendo un mayor alcance.
-   [ ] **Testing**: Creación de pruebas unitarias y de instrumentación para garantizar la calidad, robustez y corrección del código.
-   [ ] **Accesibilidad (a11y)**: Aplicación de mejoras para asegurar la usabilidad de la aplicación por parte de personas con diversidad funcional.

---

## 📄 Licencia

Este proyecto está distribuido bajo la Licencia MIT. Consulta el archivo `LICENSE` para más detalles.
""