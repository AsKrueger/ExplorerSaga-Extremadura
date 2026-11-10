# ExplorerSaga Extremadura

Guía turística de **toda Extremadura** para Android: más de 5.700 lugares de Cáceres y Badajoz, mapa vectorial sin conexión, rutas, agenda de temporada y Mi viaje, en español e inglés. Gratis, sin cuentas y sin rastreadores.

Nació como Trabajo de Fin de Grado (guía de Mérida) y en la versión 2 se rehízo como app regional, sin servidor propio: el contenido se genera con datos abiertos y se publica en GitHub.

## Funcionalidades

- **Inicio**: «Ahora en temporada» (cerezo en flor del Jerte, berrea en Monfragüe, Festival de Mérida…), lugares cerca de ti, rutas e imprescindibles, con Mérida en primer lugar.
- **Explorar**: búsqueda de texto completo sin tildes («caceres» encuentra «Cáceres»), 8 grupos (patrimonio, museos, naturaleza, zonas de baño, comer, dormir, compras y servicios), filtros por provincia y municipio y orden por cercanía.
- **Mapa** (MapLibre + OpenFreeMap): agrupa los lugares por zonas, muestra tu ubicación y se puede descargar el de Extremadura (unos 60 MB) para usarlo sin conexión.
- **Ficha de lugar**: foto de Wikimedia Commons con autor y licencia, descripción de Wikipedia en cada idioma, cómo llegar, llamar, web, compartir, horario, accesibilidad y fuentes.
- **Rutas**: 8 itinerarios (Mérida romana, Cáceres monumental, Vía de la Plata, Conquistadores, Monfragüe, Valle del Jerte, Geoparque y judería de Hervás) con mapa, paradas y navegación en la app de mapas.
- **Mi viaje**: favoritos organizados por días, sin conexión. Se conservan al actualizar el contenido.
- **Servicios útiles**: farmacia, centro de salud, oficina de turismo y cargador más cercanos, y llamada al 112.
- **Ajustes**: idioma (español o inglés), tema, imágenes sin conexión, actualización del contenido (también solo con wifi), mapa sin conexión, borrar caché y datos, y enviar comentarios por correo.

## Arquitectura

```
tools/datafactory  ──►  pack.json.gz + manifest.json  ──►  GitHub Release "content-pack"
(OSM, Wikidata,          (versionado, con sha256)              │
 Wikipedia, Commons,            │                              ▼
 Junta de Extremadura)          └── copia en la app ──►  Room (+ FTS) ◄── WorkManager
                                    (primer arranque)          │
                                                        ViewModels ─► Jetpack Compose
```

- **App**: Kotlin, Jetpack Compose (Material 3), Navigation con rutas tipadas, Room con FTS4, DataStore, WorkManager, Coil 3, OkHttp y MapLibre Native.
- **Fábrica de datos**: Python 3.12 sin dependencias. Es reproducible: con la misma caché genera el mismo paquete byte a byte. Ver [tools/datafactory/README.md](tools/datafactory/README.md).
- **Backend Spring Boot** (`backend/`): versión 1 del TFG. La app v2 ya no lo usa.

## Puesta en marcha

Ver [docs/SETUP.md](docs/SETUP.md). En resumen: JDK 17, Android SDK 37 y `./gradlew installDebug`.

## Pruebas

| Suite | Comando | Pruebas |
| --- | --- | --- |
| Unitarias Android | `./gradlew testDebugUnitTest` | 26 |
| Interfaz en emulador | `./gradlew connectedDebugAndroidTest` | 3 |
| Fábrica de datos | `python -m unittest discover -s tests` (en `tools/datafactory`) | 16 |
| Backend v1 | `./mvnw test` (en `backend`) | 48 |

## Publicación

[docs/play-store/RELEASE.md](docs/play-store/RELEASE.md) explica la firma, el AAB y Play Console. [docs/play-store/listing.md](docs/play-store/listing.md) tiene la ficha en español e inglés y el formulario de seguridad de los datos. La política de privacidad está en [docs/privacy-policy.md](docs/privacy-policy.md).

## Datos y licencias

Lugares y mapas © colaboradores de OpenStreetMap (ODbL). Wikidata (CC0), Wikipedia (CC BY-SA 4.0), fotos de Wikimedia Commons con la licencia de cada una y datos abiertos de la Junta de Extremadura (CC BY 4.0). Código bajo licencia MIT ([LICENSE](LICENSE)).
