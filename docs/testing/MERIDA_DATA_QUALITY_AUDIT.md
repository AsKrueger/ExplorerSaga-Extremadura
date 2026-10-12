# Auditoría de Cobertura y Calidad de Datos Turísticos de Mérida

**Proyecto:** ExplorerSaga-Extremadura  
**Issue:** #19 — Auditoría de cobertura y calidad de datos turísticos de Mérida  
**Fecha de Evaluación:** Octubre de 2026  
**Entorno:** Backend Spring Boot 3.3.0, PostgreSQL 15 / H2, Overpass API (OpenStreetMap)

---

## 1. Introducción y Objetivo

Esta auditoría evalúa la cobertura, precisión y calidad de los datos turísticos de **Mérida** obtenidos mediante la integración con OpenStreetMap (Overpass API) implementada en la Issue #13 y validada en la Issue #18. El propósito es determinar si el volumen y la calidad de los datos actuales son suficientes para respaldar un MVP funcional y útil sin necesidad inmediata de incorporar una segunda fuente externa.

---

## 2. Método de Evaluación

La evaluación se ha llevado a cabo mediante:
1. **Inspección de la consulta Overpass** (`OverpassAdapter.java`) acotada al perímetro geográfico e histórico de Mérida (`38.88,-6.36,38.95,-6.30`).
2. **Análisis de las entidades importadas** y su categorización en la base de datos local.
3. **Verificación de la integridad de coordenadas** y propiedades clave (nombre, dirección, tipo de elemento OSM como `node` o `way`).
4. **Comparación con el conjunto de datos estáticos locales** de respaldo (`monuments.json`, etc.).

---

## 3. Inventario y Distribución por Categorías

El adaptador de Overpass API filtra y clasifica los elementos en tres categorías principales dentro del modelo de ExplorerSaga:

| Categoría Interna | Descripción en OpenStreetMap | Porcentaje Estimado | Cobertura en Mérida |
| :--- | :--- | :--- | :--- |
| **Historical Monument** | `historic=monument`, ruinas romanas, murallas | ~50% | Excelente (Teatro Romano, Anfiteatro, Templo de Diana, Arco de Trajano) |
| **Tourism Attraction** | `tourism=attraction`, puntos de interés general | ~30% | Alta (Museos, centros de interpretación) |
| **Religious Site** | `amenity=place_of_worship`, basílicas, iglesias | ~20% | Alta (Concatedral de Santa María, Basílica de Santa Eulalia) |

---

## 4. Calidad y Consistencia de Datos

- **Nombres y Coordenadas**: La precisión espacial de OpenStreetMap en Mérida es sobresaliente, ofreciendo coordenadas exactas de latitud y longitud para cada monumento y atracción.
- **Identidad Externa**: El uso de la identidad compuesta `type/id` (`node/123`, `way/456`) evita colisiones y garantiza una identificación unívoca.
- **Descripciones y Direcciones**: Algunas entidades en OpenStreetMap carecen de descripciones detalladas o direcciones estructuradas completas (`addr:street`). No obstante, el sistema de respaldo y normalización compensa estas carencias permitiendo complementar los datos con información local de assets o descripciones estándar.
- **Idempotencia**: Las ejecuciones repetidas de la importación no generan duplicados gracias a la restricción única `(source_id, external_id)` en la tabla `place_sources`.

---

## 5. Limitaciones de la Fuente Actual

1. **Variabilidad en Descripciones**: Al ser una fuente colaborativa (OSM), el nivel de detalle en las etiquetas de descripción (`description`) varía de un monumento a otro.
2. **Dependencia de Conectividad**: Requiere acceso a la red de Overpass API en el momento de la ingesta (mitigado con éxito mediante el modo offline y respaldo local en Android).

---

## 6. Decisión Justificada sobre una Segunda Fuente Externa

> ### 🛑 Conclusión: NO ES NECESARIA una segunda fuente externa (como Open Data Extremadura) para el MVP.

### Justificación:
- **Cobertura Suficiente**: OpenStreetMap cubre la totalidad de los monumentos romanos y puntos de interés histórico-artísticos esenciales de Mérida con alta precisión geográfica.
- **Cumplimiento de Costes**: La fuente actual mantiene un coste operativo de **0 €**, licencia abierta (ODbL) y estabilidad contrastada.
- **Estrategia Recomendada**: Consolidar el MVP con la fuente actual y posponer la integración de Open Data Extremadura para fases posteriores (post-MVP).

---

## 7. Acciones Recomendadas y Prioridad

1. **Prioridad Baja**: Añadir descripciones enriquecidas opcionales en el repositorio local para aquellos puntos de interés cuya descripción en OSM sea demasiado escueta.
2. **Prioridad Baja**: Mantener el adaptador Overpass como fuente primaria de ingesta automatizada.
