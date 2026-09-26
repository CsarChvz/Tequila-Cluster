-- Minimal reference-data seed: process stages and role catalog (FR-02, NFR-15).
-- The DDL script only creates tables; this seed provides the catalog rows the code assumes
-- (see ProcessStageCodes / RoleCodes). Applied automatically on first container start by
-- compose.yml (docker-entrypoint-initdb.d), right after the DDL. Safe to re-run manually
-- thanks to ON CONFLICT DO NOTHING.
--
-- NOT seeded here (Administrator's job per ERS 2.5, not this base): app_user rows,
-- user_role assignments, or role_stage_permission rows — without at least one
-- role_stage_permission row per role+stage, StagePermissionService will deny every action.
-- Dev TODO: seed an initial Administrator user + full permissions once user management
-- (FR-04) exists, or insert them by hand for local testing.

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

COMMIT;
