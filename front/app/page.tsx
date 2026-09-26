"use client";

import React, { useState } from "react";
import {
  Title,
  Text,
  Grid,
  Card,
  Group,
  ThemeIcon,
  Badge,
  Table,
  Button,
  Stack,
  Timeline,
  Paper,
  ActionIcon,
  Tooltip,
  Alert,
  Tabs,
  Progress,
  Divider,
  Box,
} from "@mantine/core";
import {
  IconPlant2,
  IconFlame,
  IconBottle,
  IconTruckDelivery,
  IconAlertTriangle,
  IconCheck,
  IconArrowUpRight,
  IconMicroscope,
  IconShieldCheck,
  IconRefresh,
  IconCalendar,
  IconBuildingWarehouse,
} from "@tabler/icons-react";
import {
  INITIAL_HARVEST_BATCHES,
  INITIAL_DISTILLATION_BATCHES,
  INITIAL_BOTTLING_BATCHES,
  INITIAL_SHIPMENTS,
  INITIAL_ALERTS,
} from "@/lib/mockData";

export default function DashboardPage() {
  const [alerts, setAlerts] = useState(INITIAL_ALERTS);
  const [selectedBatchCode, setSelectedBatchCode] = useState<string>("TRZ-2026-00301");

  const totalHarvestKg = INITIAL_HARVEST_BATCHES.reduce((acc, b) => acc + b.totalWeightKg, 0);
  const totalDistilledL = INITIAL_DISTILLATION_BATCHES.reduce((acc, b) => acc + b.heartsVolumeL, 0);
  const totalBottledUnits = INITIAL_BOTTLING_BATCHES.reduce((acc, b) => acc + b.unitsBottled, 0);
  const activeShipments = INITIAL_SHIPMENTS.filter((s) => s.status === "IN_TRANSIT").length;

  const handleResolveAlert = (alertId: string) => {
    setAlerts((prev) =>
      prev.map((a) => (a.id === alertId ? { ...a, resolved: true, resolvedBy: "admin.jcuervo" } : a))
    );
  };

  return (
    <Stack gap="lg">
      {/* Page Header */}
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Panel de Control y Trazabilidad Global
          </Title>
          <Text size="sm" c="dimmed">
            Visión general del ciclo de vida de producción y validación de reglas (José Cuervo v1.4)
          </Text>
        </div>
        <Group>
          <Badge color="amber" variant="light" size="lg">
            Entrega Proyecto: 2026-09-29
          </Badge>
          <Button leftSection={<IconRefresh size={16} />} variant="outline" color="teal">
            Actualizar Datos
          </Button>
        </Group>
      </Group>

      {/* Metric Cards */}
      <Grid>
        <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
          <Paper p="md" radius="md" withBorder shadow="xs" style={{ borderTop: "4px solid #209b99" }}>
            <Group justify="space-between" mb="xs">
              <Text size="xs" c="dimmed" fw={700} tt="uppercase">
                Agave Cosechado (Jima)
              </Text>
              <ThemeIcon color="teal" variant="light" radius="md">
                <IconPlant2 size={20} />
              </ThemeIcon>
            </Group>
            <Group align="flex-end" gap="xs">
              <Text size="xl" fw={700}>
                {totalHarvestKg.toLocaleString()} kg
              </Text>
              <Badge color="green" variant="light" size="xs">
                +12% vs mes anterior
              </Badge>
            </Group>
            <Text size="xs" c="dimmed" mt="xs">
              {INITIAL_HARVEST_BATCHES.length} lotes registrados en DO
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
          <Paper p="md" radius="md" withBorder shadow="xs" style={{ borderTop: "4px solid #ff8f00" }}>
            <Group justify="space-between" mb="xs">
              <Text size="xs" c="dimmed" fw={700} tt="uppercase">
                Tequila Destilado (Hearts)
              </Text>
              <ThemeIcon color="orange" variant="light" radius="md">
                <IconFlame size={20} />
              </ThemeIcon>
            </Group>
            <Group align="flex-end" gap="xs">
              <Text size="xl" fw={700}>
                {totalDistilledL.toLocaleString()} L
              </Text>
              <Badge color="blue" variant="light" size="xs">
                Rendimiento 12.1%
              </Badge>
            </Group>
            <Text size="xs" c="dimmed" mt="xs">
              {INITIAL_DISTILLATION_BATCHES.length} lotes de alambique
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
          <Paper p="md" radius="md" withBorder shadow="xs" style={{ borderTop: "4px solid #2daeac" }}>
            <Group justify="space-between" mb="xs">
              <Text size="xs" c="dimmed" fw={700} tt="uppercase">
                Unidades Embotelladas
              </Text>
              <ThemeIcon color="cyan" variant="light" radius="md">
                <IconBottle size={20} />
              </ThemeIcon>
            </Group>
            <Group align="flex-end" gap="xs">
              <Text size="xl" fw={700}>
                {totalBottledUnits.toLocaleString()} botellas
              </Text>
              <Badge color="teal" variant="light" size="xs">
                100% Marbetes SAT
              </Badge>
            </Group>
            <Text size="xs" c="dimmed" mt="xs">
              Código QR / Barcode asignado
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 3 }}>
          <Paper p="md" radius="md" withBorder shadow="xs" style={{ borderTop: "4px solid #d9480f" }}>
            <Group justify="space-between" mb="xs">
              <Text size="xs" c="dimmed" fw={700} tt="uppercase">
                Embarques en Tránsito
              </Text>
              <ThemeIcon color="red" variant="light" radius="md">
                <IconTruckDelivery size={20} />
              </ThemeIcon>
            </Group>
            <Group align="flex-end" gap="xs">
              <Text size="xl" fw={700}>
                {activeShipments} activos
              </Text>
              <Badge color="orange" variant="light" size="xs">
                Carta Porte Ok
              </Badge>
            </Group>
            <Text size="xs" c="dimmed" mt="xs">
              CEDIS Guadalajara & Exportación
            </Text>
          </Paper>
        </Grid.Col>
      </Grid>

      {/* Main Interactive Traceability & Lineage Section */}
      <Grid>
        <Grid.Col span={{ base: 12, md: 7 }}>
          <Card withBorder radius="md" p="lg" shadow="xs">
            <Group justify="space-between" mb="md">
              <div>
                <Title order={4}>Visualizador de Trazabilidad y Árbol de Linaje (FR-37 / FR-38)</Title>
                <Text size="xs" c="dimmed">
                  Selecciona un lote para rastrear su origen desde la jima hasta el embarque final
                </Text>
              </div>
              <Badge color="teal">Modo Interactivo</Badge>
            </Group>

            {/* Batch selector buttons */}
            <Group gap="xs" mb="lg">
              <Text size="xs" fw={700}>Lotes de Ejemplo:</Text>
              {["TRZ-2026-00301", "TRZ-2026-00201", "TRZ-2026-00203", "TRZ-2026-00101"].map((code) => (
                <Button
                  key={code}
                  size="xs"
                  variant={selectedBatchCode === code ? "filled" : "outline"}
                  color="teal"
                  onClick={() => setSelectedBatchCode(code)}
                >
                  {code}
                </Button>
              ))}
            </Group>

            {/* Interactive Timeline */}
            <Timeline active={3} bulletSize={32} lineWidth={3} color="teal">
              <Timeline.Item
                bullet={<IconPlant2 size={16} />}
                title={
                  <Group justify="space-between">
                    <Text fw={700} size="sm">
                      1. Jima / Cosecha Agave — TRZ-2026-00101
                    </Text>
                    <Badge color="green" size="xs">COMPLETED</Badge>
                  </Group>
                }
              >
                <Paper p="xs" radius="sm" withBorder bg="gray.0" mt="4px">
                  <Text size="xs"><b>Predio:</b> Rancho Tequileño Sector Norte</Text>
                  <Text size="xs"><b>Proveedor:</b> Agaves del Valle de Amatitán S.A. (Activo)</Text>
                  <Text size="xs"><b>Denominación de Origen:</b> DO-JALISCO-AMATITAN-01 (Vigente)</Text>
                  <Text size="xs"><b>Peso Agave:</b> 28,500 kg (712 piñas)</Text>
                  <Text size="xs"><b>Permiso Transporte SAT:</b> TP-SAT-2026-0881 (Concluido)</Text>
                </Paper>
              </Timeline.Item>

              <Timeline.Item
                bullet={<IconFlame size={16} />}
                title={
                  <Group justify="space-between">
                    <Text fw={700} size="sm">
                      2. Destilación y Alambique — TRZ-2026-00201
                    </Text>
                    <Badge color="blue" size="xs">MATURING</Badge>
                  </Group>
                }
              >
                <Paper p="xs" radius="sm" withBorder bg="gray.0" mt="4px">
                  <Text size="xs"><b>Fecha Destilación:</b> 2026-09-21</Text>
                  <Text size="xs"><b>Cortes de Destilado:</b> Cabeza: 250L | Corazón: 3,200L | Cola: 450L</Text>
                  <Text size="xs"><b>Riqueza Alcohólica:</b> 54.5% ABV (Dentro de norma 40-60%)</Text>
                  <Text size="xs"><b>Maduración:</b> Reposado en barrica (60 días requeridos)</Text>
                  <Text size="xs"><b>Fecha Lista Envasado:</b> 2026-11-21</Text>
                </Paper>
              </Timeline.Item>

              <Timeline.Item
                bullet={<IconBottle size={16} />}
                title={
                  <Group justify="space-between">
                    <Text fw={700} size="sm">
                      3. Envasado y Marbetes SAT — TRZ-2026-00301
                    </Text>
                    <Badge color="teal" size="xs">COMPLETED</Badge>
                  </Group>
                }
              >
                <Paper p="xs" radius="sm" withBorder bg="gray.0" mt="4px">
                  <Text size="xs"><b>Marca:</b> José Cuervo Tradicional (Categoría Blanco)</Text>
                  <Text size="xs"><b>Presentación:</b> Botella 750 ml</Text>
                  <Text size="xs"><b>Unidades Producidas:</b> 4,500 botellas con código QR único</Text>
                  <Text size="xs"><b>Marbetes SAT Asignados:</b> SAT-MARB-2026-770001 al 774515 (4,515 folios)</Text>
                  <Text size="xs"><b>Reconciliación:</b> 4,500 usadas + 15 merma (0.33% merma ≤ 2% tolerado)</Text>
                </Paper>
              </Timeline.Item>

              <Timeline.Item
                bullet={<IconTruckDelivery size={16} />}
                title={
                  <Group justify="space-between">
                    <Text fw={700} size="sm">
                      4. Embarque y Distribución — EMB-2026-00501
                    </Text>
                    <Badge color="orange" size="xs">IN_TRANSIT</Badge>
                  </Group>
                }
              >
                <Paper p="xs" radius="sm" withBorder bg="gray.0" mt="4px">
                  <Text size="xs"><b>Transportista:</b> Transportes Tequileros del Occidente S.A.</Text>
                  <Text size="xs"><b>Destino:</b> CEDIS Cuervo Guadalajara - Bodega Central</Text>
                  <Text size="xs"><b>Documentación SAT:</b> Carta Porte Digital CFDI + Manifiesto CRT (Válidos)</Text>
                  <Text size="xs"><b>Llegada Estimada:</b> 2026-09-25 14:00 hrs</Text>
                </Paper>
              </Timeline.Item>
            </Timeline>
          </Card>
        </Grid.Col>

        {/* Quality Alerts & Critical Validation Table (FR-44) */}
        <Grid.Col span={{ base: 12, md: 5 }}>
          <Stack gap="md">
            <Card withBorder radius="md" p="md" shadow="xs">
              <Group justify="space-between" mb="xs">
                <Group gap="xs">
                  <IconShieldCheck color="#d9480f" size={20} />
                  <Title order={4}>Alertas y Validaciones (FR-44)</Title>
                </Group>
                <Badge color="red">{alerts.filter((a) => !a.resolved).length} pendientes</Badge>
              </Group>
              <Text size="xs" c="dimmed" mb="md">
                Alertas bloqueantes de proceso enviadas automáticamente a Administrador y Operadores
              </Text>

              <Stack gap="xs">
                {alerts.map((alert) => (
                  <Paper
                    key={alert.id}
                    p="xs"
                    radius="md"
                    withBorder
                    style={{
                      borderColor: alert.resolved
                        ? "#b2f2bb"
                        : alert.severity === "CRITICAL"
                        ? "#ffc9c9"
                        : "#ffec99",
                      backgroundColor: alert.resolved ? "#f4fce3" : "#fff",
                    }}
                  >
                    <Group justify="space-between" align="flex-start">
                      <Box style={{ flex: 1 }}>
                        <Group gap="xs" mb="4px">
                          <Badge
                            color={alert.severity === "CRITICAL" ? "red" : "orange"}
                            size="xs"
                          >
                            {alert.severity}
                          </Badge>
                          <Text size="xs" fw={700}>
                            {alert.batchCode} ({alert.stage})
                          </Text>
                        </Group>
                        <Text size="xs" c="gray.8">
                          {alert.message}
                        </Text>
                        {alert.resolved ? (
                          <Text size="10px" c="green.8" mt="4px" fw={600}>
                            ✓ Resuelta por {alert.resolvedBy}
                          </Text>
                        ) : (
                          <Text size="10px" c="dimmed" mt="4px">
                            Creada: {alert.createdAt}
                          </Text>
                        )}
                      </Box>
                      {!alert.resolved && (
                        <Tooltip label="Marcar como Resuelta">
                          <Button
                            size="xs"
                            color="teal"
                            variant="light"
                            onClick={() => handleResolveAlert(alert.id)}
                          >
                            Resolver
                          </Button>
                        </Tooltip>
                      )}
                    </Group>
                  </Paper>
                ))}
              </Stack>
            </Card>

            {/* Quick Status Overview of Stages */}
            <Card withBorder radius="md" p="md" shadow="xs">
              <Title order={5} mb="xs">Cumplimiento de Reglas de Negocio (RB-*)</Title>
              <Stack gap="xs">
                <div>
                  <Group justify="space-between" mb="4px">
                    <Text size="xs">RB-101 Áreas Autorizadas DO Vigentes</Text>
                    <Text size="xs" fw={700} c="green">100% Válido</Text>
                  </Group>
                  <Progress value={100} color="teal" size="sm" radius="xl" />
                </div>

                <div>
                  <Group justify="space-between" mb="4px">
                    <Text size="xs">RB-203 Graduación Alcohólica en Norma (40-60%)</Text>
                    <Text size="xs" fw={700} c="orange">66.6% (1 Alerta)</Text>
                  </Group>
                  <Progress value={66.6} color="orange" size="sm" radius="xl" />
                </div>

                <div>
                  <Group justify="space-between" mb="4px">
                    <Text size="xs">RB-302 Conciliación de Marbetes SAT</Text>
                    <Text size="xs" fw={700} c="green">100% Conciliado</Text>
                  </Group>
                  <Progress value={100} color="teal" size="sm" radius="xl" />
                </div>

                <div>
                  <Group justify="space-between" mb="4px">
                    <Text size="xs">RB-402 Documentos de Embarque Requeridos</Text>
                    <Text size="xs" fw={700} c="green">100% Válidos</Text>
                  </Group>
                  <Progress value={100} color="teal" size="sm" radius="xl" />
                </div>
              </Stack>
            </Card>
          </Stack>
        </Grid.Col>
      </Grid>
    </Stack>
  );
}
