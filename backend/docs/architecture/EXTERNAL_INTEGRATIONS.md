# Diseño de Integración con Fuentes Externas (ExplorerSaga Backend)

Este documento detalla el diseño arquitectónico, la evaluación de fuentes y la estrategia de integración provider-neutral para incorporar datos turísticos externos en **ExplorerSaga Backend** (Issue #12).

---

## 1. Fuentes Evaluadas y Comparativa

| Criterio | Open Data Extremadura | OpenStreetMap / Overpass | Wikidata | Google Places | Turismo Mérida |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Cobertura Regional** | Alta (Extremadura) | Global (Excelente en Mérida) | Global | Global | Alta (Solo Mérida) |
| **Identificador Externo** | IDs propios / URIs | `node/way/id` estables | Q-IDs (`Q12345`) | `place_id` / `reference` | IDs locales |
| **Formato** | JSON / CSV / XML | JSON / XML (Overpass QL) | JSON (SPARQL) | JSON (REST) | HTML / JSON |
| **API** | REST / CKAN | Overpass API HTTP | SPARQL Endpoint | Google Places REST | Web / Parcial |
| **Coste** | **0 € (Público)** | **0 € (Público/Open)** | **0 € (Público)** | De pago (Free tier limitado) | **0 € (Público)** |
| **Licencia** | CC BY 4.0 / Open Data | ODbL | CC0 / Public Domain | Restrictiva (T&C Google) | Institucional |
| **Atribución** | Requerida | Requerida (© OpenStreetMap) | Opcional/Requerida | Requerida (Google) | Requerida |
| **Rate Limits** | Generosos / Libres | Moderados (uso razonable) | Altos (SPARQL limits) | Estrictos (QPS / Billing) | N/A |
| **Calidad de Datos** | Oficial institucional | Alta en patrimonio local | Enriquecida pero dispersa | Muy alta | Oficial local |
| **Estabilidad** | Institucional | Alta | Alta | Muy alta | Variable |

---

## 2. Fuente Seleccionada para la Primera Implementación

* **Selección**: **Open Data Extremadura** combinada con **OpenStreetMap (Overpass API)** como fuente complementaria de geolocalización.
* **Justificación**:
  1. **Alineación con el objetivo de 0 €**: Ambas fuentes son 100% públicas, gratuitas y de libre acceso sin tarjeta de crédito ni riesgo de facturación.
  2. **Licencias Abiertas**: Compatibles con los términos de redistribución de ExplorerSaga (CC BY / ODbL).
  3. **Relevancia Local**: Open Data Extremadura aporta información oficial de recursos turísticos en la región, mientras que Overpass API aporta precisión cartográfica y coordenadas exactas para los monumentos de Mérida y Extremadura.

---

## 3. Arquitectura del External Adapter

La integración se diseñará bajo el principio **provider-neutral**, aislando por completo al núcleo de la aplicación mediante un **Output Port** específico (`TourismDataProvider`).

```text
                 ┌──────────────────────────┐
                 │ Open Data Extremadura /  │
                 │ Overpass API (Provider)  │
                 └────────────┬─────────────┘
                              │ HTTP / JSON
                              ▼
                 ┌──────────────────────────┐
                 │ External Adapter         │
                 │ (Infrastructure Layer)   │
                 └────────────┬─────────────┘
                              │
                              ▼
                 ┌──────────────────────────┐
                 │ External DTOs            │
                 │ (Deserialized Model)     │
                 └────────────┬─────────────┘
                              │
                              ▼
                 ┌──────────────────────────┐
                 │ Normalization / Mapping  │
                 └────────────┬─────────────┘
                              │
                              ▼
                 ┌──────────────────────────┐
                 │ application.port.out     │
                 │ (TourismDataProvider)    │
                 └────────────┬─────────────┘
                              │
                              ▼
                 ┌──────────────────────────┐
                 │ Application Services     │
                 └──────────────────────────┘
```

---

## 4. Contrato del Puerto (`TourismDataProvider`)

Se ubicará en `com.alonso.explorersaga.application.port.out`:

```java
public interface TourismDataProvider {
    List<NormalizedPlaceData> fetchTourismPlaces();
}
```

Donde `NormalizedPlaceData` es un modelo plano agnóstico de la fuente externa:
* `externalId`
* `sourceCode`
* `name`
* `description`
* `address`
* `latitude`
* `longitude`
* `categoryName`

---

## 5. External DTOs y Mapeo

* **External DTOs**: Clases separadas (ej. `OpenDataPlaceResponse`, `OverpassElementDto`) para deserializar la respuesta JSON de la API externa.
* **Mapping**: El `External Adapter` será el único responsable de transformar el `External DTO` en el objeto normalizado de la aplicación. El dominio y los servicios de aplicación nunca verán la estructura cruda del proveedor.

---

## 6. Normalización e Identidad Externa

* **Normalización**: 
  * Limpieza de espacios, normalización de acentos y conversión de coordenadas a `Double` válido dentro de rangos (`[-90, 90]`, `[-180, 180]`).
  * Asignación automática o mapeo a categorías internas existentes (`Category`).
* **Identidad Externa (`PlaceSource`)**:
  * Cada elemento importado se asociará a una entidad `Source` (ej. `code = "OPEN_DATA_EXT"` o `"OSM"`).
  * La identidad única externa se garantizará mediante la tupla relacional ya existente:
    $$\text{Unique Constraint: } (\text{source\_id}, \text{external\_id})$$

---

## 7. Deduplicación Futura

Para futuras importaciones masivas, se establece la siguiente estrategia de deduplicación:
1. **Coincidencia exacta de `(source_id, external_id)`**: Actualización de datos si el registro ya existe.
2. **Coincidencia por proximidad y nombre normalizado**: Si no hay ID externo previo, se evaluará la cercanía geográfica ($\Delta < 20\text{ metros}$) combinada con similitud fonética de nombres.

---

## 8. Gestión de Errores y Resiliencia

* **Timeouts y Fallas HTTP**: El adaptador envolverá las llamadas HTTP en manejo de excepciones de infraestructura, transformándolas en excepciones de proveedor (`ExternalServiceException`) sin propagar detalles de librerías HTTP (como RestClient/WebClient) al dominio.
* **Degradación Grácil**: Si la fuente externa no está disponible, la aplicación principal seguirá operando con normalidad utilizando la base de datos local PostgreSQL.

---

## 9. Configuración Externa (`application.yml`)

Las credenciales, URLs base y timeouts se inyectarán mediante propiedades externalizadas:

```yaml
explorersaga:
  providers:
    opendata:
      base-url: https://opendata.extremadura.es/api/v1
      timeout-ms: 5000
      enabled: true
```

---

## 10. Coste Operativo

* **Coste económico**: **0 € / mes**.
* **Infraestructura**: Sin servidores dedicados adicionales; las consultas se realizarán on-demand o mediante sincronización planificada ligera.

---

## 11. Estrategia de Testing

1. **Adapter Unit Tests**: Pruebas con archivos JSON estáticos simulando respuestas de la API externa (WireMock o MockRestServiceServer).
2. **Application Tests**: Uso de fakes/mocks del puerto `TourismDataProvider` para verificar la lógica de sincronización.
3. **Integration Tests**: Aislados para evitar dependencia de red continua en CI/CD.

---

## 12. Decisiones Descartadas

* **Usar Google Places como fuente principal**: Descartado por su coste a escala y condiciones restrictivas de almacenamiento de datos.
* **Inyectar DTOs externos en la capa de servicio**: Descartado por violar la independencia de la arquitectura hexagonal.

---

## 13. Plan de Implementación (Próximas Issues)

1. **Issue #13 (o posterior)**: Implementar el cliente HTTP (RestClient) y el adaptador concreto para Open Data Extremadura / Overpass API.
2. **Sincronización**: Crear un caso de uso o comando de aplicación para ingestar y persistir los lugares normalizados utilizando `PlaceSource`.
