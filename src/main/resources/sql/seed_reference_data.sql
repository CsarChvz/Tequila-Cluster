-- Minimal reference-data seed: process stages, role catalog and role_stage_permission
-- (FR-02, FR-03, NFR-15). The DDL script only creates tables; this seed provides the catalog
-- rows the code assumes (see ProcessStageCodes / RoleCodes). Applied automatically on first
-- container start by compose.yml (docker-entrypoint-initdb.d), right after the DDL. Safe to
-- re-run manually thanks to ON CONFLICT DO NOTHING.
--
-- role_stage_permission rows below mirror the "Roles y permisos (RBAC)" table in CLAUDE.md:
-- Administrator = todas las etapas; each Operator role = solo su etapa; Auditor = solo vista,
-- todas las etapas. Without these rows StagePermissionService denies every action.
--
-- The bootstrap Administrator user (admin.jcuervo / password123, matching the front's demo
-- login defaults) is NOT created here — it's created at application startup by
-- BootstrapDataSeeder (Java), which needs the real PasswordEncoder bean to hash the password.

BEGIN;

INSERT INTO process_stage (code, name, sort_order) VALUES
    ('HARVEST', 'Harvest (Jima)', 1),
    ('DISTILLATION', 'Distillation', 2),
    ('BOTTLING', 'Bottling', 3),
    ('LOGISTICS', 'Shipping Logistics', 4)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role (code, name, active) VALUES
    ('ADMINISTRATOR', 'Administrator', true),
    ('JIMA_OPERATOR', 'Jima Operator', true),
    ('DISTILLATION_OPERATOR', 'Distillation Operator', true),
    ('BOTTLING_OPERATOR', 'Bottling Operator', true),
    ('LOGISTICS_OPERATOR', 'Logistics Operator', true),
    ('AUDITOR', 'Auditor', true)
ON CONFLICT (code) DO NOTHING;

-- Administrator: full access to every stage.
INSERT INTO role_stage_permission (role_id, stage_code, can_view, can_create, can_update, can_complete)
SELECT r.id, s.code, true, true, true, true
FROM role r CROSS JOIN process_stage s
WHERE r.code = 'ADMINISTRATOR'
ON CONFLICT (role_id, stage_code) DO NOTHING;

-- Auditor: view-only on every stage.
INSERT INTO role_stage_permission (role_id, stage_code, can_view, can_create, can_update, can_complete)
SELECT r.id, s.code, true, false, false, false
FROM role r CROSS JOIN process_stage s
WHERE r.code = 'AUDITOR'
ON CONFLICT (role_id, stage_code) DO NOTHING;

-- Each Operator role: full access to its own stage only.
INSERT INTO role_stage_permission (role_id, stage_code, can_view, can_create, can_update, can_complete)
SELECT r.id, x.stage_code, true, true, true, true
FROM role r
JOIN (VALUES
    ('JIMA_OPERATOR', 'HARVEST'),
    ('DISTILLATION_OPERATOR', 'DISTILLATION'),
    ('BOTTLING_OPERATOR', 'BOTTLING'),
    ('LOGISTICS_OPERATOR', 'LOGISTICS')
) AS x(role_code, stage_code) ON x.role_code = r.code
ON CONFLICT (role_id, stage_code) DO NOTHING;

COMMIT;
