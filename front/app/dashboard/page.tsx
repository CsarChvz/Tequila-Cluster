"use client";

import React, { useState, useEffect, useCallback } from "react";
import {
  Title,
  Text,
  Grid,
  Card,
  Group,
  ThemeIcon,
  Badge,
  Button,
  Stack,
  Timeline,
  Paper,
  Tooltip,
  Alert,
  Select,
  Box,
  Loader,
  Center,
} from "@mantine/core";
import {
  IconPlant2,
  IconFlame,
  IconBottle,
  IconTruckDelivery,
  IconAlertTriangle,
  IconRefresh,
  IconShieldCheck,
} from "@tabler/icons-react";
import {
  harvestApi,
  distillationApi,
  bottlingApi,
  logisticsApi,
  alertsApi,
  traceabilityApi,
  JimaBatchResponse,
  DistillationBatchResponse,
  BottlingBatchResponse,
  ShipmentResponse,
  ProcessAlertResponse,
  BackwardTraceabilityResponse,
} from "@/lib/api";

const STAGE_ICONS: Record<string, any> = {
  HARVEST: IconPlant2,
  DISTILLATION: IconFlame,
  BOTTLING: IconBottle,
  LOGISTICS: IconTruckDelivery,
};

export default function DashboardOverviewPage() {
  const [harvestBatches, setHarvestBatches] = useState<JimaBatchResponse[]>([]);
  const [distillationBatches, setDistillationBatches] = useState<DistillationBatchResponse[]>([]);
  const [bottlingBatches, setBottlingBatches] = useState<BottlingBatchResponse[]>([]);
  const [shipments, setShipments] = useState<ShipmentResponse[]>([]);
  const [alerts, setAlerts] = useState<ProcessAlertResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [selectedCode, setSelectedCode] = useState<string | null>(null);
  const [trace, setTrace] = useState<BackwardTraceabilityResponse | null>(null);
  const [traceLoading, setTraceLoading] = useState(false);
  const [traceError, setTraceError] = useState<string | null>(null);

  const loadAll = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [harvestList, distillationList, bottlingList, shipmentList, alertList] = await Promise.all([
        harvestApi.list(),
        distillationApi.list(),
        bottlingApi.list(),
        logisticsApi.list(),
        alertsApi.list({ status: "OPEN" }),
      ]);
      setHarvestBatches(harvestList);
      setDistillationBatches(distillationList);
      setBottlingBatches(bottlingList);
      setShipments(shipmentList);
      setAlerts(alertList);
      if (!selectedCode && bottlingList.length > 0) {
        setSelectedCode(bottlingList[0].traceabilityCode);
      } else if (!selectedCode && distillationList.length > 0) {
        setSelectedCode(distillationList[0].traceabilityCode);
      } else if (!selectedCode && harvestList.length > 0) {
        setSelectedCode(harvestList[0].traceabilityCode);
      }
    } catch (err: any) {
      setLoadError(err.message || "No se pudo conectar con el backend");
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  useEffect(() => {
    if (!selectedCode) return;
    setTraceLoading(true);
    setTraceError(null);
    traceabilityApi
      .backward(selectedCode)
      .then(setTrace)
      .catch((err) => setTraceError(err.message || "No se pudo cargar la trazabilidad"))
      .finally(() => setTraceLoading(false));
  }, [selectedCode]);

  const handleResolveAlert = async (id: string) => {
    try {
      await alertsApi.resolve(id);
      setAlerts((prev) => prev.filter((a) => a.id !== id));
    } catch (err: any) {
      alert(err.message || "No se pudo resolver la alerta");
    }
  };

  const totalHarvestKg = harvestBatches.reduce((acc, b) => acc + Number(b.totalWeightKg), 0);
  const totalDistilledL = distillationBatches.reduce((acc, b) => acc + Number(b.heartsVolumeL), 0);
  const totalBottledUnits = bottlingBatches.reduce((acc, b) => acc + b.unitsBottled, 0);
  const activeShipments = shipments.filter((s) => s.status === "IN_TRANSIT").length;

  const batchOptions = [
    ...bottlingBatches.map((b) => ({ value: b.traceabilityCode, label: `${b.traceabilityCode} (Envasado)` })),
    ...distillationBatches.map((b) => ({ value: b.traceabilityCode, label: `${b.traceabilityCode} (Destilación)` })),
    ...harvestBatches.map((b) => ({ value: b.traceabilityCode, label: `${b.traceabilityCode} (Jima)` })),
  ];

  if (loading) {
    return (
      <Center style={{ minHeight: 300 }}>
        <Loader color="teal" />
      </Center>
    );
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif" }}>
            Panel de Control y Trazabilidad Global
          </Title>
          <Text size="sm" c="dimmed">
            Visión general del ciclo de vida de producción, datos en vivo desde el backend (José Cuervo v1.4)
          </Text>
        </div>
        <Group>
          <Badge color="amber" variant="light" size="lg">Entrega Proyecto: 2026-09-29</Badge>
          <Button leftSection={<IconRefresh size={16} />} variant="outline" color="teal" onClick={loadAll}>
            Actualizar Datos
          </Button>
        </Group>
      </Group>

      {loadError && (
        <Alert color="red" title="Error de conexión con el backend" icon={<IconAlertTriangle size={18} />}>
          {loadError}
        </Alert>
      )}

      <Grid>
        <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
          <Paper p="md" radius="md" withBorder shadow="xs" style={{ borderTop: "4px solid #209b99" }}>
            <Group justify="space-between" mb="xs">
              <Text size="xs" c="dimmed" fw={700} tt="uppercase">Agave Cosechado (Jima)</Text>
              <ThemeIcon color="teal" variant="light" radius="md"><IconPlant2 size={20} /></ThemeIcon>
            </Group>
            <Text size="xl" fw={700}>{totalHarvestKg.toLocaleString()} kg</Text>
            <Text size="xs" c="dimmed" mt="xs">{harvestBatches.length} lotes registrados</Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
          <Paper p="md" radius="md" withBorder shadow="xs" style={{ borderTop: "4px solid #ff8f00" }}>
            <Group justify="space-between" mb="xs">
              <Text size="xs" c="dimmed" fw={700} tt="uppercase">Tequila Destilado (Hearts)</Text>
              <ThemeIcon color="orange" variant="light" radius="md"><IconFlame size={20} /></ThemeIcon>
            </Group>
            <Text size="xl" fw={700}>{totalDistilledL.toLocaleString()} L</Text>
            <Text size="xs" c="dimmed" mt="xs">{distillationBatches.length} lotes de alambique</Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
          <Paper p="md" radius="md" withBorder shadow="xs" style={{ borderTop: "4px solid #2daeac" }}>
            <Group justify="space-between" mb="xs">
              <Text size="xs" c="dimmed" fw={700} tt="uppercase">Unidades Embotelladas</Text>
              <ThemeIcon color="cyan" variant="light" radius="md"><IconBottle size={20} /></ThemeIcon>
            </Group>
            <Text size="xl" fw={700}>{totalBottledUnits.toLocaleString()} botellas</Text>
            <Text size="xs" c="dimmed" mt="xs">{bottlingBatches.length} lotes de envasado</Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
          <Paper p="md" radius="md" withBorder shadow="xs" style={{ borderTop: "4px solid #d9480f" }}>
            <Group justify="space-between" mb="xs">
              <Text size="xs" c="dimmed" fw={700} tt="uppercase">Embarques en Tránsito</Text>
              <ThemeIcon color="red" variant="light" radius="md"><IconTruckDelivery size={20} /></ThemeIcon>
            </Group>
            <Text size="xl" fw={700}>{activeShipments} activos</Text>
            <Text size="xs" c="dimmed" mt="xs">{shipments.length} embarques totales</Text>
          </Paper>
        </Grid.Col>
      </Grid>

      <Grid>
        <Grid.Col span={{ base: 12, md: 7 }}>
          <Card withBorder radius="md" p="lg" shadow="xs">
            <Group justify="space-between" mb="md">
              <div>
                <Title order={4}>Visualizador de Trazabilidad hacia Atrás (FR-37)</Title>
                <Text size="xs" c="dimmed">Selecciona un lote para rastrear su origen</Text>
              </div>
              <Badge color="teal">Datos reales</Badge>
            </Group>

            <Select
              label="Lote a rastrear"
              data={batchOptions}
              value={selectedCode}
              onChange={setSelectedCode}
              searchable
              mb="md"
            />

            {traceLoading && <Center py="lg"><Loader color="teal" /></Center>}
            {traceError && <Alert color="red">{traceError}</Alert>}

            {trace && !traceLoading && (
              <Timeline active={trace.nodes.length} bulletSize={32} lineWidth={3} color="teal">
                {[...trace.nodes].reverse().map((node) => {
                  const Icon = STAGE_ICONS[node.stageCode] || IconPlant2;
                  return (
                    <Timeline.Item
                      key={node.batchId}
                      bullet={<Icon size={16} />}
                      title={
                        <Group justify="space-between">
                          <Text fw={700} size="sm">{node.stageName} — {node.traceabilityCode}</Text>
                          <Badge color={node.status === "COMPLETED" ? "green" : node.status === "CANCELLED" ? "red" : "blue"} size="xs">
                            {node.status}
                          </Badge>
                        </Group>
                      }
                    >
                      <Paper p="xs" radius="sm" withBorder bg="gray.0" mt="4px">
                        {node.volume != null && <Text size="xs"><b>Volumen/peso:</b> {Number(node.volume).toLocaleString()}</Text>}
                        <Text size="xs"><b>Creado:</b> {new Date(node.createdAt).toLocaleString()}</Text>
                        {node.completedAt && <Text size="xs"><b>Completado:</b> {new Date(node.completedAt).toLocaleString()}</Text>}
                      </Paper>
                    </Timeline.Item>
                  );
                })}

                {trace.harvestOrigins.map((origin) => (
                  <Timeline.Item key={origin.batchId} bullet={<IconPlant2 size={16} />} title={<Text fw={700} size="sm">Origen agrícola — {origin.traceabilityCode}</Text>}>
                    <Paper p="xs" radius="sm" withBorder bg="gray.0" mt="4px">
                      <Text size="xs"><b>Predio:</b> {origin.fieldName} ({origin.fieldCode})</Text>
                      <Text size="xs"><b>Proveedor:</b> {origin.supplierName}</Text>
                      <Text size="xs"><b>Área DO:</b> {origin.authorizedAreaName} ({origin.authorizedAreaCode})</Text>
                      <Text size="xs"><b>Peso:</b> {Number(origin.totalWeightKg).toLocaleString()} kg ({origin.agaveHeartsCount} piñas)</Text>
                    </Paper>
                  </Timeline.Item>
                ))}
              </Timeline>
            )}
          </Card>
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 5 }}>
          <Card withBorder radius="md" p="md" shadow="xs">
            <Group justify="space-between" mb="xs">
              <Group gap="xs">
                <IconShieldCheck color="#d9480f" size={20} />
                <Title order={4}>Alertas Abiertas (FR-44)</Title>
              </Group>
              <Badge color="red">{alerts.length} pendientes</Badge>
            </Group>
            <Text size="xs" c="dimmed" mb="md">Alertas de proceso sin resolver, en vivo desde el backend</Text>

            <Stack gap="xs">
              {alerts.length === 0 && <Text size="xs" c="dimmed">Sin alertas abiertas.</Text>}
              {alerts.map((alert) => (
                <Paper
                  key={alert.id}
                  p="xs"
                  radius="md"
                  withBorder
                  style={{ borderColor: alert.severity === "CRITICAL" ? "#ffc9c9" : "#ffec99" }}
                >
                  <Group justify="space-between" align="flex-start">
                    <Box style={{ flex: 1 }}>
                      <Group gap="xs" mb="4px">
                        <Badge color={alert.severity === "CRITICAL" ? "red" : "orange"} size="xs">{alert.severity}</Badge>
                        <Text size="xs" fw={700}>{alert.batchTraceabilityCode || alert.shipmentNumber}</Text>
                      </Group>
                      <Text size="xs" c="gray.8">{alert.message}</Text>
                      <Text size="10px" c="dimmed" mt="4px">Creada: {new Date(alert.detectedAt).toLocaleString()}</Text>
                    </Box>
                    <Tooltip label="Marcar como Resuelta">
                      <Button size="xs" color="teal" variant="light" onClick={() => handleResolveAlert(alert.id)}>
                        Resolver
                      </Button>
                    </Tooltip>
                  </Group>
                </Paper>
              ))}
            </Stack>
          </Card>
        </Grid.Col>
      </Grid>
    </Stack>
  );
}
