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
### `GET /places/{id}`
Devuelve el detalle enriquecido de un lugar, combinando datos persistentes y consultas dinámicas (fotos, horarios actualizados).

```json
{
  "id": 1,
  "name": "Teatro Romano de Mérida",
  "description": "Edificio construido por Roma en la colonia Augusta Emerita...",
  "category": "MONUMENT",
  "location": {
    "latitude": 38.9153,
    "longitude": -6.3386,
    "address": "Plaza Margarita Xirgú, s/n"
  },
  "opening_hours": {
    "status": "OPEN",
    "periods": [...],
    "text": "Abierto hasta las 21:00"
  },
  "multimedia": {
    "main_image": "https://maps.googleapis.com/...",
    "attribution": "Google Maps"
  },
  "sources": [
    {"name": "TURISMO_MERIDA", "external_id": "BIC-123"},
    {"name": "GOOGLE_PLACES", "external_id": "ChIJ..."}
  ]
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
