# ExplorerSaga Extremadura

**ExplorerSaga Extremadura** es una aplicación móvil Android diseñada para ofrecer una experiencia turística sencilla, visual y atractiva, centrada inicialmente en la ciudad de Mérida (Extremadura). La app permite a los usuarios explorar lugares históricos, restaurantes, tiendas y rutas recomendadas sin necesidad de registrarse.

Este proyecto nace como parte de un Trabajo de Fin de Grado (TFG), con el objetivo de aplicar conceptos modernos de desarrollo en Android y diseño de experiencia de usuario (UX/UI).

---

## 🎯 Objetivo General

La aplicación busca **ofrecer una experiencia turística sencilla, visual y atractiva**. El usuario podrá **explorar lugares históricos, restaurantes, tiendas y rutas recomendadas** sin necesidad de registrarse ni complicaciones, con un diseño moderno y minimalista.

---

## ✨ Características Principales

La app está dividida en 6 escenas principales que permiten una navegación clara y progresiva:

1.  **🏠 HomeScreen**: Pantalla de bienvenida con una presentación atractiva que invita a explorar la ciudad de Mérida.
2.  **🏛️ Información**: Ofrece un contexto histórico y cultural de la ciudad antes de que el usuario comience a explorar.
3.  **🏪 Lugares (Categorías)**: Un menú visual para que el usuario elija qué tipo de lugar le interesa (monumentos, restaurantes, tiendas).
4.  **📋 Listados Detallados**: Muestra una lista de lugares según la categoría elegida, con información relevante como nombre, descripción y popularidad.
5.  **🗺️ Mapa Interactivo**: Un mapa funcional basado en OpenStreetMap que muestra la ubicación de Mérida y un marcador de ejemplo.
6.  **🎚️ Filtrado**: Pantalla complementaria al mapa para personalizar los lugares mostrados según el interés del usuario.

---

## 🧭 Flujo de Navegación

El flujo de usuario está diseñado para ser intuitivo y directo:

```
🏠 HomeScreen
   ↓
🏛️ Información
   ├──→ 🗺️ Mapa → 🎚️ Filtrado → Mapa (actualizado)
   └──→ 🏪 Lugares → 📋 [Listados detallados]
```

---

## 🛠️ Tecnología Utilizada

*   **Lenguaje de programación**: [Kotlin](https://kotlinlang.org/)
*   **UI Toolkit**: [Jetpack Compose](https://developer.android.com/jetpack/compose) para una interfaz de usuario declarativa y moderna.
*   **Navegación**: [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation) para gestionar el flujo entre pantallas.
*   **Mapas**: [OpenStreetMap (OSM)](https://www.openstreetmap.org/) a través de la librería `osmdroid-android` para una solución de mapas gratuita y de código abierto.
*   **Arquitectura**: Estructura simple basada en MVVM (Model-View-ViewModel), con los Composables actuando como Vistas.
*   **Diseño**: [Material Design 3](https://m3.material.io/) con una paleta de colores personalizada inspirada en la bandera de Extremadura.

---

## 🚀 Cómo Empezar

1.  Clona este repositorio:
    ```bash
    git clone https://github.com/tu-usuario/ExplorerSaga-Extremadura.git
    ```
2.  Abre el proyecto en la última versión de [Android Studio](https://developer.android.com/studio).
3.  Sincroniza el proyecto con Gradle y ejecútalo en un emulador o dispositivo físico.

---

## 🔮 Futuras Mejoras

-   [x] **~~Integración de Google Maps~~ -> Implementar mapa funcional con OpenStreetMap.**
-   [ ] **Datos Dinámicos**: Conectar la app a una base de datos local (**Room**) para gestionar los lugares de interés.
-   [ ] **Poblar la Base de Datos**: Crear un sistema que inserte los datos iniciales desde un archivo local (JSON) a la base de datos.
-   [ ] **Imágenes Reales**: Sustituir los placeholders por imágenes de alta calidad de Mérida.
-   [ ] **Rutas por Días**: Desarrollar la funcionalidad opcional de rutas personalizadas.
