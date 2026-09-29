// Real Spring Boot API client. No mock fallback: a failed request is a real error the UI must
// show, not a silently-substituted fake success — that was hiding backend connectivity
// problems during development.

export interface User {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  user: User;
}

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1";

export function getStoredToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem("tequila_jwt_token");
}

export function setStoredToken(token: string) {
  if (typeof window !== "undefined") {
    localStorage.setItem("tequila_jwt_token", token);
  }
}

export function removeStoredToken() {
  if (typeof window !== "undefined") {
    localStorage.removeItem("tequila_jwt_token");
  }
}

/** Fetch wrapper with automatic Bearer token injection and no-content handling. */
export async function apiFetch<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = getStoredToken();

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options.headers as Record<string, string>),
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({ message: `Error HTTP ${response.status}` }));
    throw new Error(errorData.message || `Error HTTP ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json();
}

// ---------------------------------------------------------------------------
// Auth (FR-01, FR-02)
// ---------------------------------------------------------------------------

/** Shape the backend's LoginResponse actually returns: {token, userId, username, roles}. */
interface BackendLoginResponse {
  token: string;
  userId: string;
  username: string;
  roles: string[];
}

function toAuthResponse(backend: BackendLoginResponse, email?: string): AuthResponse {
  return {
    token: backend.token,
    user: {
      id: backend.userId,
      username: backend.username,
      email: email || `${backend.username}@tequilacuervo.com`,
      roles: backend.roles,
    },
  };
}

export const authApi = {
  login: async (username: string, password: string): Promise<AuthResponse> => {
    const backend = await apiFetch<BackendLoginResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username, password }),
    });
    return toAuthResponse(backend);
  },

  register: async (data: { username: string; email: string; password: string; roleCode: string }): Promise<AuthResponse> => {
    const backend = await apiFetch<BackendLoginResponse>("/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    });
    return toAuthResponse(backend, data.email);
  },
};

// ---------------------------------------------------------------------------
// Catalogs (FR-04) — only the ones the existing dashboards need as dropdown data
// ---------------------------------------------------------------------------

export interface CatalogOption {
  id: string;
  code?: string;
  name: string;
  active: boolean;
}

export const catalogsApi = {
  agaveFields: () =>
    apiFetch<Array<{ id: string; fieldCode: string; name: string; active: boolean; authorizedAreaCode: string | null }>>(
      "/catalogs/agave-fields"
    ),
  suppliers: () =>
    apiFetch<Array<{ id: string; supplierCode: string; legalName: string; active: boolean }>>("/catalogs/suppliers"),
  brands: () => apiFetch<Array<{ id: string; name: string; active: boolean }>>("/catalogs/brands"),
  tequilaCategories: () =>
    apiFetch<Array<{ id: string; code: string; name: string; minimumMaturationDays: number; active: boolean }>>(
      "/catalogs/tequila-categories"
    ),
  carriers: () => apiFetch<Array<{ id: string; name: string; active: boolean }>>("/catalogs/carriers"),
  shipmentTypes: () => apiFetch<Array<{ id: string; code: string; name: string; active: boolean }>>("/catalogs/shipment-types"),
  documentTypes: () => apiFetch<Array<{ id: string; code: string; name: string; active: boolean }>>("/catalogs/document-types"),
  labelRequirements: () =>
    apiFetch<Array<{ id: string; code: string; displayName: string; required: boolean; active: boolean }>>(
      "/catalogs/label-requirements"
    ),
};

// ---------------------------------------------------------------------------
// Harvest / Jima (FR-05 a FR-11)
// ---------------------------------------------------------------------------

export type BatchStatus = "DRAFT" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

export interface JimaBatchResponse {
  batchId: string;
  traceabilityCode: string;
  status: BatchStatus;
  fieldCode: string;
  supplierCode: string;
  harvestDate: string;
  totalWeightKg: number;
  agaveHeartsCount: number;
  estimatedYieldL: number;
  estimatedYieldFactor: number;
  transportPermitNumber: string | null;
  capacityWarning: boolean;
}

export interface JimaBatchCreateRequest {
  fieldId: string;
  supplierId: string;
  harvestDate: string;
  totalWeightKg: number;
  agaveHeartsCount: number;
  notes?: string;
}

export const harvestApi = {
  list: () => apiFetch<JimaBatchResponse[]>("/harvest/batches"),
  create: (payload: JimaBatchCreateRequest) =>
    apiFetch<JimaBatchResponse>("/harvest/batches", { method: "POST", body: JSON.stringify(payload) }),
  complete: (batchId: string) => apiFetch<void>(`/harvest/batches/${batchId}/complete`, { method: "POST" }),
  cancel: (batchId: string, reason: string) =>
    apiFetch<void>(`/harvest/batches/${batchId}/cancel`, { method: "POST", body: JSON.stringify({ reason }) }),
};

// ---------------------------------------------------------------------------
// Distillation (FR-12 a FR-19)
// ---------------------------------------------------------------------------

export interface DistillationBatchResponse {
  batchId: string;
  traceabilityCode: string;
  status: BatchStatus;
  distillationDate: string;
  totalDistilledVolumeL: number;
  headsVolumeL: number;
  heartsVolumeL: number;
  tailsVolumeL: number;
  alcoholContentPct: number;
  cookingTemperatureC: number | null;
  fermentationPh: number | null;
  actualYieldL: number;
  estimatedYieldLSnapshot: number;
  maturationRequired: boolean;
  maturationStartDate: string | null;
  requiredMaturationDays: number | null;
  readyForBottlingAt: string | null;
  notes: string | null;
  sourceTraceabilityCodes: string[];
}

export interface DistillationBatchCreateRequest {
  sourceHarvestBatches: Array<{ harvestBatchId: string; quantityUsed: number; unit: string }>;
  distillationDate: string;
  totalDistilledVolumeL: number;
  headsVolumeL: number;
  heartsVolumeL: number;
  tailsVolumeL: number;
  alcoholContentPct: number;
  cookingTemperatureC?: number;
  fermentationPh?: number;
  actualYieldL: number;
  maturationRequired: boolean;
  maturationStartDate?: string;
  requiredMaturationDays?: number;
  notes?: string;
}

export const distillationApi = {
  list: () => apiFetch<DistillationBatchResponse[]>("/distillation/batches"),
  create: (payload: DistillationBatchCreateRequest) =>
    apiFetch<DistillationBatchResponse>("/distillation/batches", { method: "POST", body: JSON.stringify(payload) }),
  complete: (batchId: string) => apiFetch<void>(`/distillation/batches/${batchId}/complete`, { method: "POST" }),
  cancel: (batchId: string, reason: string) =>
    apiFetch<void>(`/distillation/batches/${batchId}/cancel`, { method: "POST", body: JSON.stringify({ reason }) }),
};

// ---------------------------------------------------------------------------
// Bottling (FR-20 a FR-25)
// ---------------------------------------------------------------------------

export interface BottlingBatchResponse {
  batchId: string;
  traceabilityCode: string;
  status: BatchStatus;
  brandName: string | null;
  categoryName: string | null;
  bottlingDate: string;
  bottleCapacityMl: number;
  totalVolumeL: number;
  productionLotNumber: string;
  unitsBottled: number;
  registeredLossesUnits: number;
  /** Count of tax labels already marked USED for this batch (FR-25/RB-306). */
  taxLabelsAssigned: number;
  /** Count of bottled units still AVAILABLE (not shipped/recalled) for this batch. */
  bottledUnitsCreated: number;
  sourceDistillationTraceabilityCode: string | null;
  notes: string | null;
  taxLabelReconciliationWarning: boolean;
}

export interface BottlingBatchCreateRequest {
  distillationBatchId: string;
  brandId: string;
  categoryId: string;
  bottlingDate: string;
  bottleCapacityMl: number;
  totalVolumeL: number;
  productionLotNumber: string;
  unitsBottled: number;
  registeredLossesUnits?: number;
  assignedTaxLabelIds: string[];
  labelValues: Record<string, string>;
  notes?: string;
}

export interface TaxLabelResponse {
  id: string;
  folio: string;
  status: "AVAILABLE" | "ASSIGNED" | "USED" | "LOST" | "VOID";
}

export const bottlingApi = {
  list: () => apiFetch<BottlingBatchResponse[]>("/bottling/batches"),
  create: (payload: BottlingBatchCreateRequest) =>
    apiFetch<BottlingBatchResponse>("/bottling/batches", { method: "POST", body: JSON.stringify(payload) }),
  complete: (batchId: string) => apiFetch<void>(`/bottling/batches/${batchId}/complete`, { method: "POST" }),
  cancel: (batchId: string, reason: string) =>
    apiFetch<void>(`/bottling/batches/${batchId}/cancel`, { method: "POST", body: JSON.stringify({ reason }) }),
  availableTaxLabels: () => apiFetch<TaxLabelResponse[]>("/bottling/batches/tax-labels?status=AVAILABLE"),
};

// ---------------------------------------------------------------------------
// Shipping Logistics (FR-26 a FR-32)
// ---------------------------------------------------------------------------

export type ShipmentStatus = "PLANNED" | "IN_TRANSIT" | "DELIVERED" | "CANCELLED";

export interface ShipmentItemResponse {
  bottlingBatchId: string;
  productionLotNumber: string;
  traceabilityCode: string;
  quantityUnits: number;
}

export interface ShipmentDocumentResponse {
  id: string;
  documentTypeId: string;
  documentTypeName: string;
  documentNumber: string | null;
  fileUrl: string | null;
  valid: boolean;
}

export interface ShipmentResponse {
  id: string;
  shipmentNumber: string;
  shipmentTypeId: string;
  shipmentTypeCode: string;
  shipmentTypeName: string;
  carrierId: string;
  carrierName: string;
  vehicleLicensePlate: string;
  destination: string;
  departureAt: string;
  estimatedArrivalAt: string;
  deliveredAt: string | null;
  status: ShipmentStatus;
  createdByUsername: string | null;
  createdAt: string;
  items: ShipmentItemResponse[];
  documents: ShipmentDocumentResponse[];
}

export interface ShipmentCreateRequest {
  shipmentNumber: string;
  shipmentTypeId: string;
  carrierId: string;
  vehicleLicensePlate: string;
  destination: string;
  departureAt: string;
  estimatedArrivalAt: string;
  items: Array<{ bottlingBatchId: string; quantityUnits: number }>;
}

export const logisticsApi = {
  list: () => apiFetch<ShipmentResponse[]>("/logistics/shipments"),
  create: (payload: ShipmentCreateRequest) =>
    apiFetch<ShipmentResponse>("/logistics/shipments", { method: "POST", body: JSON.stringify(payload) }),
  addDocument: (id: string, payload: { documentTypeId: string; documentNumber?: string; fileUrl?: string; valid?: boolean }) =>
    apiFetch<ShipmentDocumentResponse>(`/logistics/shipments/${id}/documents`, { method: "POST", body: JSON.stringify(payload) }),
  startTransit: (id: string) => apiFetch<ShipmentResponse>(`/logistics/shipments/${id}/start-transit`, { method: "POST" }),
  cancel: (id: string, reason: string) =>
    apiFetch<ShipmentResponse>(`/logistics/shipments/${id}/cancel`, { method: "POST", body: JSON.stringify({ reason }) }),
  deliver: (id: string) => apiFetch<ShipmentResponse>(`/logistics/shipments/${id}/deliver`, { method: "POST" }),
};

// ---------------------------------------------------------------------------
// Quality (FR-33, FR-34)
// ---------------------------------------------------------------------------

export type NonConformitySeverity = "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
export type NonConformityStatus = "OPEN" | "INVESTIGATING" | "RESOLVED" | "CLOSED";
export type RecallType = "PARTIAL" | "COMPLETE";
export type RecallStatus = "OPEN" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";

export interface NonConformityResponse {
  id: string;
  batchId: string;
  batchTraceabilityCode: string;
  batchStageCode: string;
  bottledUnitId: string | null;
  unitCode: string | null;
  title: string;
  description: string;
  severity: NonConformitySeverity;
  status: NonConformityStatus;
  reportedByUsername: string | null;
  reportedAt: string;
  resolvedAt: string | null;
}

export interface RecallResponse {
  id: string;
  sourceBatchId: string;
  sourceBatchTraceabilityCode: string;
  sourceBatchStageCode: string;
  nonConformityId: string | null;
  nonConformityTitle: string | null;
  recallType: RecallType;
  reason: string;
  status: RecallStatus;
  startedByUsername: string | null;
  startedAt: string;
  completedAt: string | null;
  affectedUnitsCount: number;
}

export const qualityApi = {
  listNonConformities: (params?: { batchId?: string; status?: NonConformityStatus; severity?: NonConformitySeverity }) => {
    const qs = new URLSearchParams(params as Record<string, string>).toString();
    return apiFetch<NonConformityResponse[]>(`/quality/non-conformities${qs ? `?${qs}` : ""}`);
  },
  createNonConformity: (payload: { batchId: string; bottledUnitId?: string; title: string; description: string; severity: NonConformitySeverity }) =>
    apiFetch<NonConformityResponse>("/quality/non-conformities", { method: "POST", body: JSON.stringify(payload) }),
  updateNonConformityStatus: (id: string, status: NonConformityStatus) =>
    apiFetch<NonConformityResponse>(`/quality/non-conformities/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),

  listRecalls: (params?: { sourceBatchId?: string; status?: RecallStatus; recallType?: RecallType }) => {
    const qs = new URLSearchParams(params as Record<string, string>).toString();
    return apiFetch<RecallResponse[]>(`/quality/recalls${qs ? `?${qs}` : ""}`);
  },
  createRecall: (payload: { sourceBatchId: string; nonConformityId?: string; recallType: RecallType; reason: string; bottledUnitIds?: string[] }) =>
    apiFetch<RecallResponse>("/quality/recalls", { method: "POST", body: JSON.stringify(payload) }),
  updateRecallStatus: (id: string, status: RecallStatus) =>
    apiFetch<RecallResponse>(`/quality/recalls/${id}/status`, { method: "PATCH", body: JSON.stringify({ status }) }),
};

// ---------------------------------------------------------------------------
// Traceability (FR-37, FR-38, FR-43) and Alerts (FR-44)
// ---------------------------------------------------------------------------

export interface BatchHistoryResponse {
  batchId: string;
  traceabilityCode: string;
  stageCode: string;
  stageName: string;
  status: BatchStatus;
  createdByUsername: string | null;
  createdAt: string;
  completedAt: string | null;
  cancelledAt: string | null;
  cancellationReason: string | null;
  stageDetail: unknown;
  transitions: Array<{ fromStatus: string | null; toStatus: string; changedByUsername: string | null; changedAt: string; reason: string | null }>;
  alerts: ProcessAlertResponse[];
  nonConformities: NonConformityResponse[];
}

export interface TraceabilityNodeDto {
  batchId: string;
  traceabilityCode: string;
  stageCode: string;
  stageName: string;
  status: BatchStatus;
  volume: number | null;
  createdAt: string;
  completedAt: string | null;
  cancelledAt: string | null;
}

export interface HarvestOriginDto {
  batchId: string;
  traceabilityCode: string;
  harvestDate: string;
  totalWeightKg: number;
  agaveHeartsCount: number;
  fieldCode: string;
  fieldName: string;
  supplierName: string;
  authorizedAreaCode: string;
  authorizedAreaName: string;
  stateName: string;
  municipality: string | null;
}

export interface BackwardTraceabilityResponse {
  queryCode: string;
  searchType: string;
  targetBatch: TraceabilityNodeDto | null;
  nodes: TraceabilityNodeDto[];
  harvestOrigins: HarvestOriginDto[];
}

export interface ImpactedBottlingBatchDto {
  bottlingBatchId: string;
  traceabilityCode: string;
  productionLotNumber: string;
  brandName: string;
  categoryName: string;
  unitsBottled: number;
  unitCountsByStatus: Record<string, number>;
}

export interface ImpactedShipmentDto {
  shipmentId: string;
  shipmentNumber: string;
  status: ShipmentStatus;
  carrierName: string;
  destination: string;
  quantityUnits: number;
}

export interface ForwardTraceabilityResponse {
  originType: string;
  originId: string;
  nodes: TraceabilityNodeDto[];
  impactedBottlingBatches: ImpactedBottlingBatchDto[];
  impactedShipments: ImpactedShipmentDto[];
}

export const traceabilityApi = {
  getHistory: (traceabilityCode: string) => apiFetch<BatchHistoryResponse>(`/traceability/${encodeURIComponent(traceabilityCode)}`),
  backward: (code: string) => apiFetch<BackwardTraceabilityResponse>(`/traceability/backward/${encodeURIComponent(code)}`),
  forward: (params: { batchId?: string; supplierId?: string; fieldId?: string }) => {
    const qs = new URLSearchParams(params as Record<string, string>).toString();
    return apiFetch<ForwardTraceabilityResponse>(`/traceability/forward${qs ? `?${qs}` : ""}`);
  },
};

export type AlertSeverity = "INFO" | "WARNING" | "CRITICAL";

export interface ProcessAlertResponse {
  id: string;
  batchId: string | null;
  batchTraceabilityCode: string | null;
  batchStageCode: string | null;
  shipmentId: string | null;
  shipmentNumber: string | null;
  alertType: string;
  severity: AlertSeverity;
  message: string;
  detectedAt: string;
  resolvedAt: string | null;
  resolvedByUsername: string | null;
}

export const alertsApi = {
  list: (params?: { status?: string; severity?: AlertSeverity }) => {
    const qs = new URLSearchParams(params as Record<string, string>).toString();
    return apiFetch<ProcessAlertResponse[]>(`/alerts${qs ? `?${qs}` : ""}`);
  },
  resolve: (id: string) => apiFetch<ProcessAlertResponse>(`/alerts/${id}/resolve`, { method: "POST" }),
};

// ---------------------------------------------------------------------------
// Audit log (FR-42) — Administrator/Auditor only
// ---------------------------------------------------------------------------

export interface AuditLogResponse {
  id: number;
  userId: string | null;
  username: string | null;
  action: string;
  entityType: string;
  entityId: string | null;
  beforeData: string | null;
  afterData: string | null;
  ipAddress: string | null;
  occurredAt: string;
}

export const auditLogApi = {
  list: (limit = 200) => apiFetch<AuditLogResponse[]>(`/audit-log?limit=${limit}`),
};
