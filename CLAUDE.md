# Tequila Cluster Traceability System

Proyecto de curso (UNIVA, *Marcos de Trabajo*) para **Tequilera José Cuervo**. Sistema web que
registra y liga cada etapa de producción de tequila — Jima (cosecha), Destilación, Envasado y
Logística de embarque — con trazabilidad completa hacia atrás y hacia adelante. Entrega final:
**2026-09-29**. Este archivo condensa el ERS (`SRS_TequilaCluster_Versión1-4.docx` v1.4) y el DDL
(`src/main/resources/sql/DDL_Tequila_Traceability.sql`) para no tener que releer el docx.

> Fuera de alcance en esta versión: blockchain, integración automática con SAT/CRT/ERP, app
> pública de consulta para el consumidor. Los QR/barcode son solo para trazabilidad interna.

## Stack y arquitectura

- **Frontend**: React (a cargo de otro equipo, fuera del alcance de este repo backend).
- **Backend**: Spring Boot 4.1 / Java 21, expone REST `/api/v1` con JSON. Paquete raíz
  `org.dev.tequilacluster` (el diagrama de paquetes original dice `com.dev...`; se resolvió a
  favor del código ya existente).
- **Base de datos**: PostgreSQL, estructura definida en `src/main/resources/sql/DDL_Tequila_Traceability.sql`
  (fuente de verdad del modelo de datos — **no reinterpretar el modelo sin mirar el DDL**).
- Comunicación exclusivamente HTTPS/JSON. Toda entrada se valida en frontend **y** backend.
- Arquitectura en **capas planas** (decisión del PM, no un patrón con nombre formal):
  `controllers/ → services/ → repositories/ → models/`, más `dtos/`, `exceptions/`, `utils/`.
  Cada capa se organiza en subpaquetes por dominio para reflejar NFR-15 (módulos por etapa) sin
  reestructurar la carpeta plana: `security`, `shared`, `catalogs`, `harvest`, `distillation`,
  `bottling`, `logistics`, `quality`, `inventory`.
- `docker compose up --build -d` levanta `db` (Postgres) y luego `app` (Spring Boot), en ese
  orden (`depends_on: condition: service_healthy`). Ver `compose.yml` y `Dockerfile`.
- Variables de entorno: copiar `src/main/resources/static/backend.env.example` a `.env` (nunca
  commitear `.env`). Los parámetros configurables (capacidad máx. de planta, retención de
  registros) viven en `.env`; las reglas de proceso (rangos, tolerancias) viven en la tabla
  `validation_rule`.

## Roles y permisos (RBAC)

Catálogo de roles en `role`, permisos por rol+etapa en `role_stage_permission` (view/create/
update/complete). Un usuario puede tener uno o más roles (`user_role`). Roles iniciales:

| Rol | Responsabilidad | Acceso inicial |
|---|---|---|
| Administrator | Usuarios, roles, permisos, catálogos. Recibe todas las alertas. | Todas las etapas |
| Jima Operator | Lotes de cosecha y permisos de transporte. | Harvest |
| Distillation Operator | Lotes de destilación, cortes, parámetros. Recibe alertas de destilación. | Distillation |
| Bottling Operator | Lotes de envasado, marbetes, valores de etiqueta, unidades. Recibe alertas de envasado. | Bottling |
| Logistics Operator | Embarques, documentos, entregas, movimientos de inventario. Recibe alertas logísticas. | Logistics |
| Auditor | Revisa trazabilidad, historial de transición y auditoría. | Solo vista, todas las etapas |

## Glosario clave

- **Batch**: unidad de producción registrada en una etapa (Harvest/Distillation/Bottling).
- **Traceability code**: único por lote, formato `TRZ-YYYY-NNNNN`, generado al crear el batch.
- **Batch lineage**: relación padre-hijo entre lotes de etapas consecutivas + cantidad usada.
- **Jima**: etapa de cosecha de agave. **ABV**: alcohol por volumen (%).
- **DO / authorized production area**: zona geográfica autorizada (Denominación de Origen).
- **Tax label (marbete)**: etiqueta fiscal SAT, folio único, obligatoria por botella.
- **Validation rule**: mínimo/máximo/desviación permitida configurable por etapa+parámetro.
- **Non-conformity / Recall**: incidencia de calidad / retiro parcial o total de producto.

## Requerimientos funcionales (FR) por módulo

Prioridad: H = esencial v1, M = importante, L = deseable. "(configurable)" = valor en `.env` o
en `validation_rule`, no hardcodear.

### Access control (security)
| FR | Requerimiento | Pri | RB |
|---|---|---|---|
| FR-01 | Login y acceso restringido por permisos del rol | H | RB-504 |
| FR-02 | Catálogo de roles (Administrator, Jima/Distillation/Bottling/Logistics Operator, Auditor); multi-rol | H | RB-504 |
| FR-03 | Permisos view/create/update/complete por rol+etapa | H | RB-504 |
| FR-04 | Administrator gestiona usuarios, roles, permisos y catálogos | H | RB-101,102,207 |

### Harvest / Jima
| FR | Requerimiento | Pri | RB |
|---|---|---|---|
| FR-05 | Código de trazabilidad único `TRZ-YYYY-NNNNN` al crear cualquier batch | H | RB-004 |
| FR-06 | Campo, proveedor, fecha cosecha, peso total (kg), # piñas — todos obligatorios | H | RB-105 |
| FR-07 | Rechazar si el campo pertenece a área autorizada inactiva o fuera de vigencia | H | RB-101 |
| FR-08 | Bloquear si proveedor no registrado/inactivo; alertar al Administrator | H | RB-102 |
| FR-09 | `estimated_yield_l = total_weight_kg * estimated_yield_factor` (default 0.12) | M | RB-103 |
| FR-10 | Generar automáticamente `transport_permit` (status inicial `GENERATED`) | H | RB-106 |
| FR-11 | Warning no bloqueante si peso excede capacidad máx. de planta (configurable) | L | RB-104 |

### Distillation
| FR | Requerimiento | Pri | RB |
|---|---|---|---|
| FR-12 | Solo desde harvest batches con status `COMPLETED` | H | RB-201, RB-001 |
| FR-13 | Uno o más harvest batches vía `batch_lineage` (cantidad usada + unidad) | H | RB-201 |
| FR-14 | Fecha, volumen total destilado, volúmenes de heads/hearts/tails (L) | H | RB-202 |
| FR-15 | Rechazar si suma de cortes > volumen total destilado | H | RB-202 |
| FR-16 | Alcohol content (%); alerta CRITICAL a Distillation Operator + Administrator si fuera de rango (default 40–60%) | H | RB-203 |
| FR-17 | Actual yield (L); alertar si difiere del estimado más de la desviación permitida (default 15%) | M | RB-103, RB-204 |
| FR-18 | Temperatura de cocción y pH de fermentación; alertar si fuera de `validation_rule` (default 100–110°C, pH 4.0–5.0) | M | RB-207 |
| FR-19 | Maduración: requerida sí/no, fecha inicio, días requeridos (capturados por Distillation Operator); `ready_for_bottling_at` se fija al cumplirse los días. Sin maduración: listo al completarse. | H | RB-205, RB-206 |

### Bottling
| FR | Requerimiento | Pri | RB |
|---|---|---|---|
| FR-20 | Solo desde distillation batch `COMPLETED`, ready-for-bottling alcanzado, y `required_maturation_days >= minimum_maturation_days` de la categoría elegida | H | RB-301, RB-205, RB-001 |
| FR-21 | Marca, categoría, fecha, capacidad botella (ml), volumen total (L), lote de producción único, unidades embotelladas, pérdidas registradas | H | RB-301 |
| FR-22 | Marbetes por folio único, status (AVAILABLE/ASSIGNED/USED/LOST/VOID); bloquear envasado si marbetes asignados < unidades a embotellar | H | RB-302, RB-303 |
| FR-23 | Código único (QR/barcode) por unidad embotellada, ligado a batch + marbete; status (AVAILABLE/RESERVED/SHIPPED/DELIVERED/RECALLED/LOST/DAMAGED) | M | RB-304 |
| FR-24 | Capturar valor por cada `label_requirement`; no completar batch si falta un requisito obligatorio | H | RB-305, RB-307 |
| FR-25 | Reconciliar marbetes usados vs. unidades embotelladas + pérdidas; alertar si discrepancia > desviación permitida (default 2%) | M | RB-306 |

### Shipping Logistics
| FR | Requerimiento | Pri | RB |
|---|---|---|---|
| FR-26 | Solo bottling batches `COMPLETED`; un embarque puede incluir varios batches con unidades | H | RB-401 |
| FR-27 | Rechazar item de embarque si unidades > unidades disponibles del batch | H | RB-401 |
| FR-28 | Número de embarque, tipo, transportista, placas, destino, salida, llegada estimada | H | RB-403 |
| FR-29 | Documentos de embarque (tipo, número, archivo, vigencia); no salir de `PLANNED` sin todos los documentos requeridos por su tipo de embarque, válidos | H | RB-402 |
| FR-30 | Un bottling batch puede repartirse en varios embarques (suma ≤ unidades disponibles) | M | RB-406 |
| FR-31 | Alertar a Logistics Operator + Administrator si se excede la llegada estimada por más de la desviación permitida (default 48h) | M | RB-404 |
| FR-32 | Status de embarque (PLANNED/IN_TRANSIT/DELIVERED/CANCELLED); confirmar entrega (fecha + unidades a DELIVERED) | H | RB-405 |

### Quality
| FR | Requerimiento | Pri | RB |
|---|---|---|---|
| FR-33 | No conformidades sobre batch (opcional: unidad embotellada); título, descripción, severidad (LOW/MEDIUM/HIGH/CRITICAL); status (OPEN/INVESTIGATING/RESOLVED/CLOSED) | M | RB-407 |
| FR-34 | Recalls (PARTIAL/COMPLETE) desde un batch origen, opcionalmente ligados a una no conformidad; listar unidades afectadas → RECALLED; status (OPEN/IN_PROGRESS/COMPLETED/CANCELLED) | M | RB-407 |

### Inventory
| FR | Requerimiento | Pri | RB |
|---|---|---|---|
| FR-35 | Movimientos de inventario por bottling batch + ubicación (INITIAL/PRODUCTION_IN/RETURN_IN/SHIPMENT_OUT/LOSS_OUT/RECALL_OUT/ADJUSTMENT) con razón y referencia | M | RB-506 |
| FR-36 | Reconciliar inventario (periódico o bajo demanda) y reportar discrepancias | M | RB-506 |

### Traceability, status y history
| FR | Requerimiento | Pri | RB |
|---|---|---|---|
| FR-37 | Trazabilidad hacia atrás desde código o unidad embotellada → harvest batch(es), proveedor, campo, área autorizada | H | RB-501 |
| FR-38 | Trazabilidad hacia adelante: todos los batches derivados de un campo, proveedor o batch | H | RB-502 |
| FR-39 | Mostrar etapa y status de cada batch (ver Appendix A / modelo de estados abajo) | H | RB-001 |
| FR-40 | Status de batch (DRAFT/IN_PROGRESS/COMPLETED/CANCELLED); rechazar COMPLETED si faltan validaciones o hay alerta CRITICAL abierta; cancelar requiere fecha+razón | H | RB-001,203,503 |
| FR-41 | Registrar toda transición de etapa/status: origen, destino, usuario, razón, fecha | H | RB-003 |
| FR-42 | Audit log de toda acción relevante: usuario, acción, entidad, datos antes/después, IP, fecha | H | RB-003 |
| FR-43 | Mostrar historial completo de un batch desde su código de trazabilidad | H | RB-501 |
| FR-44 | Alertas ligadas a batch o embarque (tipo, severidad, mensaje); panel visible según rol; registrar quién y cuándo se resolvió | M | RB-203 |

## Reglas de negocio críticas (no exhaustivo — ver DDL para CHECKs exactos)

- **RB-001/002**: un batch solo pasa a `COMPLETED` si todas las validaciones de su etapa pasan y
  no hay alerta `CRITICAL` sin resolver; al completarse, sus registros son de solo lectura.
- **RB-003**: toda transición → `batch_transition_history`; todo cambio de datos → `audit_log`
  (antes/después). Ambas tablas son **append-only** (NFR-10): nunca update/delete.
  **NFR-09**: nunca se borra nada; cancelar = status `CANCELLED` + fecha + razón.
- **RB-004**: traceability code único por batch; lotes de etapas consecutivas se ligan por
  `batch_lineage`, no comparten código.
- **RB-101/102**: validar `authorized_production_area` (activa, vigente) y `supplier` (activo)
  antes de crear un harvest batch.
- **RB-202**: `heads_volume_l + hearts_volume_l + tails_volume_l <= total_distilled_volume_l`.
- **RB-205/206**: si `maturation_required = true`, no se puede embotellar hasta cumplir
  `required_maturation_days` desde `maturation_start_date`, y esos días deben ser
  `>= minimum_maturation_days` de la `tequila_category` elegida al crear el bottling batch.
- **RB-302/303/306**: marbetes asignados >= unidades a embotellar; 1 marbete = 1 unidad;
  marbetes usados = unidades embotelladas + pérdidas registradas (± desviación permitida).
- **RB-401/402/406**: unidades de envío <= disponibles del batch; un embarque no sale de
  `PLANNED` sin todos los documentos requeridos por su `shipment_type`, válidos.
- **RB-501/502**: trazabilidad hacia atrás = `bottled_unit → bottling_batch → distillation_batch
  → harvest batch(es) → agave_field/supplier` vía `batch_lineage`. Hacia adelante = inverso.
- **RB-504**: cada rol actúa solo donde sus permisos lo permiten; Auditor = solo vista, todas
  las etapas.
- **RB-505/NFR-17**: retención mínima 5 años (configurable), no purgar antes.

## Modelo de estados (Appendix A del ERS)

| Entidad | Estados | Notas |
|---|---|---|
| Batch | DRAFT → IN_PROGRESS → COMPLETED; CANCELLED | COMPLETED solo sin validaciones pendientes ni alerta CRITICAL abierta |
| Transport permit | GENERATED → AUTHORIZED → IN_TRANSIT → COMPLETED; CANCELLED | Se crea automático con cada harvest batch |
| Tax label | AVAILABLE → ASSIGNED → USED; LOST; VOID | Asignado a un bottling batch, usado por una unidad |
| Bottled unit | AVAILABLE → RESERVED → SHIPPED → DELIVERED; RECALLED; LOST; DAMAGED | Identificado por QR/barcode único |
| Shipment | PLANNED → IN_TRANSIT → DELIVERED; CANCELLED | Sale de PLANNED solo con todos los documentos |
| Non-conformity | OPEN → INVESTIGATING → RESOLVED → CLOSED | Severidad: LOW/MEDIUM/HIGH/CRITICAL |
| Recall | OPEN → IN_PROGRESS → COMPLETED; CANCELLED | Tipo: PARTIAL o COMPLETE |
| Alert | Open → Resolved | Severidad: INFO/WARNING/CRITICAL |

**Condiciones derivadas** (no se guardan como status, se calculan):
- *Under review*: distillation batch con alerta CRITICAL sin resolver (no se puede completar).
- *Maturing*: distillation batch que requiere maduración y aún no llega su fecha lista-para-envasar.
- *Ready for bottling*: distillation batch COMPLETED que ya alcanzó su fecha lista-para-envasar.
- *Label incomplete*: bottling batch con un `label_requirement` obligatorio sin valor.
- *Pending documents*: shipment PLANNED al que le falta un documento requerido por su tipo.

## No-funcionales relevantes para el código

- **NFR-06/07**: autenticación obligatoria en toda operación, passwords solo hasheados, RBAC
  por etapa (ver sección de roles).
- **NFR-08/09/10**: batch COMPLETED = solo lectura; nada se borra físicamente; audit log y
  transition history append-only.
- **NFR-12/13**: validar en frontend y backend; responder con HTTP status estándar + JSON.
- **NFR-14**: registros/consultas ≤ 2s (95%); trazabilidad completa hacia atrás ≤ 5s; hasta 20
  usuarios concurrentes.
- **NFR-15**: backend organizado en módulos por etapa (harvest/distillation/bottling/logistics)
  + módulos compartidos (quality, inventory, traceability, security) — ver sección de arquitectura.
- **NFR-16**: Git/GitHub con ramas protegidas `main`, `uat`, `dev`.

## Dónde está cada cosa

- ERS completo: `C:\Users\Edgardo Jr\Documents\cuatris\cuatri 7\MarcosDeTrabajo\SRS_TequilaCluster_Versión1-4.docx`
- Diagramas UML (PDF, no rasterizables en este entorno — ver `.claude/memory/diagrams_limitation.md`):
  `C:\Users\Edgardo Jr\Documents\cuatris\cuatri 7\MarcosDeTrabajo\TequilaCluster_ERS&Diagrams\`
- DDL (fuente de verdad del modelo de datos): `src/main/resources/sql/DDL_Tequila_Traceability.sql`
- Variables de entorno de ejemplo: `src/main/resources/static/backend.env.example`
- Memoria de proyecto para Claude Code: `.claude/memory/`
- Reportes de TODO para los devs: `C:\Users\Edgardo Jr\Documents\cuatris\cuatri 7\MarcosDeTrabajo\mds\`
