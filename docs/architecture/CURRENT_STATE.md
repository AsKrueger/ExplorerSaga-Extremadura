# Estado Actual de ExplorerSaga Extremadura

## 🏗️ Arquitectura Actual
La aplicación Android sigue el patrón **MVVM (Model-View-ViewModel)** con componentes modernos de Jetpack.

### Componentes Clave:
- **UI**: Desarrollada íntegramente con **Jetpack Compose**. Las pantallas principales incluyen `HomeScreen`, `MapScreen`, `PlacesListScreen` y `PlaceDetailScreen`.
- **Navegación**: Utiliza `androidx.navigation.compose`. Las rutas están definidas en `AppScreens.kt` y la lógica de navegación en `AppNavigation.kt`.
- **Gestión de Estado**: Los `ViewModel` (ej. `PlacesViewModel`) utilizan `StateFlow` y `MutableStateFlow` para exponer el estado de la UI de forma reactiva.
- **Inyección de Dependencias**: Inyección manual a través de un `AppContainer` definido en la clase `ExplorerSagaApplication`.
- **Mapas**: Implementado con **osmdroid** (OpenStreetMap) para mantener el coste 0.
- **Persistencia**: Estructura preliminar de **Room** (`PlaceEntity`, `PlaceDao`, `AppDatabase`), aunque actualmente los datos se cargan desde archivos locales.

## 🗃️ Gestión de Datos Actual
Actualmente, la aplicación es "estática" y depende de archivos locales.

### Origen de Datos:
Los datos se encuentran en `app/src/main/assets/`:
- `monuments.json`: Lugares históricos y monumentos.
- `gastronomia.json`: Restaurantes y cafeterías.
- `tiendas.json`: Comercios locales.

### Flujo de Datos:
1. Al iniciar, el `PlaceRepository` carga los JSONs mediante `loadPlacesFromAssets()`.
2. Los datos se parsean a objetos `Place` (Kotlin Serialization) y se convierten en `PlaceEntity`.
3. El `PlacesViewModel` solicita datos al repositorio filtrando por categorías.
4. Los datos se filtran en memoria y se devuelven como un `Flow`.

## 📋 Modelo de Datos `Place`
```kotlin
data class Place(
    val id: Int,
    val name: String,
    val description: String,
    val category: String,
    val horarios: String?,
    val direccion: String?,
    val latitude: Double,
    val longitude: Double,
    val photo: String?,
    val website: String?
)
```

## ⚠️ Limitaciones y Problemas Detectados

1. **Datos Estáticos**: Cualquier cambio en la información turística requiere una actualización y publicación de la aplicación en la Play Store.
2. **Escalabilidad Geográfica Limitada**: El sistema de carga de JSONs locales no es eficiente para gestionar miles de lugares en toda una región o país.
3. **Mantenimiento Manual**: La recopilación de datos es manual, lo que dificulta mantener la información actualizada (horarios, descripciones).
4. **Duplicidad Potencial**: No existe un mecanismo para detectar lugares repetidos si se añadieran más fuentes manuales.
5. **Categorización Rígida**: Las categorías están hardcodeadas en el `ViewModel` y el `Repository`, dificultando la adición de nuevos tipos de lugares.
6. **Sin Capacidad Offline Real**: Aunque existe Room, el flujo actual carga de assets en cada sesión, sin aprovechar una sincronización real.

Este análisis justifica la necesidad de un **Backend centralizado** que gestione la complejidad de los datos y las fuentes externas.
