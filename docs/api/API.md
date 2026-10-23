# API REST de ExplorerSaga

Documentación de la API implementada en el backend Spring Boot. La especificación interactiva completa (esquemas y ejemplos) está disponible en Swagger UI: `http://localhost:8080/swagger-ui/index.html`.

## 🌐 Configuración Base
- **Versión**: `v1`
- **Formato**: `application/json`
- **URL Base (Local)**: `http://localhost:8080/api/v1`
- **Desde el emulador Android**: `http://10.0.2.2:8080/api/v1`

## 🛣️ Endpoints

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `/health` | Estado del servicio (`{"status":"UP"}`). |
| `GET` | `/places` | Lista de lugares. Parámetro opcional `category`: filtra por nombre de categoría (coincidencia parcial, sin distinguir mayúsculas). |
| `GET` | `/places/{id}` | Detalle de un lugar. |
| `POST` | `/places` | Crea un lugar (`name`, `latitude`, `longitude`, `categoryId`, y opcionalmente `description` y `address`). |
| `POST` | `/places/import` | Importa lugares de Mérida desde OpenStreetMap/Overpass API. |
| `GET` | `/categories`, `/categories/{id}` | Lista o detalle de categorías. |
| `POST` | `/categories` | Crea una categoría. |
| `GET` | `/sources`, `/sources/{id}` | Lista o detalle de fuentes de datos. |
| `POST` | `/sources` | Crea una fuente (`code`, `name`, `description`). |
| `POST` | `/place-sources` | Vincula un lugar con una fuente externa (`placeId`, `sourceId`, `externalId`). |

### Lugar (`PlaceResponse`)
```json
{
  "id": 1,
  "name": "Teatro Romano de Mérida",
  "description": "Construido entre los años 16 y 15 a.C., ...",
  "address": "Plaza Margarita Xirgu, s/n, 06800 Mérida",
  "latitude": 38.915222,
  "longitude": -6.338278,
  "category": { "id": 6, "name": "monumento", "description": "Monumentos históricos y yacimientos romanos" }
}
```

### Categorías
Las categorías siguen un único vocabulario en español, compartido por el seed de Flyway, la importación desde OpenStreetMap y los datos locales de Android:

`monumento`, `iglesia`, `museo`, `restaurante`, `cafeteria`, `bar`, `heladeria`, `tienda_alimentos`, `souvenir`, `supermercado`, `libreria`, `ropa`, `tienda_general`.

### Importación (`POST /places/import`)
Respuesta `200 OK` con las métricas de la importación:
```json
{
  "processed": 220,
  "inserted": 202,
  "updated": 0,
  "skipped": 0,
  "failed": 0,
  "merged": 18
}
```

- `inserted`: lugares nuevos.
- `merged`: elementos de OSM que coinciden por nombre y cercanía con un lugar ya registrado (por ejemplo, del seed). Se enlazan a ese lugar en lugar de duplicarlo. El radio es de 750 m para monumentos, museos e iglesias y de 100 m para el resto.
- `updated`: lugares procedentes solo de OSM cuyos datos han cambiado.
- `skipped`: elementos ya importados sin cambios, o vinculados a varias fuentes (que conservan sus datos).
- `failed`: elementos que no se pudieron guardar.

La importación es idempotente: repetirla sin cambios en OSM devuelve `inserted: 0`, `updated: 0` y `merged: 0`.

Se descartan los alojamientos y puntos de información (`tourism=hotel`, `hostel`, `apartment`, `camp_site`, `information`…) y los comercios de servicios o vehículos (`shop=car`, `car_repair`, `hairdresser`, `bookmaker`…).

Si ninguna instancia de Overpass responde tras los reintentos configurados, la API devuelve **`502 Bad Gateway`** en lugar de una importación vacía.

## 🚥 Códigos de Estado HTTP
- `200 OK`: Éxito.
- `201 Created`: Recurso creado.
- `400 Bad Request`: Datos inválidos (por ejemplo, coordenadas fuera de rango).
- `404 Not Found`: Recurso no encontrado.
- `409 Conflict`: Recurso duplicado.
- `502 Bad Gateway`: El proveedor externo (Overpass API) no está disponible.
- `500 Internal Server Error`: Error inesperado del servidor.

Los errores se devuelven con el formato `ErrorResponse`:
```json
{ "status": 502, "error": "Bad Gateway", "message": "Overpass API no está disponible tras 2 ronda(s) de intentos: ...", "path": "/api/v1/places/import", "timestamp": "..." }
```

## ⚙️ Configuración del Proveedor Overpass (`application-dev.yml`)
```yaml
explorersaga:
  providers:
    overpass:
      base-urls: https://overpass-api.de/api/interpreter,https://overpass.kumi.systems/api/interpreter
      timeout-ms: 30000      # timeout de conexión y lectura por petición
      max-attempts: 2        # rondas sobre todas las instancias
      retry-backoff-ms: 2000 # espera antes de la siguiente ronda (se duplica en cada ronda)
```
