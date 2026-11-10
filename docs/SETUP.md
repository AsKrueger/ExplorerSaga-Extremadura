# Puesta en marcha de ExplorerSaga Extremadura (v2)

La app v2 no necesita servidor: el contenido va incluido en la app y se actualiza desde GitHub Releases. El backend Spring Boot de la versión 1 sigue en [backend/](../backend/README.md), pero la app ya no lo usa.

## 1. Requisitos

| Herramienta | Versión | Para qué |
| --- | --- | --- |
| JDK | 17 (o 21) | Compilar con Gradle 9.6 |
| Android Studio | Compatible con AGP 9.4 | Compilar y ejecutar la app |
| Android SDK | Plataforma API 37 (mínimo API 26, Android 8.0) | Compilación y emulador |
| Python | 3.12, sin paquetes extra | Fábrica de datos (opcional) |

## 2. Configuración local

Copia `local.properties.example` a `local.properties` (no se versiona) y ajusta:

- `sdk.dir`: ruta del Android SDK. Android Studio la rellena solo.
- `feedback.email`: dirección que recibe **Ajustes › Enviar comentarios**.
- `release.*`: keystore de publicación (solo para Google Play, ver [play-store/RELEASE.md](play-store/RELEASE.md)).

## 3. Compilar, probar y ejecutar

```powershell
./gradlew assembleDebug             # compilar
./gradlew testDebugUnitTest         # 26 pruebas unitarias
./gradlew lintDebug                 # análisis estático
./gradlew installDebug              # instalar en el emulador o el móvil
./gradlew connectedDebugAndroidTest # 3 pruebas de interfaz (emulador encendido)
./gradlew bundleRelease             # AAB para Google Play
```

Las pruebas unitarias cubren:
- importación del paquete de contenido y verificación sha256 (`ContentImporterTest`);
- búsqueda sin tildes (`TextTest`);
- agenda de temporada (`SeasonWindowTest`);
- filtros, orden y destacados de Explorar e Inicio (`ExploreAndSeasonTest`);
- distancias (`GeoTest`);
- correo de comentarios (`FeedbackComposerTest`);
- almacenamiento (`StorageTest`).

Las pruebas de interfaz (`MainFlowsTest`) recorren la búsqueda, la ficha, Mi viaje, Inicio y Ajustes sobre la app real.

## 4. Contenido (fábrica de datos)

```powershell
cd tools/datafactory
python factory.py fetch          # descarga OSM, Wikidata, Wikipedia, Commons y Junta (unos 15 min)
python factory.py build --app    # genera el paquete y lo copia a la app
python -m unittest discover -s tests
```

Más detalles en [tools/datafactory/README.md](../tools/datafactory/README.md). El workflow [content-pack.yml](../.github/workflows/content-pack.yml) publica cada lunes el paquete en la release `content-pack`, de donde lo descarga la app.

## 5. Comprobaciones manuales antes de publicar

| Escenario | Pasos | Resultado esperado |
| --- | --- | --- |
| Primer arranque sin red | Instalar, activar el modo avión y abrir | Bienvenida y después Inicio con temporada, rutas e imprescindibles (contenido incluido) |
| Búsqueda | Explorar › «caceres» | Lugares de Cáceres aunque no se escriba la tilde |
| Mapa sin conexión | Ajustes › Descargar el mapa, después modo avión › Mapa | El mapa de Extremadura se ve completo, también en zonas no visitadas |
| Favoritos | Ficha › corazón › Mi viaje › elegir día | Aparece en el día elegido; sigue ahí tras actualizar el contenido o borrar la caché |
| Idioma | Ajustes › Idioma › English | Interfaz, nombres y descripciones en inglés (si existen) |
| Borrar datos | Ajustes › Borrar datos guardados | Mi viaje vacío, mapa descargado borrado y contenido incluido reimportado |

## 6. Problemas frecuentes

- **`SDK location not found`**: falta `local.properties` o su `sdk.dir`. Ábrelo con Android Studio o copia la plantilla.
- **Overpass responde 429/504 al ejecutar `fetch`**: la instancia pública está saturada. La fábrica reintenta; si falla, vuelve a lanzarlo más tarde o usa otra instancia con `OVERPASS_URL`.
- **«No se pudo comprobar» en Ajustes › Buscar actualizaciones**: aún no existe la release `content-pack`. Lanza el workflow *Content pack* en GitHub Actions.
