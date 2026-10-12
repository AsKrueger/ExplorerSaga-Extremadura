# Modelo de Dominio y Evaluación de Separación Domain/JPA (ExplorerSaga Backend)

Este documento registra la auditoría formal y la decisión arquitectónica correspondiente a la **Issue #11**, evaluando si las entidades actuales de ExplorerSaga Backend justifican una separación estricta entre modelos de dominio puros y entidades de persistencia JPA, o si conviene mantener un modelo compartido respaldado por los Output Ports ya establecidos.

---

## 1. Auditoría de Componentes del Dominio

### 1.1. `Category`
* **Responsabilidad**: Clasificación temática de los lugares turísticos (ej. Monumentos, Museos).
* **Invariantes**: 
  * El nombre es obligatorio, único y no puede estar vacío (`@NotBlank`, `unique = true`, máx. 100 caracteres).
* **Comportamiento**: Modelo anémico centrado en estado (CRUD básico).
* **Decisión**: Se mantiene como entidad de dominio/persistencia unificada. No requiere un modelo de dominio separado debido a su simplicidad (nombre y descripción).

### 1.2. `Source`
* **Responsabilidad**: Proveedor externo de información turística (ej. OpenData, OSM).
* **Invariantes**: 
  * El código (`code`) es obligatorio, único y corto (máx. 50 caracteres).
  * El nombre es obligatorio.
* **Comportamiento**: Modelo anémico centrado en metadatos del proveedor.
* **Decisión**: Se mantiene unificado. No aporta valor añadir una capa de mapeo Domain ↔ JPA para una entidad de referencia estática.

### 1.3. `Place`
* **Responsabilidad**: Lugar turístico principal gestionado por el sistema.
* **Invariantes**:
  * Nombre obligatorio (máx. 255 caracteres).
  * Coordenadas geográficas obligatorias y acotadas:
    * Latitud: $[-90.0, 90.0]$
    * Longitud: $[-180.0, 180.0]$
  * Debe estar asociado a una `Category` existente (`category_id NOT NULL`).
* **Comportamiento**: Contiene validaciones en servicio de aplicación/DTO (`PlaceCreateRequest`), pero posee semántica central de negocio.
* **Decisión**: Se mantiene como modelo compartido. Aunque posee reglas de validación espacial, el mapeo actual con Hibernate es directo y transparente.

### 1.4. `PlaceSource`
* **Responsabilidad**: Tabla asociativa que vincula un `Place` interno con el identificador externo (`external_id`) de una `Source`.
* **Invariantes**:
  * Relación obligatoria con `Place` y `Source`.
  * Identificador externo obligatorio.
  * Restricción de unicidad compuesta: `(source_id, external_id)`.
* **Decisión**: Se mantiene como entidad de relación relacional/asociativa.

---

## 2. Evaluación de Value Objects (`Location`)

* **Concepto**: Representación geométrica de las coordenadas geográficas de un lugar (`latitude`, `longitude`).
* **Análisis**: Podría modelarse como un Value Object inmutable (`Location`) para encapsular las reglas de validación del rango de latitud/longitud.
* **Decisión en Issue #11**: Se **descarta temporalmente** su extracción como Value Object embebido de Hibernate (`@Embeddable`) porque el beneficio actual sobre los campos planos `Double latitude` / `Double longitude` es marginal y añadiría complejidad de refactorización sin resolver ninguna limitación real de negocio.

---

## 3. Evaluación de Aggregates

* **Análisis**: ¿Existen límites de agregados complejos (Aggregate Roots)?
* **Conclusión**: El sistema opera principalmente con entidades independientes y una relación asociativa (`PlaceSource`). `Place` actúa conceptualmente como la raíz de interés principal, pero no existen reglas transaccionales complejas de grafos de objetos que justifiquen definir Aggregates estrictos con patrones DDD avanzados en este momento.

---

## 4. Decisión Arquitectónica Definitiva sobre Domain vs. JPA

> ### 🛑 Conclusión: NO SEPARA todavía Domain y JPA en la Issue #11.

### Justificación:
1. **Pragmatismo y Simplicidad**: Las entidades actuales (`Category`, `Source`, `Place`, `PlaceSource`) son esencialmente modelos relacionales limpios que ya actúan eficazmente como DTOs/entidades de transporte gracias a los Output Ports y DTOs de respuesta.
2. **Evitar Sobrecoste (Over-engineering)**: Introducir entidades de dominio duplicadas, mappers bidireccionales (`Domain ↔ JPA`) y fábricas en este punto añadiría una cantidad considerable de código boilerplate sin aportar un beneficio real en legibilidad, testabilidad o flexibilidad.
3. **Aislamiento Logrado**: Con los **Input Ports** (#9) y **Output Ports** (#10) ya implementados, la capa de aplicación y los controladores están totalmente desacoplados de Spring Data y de los detalles de infraestructura. El acceso a bases de datos está confinado en los `Persistence Adapters`.

---

## 5. Próximos Pasos Arquitectónicos

La arquitectura actual queda formalizada como:

```text
REST Adapters
      ↓
Input Ports (Use Cases)
      ↓
Application Services
      ↓
Output Ports (Repositories)
      ↓
Persistence Adapters
      ↓
JPA Entities / Shared Domain Model
      ↓
Spring Data JPA / PostgreSQL
```

Esta decisión consciente y documentada evita la complejidad prematura mientras mantiene una arquitectura hexagonal limpia, robusta y 100% testeable (33/33 tests en verde).
