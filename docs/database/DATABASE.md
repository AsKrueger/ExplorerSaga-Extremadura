# Base de Datos: ExplorerSaga Extremadura

## 📌 Introducción
Capa de persistencia inicial implementada con PostgreSQL 15 y gestionada mediante Flyway. El diseño garantiza la independencia del dominio frente a proveedores externos mediante una estructura multi-fuente genérica.

## 🗄️ Esquema Físico (V1)

### 1. `categories`
Almacena la taxonomía interna de lugares de ExplorerSaga.
- `id`: BIGSERIAL (PK)
- `name`: VARCHAR(100) (UNIQUE, NOT NULL) - Ej: MONUMENT, RESTAURANT.
- `description`: TEXT

### 2. `sources`
Catálogo de fuentes externas de información.
- `id`: BIGSERIAL (PK)
- `code`: VARCHAR(50) (UNIQUE, NOT NULL) - Ej: GOOGLE_PLACES, OPEN_DATA_EXTREMADURA.
- `name`: VARCHAR(100) (NOT NULL)
- `description`: TEXT

### 3. `places`
Entidad núcleo que representa un lugar de interés.
- `id`: BIGSERIAL (PK)
- `name`: VARCHAR(255) (NOT NULL)
- `description`: TEXT
- `address`: VARCHAR(500)
- `latitude`: DOUBLE PRECISION (NOT NULL)
- `longitude`: DOUBLE PRECISION (NOT NULL)
- `category_id`: BIGINT (FK -> categories.id)
- `created_at`: TIMESTAMP WITH TIME ZONE (Default: NOW)
- `updated_at`: TIMESTAMP WITH TIME ZONE (Default: NOW)

### 4. `place_sources`
Tabla de enlace que permite la trazabilidad multi-fuente.
- `id`: BIGSERIAL (PK)
- `place_id`: BIGINT (FK -> places.id)
- `source_id`: BIGINT (FK -> sources.id)
- `external_id`: VARCHAR(255) (NOT NULL) - Identificador en la fuente original (ej: Place ID).
- `last_sync`: TIMESTAMP WITH TIME ZONE

## 🛡️ Restricciones de Integridad
- **Unicidad**: Restricción `UNIQUE(source_id, external_id)` en `place_sources` para evitar duplicidad de registros de una misma fuente.
- **Referencial**: La eliminación de categorías está protegida si existen lugares asociados.
- **Tipado**: Uso de `DOUBLE PRECISION` para coordenadas y `TIMESTAMP WITH TIME ZONE` para precisión temporal.

## 🔄 Datos Transitorios y Política de Capa REST
Siguiendo las políticas de proveedores (Google Places EEE 2026), los siguientes datos **no se persisten** en la base de datos núcleo:
- Fotografías de terceros.
- Horarios de apertura dinámicos.
- Valoraciones de usuarios.

La API REST actúa como orquestador, combinando los datos persistentes de PostgreSQL con información dinámica obtenida en tiempo real cuando es necesario.
