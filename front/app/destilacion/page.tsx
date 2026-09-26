"use client";

import React, { useState } from "react";
import {
  Title,
  Text,
  Group,
  Button,
  Table,
  Badge,
  Card,
  Modal,
  TextInput,
  NumberInput,
  Select,
  Stack,
  Alert,
  Paper,
  Grid,
  Checkbox,
  ActionIcon,
  Tooltip,
} from "@mantine/core";
import {
  IconFlame,
  IconPlus,
  IconAlertTriangle,
  IconCheck,
  IconEye,
  IconClock,
  IconInfoCircle,
} from "@tabler/icons-react";
import { INITIAL_DISTILLATION_BATCHES, INITIAL_HARVEST_BATCHES, DistillationBatch } from "@/lib/mockData";

export default function DistillationPage() {
  const [batches, setBatches] = useState<DistillationBatch[]>(INITIAL_DISTILLATION_BATCHES);
  const [modalOpen, setModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState<DistillationBatch | null>(null);

  // Form State
  const [parentHarvestCode, setParentHarvestCode] = useState("TRZ-2026-00101");
  const [headsVolumeL, setHeadsVolumeL] = useState<number | "">(200);
  const [heartsVolumeL, setHeartsVolumeL] = useState<number | "">(3500);
  const [tailsVolumeL, setTailsVolumeL] = useState<number | "">(400);
  const [totalDistilledVolumeL, setTotalDistilledVolumeL] = useState<number | "">(4100);
  const [abvPercentage, setAbvPercentage] = useState<number | "">(55.0);
  const [cookingTempC, setCookingTempC] = useState<number | "">(105);
  const [fermentationPh, setFermentationPh] = useState<number | "">(4.2);
  const [maturationRequired, setMaturationRequired] = useState(true);
  const [maturationCategory, setMaturationCategory] = useState<"Blanco" | "Reposado" | "Añejo" | "Extra Añejo">("Reposado");
  const [maturationDays, setMaturationDays] = useState<number | "">(60);

  const [validationError, setValidationError] = useState<string | null>(null);
  const [abvCriticalAlert, setAbvCriticalAlert] = useState<boolean>(false);

  const handleCreateDistillationBatch = (e: React.FormEvent) => {
    e.preventDefault();
    setValidationError(null);
    setAbvCriticalAlert(false);

    const heads = Number(headsVolumeL) || 0;
    const hearts = Number(heartsVolumeL) || 0;
    const tails = Number(tailsVolumeL) || 0;
    const total = Number(totalDistilledVolumeL) || 0;
    const abv = Number(abvPercentage) || 0;

    // FR-15 / RB-202: Validate sum of cuts <= total distilled volume
    if (heads + hearts + tails > total) {
      setValidationError(
        `Error RB-202: La suma de cortes (${heads + hearts + tails} L) excede el volumen total destilado (${total} L).`
      );
      return;
    }

    // FR-16 / RB-203: Validate ABV range 40.0% - 60.0%
    const isAbvOutOfRange = abv < 40.0 || abv > 60.0;

    const nextIdNumber = batches.length + 201;
    const status: DistillationBatch["status"] = isAbvOutOfRange
      ? "UNDER_REVIEW"
      : maturationRequired
      ? "MATURING"
      : "COMPLETED";

    const newBatch: DistillationBatch = {
      id: `d-${batches.length + 1}`,
      traceabilityCode: `TRZ-2026-00${nextIdNumber}`,
      parentHarvestBatchCodes: [parentHarvestCode],
      distillationDate: "2026-09-26",
      headsVolumeL: heads,
      heartsVolumeL: hearts,
      tailsVolumeL: tails,
      totalDistilledVolumeL: total,
      abvPercentage: abv,
      cookingTempC: Number(cookingTempC) || 105,
      fermentationPh: Number(fermentationPh) || 4.3,
      maturationRequired,
      maturationCategory,
      maturationDaysRequired: maturationRequired ? Number(maturationDays) : 0,
      readyForBottlingAt: maturationRequired ? "2026-11-25" : "2026-09-26",
      status,
    };

    setBatches([newBatch, ...batches]);

    if (isAbvOutOfRange) {
      setAbvCriticalAlert(true);
    } else {
      setModalOpen(false);
    }
  };

  return (
    <Stack gap="lg">
      {/* Title & Actions */}
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Etapa 2: Destilación y Maduración
          </Title>
          <Text size="sm" c="dimmed">
            Registro de cortes de alambique (Cabezas/Corazón/Colas), graduación ABV y seguimiento de maduración en barrica (FR-12 a FR-19)
          </Text>
        </div>
        <Button
          leftSection={<IconPlus size={18} />}
          color="orange"
          radius="md"
          onClick={() => setModalOpen(true)}
        >
          Registrar Nueva Destilación
        </Button>
      </Group>

      {/* Summary Cards */}
      <Grid>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ff8f00" }}>
            <Text size="xs" c="dimmed" fw={700}>VOLUMEN CORAZÓN (TEQUILA BASE)</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.reduce((acc, b) => acc + b.heartsVolumeL, 0).toLocaleString()} Litros
            </Text>
            <Text size="xs" c="dimmed" mt="4px">
              Destilado apto para envasado / maduración
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #2daeac" }}>
            <Text size="xs" c="dimmed" fw={700}>LOTES EN MADURACIÓN (BARICA)</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.filter((b) => b.status === "MATURING").length} Lotes Activos
            </Text>
            <Text size="xs" c="teal" mt="4px">
              Reposados y Añejos (RB-205)
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #d9480f" }}>
            <Text size="xs" c="dimmed" fw={700}>ALERTAS ABV DE REVISIÓN</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.filter((b) => b.status === "UNDER_REVIEW").length} Lotes Bloqueados
            </Text>
            <Text size="xs" c="red" mt="4px">
              Requieren resolución del Administrador (RB-203)
            </Text>
          </Paper>
        </Grid.Col>
      </Grid>

      {/* Batches Table */}
      <Card withBorder radius="md" p="md" shadow="xs">
        <Group justify="space-between" mb="md">
          <Title order={4}>Lotes de Destilación Registrados</Title>
          <Badge color="orange" size="lg">{batches.length} Lotes</Badge>
        </Group>

        <Table highlightOnHover striped verticalSpacing="sm">
          <Table.Thead bg="gray.1">
            <Table.Tr>
              <Table.Th>Código Trazabilidad</Table.Th>
              <Table.Th>Lote Origen (Jima)</Table.Th>
              <Table.Th>Cortes (Head / Heart / Tail)</Table.Th>
              <Table.Th>Vol. Total (L)</Table.Th>
              <Table.Th>Graduación ABV</Table.Th>
              <Table.Th>Cocción / pH</Table.Th>
              <Table.Th>Maduración</Table.Th>
              <Table.Th>Estado</Table.Th>
              <Table.Th>Acción</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {batches.map((b) => (
              <Table.Tr key={b.id}>
                <Table.Td>
                  <Text fw={700} size="sm" c="orange.8">
                    {b.traceabilityCode}
                  </Text>
                  <Text size="11px" c="dimmed">{b.distillationDate}</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color="teal" variant="light" size="xs">
                    {b.parentHarvestBatchCodes.join(", ")}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs">
                    C: {b.headsVolumeL}L | <b>Corazón: {b.heartsVolumeL.toLocaleString()}L</b> | Co: {b.tailsVolumeL}L
                  </Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700}>{b.totalDistilledVolumeL.toLocaleString()} L</Text>
                </Table.Td>
                <Table.Td>
                  <Badge
                    color={b.abvPercentage < 40 || b.abvPercentage > 60 ? "red" : "green"}
                    size="sm"
                  >
                    {b.abvPercentage}% ABV
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs">{b.cookingTempC}°C / pH {b.fermentationPh}</Text>
                </Table.Td>
                <Table.Td>
                  {b.maturationRequired ? (
                    <Text size="xs">
                      {b.maturationCategory} ({b.maturationDaysRequired} días)
                    </Text>
                  ) : (
                    <Badge color="gray" variant="dot" size="xs">Sin Maduración</Badge>
                  )}
                </Table.Td>
                <Table.Td>
                  <Badge
                    color={
                      b.status === "COMPLETED"
                        ? "green"
                        : b.status === "MATURING"
                        ? "blue"
                        : b.status === "UNDER_REVIEW"
                        ? "red"
                        : "orange"
                    }
                    size="sm"
                  >
                    {b.status}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Tooltip label="Ver linaje y parámetros de laboratorio">
                    <ActionIcon
                      variant="light"
                      color="orange"
                      onClick={() => {
                        setSelectedBatch(b);
                        setDetailModalOpen(true);
                      }}
                    >
                      <IconEye size={16} />
                    </ActionIcon>
                  </Tooltip>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Card>

      {/* Modal to Create Distillation Batch */}
      <Modal
        opened={modalOpen}
        onClose={() => setModalOpen(false)}
        title={
          <Group gap="xs">
            <IconFlame color="#ff8f00" size={20} />
            <Text fw={700}>Registrar Lote de Destilación (FR-12 / FR-14)</Text>
          </Group>
        }
        size="lg"
        radius="md"
      >
        <form onSubmit={handleCreateDistillationBatch}>
          <Stack gap="sm">
            <Alert color="orange" title="Regla de Enlace Lineal RB-201" icon={<IconInfoCircle size={18} />}>
              Solo se puede destilar desde lotes de cosecha en estado <b>COMPLETED</b>.
            </Alert>

            {validationError && (
              <Alert color="red" title="Error de Validación de Cortes (RB-202)" icon={<IconAlertTriangle size={18} />}>
                {validationError}
              </Alert>
            )}

            {abvCriticalAlert && (
              <Alert color="red" title="¡ALERTA CRÍTICA GENERADA! (RB-203)" icon={<IconAlertTriangle size={18} />}>
                La graduación alcohólica ({abvPercentage}% ABV) sobrepasa el límite normativo (40.0% - 60.0%). El lote ha sido creado en estado <b>UNDER_REVIEW</b> y se ha notificado al Administrador. No se podrá avanzar a envasado hasta resolver.
              </Alert>
            )}

            <Select
              label="Lote de Cosecha Origen (Jima COMPLETED)"
              data={INITIAL_HARVEST_BATCHES.map((h) => ({
                value: h.traceabilityCode,
                label: `${h.traceabilityCode} - ${h.field} (${h.totalWeightKg.toLocaleString()} kg)`,
              }))}
              value={parentHarvestCode}
              onChange={(val) => setParentHarvestCode(val || "")}
              required
            />

            <Grid>
              <Grid.Col span={4}>
                <NumberInput
                  label="Cabezas (L)"
                  value={headsVolumeL}
                  onChange={(val) => setHeadsVolumeL(val === "" ? "" : Number(val))}
                  min={0}
                  required
                />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput
                  label="Corazón / Tequila (L)"
                  value={heartsVolumeL}
                  onChange={(val) => setHeartsVolumeL(val === "" ? "" : Number(val))}
                  min={1}
                  required
                />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput
                  label="Colas (L)"
                  value={tailsVolumeL}
                  onChange={(val) => setTailsVolumeL(val === "" ? "" : Number(val))}
                  min={0}
                  required
                />
              </Grid.Col>
            </Grid>

            <Grid>
              <Grid.Col span={6}>
                <NumberInput
                  label="Volumen Total Destilado (L)"
                  value={totalDistilledVolumeL}
                  onChange={(val) => setTotalDistilledVolumeL(val === "" ? "" : Number(val))}
                  min={1}
                  required
                />
              </Grid.Col>

              <Grid.Col span={6}>
                <NumberInput
                  label="Riqueza Alcohólica (% ABV)"
                  value={abvPercentage}
                  onChange={(val) => setAbvPercentage(val === "" ? "" : Number(val))}
                  decimalScale={1}
                  min={0}
                  max={100}
                  required
                />
              </Grid.Col>
            </Grid>

            <Grid>
              <Grid.Col span={6}>
                <NumberInput
                  label="Temperatura Cocción (°C)"
                  value={cookingTempC}
                  onChange={(val) => setCookingTempC(val === "" ? "" : Number(val))}
                  min={50}
                  max={150}
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <NumberInput
                  label="pH de Fermentación"
                  value={fermentationPh}
                  onChange={(val) => setFermentationPh(val === "" ? "" : Number(val))}
                  decimalScale={1}
                  min={1}
                  max={14}
                />
              </Grid.Col>
            </Grid>

            <Checkbox
              label="Requiere Maduración en Barrica (Reposado / Añejo)"
              checked={maturationRequired}
              onChange={(e) => setMaturationRequired(e.currentTarget.checked)}
            />

            {maturationRequired && (
              <Grid>
                <Grid.Col span={6}>
                  <Select
                    label="Categoría de Tequila"
                    data={["Reposado", "Añejo", "Extra Añejo"]}
                    value={maturationCategory}
                    onChange={(val) => setMaturationCategory((val as any) || "Reposado")}
                  />
                </Grid.Col>
                <Grid.Col span={6}>
                  <NumberInput
                    label="Días Mínimos de Barrica"
                    value={maturationDays}
                    onChange={(val) => setMaturationDays(val === "" ? "" : Number(val))}
                    min={60}
                  />
                </Grid.Col>
              </Grid>
            )}

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                {abvCriticalAlert ? "Cerrar" : "Cancelar"}
              </Button>
              {!abvCriticalAlert && (
                <Button color="orange" type="submit">
                  Guardar Destilación
                </Button>
              )}
            </Group>
          </Stack>
        </form>
      </Modal>

      {/* Batch Detail Modal */}
      {selectedBatch && (
        <Modal
          opened={detailModalOpen}
          onClose={() => setDetailModalOpen(false)}
          title={`Detalle de Destilación: ${selectedBatch.traceabilityCode}`}
          size="md"
        >
          <Stack gap="xs">
            <Text size="xs"><b>Lotes Origen Jima:</b> {selectedBatch.parentHarvestBatchCodes.join(", ")}</Text>
            <Text size="xs"><b>Corte Cabezas:</b> {selectedBatch.headsVolumeL} L</Text>
            <Text size="xs"><b>Corte Corazón:</b> {selectedBatch.heartsVolumeL.toLocaleString()} L</Text>
            <Text size="xs"><b>Corte Colas:</b> {selectedBatch.tailsVolumeL} L</Text>
            <Text size="xs"><b>Volumen Total Destilado:</b> {selectedBatch.totalDistilledVolumeL.toLocaleString()} L</Text>
            <Text size="xs"><b>Graduación Alcohólica (ABV):</b> {selectedBatch.abvPercentage}%</Text>
            <Text size="xs"><b>Parámetros:</b> Temp: {selectedBatch.cookingTempC}°C | pH: {selectedBatch.fermentationPh}</Text>
            <Text size="xs"><b>Estado de Maduración:</b> {selectedBatch.maturationRequired ? `${selectedBatch.maturationCategory} (${selectedBatch.maturationDaysRequired} días)` : "Sin maduración"}</Text>
            <Text size="xs"><b>Fecha Lista para Envasar:</b> {selectedBatch.readyForBottlingAt || "Inmediata"}</Text>
            <Button mt="md" color="orange" fullWidth onClick={() => setDetailModalOpen(false)}>
              Cerrar Detalle
            </Button>
          </Stack>
        </Modal>
      )}
    </Stack>
  );
}
