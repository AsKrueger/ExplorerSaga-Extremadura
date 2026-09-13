# Arquitectura Futura: ExplorerSaga Extremadura

## 🌐 Visión General
El proyecto evoluciona hacia una arquitectura **Cliente-Servidor** para centralizar la lógica de datos y permitir la actualización dinámica de contenidos con coste 0 €.

```text
Android (Cliente) <--> API REST (JSON) <--> Spring Boot (Backend) <--> PostgreSQL (DB)
                                                  ^
                                                  |
                                          FUENTES EXTERNAS (OSM, Wikidata)
```

## 🏗️ Responsabilidades por Componente

### 📱 Android (El Visualizador)
- **UI/UX**: Presentación de datos mediante Jetpack Compose.
- **Geolocalización**: Ubicación del usuario y visualización en mapa (osmdroid).
- **Caché Local**: Uso de **Room** para almacenar los datos recibidos de la API y permitir funcionamiento offline.
- **Navegación**: Gestión de flujos de usuario.
- **Consumo de API**: Comunicación con el backend mediante **Retrofit**.

### ☕ Backend (El Cerebro) - Java + Spring Boot
- **API REST**: Exponer endpoints seguros y eficientes para el cliente.
- **Orquestación de Datos**: Gestionar la sincronización con fuentes externas.
- **Pipeline de Procesamiento**:
    1. **Data Sources**: Adaptadores específicos para cada API externa (OpenStreetMap, Wikidata).
    2. **Normalización**: Convertir datos heterogéneos a un formato interno común.
    3. **Validación**: Asegurar la integridad de los datos mínimos requeridos.
    4. **Deduplicación**: Lógica para identificar el mismo lugar en diferentes fuentes (ej. comparando coordenadas y nombres).
- **Scheduler**: Tareas programadas para actualizar la información periódicamente.

### 🗄️ PostgreSQL (La Memoria)
- Persistencia robusta de lugares, categorías y fuentes.
- Almacenamiento de metadatos de sincronización.

## 🔌 Arquitectura Multi-fuente (Patrón Adapter)
Para evitar el acoplamiento con fuentes externas, el backend utilizará una abstracción:

1. **`DataSource` (Interfaz)**: Define el contrato para obtener datos.
2. **`OpenStreetMapDataSource`**: Implementación que consulta Overpass API.
3. **`WikidataDataSource`**: Implementación para datos enriquecidos (futuro).
4. **`ExternalDTO`**: Objeto temporal que refleja la respuesta de la fuente original.
5. **`InternalMapper`**: Convierte el DTO externo a la entidad `Place` de ExplorerSaga.

## 🔄 Flujo de Sincronización
1. El **Scheduler** activa una tarea de sincronización.
2. Se consultan las fuentes externas (ej. Overpass para un área geográfica).
3. Se reciben los datos y se parsean a DTOs externos.
4. Se aplica el proceso de **Normalización** (limpieza de cadenas, formato de horarios).
5. El **Deduplicador** busca coincidencias en la DB (usando `PlaceSource` y cercanía geográfica).
6. Se insertan nuevos lugares o se actualizan los existentes.
7. Los cambios quedan disponibles inmediatamente para la API REST.

## 🌍 Evolución y Escalabilidad
La arquitectura está diseñada para crecer:
- **Mérida (MVP)**: Consultas a Overpass limitadas a coordenadas de Mérida.
- **Extremadura**: Ampliación de áreas de búsqueda y gestión de ciudades/provincias en el modelo.
- **España**: Optimización de queries y posible fragmentación de sincronización por regiones.

## 💰 Estrategia de Coste 0 €
- **Desarrollo**: Java (OpenJDK), PostgreSQL, Docker (Community Edition).
- **Datos**: OpenStreetMap (Licencia ODbL) y Wikidata (Dominio Público).
- **Hosting**: Se evaluarán servicios como Render o Fly.io que ofrecen capas gratuitas para Spring Boot y PostgreSQL, siempre bajo la premisa de no generar cargos automáticos.
