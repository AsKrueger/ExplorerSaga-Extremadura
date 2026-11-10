# Fábrica de datos de ExplorerSaga

Genera el **paquete de contenido** de toda Extremadura que usa la app: lugares, municipios, rutas y agenda de temporada, en español e inglés y con fuentes y licencias trazables. No necesita servidor: se publica en la release `content-pack` de GitHub.

## Fuentes

| Fuente | Qué aporta | Licencia |
| --- | --- | --- |
| OpenStreetMap (Overpass) | Ubicación, categoría, horario, web, teléfono y límites municipales | ODbL 1.0 |
| Wikidata | Nombre en inglés, patrimonio protegido, enlace a Wikipedia e imagen | CC0 |
| Wikipedia (es / en) | Descripción: la introducción del artículo | CC BY-SA 4.0 |
| Wikimedia Commons | Foto con autor y licencia (solo licencias libres) | La de cada imagen |
| Junta de Extremadura | Registro oficial de alojamientos hoteleros (sin coordenadas, se cruzan con OSM por nombre y municipio) | CC BY 4.0 |
| `curated/` | Rutas temáticas y agenda de temporada, redactadas a mano en los dos idiomas | Del proyecto |

## Uso

Solo usa la biblioteca estándar de Python 3.12.

```bash
cd tools/datafactory
python factory.py fetch               # descarga las fuentes en .cache/ (unos minutos)
python factory.py build --app         # genera dist/ y copia el paquete a la app
python -m unittest discover -s tests  # pruebas
```

- `fetch --refresh` vuelve a descargar todo. Sin `--refresh` reutiliza la caché.
- `build` no usa la red: con la misma caché y la misma `--version`, el paquete es idéntico byte a byte.
- `--app` copia el paquete (como `pack.bin`, porque aapt quita la extensión `.gz`) y `manifest.json` a `app/src/main/assets/content/`. Es el paquete con el que arranca la app sin conexión.

## Salida

- `dist/pack.json.gz` es el paquete (JSON comprimido): `places`, `municipalities`, `routes`, `events` y `attribution`.
- `dist/manifest.json` contiene la versión (`AAAAMMDD`), el `sha256`, el tamaño y los recuentos. La app compara la versión y comprueba el hash antes de importar.

Los ids de los lugares son estables (`n123`, `w456` o `r789`, el tipo y el id de OSM), así que los favoritos sobreviven a las actualizaciones.

## Publicación

El workflow [content-pack.yml](../../.github/workflows/content-pack.yml) se ejecuta cada lunes o a mano (*Run workflow*). Pasa las pruebas, genera el paquete y lo sube a la release `content-pack`. La app lo descarga de:

```
https://github.com/AsKrueger/ExplorerSaga-Extremadura/releases/download/content-pack/manifest.json
```
