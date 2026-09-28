export interface HarvestBatch {
  id: string;
  traceabilityCode: string;
  field: string;
  supplier: string;
  harvestDate: string;
  totalWeightKg: number;
  pinasCount: number;
  estimatedYieldL: number;
  transportPermitCode: string;
  transportPermitStatus: 'GENERATED' | 'AUTHORIZED' | 'IN_TRANSIT' | 'COMPLETED' | 'CANCELLED';
  status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
  authorizedArea: string;
  areaValidUntil: string;
}

export interface DistillationBatch {
  id: string;
  traceabilityCode: string;
  parentHarvestBatchCodes: string[];
  distillationDate: string;
  headsVolumeL: number;
  heartsVolumeL: number;
  tailsVolumeL: number;
  totalDistilledVolumeL: number;
  abvPercentage: number;
  cookingTempC: number;
  fermentationPh: number;
  maturationRequired: boolean;
  maturationCategory?: 'Blanco' | 'Reposado' | 'Añejo' | 'Extra Añejo';
  maturationDaysRequired?: number;
  maturationStartDate?: string;
  readyForBottlingAt?: string;
  status: 'DRAFT' | 'IN_PROGRESS' | 'UNDER_REVIEW' | 'MATURING' | 'READY_FOR_BOTTLING' | 'COMPLETED' | 'CANCELLED';
}

export interface BottlingBatch {
  id: string;
  traceabilityCode: string;
  parentDistillationCode: string;
  bottlingDate: string;
  brand: string;
  category: 'Blanco' | 'Reposado' | 'Añejo' | 'Extra Añejo';
  bottleCapacityMl: number;
  totalVolumeL: number;
  productionLotCode: string;
  unitsBottled: number;
  lossesRecorded: number;
  satMarbeteStartFolio: string;
  satMarbeteEndFolio: string;
  marbetesAssigned: number;
  marbeteStatus: 'AVAILABLE' | 'ASSIGNED' | 'USED' | 'RECONCILED_WITH_DISCREPANCY';
  labelRequirementsComplete: boolean;
  status: 'DRAFT' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
}

export interface Shipment {
  id: string;
  shipmentNumber: string;
  carrier: string;
  driverName: string;
  licensePlate: string;
  destination: string;
  departureDate: string;
  estimatedArrivalDate: string;
  bottlingBatchCodes: { code: string; units: number }[];
  status: 'PLANNED' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELLED';
  documentsComplete: boolean;
  documentsList: { name: string; required: boolean; uploaded: boolean; valid: boolean }[];
}

export interface QualityAlert {
  id: string;
  batchCode: string;
  stage: 'Harvest' | 'Distillation' | 'Bottling' | 'Logistics';
  severity: 'INFO' | 'WARNING' | 'CRITICAL';
  message: string;
  createdAt: string;
  resolved: boolean;
  resolvedBy?: string;
  resolvedAt?: string;
}

export interface NonConformity {
  id: string;
  code: string;
  batchCode: string;
  title: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'OPEN' | 'INVESTIGATING' | 'RESOLVED' | 'CLOSED';
  createdAt: string;
  reportedBy: string;
}

export interface AuditLogItem {
  id: string;
  timestamp: string;
  user: string;
  role: string;
  action: string;
  entity: string;
  entityId: string;
  details: string;
  ipAddress: string;
}

// Initial Mock Data
export const INITIAL_HARVEST_BATCHES: HarvestBatch[] = [
  {
    id: 'h-1',
    traceabilityCode: 'TRZ-2026-00101',
    field: 'Rancho Tequileño Sector Norte',
    supplier: 'Agaves del Valle de Amatitán S.A.',
    harvestDate: '2026-09-20',
    totalWeightKg: 28500,
    pinasCount: 712,
    estimatedYieldL: 3420, // 28500 * 0.12
    transportPermitCode: 'TP-SAT-2026-0881',
    transportPermitStatus: 'COMPLETED',
    status: 'COMPLETED',
    authorizedArea: 'DO-JALISCO-AMATITAN-01',
    areaValidUntil: '2028-12-31',
  },
  {
    id: 'h-2',
    traceabilityCode: 'TRZ-2026-00102',
    field: 'Predio El Volcán Parcela 4',
    supplier: 'Cooperativa Agavera Los Altos',
    harvestDate: '2026-09-22',
    totalWeightKg: 32000,
    pinasCount: 800,
    estimatedYieldL: 3840,
    transportPermitCode: 'TP-SAT-2026-0892',
    transportPermitStatus: 'COMPLETED',
    status: 'COMPLETED',
    authorizedArea: 'DO-JALISCO-ARANDAS-04',
    areaValidUntil: '2027-06-30',
  },
  {
    id: 'h-3',
    traceabilityCode: 'TRZ-2026-00103',
    field: 'Agaves Cuervo - Tablón 12',
    supplier: 'Agrícola José Cuervo Directo',
    harvestDate: '2026-09-25',
    totalWeightKg: 19800,
    pinasCount: 495,
    estimatedYieldL: 2376,
    transportPermitCode: 'TP-SAT-2026-0910',
    transportPermitStatus: 'IN_TRANSIT',
    status: 'IN_PROGRESS',
    authorizedArea: 'DO-JALISCO-TEQUILA-02',
    areaValidUntil: '2029-10-15',
  }
];

export const INITIAL_DISTILLATION_BATCHES: DistillationBatch[] = [
  {
    id: 'd-1',
    traceabilityCode: 'TRZ-2026-00201',
    parentHarvestBatchCodes: ['TRZ-2026-00101'],
    distillationDate: '2026-09-21',
    headsVolumeL: 250,
    heartsVolumeL: 3200,
    tailsVolumeL: 450,
    totalDistilledVolumeL: 3900,
    abvPercentage: 54.5,
    cookingTempC: 106,
    fermentationPh: 4.3,
    maturationRequired: true,
    maturationCategory: 'Reposado',
    maturationDaysRequired: 60,
    maturationStartDate: '2026-09-22',
    readyForBottlingAt: '2026-11-21',
    status: 'MATURING',
  },
  {
    id: 'd-2',
    traceabilityCode: 'TRZ-2026-00202',
    parentHarvestBatchCodes: ['TRZ-2026-00102'],
    distillationDate: '2026-09-23',
    headsVolumeL: 300,
    heartsVolumeL: 3600,
    tailsVolumeL: 500,
    totalDistilledVolumeL: 4400,
    abvPercentage: 42.0,
    cookingTempC: 104,
    fermentationPh: 4.5,
    maturationRequired: false,
    readyForBottlingAt: '2026-09-23',
    status: 'COMPLETED',
  },
  {
    id: 'd-3',
    traceabilityCode: 'TRZ-2026-00203',
    parentHarvestBatchCodes: ['TRZ-2026-00101', 'TRZ-2026-00102'],
    distillationDate: '2026-09-24',
    headsVolumeL: 400,
    heartsVolumeL: 4100,
    tailsVolumeL: 600,
    totalDistilledVolumeL: 5100,
    abvPercentage: 62.5, // CRITICAL: Exceeds max 60%
    cookingTempC: 112, // Warning: exceeds 110C
    fermentationPh: 3.8, // Warning: below 4.0
    maturationRequired: true,
    maturationCategory: 'Añejo',
    maturationDaysRequired: 365,
    status: 'UNDER_REVIEW',
  }
];

export const INITIAL_BOTTLING_BATCHES: BottlingBatch[] = [
  {
    id: 'b-1',
    traceabilityCode: 'TRZ-2026-00301',
    parentDistillationCode: 'TRZ-2026-00202',
    bottlingDate: '2026-09-24',
    brand: 'José Cuervo Tradicional',
    category: 'Blanco',
    bottleCapacityMl: 750,
    totalVolumeL: 3375,
    productionLotCode: 'LOT-JCT-2026-09A',
    unitsBottled: 4500,
    lossesRecorded: 15,
    satMarbeteStartFolio: 'SAT-MARB-2026-770001',
    satMarbeteEndFolio: 'SAT-MARB-2026-774515',
    marbetesAssigned: 4515,
    marbeteStatus: 'USED',
    labelRequirementsComplete: true,
    status: 'COMPLETED',
  }
];

export const INITIAL_SHIPMENTS: Shipment[] = [
  {
    id: 's-1',
    shipmentNumber: 'EMB-2026-00501',
    carrier: 'Transportes Tequileros del Occidente S.A.',
    driverName: 'Carlos Mendoza',
    licensePlate: 'JV-884-91',
    destination: 'CEDIS Cuervo Guadalajara - Bodega Central',
    departureDate: '2026-09-25 08:30',
    estimatedArrivalDate: '2026-09-25 14:00',
    bottlingBatchCodes: [
      { code: 'TRZ-2026-00301', units: 2400 }
    ],
    status: 'IN_TRANSIT',
    documentsComplete: true,
    documentsList: [
      { name: 'Carta Porte Digital SAT (CFDI)', required: true, uploaded: true, valid: true },
      { name: 'Manifiesto de Carga de Alcohol', required: true, uploaded: true, valid: true },
      { name: 'Póliza de Seguro de Transporte', required: true, uploaded: true, valid: true },
      { name: 'Certificado Fitosanitario CRT', required: true, uploaded: true, valid: true },
    ]
  }
];

export const INITIAL_ALERTS: QualityAlert[] = [
  {
    id: 'alt-1',
    batchCode: 'TRZ-2026-00203',
    stage: 'Distillation',
    severity: 'CRITICAL',
    message: 'Graduación alcohólica (62.5% ABV) fuera del rango reglamentario (40.0% - 60.0%). Lote bloqueado.',
    createdAt: '2026-09-24 16:20',
    resolved: false,
  },
  {
    id: 'alt-2',
    batchCode: 'TRZ-2026-00203',
    stage: 'Distillation',
    severity: 'WARNING',
    message: 'Temperatura de cocción (112°C) excede límite superior de regla de validación (110°C).',
    createdAt: '2026-09-24 16:21',
    resolved: false,
  },
  {
    id: 'alt-3',
    batchCode: 'TRZ-2026-00103',
    stage: 'Harvest',
    severity: 'WARNING',
    message: 'Permiso de transporte TP-SAT-2026-0910 en tránsito. Requiere confirmación de llegada en recepción.',
    createdAt: '2026-09-25 09:15',
    resolved: false,
  }
];

export const INITIAL_AUDIT_LOGS: AuditLogItem[] = [
  {
    id: 'aud-1',
    timestamp: '2026-09-25 10:15:32',
    user: 'admin.jcuervo',
    role: 'Administrator',
    action: 'CREATE_HARVEST_BATCH',
    entity: 'HarvestBatch',
    entityId: 'TRZ-2026-00103',
    details: 'Creado lote de jima con 19,800 kg de agave en Tablón 12.',
    ipAddress: '192.168.1.104',
  },
  {
    id: 'aud-2',
    timestamp: '2026-09-25 08:30:00',
    user: 'logistica.operador',
    role: 'Logistics Operator',
    action: 'DISPATCH_SHIPMENT',
    entity: 'Shipment',
    entityId: 'EMB-2026-00501',
    details: 'Despacho de embarque EMB-2026-00501 con 2,400 unidades embotelladas.',
    ipAddress: '192.168.1.118',
  },
  {
    id: 'aud-3',
    timestamp: '2026-09-24 16:20:11',
    user: 'destilacion.operador',
    role: 'Distillation Operator',
    action: 'VALIDATION_FAILED',
    entity: 'DistillationBatch',
    entityId: 'TRZ-2026-00203',
    details: 'Alerta CRITICAL generada por ABV al 62.5%. Estado asignado a UNDER_REVIEW.',
    ipAddress: '192.168.1.109',
  }
];
