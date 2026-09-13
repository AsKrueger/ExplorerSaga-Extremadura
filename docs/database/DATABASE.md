# Modelo de Datos Inicial (Conceptual)

## 📌 Introducción
El modelo de datos de ExplorerSaga está diseñado para ser flexible, permitiendo almacenar información de múltiples fuentes y garantizando la trazabilidad del dato original.

## 🗄️ Entidades Principales

### 1. `Place` (Lugar)
Representa el punto de interés final que verá el usuario.
- **Campos**:
    - `id` (PK): Identificador interno.
    - `name`: Nombre del lugar (normalizado).
    - `description`: Descripción principal.
    - `latitude`, `longitude`: Coordenadas geográficas.
    - `address`: Dirección física legible.
    - `category_id` (FK): Referencia a la categoría.
    - `image_url`: Enlace a la imagen principal.
    - `website`: Sitio web oficial.
    - `created_at`, `updated_at`: Timestamps.

### 2. `Category` (Categoría)
Define el tipo de lugar.
- **Campos**:
    - `id` (PK).
    - `name`: Nombre interno (ej. MONUMENT).
    - `display_name`: Nombre para mostrar (ej. Monumento).
    - `icon`: Identificador del icono a usar en la UI.

### 3. `Source` (Fuente)
Define de dónde provienen los datos.
- **Campos**:
    - `id` (PK).
    - `name`: Nombre de la fuente (ej. OPENSTREETMAP, WIKIDATA).
    - `base_url`: URL de la fuente externa.

### 4. `PlaceSource` (Relación Lugar-Fuente)
Crucial para la deduplicación y actualización. Mapea un lugar de ExplorerSaga con su equivalente en una fuente externa.
- **Campos**:
    - `place_id` (FK): Referencia al lugar interno.
    - `source_id` (FK): Referencia a la fuente.
    - `external_id`: ID que utiliza la fuente externa (ej. ID de nodo en OSM).
    - `last_sync`: Fecha de la última vez que se actualizó desde esta fuente específica.
    - `raw_data`: (Opcional) JSON con la respuesta original para auditoría.

## 🤝 Relaciones
- Un `Place` tiene una única `Category` (relación Many-to-One).
- Un `Place` puede tener múltiples `PlaceSource` (relación One-to-Many). Esto permite que un mismo monumento se alimente de OSM para las coordenadas y de Wikidata para la descripción histórica.
- Una `Source` puede estar vinculada a muchos `PlaceSource`.

## 🛡️ Estrategia contra Duplicados
Al recibir un lugar de una fuente externa:
1. Se comprueba si existe un `PlaceSource` con el mismo `external_id` y `source_id`.
2. Si no existe, se realiza una búsqueda geográfica en la tabla `Place` (ej. lugares en un radio de 20 metros con nombre similar).
3. Si hay coincidencia, se añade una nueva `PlaceSource` al lugar existente en lugar de crear uno nuevo.
4. Si no hay coincidencia, se crea un nuevo `Place`.
