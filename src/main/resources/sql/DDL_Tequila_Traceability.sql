BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- Entidad: process_stage
-- Descripción: Catálogo de etapas del proceso productivo. Define las fases principales de la trazabilidad (por ejemplo Jima, Destilación, Envasado y Logística) y permite ordenar el flujo del sistema.
-- ============================================================
CREATE TABLE process_stage (
    code            varchar(30) PRIMARY KEY,
    name            varchar(80) NOT NULL UNIQUE,
    sort_order      smallint NOT NULL UNIQUE
);

-- ============================================================
-- Entidad: role
-- Descripción: Catálogo de roles del sistema. Su objetivo es definir los perfiles de usuario que tendrán distintos niveles de acceso y responsabilidad dentro de la plataforma.
-- ============================================================
CREATE TABLE role (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code            varchar(40) NOT NULL UNIQUE,
    name            varchar(100) NOT NULL UNIQUE,
    active          boolean NOT NULL DEFAULT true
);

-- ============================================================
-- Entidad: app_user
-- Descripción: Usuarios que pueden iniciar sesión en la aplicación. Almacena la identidad básica, credenciales protegidas y estado de cada persona que utiliza el sistema.
-- ============================================================
CREATE TABLE app_user (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username        varchar(80) NOT NULL UNIQUE,
    email           varchar(180) NOT NULL UNIQUE,
    password_hash   varchar(255) NOT NULL,
    full_name       varchar(160) NOT NULL,
    active          boolean NOT NULL DEFAULT true,
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- Entidad: user_role
-- Descripción: Tabla intermedia que relaciona usuarios con roles. Permite que un mismo usuario tenga uno o varios roles sin duplicar información.
-- ============================================================
CREATE TABLE user_role (
    user_id         uuid NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    role_id         uuid NOT NULL REFERENCES role(id) ON DELETE RESTRICT,
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- Entidad: role_stage_permission
-- Descripción: Permisos de cada rol sobre cada etapa del proceso. Controla si un rol puede consultar, crear, modificar o completar información en una etapa específica.
-- ============================================================
CREATE TABLE role_stage_permission (
    role_id         uuid NOT NULL REFERENCES role(id) ON DELETE RESTRICT,
    stage_code      varchar(30) NOT NULL REFERENCES process_stage(code) ON DELETE RESTRICT,
    can_view        boolean NOT NULL DEFAULT false,
    can_create      boolean NOT NULL DEFAULT false,
    can_update      boolean NOT NULL DEFAULT false,
    can_complete    boolean NOT NULL DEFAULT false,
    PRIMARY KEY (role_id, stage_code)
);

-- ============================================================
-- Entidad: supplier
-- Descripción: Proveedores de materia prima, principalmente agave. Permite identificar quién suministró el producto y mantener sus datos de contacto y fiscales.
-- ============================================================
CREATE TABLE supplier (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    supplier_code   varchar(40) NOT NULL UNIQUE,
    legal_name      varchar(180) NOT NULL,
    tax_id          varchar(30),
    contact_name    varchar(140),
    phone           varchar(40),
    email           varchar(180),
    active          boolean NOT NULL DEFAULT true,
    created_at      timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- Entidad: authorized_production_area
-- Descripción: Zonas geográficas autorizadas para la producción. Su objetivo es validar que el origen del agave se encuentre dentro de un área permitida y vigente.
-- ============================================================
CREATE TABLE authorized_production_area (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code            varchar(40) NOT NULL UNIQUE,
    name            varchar(160) NOT NULL,
    state_name      varchar(100) NOT NULL,
    municipality    varchar(120),
    active          boolean NOT NULL DEFAULT true,
    valid_from      date,
    valid_to        date,
    CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to >= valid_from)
);

-- ============================================================
-- Entidad: agave_field
-- Descripción: Predios o campos donde se cultiva el agave. Relaciona cada campo con una zona autorizada y conserva datos de ubicación para rastrear el origen de la materia prima.
-- ============================================================
CREATE TABLE agave_field (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    field_code          varchar(50) NOT NULL UNIQUE,
    name                varchar(160) NOT NULL,
    authorized_area_id  uuid NOT NULL REFERENCES authorized_production_area(id) ON DELETE RESTRICT,
    address_text        text,
    latitude            numeric(9,6),
    longitude           numeric(9,6),
    active              boolean NOT NULL DEFAULT true,
    CHECK (latitude IS NULL OR latitude BETWEEN -90 AND 90),
    CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);

-- ============================================================
-- Entidad: brand
-- Descripción: Marcas comerciales bajo las cuales se envasa o comercializa el tequila. Sirve como catálogo reutilizable para los lotes de envasado.
-- ============================================================
CREATE TABLE brand (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name            varchar(120) NOT NULL UNIQUE,
    active          boolean NOT NULL DEFAULT true
);

-- ============================================================
-- Entidad: tequila_category
-- Descripción: Categorías de tequila. Define la clasificación comercial del producto y el tiempo mínimo de maduración asociado a cada categoría.
-- ============================================================
CREATE TABLE tequila_category (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code            varchar(40) NOT NULL UNIQUE,
    name            varchar(100) NOT NULL UNIQUE,
    minimum_maturation_days integer NOT NULL DEFAULT 0 CHECK (minimum_maturation_days >= 0),
    active          boolean NOT NULL DEFAULT true
);

-- ============================================================
-- Entidad: carrier
-- Descripción: Transportistas o empresas encargadas de mover producto. Su objetivo es identificar quién realiza cada traslado dentro de la etapa logística.
-- ============================================================
CREATE TABLE carrier (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name            varchar(160) NOT NULL,
    tax_id          varchar(30),
    phone           varchar(40),
    email           varchar(180),
    active          boolean NOT NULL DEFAULT true
);

-- ============================================================
-- Entidad: validation_rule
-- Descripción: Reglas configurables para validar parámetros del proceso. Permite establecer rangos mínimos, máximos y tolerancias sin modificar el código de la aplicación.
-- ============================================================
CREATE TABLE validation_rule (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    stage_code      varchar(30) NOT NULL REFERENCES process_stage(code) ON DELETE RESTRICT,
    parameter_code  varchar(60) NOT NULL,
    display_name    varchar(140) NOT NULL,
    unit            varchar(30),
    min_value       numeric(18,6),
    max_value       numeric(18,6),
    allowed_deviation numeric(18,6),
    active          boolean NOT NULL DEFAULT true,
    valid_from      timestamptz NOT NULL DEFAULT now(),
    valid_to        timestamptz,
    UNIQUE (stage_code, parameter_code, valid_from),
    CHECK (max_value IS NULL OR min_value IS NULL OR max_value >= min_value),
    CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

-- ============================================================
-- Entidad: batch
-- Descripción: Entidad central de lote trazable. Representa cualquier lote que atraviesa una etapa del proceso y concentra su código, etapa, estado y fechas principales.
-- ============================================================
CREATE TABLE batch (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    traceability_code   varchar(64) NOT NULL UNIQUE,
    stage_code          varchar(30) NOT NULL REFERENCES process_stage(code) ON DELETE RESTRICT,
    status              varchar(30) NOT NULL DEFAULT 'DRAFT'
                        CHECK (status IN ('DRAFT','IN_PROGRESS','COMPLETED','CANCELLED')),
    created_by          uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    created_at          timestamptz NOT NULL DEFAULT now(),
    completed_at        timestamptz,
    cancelled_at        timestamptz,
    cancellation_reason text,
    CHECK ((status <> 'COMPLETED') OR completed_at IS NOT NULL),
    CHECK ((status <> 'CANCELLED') OR (cancelled_at IS NOT NULL AND cancellation_reason IS NOT NULL))
);

-- ============================================================
-- Entidad: batch_lineage
-- Descripción: Relación de genealogía entre lotes. Permite saber qué lotes originaron a otros lotes y construir la trazabilidad completa hacia atrás y hacia adelante.
-- ============================================================
CREATE TABLE batch_lineage (
    parent_batch_id     uuid NOT NULL REFERENCES batch(id) ON DELETE RESTRICT,
    child_batch_id      uuid NOT NULL REFERENCES batch(id) ON DELETE RESTRICT,
    quantity_used       numeric(18,6),
    unit                varchar(20),
    linked_by           uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    linked_at           timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (parent_batch_id, child_batch_id),
    CHECK (parent_batch_id <> child_batch_id),
    CHECK (quantity_used IS NULL OR quantity_used > 0)
);

-- ============================================================
-- Entidad: jima_batch
-- Descripción: Información específica de un lote de Jima. Registra origen, proveedor, fecha de cosecha, peso, cantidad de piñas y rendimiento estimado de la materia prima.
-- ============================================================
CREATE TABLE jima_batch (
    batch_id                uuid PRIMARY KEY REFERENCES batch(id) ON DELETE RESTRICT,
    field_id                uuid NOT NULL REFERENCES agave_field(id) ON DELETE RESTRICT,
    supplier_id             uuid NOT NULL REFERENCES supplier(id) ON DELETE RESTRICT,
    harvest_date            date NOT NULL,
    total_weight_kg         numeric(14,3) NOT NULL CHECK (total_weight_kg > 0),
    agave_hearts_count      integer NOT NULL CHECK (agave_hearts_count > 0),
    estimated_yield_l       numeric(14,3) NOT NULL CHECK (estimated_yield_l >= 0),
    estimated_yield_factor  numeric(12,6) NOT NULL CHECK (estimated_yield_factor >= 0),
    notes                   text
);

-- ============================================================
-- Entidad: transport_permit
-- Descripción: Permiso asociado al traslado de un lote de Jima. Controla su folio, estado, fechas y documento para comprobar que el movimiento de materia prima está autorizado.
-- ============================================================
CREATE TABLE transport_permit (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    jima_batch_id       uuid NOT NULL UNIQUE REFERENCES jima_batch(batch_id) ON DELETE RESTRICT,
    permit_number       varchar(80) NOT NULL UNIQUE,
    status              varchar(30) NOT NULL DEFAULT 'GENERATED'
                        CHECK (status IN ('GENERATED','AUTHORIZED','IN_TRANSIT','COMPLETED','CANCELLED')),
    generated_at        timestamptz NOT NULL DEFAULT now(),
    authorized_at       timestamptz,
    completed_at        timestamptz,
    document_url        text
);

-- ============================================================
-- Entidad: distillation_batch
-- Descripción: Información específica de un lote de Destilación. Registra volúmenes, cortes, graduación alcohólica, temperatura, pH, rendimiento y condiciones de maduración.
-- ============================================================
CREATE TABLE distillation_batch (
    batch_id                    uuid PRIMARY KEY REFERENCES batch(id) ON DELETE RESTRICT,
    distillation_date           date NOT NULL,
    total_distilled_volume_l    numeric(14,3) NOT NULL CHECK (total_distilled_volume_l >= 0),
    heads_volume_l              numeric(14,3) NOT NULL DEFAULT 0 CHECK (heads_volume_l >= 0),
    hearts_volume_l             numeric(14,3) NOT NULL DEFAULT 0 CHECK (hearts_volume_l >= 0),
    tails_volume_l              numeric(14,3) NOT NULL DEFAULT 0 CHECK (tails_volume_l >= 0),
    alcohol_content_pct         numeric(6,3) NOT NULL CHECK (alcohol_content_pct BETWEEN 0 AND 100),
    cooking_temperature_c       numeric(7,3),
    fermentation_ph             numeric(5,3) CHECK (fermentation_ph IS NULL OR fermentation_ph BETWEEN 0 AND 14),
    actual_yield_l              numeric(14,3) NOT NULL CHECK (actual_yield_l >= 0),
    estimated_yield_l_snapshot  numeric(14,3) NOT NULL CHECK (estimated_yield_l_snapshot >= 0),
    maturation_required         boolean NOT NULL DEFAULT false,
    maturation_start_date       date,
    required_maturation_days    integer NOT NULL DEFAULT 0 CHECK (required_maturation_days >= 0),
    ready_for_bottling_at       timestamptz,
    notes                       text,
    CHECK (heads_volume_l + hearts_volume_l + tails_volume_l <= total_distilled_volume_l),
    CHECK (
        maturation_required = false
        OR (maturation_start_date IS NOT NULL AND required_maturation_days > 0)
    )
);

-- ============================================================
-- Entidad: bottling_batch
-- Descripción: Información específica de un lote de Envasado. Registra marca, categoría, fecha, presentación, volumen, número de lote, unidades producidas y pérdidas.
-- ============================================================
CREATE TABLE bottling_batch (
    batch_id                uuid PRIMARY KEY REFERENCES batch(id) ON DELETE RESTRICT,
    brand_id                uuid NOT NULL REFERENCES brand(id) ON DELETE RESTRICT,
    category_id             uuid NOT NULL REFERENCES tequila_category(id) ON DELETE RESTRICT,
    bottling_date           date NOT NULL,
    bottle_capacity_ml      integer NOT NULL CHECK (bottle_capacity_ml > 0),
    total_volume_l          numeric(14,3) NOT NULL CHECK (total_volume_l > 0),
    production_lot_number   varchar(80) NOT NULL UNIQUE,
    units_bottled           integer NOT NULL CHECK (units_bottled >= 0),
    registered_losses_units integer NOT NULL DEFAULT 0 CHECK (registered_losses_units >= 0),
    notes                   text
);

-- ============================================================
-- Entidad: label_requirement
-- Descripción: Catálogo de datos obligatorios o configurables del etiquetado. Define qué información debe capturarse para comprobar que un lote cumple con los requisitos de etiqueta.
-- ============================================================
CREATE TABLE label_requirement (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code            varchar(60) NOT NULL UNIQUE,
    display_name    varchar(160) NOT NULL,
    required        boolean NOT NULL DEFAULT true,
    active          boolean NOT NULL DEFAULT true
);

-- ============================================================
-- Entidad: bottling_label_value
-- Descripción: Valores de etiquetado capturados para cada lote de envasado. Relaciona un requisito de etiqueta con el valor concreto utilizado en el producto.
-- ============================================================
CREATE TABLE bottling_label_value (
    bottling_batch_id   uuid NOT NULL REFERENCES bottling_batch(batch_id) ON DELETE RESTRICT,
    requirement_id      uuid NOT NULL REFERENCES label_requirement(id) ON DELETE RESTRICT,
    value_text          text NOT NULL,
    PRIMARY KEY (bottling_batch_id, requirement_id)
);

-- ============================================================
-- Entidad: tax_label
-- Descripción: Marbetes o folios fiscales utilizados en el producto. Controla su disponibilidad, asignación, uso, pérdida o cancelación y los relaciona con un lote de envasado.
-- ============================================================
CREATE TABLE tax_label (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    folio               varchar(100) NOT NULL UNIQUE,
    status              varchar(30) NOT NULL DEFAULT 'AVAILABLE'
                        CHECK (status IN ('AVAILABLE','ASSIGNED','USED','LOST','VOID')),
    bottling_batch_id   uuid REFERENCES bottling_batch(batch_id) ON DELETE RESTRICT,
    assigned_at         timestamptz,
    used_at             timestamptz,
    CHECK (status <> 'AVAILABLE' OR bottling_batch_id IS NULL),
    CHECK (status NOT IN ('ASSIGNED','USED') OR bottling_batch_id IS NOT NULL)
);

-- ============================================================
-- Entidad: bottled_unit
-- Descripción: Unidad física individual de producto, normalmente una botella. Le asigna un identificador único o QR para poder rastrearla hasta su lote, marbete y origen.
-- ============================================================
CREATE TABLE bottled_unit (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    bottling_batch_id   uuid NOT NULL REFERENCES bottling_batch(batch_id) ON DELETE RESTRICT,
    unit_code           varchar(100) NOT NULL UNIQUE,
    barcode_type        varchar(20) NOT NULL DEFAULT 'QR' CHECK (barcode_type IN ('QR','BARCODE')),
    tax_label_id        uuid UNIQUE REFERENCES tax_label(id) ON DELETE RESTRICT,
    status              varchar(30) NOT NULL DEFAULT 'AVAILABLE'
                        CHECK (status IN ('AVAILABLE','RESERVED','SHIPPED','DELIVERED','RECALLED','LOST','DAMAGED')),
    created_at          timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- Entidad: shipment_type
-- Descripción: Catálogo de tipos de envío. Permite clasificar los embarques y asociar diferentes requisitos documentales según el tipo de movimiento logístico.
-- ============================================================
CREATE TABLE shipment_type (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code            varchar(50) NOT NULL UNIQUE,
    name            varchar(120) NOT NULL UNIQUE,
    active          boolean NOT NULL DEFAULT true
);

-- ============================================================
-- Entidad: document_type
-- Descripción: Catálogo de tipos de documentos logísticos. Define los documentos que pueden o deben acompañar un embarque.
-- ============================================================
CREATE TABLE document_type (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code            varchar(60) NOT NULL UNIQUE,
    name            varchar(160) NOT NULL UNIQUE,
    active          boolean NOT NULL DEFAULT true
);

-- ============================================================
-- Entidad: shipment_type_required_document
-- Descripción: Relación entre tipos de envío y documentos requeridos. Permite configurar qué documentación debe existir antes de autorizar un embarque.
-- ============================================================
CREATE TABLE shipment_type_required_document (
    shipment_type_id    uuid NOT NULL REFERENCES shipment_type(id) ON DELETE RESTRICT,
    document_type_id    uuid NOT NULL REFERENCES document_type(id) ON DELETE RESTRICT,
    required            boolean NOT NULL DEFAULT true,
    PRIMARY KEY (shipment_type_id, document_type_id)
);

-- ============================================================
-- Entidad: shipment
-- Descripción: Embarque o traslado de producto terminado. Registra transportista, vehículo, destino, fechas, estado y usuario responsable de crear el envío.
-- ============================================================
CREATE TABLE shipment (
    id                      uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_number         varchar(80) NOT NULL UNIQUE,
    shipment_type_id        uuid NOT NULL REFERENCES shipment_type(id) ON DELETE RESTRICT,
    carrier_id              uuid NOT NULL REFERENCES carrier(id) ON DELETE RESTRICT,
    vehicle_license_plate   varchar(30) NOT NULL,
    destination             text NOT NULL,
    departure_at            timestamptz NOT NULL,
    estimated_arrival_at    timestamptz NOT NULL,
    delivered_at            timestamptz,
    status                  varchar(30) NOT NULL DEFAULT 'PLANNED'
                            CHECK (status IN ('PLANNED','IN_TRANSIT','DELIVERED','CANCELLED')),
    created_by              uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    created_at              timestamptz NOT NULL DEFAULT now(),
    CHECK (estimated_arrival_at >= departure_at),
    CHECK ((status <> 'DELIVERED') OR delivered_at IS NOT NULL)
);

-- ============================================================
-- Entidad: shipment_item
-- Descripción: Detalle de lotes incluidos en cada embarque. Indica qué lote de envasado se transporta y cuántas unidades de ese lote forman parte del envío.
-- ============================================================
CREATE TABLE shipment_item (
    shipment_id         uuid NOT NULL REFERENCES shipment(id) ON DELETE RESTRICT,
    bottling_batch_id   uuid NOT NULL REFERENCES bottling_batch(batch_id) ON DELETE RESTRICT,
    quantity_units      integer NOT NULL CHECK (quantity_units > 0),
    PRIMARY KEY (shipment_id, bottling_batch_id)
);

-- ============================================================
-- Entidad: shipment_document
-- Descripción: Documentos asociados a un embarque. Guarda tipo, número, archivo, validez y usuario que cargó la evidencia documental.
-- ============================================================
CREATE TABLE shipment_document (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    shipment_id         uuid NOT NULL REFERENCES shipment(id) ON DELETE RESTRICT,
    document_type_id    uuid NOT NULL REFERENCES document_type(id) ON DELETE RESTRICT,
    document_number     varchar(120),
    file_url            text,
    valid               boolean NOT NULL DEFAULT true,
    uploaded_at         timestamptz NOT NULL DEFAULT now(),
    uploaded_by         uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    UNIQUE (shipment_id, document_type_id, document_number)
);

-- ============================================================
-- Entidad: inventory_location
-- Descripción: Catálogo de ubicaciones físicas de inventario. Representa almacenes, bodegas u otras zonas donde puede existir producto disponible.
-- ============================================================
CREATE TABLE inventory_location (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code            varchar(40) NOT NULL UNIQUE,
    name            varchar(120) NOT NULL UNIQUE,
    active          boolean NOT NULL DEFAULT true
);

-- ============================================================
-- Entidad: inventory_movement
-- Descripción: Movimientos de inventario de producto envasado. Registra entradas, salidas, pérdidas, devoluciones, recalls y ajustes para calcular existencias por lote y ubicación.
-- ============================================================
CREATE TABLE inventory_movement (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    bottling_batch_id   uuid NOT NULL REFERENCES bottling_batch(batch_id) ON DELETE RESTRICT,
    location_id         uuid NOT NULL REFERENCES inventory_location(id) ON DELETE RESTRICT,
    movement_type       varchar(40) NOT NULL
                        CHECK (movement_type IN ('INITIAL','PRODUCTION_IN','RETURN_IN','SHIPMENT_OUT','LOSS_OUT','RECALL_OUT','ADJUSTMENT')),
    quantity_change_units integer NOT NULL CHECK (quantity_change_units <> 0),
    reference_type      varchar(60),
    reference_id        uuid,
    reason              text,
    created_by          uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    created_at          timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- Entidad: process_alert
-- Descripción: Alertas generadas durante el proceso o la logística. Permite registrar advertencias o situaciones críticas y llevar control de su resolución.
-- ============================================================
CREATE TABLE process_alert (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id        uuid REFERENCES batch(id) ON DELETE RESTRICT,
    shipment_id     uuid REFERENCES shipment(id) ON DELETE RESTRICT,
    alert_type      varchar(60) NOT NULL,
    severity        varchar(20) NOT NULL DEFAULT 'WARNING'
                    CHECK (severity IN ('INFO','WARNING','CRITICAL')),
    message         text NOT NULL,
    detected_at     timestamptz NOT NULL DEFAULT now(),
    resolved_at     timestamptz,
    resolved_by     uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    CHECK (batch_id IS NOT NULL OR shipment_id IS NOT NULL)
);

-- ============================================================
-- Entidad: non_conformity
-- Descripción: Incidencias o incumplimientos detectados en un lote o unidad. Sirve para documentar problemas de calidad, investigar su causa y dar seguimiento hasta su resolución.
-- ============================================================
CREATE TABLE non_conformity (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id        uuid NOT NULL REFERENCES batch(id) ON DELETE RESTRICT,
    bottled_unit_id uuid REFERENCES bottled_unit(id) ON DELETE RESTRICT,
    title           varchar(180) NOT NULL,
    description     text NOT NULL,
    severity        varchar(20) NOT NULL CHECK (severity IN ('LOW','MEDIUM','HIGH','CRITICAL')),
    status          varchar(30) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','INVESTIGATING','RESOLVED','CLOSED')),
    reported_by     uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    reported_at     timestamptz NOT NULL DEFAULT now(),
    resolved_at     timestamptz
);

-- ============================================================
-- Entidad: recall
-- Descripción: Proceso formal de retiro de producto. Relaciona una causa o no conformidad con el lote de origen y controla el alcance y estado del retiro.
-- ============================================================
CREATE TABLE recall (
    id                  uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    non_conformity_id   uuid REFERENCES non_conformity(id) ON DELETE RESTRICT,
    source_batch_id     uuid NOT NULL REFERENCES batch(id) ON DELETE RESTRICT,
    recall_type         varchar(20) NOT NULL CHECK (recall_type IN ('PARTIAL','COMPLETE')),
    reason              text NOT NULL,
    status              varchar(30) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN','IN_PROGRESS','COMPLETED','CANCELLED')),
    started_by          uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    started_at          timestamptz NOT NULL DEFAULT now(),
    completed_at        timestamptz
);

-- ============================================================
-- Entidad: recall_unit
-- Descripción: Unidades individuales incluidas en un recall. Permite conocer exactamente qué botellas fueron afectadas o retiradas cuando el retiro es parcial.
-- ============================================================
CREATE TABLE recall_unit (
    recall_id       uuid NOT NULL REFERENCES recall(id) ON DELETE RESTRICT,
    bottled_unit_id uuid NOT NULL REFERENCES bottled_unit(id) ON DELETE RESTRICT,
    PRIMARY KEY (recall_id, bottled_unit_id)
);

-- ============================================================
-- Entidad: batch_transition_history
-- Descripción: Historial de cambios de etapa y estado de los lotes. Conserva quién realizó cada transición, cuándo ocurrió y la razón del cambio.
-- ============================================================
CREATE TABLE batch_transition_history (
    id              uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    batch_id        uuid NOT NULL REFERENCES batch(id) ON DELETE RESTRICT,
    from_stage      varchar(30) REFERENCES process_stage(code) ON DELETE RESTRICT,
    to_stage        varchar(30) REFERENCES process_stage(code) ON DELETE RESTRICT,
    from_status     varchar(30),
    to_status       varchar(30),
    changed_by      uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    reason          text,
    changed_at      timestamptz NOT NULL DEFAULT now()
);

-- ============================================================
-- Entidad: audit_log
-- Descripción: Bitácora general de auditoría. Registra acciones realizadas sobre las entidades del sistema, incluyendo valores anteriores y nuevos, usuario, IP y fecha del evento.
-- ============================================================
CREATE TABLE audit_log (
    id              bigserial PRIMARY KEY,
    user_id         uuid REFERENCES app_user(id) ON DELETE RESTRICT,
    action          varchar(80) NOT NULL,
    entity_type     varchar(80) NOT NULL,
    entity_id       uuid,
    before_data     jsonb,
    after_data      jsonb,
    ip_address      inet,
    occurred_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_batch_stage_status ON batch(stage_code, status);
CREATE INDEX idx_batch_lineage_child ON batch_lineage(child_batch_id);
CREATE INDEX idx_batch_lineage_parent ON batch_lineage(parent_batch_id);
CREATE INDEX idx_jima_supplier ON jima_batch(supplier_id);
CREATE INDEX idx_jima_field ON jima_batch(field_id);
CREATE INDEX idx_tax_label_batch_status ON tax_label(bottling_batch_id, status);
CREATE INDEX idx_bottled_unit_batch_status ON bottled_unit(bottling_batch_id, status);
CREATE INDEX idx_shipment_status_eta ON shipment(status, estimated_arrival_at);
CREATE INDEX idx_shipment_item_batch ON shipment_item(bottling_batch_id);
CREATE INDEX idx_inventory_batch_location ON inventory_movement(bottling_batch_id, location_id);
CREATE INDEX idx_alert_open ON process_alert(resolved_at) WHERE resolved_at IS NULL;
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id, occurred_at DESC);

COMMIT;
