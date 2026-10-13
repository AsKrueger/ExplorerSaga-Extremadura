# Guía de Instalación, Configuración y Puesta en Marcha — ExplorerSaga Extremadura

Este documento proporciona las instrucciones paso a paso para desplegar, ejecutar, probar y demostrar la versión candidata del MVP de **ExplorerSaga Extremadura**, abarcando la base de datos PostgreSQL, el backend Spring Boot 3 y la aplicación Android nativa en Kotlin con Jetpack Compose.

---

## 📋 1. Requisitos Previos y Herramientas

Para ejecutar el entorno completo de desarrollo de forma reproducible, se requieren las siguientes herramientas:

| Herramienta | Versión Recomendada | Propósito |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | JDK 17 o JDK 21 | Entorno de ejecución para el Backend Spring Boot |
| **Apache Maven** | 3.9+ (o usar `mvnw` / `mvnw.cmd` incluido) | Gestión de dependencias y construcción del Backend |
| **Docker & Docker Compose** | Docker 24+ / Compose v2.20+ | Contenerización y despliegue local de PostgreSQL 15 |
| **PostgreSQL** | 15-alpine (vía Docker) | Base de datos relacional para persistencia de datos turísticos |
| **Android Studio** | Ladybug (2024.2+) o superior | IDE para compilación y ejecución de la app Android |
| **Android SDK / Emulator** | API Level 35 (mínimo API 26 / Android 8.0) | Entorno de ejecución móvil |

---

## 🗄️ 2. Configuración y Arranque de la Base de Datos (PostgreSQL)

El proyecto incluye un archivo `docker-compose.yml` preconfigurado en la carpeta `backend/`.

### Pasos para iniciar la base de datos:

1. Navegar a la carpeta del backend:
   ```bash
   cd backend
   ```

2. Iniciar el contenedor de PostgreSQL en segundo plano:
   ```bash
   docker compose up -d
   ```

3. Verificar que el contenedor `explorersaga-db` se encuentra activo:
   ```bash
   docker compose ps
   ```

### Datos de Conexión Predeterminados:
- **Host:** `localhost`
- **Puerto:** `5432`
- **Base de datos:** `explorersaga`
- **Usuario:** `explorersaga_user` (configurable mediante variable `DB_USER`)
- **Contraseña:** `explorersaga_password` (configurable mediante variable `DB_PASSWORD`)

> ℹ️ **Migraciones de Base de Datos:** Las tablas y restricciones de la base de datos se crean automáticamente mediante **Flyway** al arrancar el backend (`V1__Initial_Schema.sql`).

---

## ⚙️ 3. Configuración y Perfiles del Backend

La configuración del backend está dividida en perfiles dentro de `backend/src/main/resources/`:

- `application.yml`: Configuración general del servidor en el puerto `8080` (perfil activo por defecto: `dev`).
- `application-dev.yml`: Configuración para desarrollo local conectando a PostgreSQL en `localhost:5432` y Overpass API (`https://overpass-api.de/api/interpreter`).
- `application-test.yml`: Configuración para pruebas automatizadas utilizando la base de datos en memoria **H2** en modo PostgreSQL con Flyway habilitado.

---

## 🚀 4. Compilación y Ejecución del Backend

### Ejecutar las Pruebas Automatizadas del Backend
Antes de iniciar el servidor, se puede validar la suite completa de pruebas unitarias e integración:

- **Windows (PowerShell/CMD):**
  ```cmd
  cd backend
  .\mvnw.cmd test
  ```
- **Linux/macOS:**
  ```bash
  cd backend
  ./mvnw test
  ```

*Resultado esperado:* **36/36 tests pasando exitosamente (`BUILD SUCCESS`).**

### Iniciar el Backend Spring Boot
Para arrancar la aplicación backend:

- **Windows:**
  ```cmd
  cd backend
  .\mvnw.cmd spring-boot:run
  ```
- **Linux/macOS:**
  ```bash
  cd backend
  ./mvnw spring-boot:run
  ```

### Verificación de Disponibilidad:
1. **Endpoint de Salud (Health Check):**
   ```bash
   curl http://localhost:8080/api/v1/health
   ```
   *Respuesta esperada:* `{"status":"UP"}`

2. **Documentación Swagger / OpenAPI Interactiva:**
   Abrir en el navegador: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## 🔄 5. Procedimiento Manual de Importación de Datos (Overpass API)

Para poblar la base de datos PostgreSQL con los lugares turísticos reales de Mérida provenientes de OpenStreetMap:

### Ejecutar Importación vía cURL:
```bash
curl -X POST http://localhost:8080/api/v1/places/import
```

*Respuesta esperada (JSON con métricas de la importación):*
```json
{
  "processed": 42,
  "inserted": 42,
  "updated": 0,
  "skipped": 0,
  "failed": 0
}
```

### Probar Idempotencia:
Si se vuelve a ejecutar la misma petición inmediatamente:
```bash
curl -X POST http://localhost:8080/api/v1/places/import
```
*Respuesta esperada:* `processed: 42`, `inserted: 0`, `updated: 0`, `skipped: 42`. Ningún registro se duplica gracias al identificador unívoco de fuente externa.

### Consultar Lugares Almacenados:
```bash
# Listado completo
curl http://localhost:8080/api/v1/places

# Filtrar por categoría (ej. Monumentos)
curl "http://localhost:8080/api/v1/places?category=Monument"
```

---

## 📱 6. Configuración y Ejecución de la Aplicación Android

### 1. Requisitos de Red y Emulador Local
- La aplicación Android está preconfigurada en `RetrofitClient` para conectarse a `http://10.0.2.2:8080/`, que es la dirección especial reservada por el emulador de Android para acceder al `localhost` del equipo anfitrión.
- Se ha incluido `network_security_config.xml` en la app para permitir explícitamente el tráfico HTTP no cifrado hacia `10.0.2.2` y `localhost` durante el desarrollo local.

### 2. Ejecutar Pruebas Automatizadas de Android
- Desde la raíz del repositorio o usando Gradle:
  ```bash
  ./gradlew test
  ```

### 3. Iniciar la App en el Emulador
1. Abrir la carpeta raíz del proyecto `ExplorerSaga-Extremadura` en Android Studio.
2. Sincronizar Gradle (`Gradle Sync`).
3. Seleccionar un dispositivo AVD en el Device Manager (ej. *Pixel 8 - API 35*).
4. Pulsar **Run 'app'** (`Shift + F10`).

---

## 🧪 7. Pruebas de Aceptación E2E y Resiliencia

| Escenario de Prueba | Procedimiento | Comportamiento Esperado |
| :--- | :--- | :--- |
| **Backend Activo + Datos Cargados** | Abrir la app Android con el backend Spring Boot corriendo. | Se muestran los lugares remotos importados de OSM. En la parte superior de los listados aparece el indicador verde `🟢 Conectado al Backend`. |
| **Navegación y Filtrado** | Seleccionar la categoría "Monumentos" o aplicar un filtro. | La app muestra los monumentos filtrados recibidos del backend. |
| **Caída del Backend / Modo Offline** | Detener el backend (`Ctrl+C` en la terminal del backend) o apagar la red en el emulador. | La app detecta el fallo de red sin cerrarse de forma abrupta y conmuta transparentemente al respaldo local basado en archivos JSON en assets. |
| **Indicador de Origen de Datos** | Comprobar la UI en modo offline. | En la pantalla de listados se muestra el indicador amarillo `🟡 Modo Local (Respaldo)`. |
| **Reintento de Conexión** | Volver a iniciar el backend y pulsar el botón **"Reintentar"** en la app. | La app realiza una nueva petición HTTP y restablece la conexión remota con el backend (`🟢 Conectado al Backend`). |
| **Persistencia tras Reinicio** | Detener y reiniciar el contenedor de PostgreSQL (`docker compose restart db`). | Los datos previamente importados de OSM permanecen guardados intactos en la base de datos PostgreSQL. |

---

## 🛠️ 8. Solución de Problemas Frecuentes

1. **Error de conexión a PostgreSQL (`Connection refused` / `port 5432`):**
   - Verificar que el contenedor de Docker está corriendo con `docker compose ps`.
   - Asegurarse de que ningún otro servicio local (como una instancia local de PostgreSQL) está ocupando el puerto 5432.

2. **Android no conecta al Backend (`Modo Local` persistente en emulador):**
   - Asegurarse de que el backend Spring Boot está corriendo en el puerto 8080 (`http://localhost:8080/api/v1/health` responde UP).
   - Recordar que en el emulador de Android debe usarse la IP `10.0.2.2` en lugar de `localhost`. Si se utiliza un dispositivo físico conectado por USB, se debe configurar `adb reverse tcp:8080 tcp:8080` o cambiar la URL base en `RetrofitClient` a la IP local de la red Wi-Fi.

3. **Límite de peticiones de Overpass API (HTTP 429 / 504 Gateway Timeout):**
   - Overpass API es una API pública compartida con límites de frecuencia. Si la importación falla por timeout, reintentar tras unos segundos. La lógica del backend captura el error amigablemente y no corrompe la base de datos.

4. **Error de compilación en Gradle (`SDK location not found`):**
   - **Causa:** El archivo `local.properties` no existe o no define la ruta válida del SDK de Android en el sistema anfitrión (`sdk.dir`). Este archivo está excluido del control de versiones (`.gitignore`) por contener rutas específicas de cada desarrollador.
   - **Solución A (Android Studio):** Abrir la carpeta raíz del proyecto en Android Studio; la IDE creará automáticamente el archivo `local.properties` con la ruta de tu SDK local.
   - **Solución B (Terminal / CLI):** Copia la plantilla `local.properties.example` a `local.properties` en la raíz del proyecto y ajusta la propiedad `sdk.dir` a la ruta de tu SDK:
     - **Windows:** `sdk.dir=C\:/Users/TU_USUARIO/AppData/Local/Android/Sdk`
     - **Linux:** `sdk.dir=/home/TU_USUARIO/Android/Sdk`
     - **macOS:** `sdk.dir=/Users/TU_USUARIO/Library/Android/sdk`
   - **Solución C (Variable de entorno):** Configurar la variable de entorno del sistema `ANDROID_HOME` apuntando al directorio raíz de tu SDK de Android.

---

## 📌 9. Limitaciones Conocidas del MVP

- **Llamada manual de importación:** La sincronización con Overpass API se desencadena actualmente mediante una petición HTTP POST manual (`/api/v1/places/import`), reservando la automatización periódica en segundo plano para fases posteriores.
- **Formato de categorías:** Las categorías de OpenStreetMap importadas utilizan nombres estándar en inglés (`Historical Monument`, `Tourism Attraction`, `Religious Site`), las cuales han sido mapeadas en la UI de Android para convivir de forma transparente con los datos locales en español.
- **Rutas turísticas:** La función de rutas en el mapa está planificada como una extensión futura.
