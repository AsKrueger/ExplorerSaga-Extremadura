# Modelo de Datos Inicial (Conceptual)

## 📌 Introducción
El modelo de datos de ExplorerSaga está diseñado para ser flexible, permitiendo almacenar información de múltiples fuentes y garantizando la trazabilidad del dato original.

## 🗄️ Entidades Principales

### 1. `Place` (Lugar)
Representa la entidad central normalizada.
- **Campos Almacenables (Persistentes)**:
    - `id` (PK): Identificador interno.
    - `name`: Nombre normalizado (prioridad Turismo Mérida para monumentos).
    - `latitude`, `longitude`: Coordenadas (refresco obligatorio cada 30 días si proceden de Google).
    - `address`: Dirección estandarizada.
    - `category_id` (FK).
    - `description`: Historia/Cultura (Turismo Mérida).
    - `website`, `phone`.
    - `created_at`, `updated_at`.
- **Campos Dinámicos (No persistentes en DB principal, consultados vía API)**:
    - Fotografías actuales de Google.
    - Horarios de apertura dinámicos (Google Pro SKU).
    - Valoraciones de usuarios.

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
    - `name`: Nombre de la fuente (ej. TURISMO_MERIDA, GOOGLE_PLACES).
    - `base_url`: URL de la fuente externa.

### 4. `PlaceSource` (Relación Lugar-Fuente)
Crucial para la deduplicación y actualización. Mapea un lugar de ExplorerSaga con su equivalente en una fuente externa.
- **Campos**:
    - `place_id` (FK): Referencia al lugar interno.
    - `source_id` (FK): Referencia a la fuente.
    - `external_id`: ID que utiliza la fuente externa (ej. Google Place ID).
    - `last_sync`: Fecha de la última actualización desde esta fuente.

## 🤝 Relaciones y Procedencia
- **Place 1:N PlaceSource**: Un monumento (ej. Teatro Romano) tendrá un `PlaceSource` para `TURISMO_MERIDA` (con su ID de BIC) y otro para `GOOGLE_PLACES` (con su `place_id`).
- **Preferencia de Atributos**:
    - **Contenido Cultural**: Turismo Mérida > Google.
    - **Ubicación GPS**: Google > Turismo Mérida.

## 🛡️ Estrategia Multinivel contra Duplicados
1. **Identificador Directo**: Coincidencia exacta de `place_id` o ID de dataset BIC.
2. **Geofencing**: Búsqueda en radio de 20m en la base de datos de ExplorerSaga.
3. **Similitud Léxica**: Normalización de nombres (lower case, sin tildes, eliminación de stop-words como "Teatro", "Museo" en la comparación) para validación final.
