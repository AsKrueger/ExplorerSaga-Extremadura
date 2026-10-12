# Arquitectura: ExplorerSaga Extremadura

## 🌐 Visión General
ExplorerSaga utiliza una arquitectura desacoplada donde el núcleo del negocio es independiente de los proveedores externos de información (Turismo Mérida, Google Places).

## 🏗️ Flujo de Datos "Policy-Aware"
Dada la naturaleza restrictiva de algunas fuentes (Google Places EEE), la arquitectura distingue entre datos persistentes y transitorios:

1.  **Fuente Externa**: Provee información bruta (Place Details, BIC Open Data).
2.  **External DTO**: Representación temporal del dato original.
3.  **Política de Utilización**: Capa que decide, según el origen y la región (EEE), qué campos pueden persistirse y cuáles deben descartarse tras su uso.
4.  **Normalizador/Mapper**: Convierte el dato válido al modelo de dominio.
5.  **Domain Place**: El concepto central utilizado por la lógica de ExplorerSaga.

## 🔌 Componentes del Backend

### Orquestador de Fuentes
Gestiona la combinación de datos:
- **Datos Estáticos/Culturales**: Almacenados en PostgreSQL (procedentes de Open Data o recursos propios).
- **Datos Dinámicos**: Consultados en tiempo real (Horarios, Fotos de Google) para cumplir con las políticas de "frescura" y evitar almacenamiento ilegal.

### Estrategia de Persistencia Mínima
La base de datos solo almacena la información necesaria para la identidad y búsqueda:
- Identificadores de fuente (`PlaceSource`).
- Atributos básicos (Nombre, Ubicación base, Categoría).
- Metadatos de sincronización.

## ⚖️ Restricciones Identificadas (Issue #3)
- **Google Places**:
    - Almacenamiento del `place_id` permitido indefinidamente.
    - Otros contenidos sujetos a políticas de caché estrictas (EEE Julio 2025).
    - Atribución obligatoria a "Google Maps".
- **Turismo Mérida**:
    - Uso prioritario de datasets BIC (Bienes de Interés Cultural) por su estructura legal y técnica.

## 💰 Coste 0 €
- Infraestructura basada en OpenJDK y PostgreSQL.
- Uso de APIs de terceros dentro de los límites de cuota gratuita por SKU.
