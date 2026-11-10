# Publicar ExplorerSaga en Google Play

## 1. Keystore de publicación (una sola vez)

```powershell
keytool -genkeypair -v -keystore C:\ruta\segura\explorersaga-release.jks -keyalg RSA -keysize 4096 -validity 10000 -alias explorersaga
```

Guarda el `.jks` y sus contraseñas **fuera del repositorio** y con copia de seguridad. Con Play App Signing, Google guarda la clave de firma de la app y este keystore es la clave de subida.

Añade a `local.properties` (no se versiona):

```properties
release.storeFile=C:/ruta/segura/explorersaga-release.jks
release.storePassword=...
release.keyAlias=explorersaga
release.keyPassword=...
```

Sin estas líneas la release se firma con la clave de depuración, que sirve para probar en local pero Google Play rechaza.

## 2. Contenido actualizado

```powershell
cd tools/datafactory
python factory.py fetch --refresh
python factory.py build --app
cd ../..
```

Esto deja en la app el paquete del día. Después, en GitHub › Actions › **Content pack** › *Run workflow*, publica el mismo paquete en la release `content-pack`, de donde la app descarga las actualizaciones.

## 3. Compilar y comprobar

```powershell
./gradlew clean testDebugUnitTest lintDebug connectedDebugAndroidTest bundleRelease
```

El AAB queda en `app/build/outputs/bundle/release/app-release.aab`. Antes de cada subida, sube `versionCode` en `app/build.gradle.kts`.

## 4. Play Console

1. Crea la app (nombre, idioma predeterminado español, app gratuita).
2. Rellena **Contenido de la app** con [listing.md](listing.md): privacidad, seguridad de los datos, clasificación, público objetivo y anuncios.
3. Rellena la **ficha de Play Store** en español e inglés con los textos de [listing.md](listing.md).
4. **Pruebas › Prueba interna**: crea una versión, sube el AAB y añade tu cuenta como tester.
5. Instala desde el enlace de prueba interna y repasa los flujos principales. Después, promociona a producción, pasando antes por prueba cerrada si la cuenta de desarrollador es personal y nueva (Google exige 12 testers durante 14 días).
