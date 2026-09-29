/**
 * Mirrors CLAUDE.md's "Roles y permisos (RBAC)" table and the backend's
 * `role_stage_permission` seed (src/main/resources/sql/seed_reference_data.sql) so the UI can
 * hide actions a role can't perform — the backend is still the real gate (RB-504), this is
 * only for not showing buttons that would 403.
 */
export type StageCode = "HARVEST" | "DISTILLATION" | "BOTTLING" | "LOGISTICS";

export const ALL_STAGES: StageCode[] = ["HARVEST", "DISTILLATION", "BOTTLING", "LOGISTICS"];

export const ROLE_LABELS: Record<string, string> = {
  ADMINISTRATOR: "Administrator",
  JIMA_OPERATOR: "Jima Operator",
  DISTILLATION_OPERATOR: "Distillation Operator",
  BOTTLING_OPERATOR: "Bottling Operator",
  LOGISTICS_OPERATOR: "Logistics Operator",
  AUDITOR: "Auditor",
};

/** Stages a role can VIEW. Administrator and Auditor see every stage; operators only their own. */
const VIEW_ACCESS: Record<string, StageCode[]> = {
  ADMINISTRATOR: ALL_STAGES,
  AUDITOR: ALL_STAGES,
  JIMA_OPERATOR: ["HARVEST"],
  DISTILLATION_OPERATOR: ["DISTILLATION"],
  BOTTLING_OPERATOR: ["BOTTLING"],
  LOGISTICS_OPERATOR: ["LOGISTICS"],
};

/** Stages a role can CREATE/UPDATE/COMPLETE in. Auditor is view-only everywhere (RB-504). */
const WRITE_ACCESS: Record<string, StageCode[]> = {
  ADMINISTRATOR: ALL_STAGES,
  AUDITOR: [],
  JIMA_OPERATOR: ["HARVEST"],
  DISTILLATION_OPERATOR: ["DISTILLATION"],
  BOTTLING_OPERATOR: ["BOTTLING"],
  LOGISTICS_OPERATOR: ["LOGISTICS"],
};

export function roleLabel(roleCode: string): string {
  return ROLE_LABELS[roleCode] || roleCode;
}

export function canViewStage(roles: string[], stage: StageCode): boolean {
  return roles.some((role) => (VIEW_ACCESS[role] || []).includes(stage));
}

export function canWriteStage(roles: string[], stage: StageCode): boolean {
  return roles.some((role) => (WRITE_ACCESS[role] || []).includes(stage));
}

export function isAdministrator(roles: string[]): boolean {
  return roles.includes("ADMINISTRATOR");
}

export function isAuditor(roles: string[]): boolean {
  return roles.includes("AUDITOR");
}
