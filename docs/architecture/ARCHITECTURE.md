# Arquitectura Futura: ExplorerSaga Extremadura

## 🌐 Visión General
El proyecto evoluciona hacia una arquitectura **Cliente-Servidor** para centralizar la lógica de datos y permitir la actualización dinámica de contenidos con coste 0 €.

```text
Android (Cliente) <--> API REST (JSON) <--> Spring Boot (Backend) <--> PostgreSQL (DB)
                                                  ^
                                                  |
                                          FUENTES EXTERNAS
                          (Turismo Mérida Open Data, Google Places)
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
- **Orquestación de Datos**: Gestionar la sincronización con fuentes externas respetando sus restricciones técnicas y legales.
- **Pipeline de Procesamiento**:
    1. **Data Sources**: Adaptadores específicos:
        - **Turismo Mérida**: Extracción desde datasets BIC de Open Data Extremadura y scraping legítimo de la web oficial.
        - **Google Places**: Integración con API Essentials (gratuita) para ubicación y Pro para detalles.
    2. **Normalización**: Convertir datos heterogéneos a un formato interno común (Taxonomía ExplorerSaga).
    3. **Validación**: Asegurar la integridad de los datos mínimos requeridos.
    4. **Deduplicación**: Estrategia multinivel (Place ID -> Geofencing -> Similitud Léxica).
- **Scheduler**: Tareas programadas para actualizar la información periódicamente, respetando el límite de 30 días para coordenadas de Google.

### 🗄️ PostgreSQL (La Memoria)
- Persistencia robusta de lugares, categorías y fuentes.
- Almacenamiento de metadatos de sincronización y caché de coordenadas (máx. 30 días para Google).

## 🔌 Arquitectura Multi-fuente (Patrón Adapter)
Para evitar el acoplamiento con fuentes externas, el backend utilizará una abstracción:

1. **`DataSource` (Interfaz)**: Define el contrato para obtener datos.
2. **`TurismoMeridaDataSource`**: Implementación que consulta el portal de Open Data Extremadura.
3. **`GooglePlacesDataSource`**: Implementación para datos comerciales y de ubicación exacta.
4. **`ExternalDTO`**: Objeto temporal que refleja la respuesta de la fuente original.
5. **`InternalMapper`**: Convierte el DTO externo a la entidad `Place` de ExplorerSaga.

## 🔄 Flujo de Sincronización
1. El **Scheduler** activa una tarea de sincronización.
2. Se consultan las fuentes externas.
3. Se reciben los datos y se parsean a DTOs externos.
4. Se aplica el proceso de **Normalización** (limpieza de cadenas, normalización de categorías internas).
5. El **Deduplicador** busca coincidencias en la DB:
    - Uso de `Place ID` para Google Places (persistente).
    - Cercanía geográfica y similitud léxica para nuevas fuentes.
6. Se insertan nuevos lugares o se actualizan los existentes.
7. Los cambios quedan disponibles inmediatamente para la API REST.

## 🌍 Evolución y Escalabilidad
La arquitectura está diseñada para crecer:
- **Mérida (MVP)**: Consultas a fuentes limitadas a coordenadas de Mérida.
- **Extremadura**: Aprovechamiento de los datasets regionales de Open Data.
- **España**: Optimización de cuotas de Google Places mediante geofencing y almacenamiento estratégico de IDs.

## 💰 Estrategia de Coste 0 €
- **Desarrollo**: Java (OpenJDK), PostgreSQL.
- **Datos**:
    - **Turismo Mérida**: Datos abiertos de libre acceso.
    - **Google Places**: Uso dentro del nivel gratuito renovado (Marzo 2025):
        - 10,000 llamadas/mes para campos básicos (Essentials: ID, Lat/Lon).
        - 5,000 llamadas/mes para campos enriquecidos (Pro: Horarios, Fotos).
    - **Optimización**: Almacenamiento persistente del `Place ID` para evitar búsquedas repetitivas y reducir el consumo de cuota.
- **Hosting**: Servicios con capas gratuitas (ej. Render, Fly.io) monitorizando estrictamente el consumo.

## ⚖️ Restricciones Legales y Técnicas (Hallazgos Issue #3)
- **Google Places API**:
    - **Almacenamiento**: Solo se permite almacenar el `place_id` de forma indefinida.
    - **Caché**: Las coordenadas (Lat/Lon) tienen un límite de **30 días**. Otros datos dinámicos (fotos, horarios) no deben persistirse permanentemente en la DB principal.
    - **Atribución**: Obligatorio mostrar "Google Maps" o logo corporativo en la interfaz donde se usen sus datos.
    - **Costes**: Modelo de cuotas gratuitas por SKU (Marzo 2025). Essentials (10k), Pro (5k).
- **Turismo Mérida / Open Data**:
    - **Datasets BIC**: Fuente estructurada principal para monumentos históricos.
    - **Web Oficial**: Fuente para enriquecimiento de historias y tarifas (vía crawling respetuoso).
