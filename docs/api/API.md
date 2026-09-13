# Definición Conceptual de la API REST

## 🌐 Configuración Base
- **Versión**: `v1`
- **Formato**: `application/json`
- **URL Base (Local)**: `http://localhost:8080/api/v1`

## 🛣️ Endpoints del MVP (Mérida)

### 1. Lugares (`/places`)
Permite obtener la lista de puntos de interés.

- **`GET /places`**: Lista general de lugares.
    - **Parámetros**:
        - `category`: Filtrar por slug de categoría (opcional).
        - `city`: Filtrar por ciudad (opcional, por defecto "Mérida").
        - `page`, `size`: Paginación.
- **`GET /places/{id}`**: Detalle completo de un lugar específico.
- **`GET /places/nearby`**: Lugares cercanos a una ubicación.
    - **Parámetros**:
        - `lat`, `lon`: Coordenadas del usuario.
        - `radius`: Radio en metros (opcional).

### 2. Categorías (`/categories`)
Obtener el catálogo de categorías disponibles para alimentar los filtros de la App.

- **`GET /categories`**: Lista de categorías (Nombre, Icono, ID).

### 3. Sincronización (Administración/Interno)
Endpoints para forzar actualizaciones (útiles durante desarrollo).

- **`POST /sync/osm`**: Dispara la sincronización manual con OpenStreetMap para un área definida.

## 📦 Estructura de Respuesta (Ejemplo)

### `GET /places/1`
```json
{
  "id": 1,
  "name": "Teatro Romano",
  "description": "El teatro romano de Mérida...",
  "category": {
    "id": 1,
    "name": "MONUMENT",
    "display": "Monumento"
  },
  "location": {
    "latitude": 38.915363,
    "longitude": -6.338650,
    "address": "Plaza Margarita Xirgú, s/n"
  },
  "media": {
    "image_url": "https://...",
    "website": "https://..."
  },
  "updated_at": "2024-03-20T10:00:00Z"
}
```

## 🚥 Códigos de Estado HTTP
- `200 OK`: Éxito.
- `201 Created`: Recurso creado (ej. tras sync exitosa).
- `400 Bad Request`: Parámetros inválidos.
- `404 Not Found`: Recurso no encontrado.
- `500 Internal Server Error`: Error genérico del servidor.

## 🛡️ Consideraciones de Diseño
- **Paginación**: Obligatoria desde el inicio para evitar respuestas masivas.
- **Filtrado en Backend**: El servidor asume la carga de filtrado, enviando a Android solo lo que necesita mostrar.
- **DTOs**: Se utilizarán objetos de transferencia de datos para no exponer directamente las entidades de la base de datos.
