"use client";

import React, { useState, useEffect, useCallback } from "react";
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
  Textarea,
  NumberInput,
  Select,
  Stack,
  Alert,
  Paper,
  Grid,
  Checkbox,
  ActionIcon,
  Tooltip,
  Loader,
  Center,
} from "@mantine/core";
import {
  IconFlame,
  IconPlus,
  IconAlertTriangle,
  IconCheck,
  IconEye,
  IconInfoCircle,
  IconBan,
} from "@tabler/icons-react";
import { distillationApi, harvestApi, DistillationBatchResponse, JimaBatchResponse } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { canWriteStage } from "@/lib/roles";

export default function DistillationPage() {
  const { user } = useAuth();
  const canWrite = canWriteStage(user?.roles || [], "DISTILLATION");

  const [batches, setBatches] = useState<DistillationBatchResponse[]>([]);
  const [completedHarvestBatches, setCompletedHarvestBatches] = useState<JimaBatchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState<DistillationBatchResponse | null>(null);
  const [cancelTarget, setCancelTarget] = useState<DistillationBatchResponse | null>(null);
  const [cancelReason, setCancelReason] = useState("");

  const [sourceHarvestBatchId, setSourceHarvestBatchId] = useState<string | null>(null);
  const [distillationDate, setDistillationDate] = useState("");
  const [headsVolumeL, setHeadsVolumeL] = useState<number | "">("");
  const [heartsVolumeL, setHeartsVolumeL] = useState<number | "">("");
  const [tailsVolumeL, setTailsVolumeL] = useState<number | "">("");
  const [totalDistilledVolumeL, setTotalDistilledVolumeL] = useState<number | "">("");
  const [alcoholContentPct, setAlcoholContentPct] = useState<number | "">("");
  const [cookingTemperatureC, setCookingTemperatureC] = useState<number | "">("");
  const [fermentationPh, setFermentationPh] = useState<number | "">("");
  const [actualYieldL, setActualYieldL] = useState<number | "">("");
  const [maturationRequired, setMaturationRequired] = useState(true);
  const [maturationStartDate, setMaturationStartDate] = useState("");
  const [requiredMaturationDays, setRequiredMaturationDays] = useState<number | "">(60);
  const [notes, setNotes] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const loadAll = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [distillationList, harvestList] = await Promise.all([distillationApi.list(), harvestApi.list()]);
      setBatches(distillationList);
      setCompletedHarvestBatches(harvestList.filter((h) => h.status === "COMPLETED"));
    } catch (err: any) {
      setLoadError(err.message || "No se pudo conectar con el backend");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  const resetForm = () => {
    setSourceHarvestBatchId(null);
    setDistillationDate("");
    setHeadsVolumeL("");
    setHeartsVolumeL("");
    setTailsVolumeL("");
    setTotalDistilledVolumeL("");
    setAlcoholContentPct("");
    setCookingTemperatureC("");
    setFermentationPh("");
    setActualYieldL("");
    setMaturationRequired(true);
    setMaturationStartDate("");
    setRequiredMaturationDays(60);
    setNotes("");
    setFormError(null);
  };

  const handleCreateBatch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!sourceHarvestBatchId || !distillationDate) return;
    setFormError(null);
    setSubmitting(true);
    try {
      const sourceBatch = completedHarvestBatches.find((h) => h.batchId === sourceHarvestBatchId);
      await distillationApi.create({
        sourceHarvestBatches: [
          { harvestBatchId: sourceHarvestBatchId, quantityUsed: Number(sourceBatch?.totalWeightKg || 0), unit: "kg" },
        ],
        distillationDate,
        totalDistilledVolumeL: Number(totalDistilledVolumeL) || 0,
        headsVolumeL: Number(headsVolumeL) || 0,
        heartsVolumeL: Number(heartsVolumeL) || 0,
        tailsVolumeL: Number(tailsVolumeL) || 0,
        alcoholContentPct: Number(alcoholContentPct) || 0,
        cookingTemperatureC: cookingTemperatureC === "" ? undefined : Number(cookingTemperatureC),
        fermentationPh: fermentationPh === "" ? undefined : Number(fermentationPh),
        actualYieldL: Number(actualYieldL) || 0,
        maturationRequired,
        maturationStartDate: maturationRequired ? maturationStartDate : undefined,
        requiredMaturationDays: maturationRequired ? Number(requiredMaturationDays) : undefined,
        notes: notes || undefined,
      });
      setModalOpen(false);
      resetForm();
      await loadAll();
    } catch (err: any) {
      setFormError(err.message || "Error al registrar la destilación");
    } finally {
      setSubmitting(false);
    }
  };

  const handleComplete = async (batch: DistillationBatchResponse) => {
    try {
      await distillationApi.complete(batch.batchId);
      await loadAll();
    } catch (err: any) {
      alert(err.message || "No se pudo completar el lote (revisa si hay alertas CRITICAL abiertas, RB-001)");
    }
  };

  const handleCancelConfirm = async () => {
    if (!cancelTarget || !cancelReason.trim()) return;
    try {
      await distillationApi.cancel(cancelTarget.batchId, cancelReason.trim());
      setCancelTarget(null);
      setCancelReason("");
      await loadAll();
    } catch (err: any) {
      alert(err.message || "No se pudo cancelar el lote");
    }
  };

  if (loading) {
    return (
      <Center style={{ minHeight: 300 }}>
        <Loader color="orange" />
      </Center>
    );
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Etapa 2: Destilación y Maduración
          </Title>
          <Text size="sm" c="dimmed">
            Registro de cortes de alambique (Cabezas/Corazón/Colas), graduación ABV y seguimiento de maduración (FR-12 a FR-19)
          </Text>
        </div>
        {canWrite && (
          <Button leftSection={<IconPlus size={18} />} color="orange" radius="md" onClick={() => setModalOpen(true)}>
            Registrar Nueva Destilación
          </Button>
        )}
      </Group>

      {loadError && (
        <Alert color="red" title="Error de conexión con el backend" icon={<IconAlertTriangle size={18} />}>
          {loadError}
        </Alert>
      )}

      <Grid>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ff8f00" }}>
            <Text size="xs" c="dimmed" fw={700}>VOLUMEN CORAZÓN (TEQUILA BASE)</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.reduce((acc, b) => acc + Number(b.heartsVolumeL), 0).toLocaleString()} Litros
            </Text>
          </Paper>
        </Grid.Col>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #2daeac" }}>
            <Text size="xs" c="dimmed" fw={700}>LOTES EN MADURACIÓN</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.filter((b) => b.maturationRequired && b.status !== "COMPLETED").length} Lotes Activos
            </Text>
            <Text size="xs" c="teal" mt="4px">RB-205 / RB-206</Text>
          </Paper>
        </Grid.Col>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #d9480f" }}>
            <Text size="xs" c="dimmed" fw={700}>LOTES FUERA DE RANGO ABV (40–60%)</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.filter((b) => b.alcoholContentPct < 40 || b.alcoholContentPct > 60).length}
            </Text>
            <Text size="xs" c="red" mt="4px">Generan alerta CRITICAL (RB-203)</Text>
          </Paper>
        </Grid.Col>
      </Grid>

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
              <Table.Tr key={b.batchId}>
                <Table.Td>
                  <Text fw={700} size="sm" c="orange.8">{b.traceabilityCode}</Text>
                  <Text size="11px" c="dimmed">{b.distillationDate}</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color="teal" variant="light" size="xs">{b.sourceTraceabilityCodes.join(", ")}</Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs">
                    C: {b.headsVolumeL}L | <b>Corazón: {Number(b.heartsVolumeL).toLocaleString()}L</b> | Co: {b.tailsVolumeL}L
                  </Text>
                </Table.Td>
                <Table.Td><Text size="xs" fw={700}>{Number(b.totalDistilledVolumeL).toLocaleString()} L</Text></Table.Td>
                <Table.Td>
                  <Badge color={b.alcoholContentPct < 40 || b.alcoholContentPct > 60 ? "red" : "green"} size="sm">
                    {b.alcoholContentPct}% ABV
                  </Badge>
                </Table.Td>
                <Table.Td><Text size="xs">{b.cookingTemperatureC ?? "—"}°C / pH {b.fermentationPh ?? "—"}</Text></Table.Td>
                <Table.Td>
                  {b.maturationRequired ? (
                    <Text size="xs">{b.requiredMaturationDays} días desde {b.maturationStartDate}</Text>
                  ) : (
                    <Badge color="gray" variant="dot" size="xs">Sin Maduración</Badge>
                  )}
                </Table.Td>
                <Table.Td>
                  <Badge color={b.status === "COMPLETED" ? "green" : b.status === "CANCELLED" ? "red" : "blue"} size="sm">
                    {b.status}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    <Tooltip label="Ver linaje y parámetros">
                      <ActionIcon variant="light" color="orange" onClick={() => { setSelectedBatch(b); setDetailModalOpen(true); }}>
                        <IconEye size={16} />
                      </ActionIcon>
                    </Tooltip>
                    {canWrite && b.status === "IN_PROGRESS" && (
                      <>
                        <Tooltip label="Completar lote">
                          <ActionIcon variant="light" color="green" onClick={() => handleComplete(b)}>
                            <IconCheck size={16} />
                          </ActionIcon>
                        </Tooltip>
                        <Tooltip label="Cancelar lote">
                          <ActionIcon variant="light" color="red" onClick={() => setCancelTarget(b)}>
                            <IconBan size={16} />
                          </ActionIcon>
                        </Tooltip>
                      </>
                    )}
                  </Group>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Card>

      <Modal
        opened={modalOpen}
        onClose={() => { setModalOpen(false); resetForm(); }}
        title={<Group gap="xs"><IconFlame color="#ff8f00" size={20} /><Text fw={700}>Registrar Lote de Destilación (FR-12 / FR-14)</Text></Group>}
        size="lg"
        radius="md"
      >
        <form onSubmit={handleCreateBatch}>
          <Stack gap="sm">
            <Alert color="orange" title="Regla de Enlace Lineal RB-201" icon={<IconInfoCircle size={18} />}>
              Solo se puede destilar desde lotes de cosecha en estado <b>COMPLETED</b>. La categoría comercial (Blanco/Reposado/Añejo) se elige al crear el lote de envasado, no aquí (RB-206).
            </Alert>

            {formError && (
              <Alert color="red" title="Error" icon={<IconAlertTriangle size={18} />}>{formError}</Alert>
            )}

            <Select
              label="Lote de Cosecha Origen (Jima COMPLETED)"
              data={completedHarvestBatches.map((h) => ({
                value: h.batchId,
                label: `${h.traceabilityCode} — ${h.fieldCode} (${Number(h.totalWeightKg).toLocaleString()} kg)`,
              }))}
              value={sourceHarvestBatchId}
              onChange={setSourceHarvestBatchId}
              searchable
              required
            />

            <TextInput type="date" label="Fecha de Destilación" value={distillationDate} onChange={(e) => setDistillationDate(e.currentTarget.value)} required />

            <Grid>
              <Grid.Col span={4}>
                <NumberInput label="Cabezas (L)" value={headsVolumeL} onChange={(val) => setHeadsVolumeL(val === "" ? "" : Number(val))} min={0} required />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput label="Corazón / Tequila (L)" value={heartsVolumeL} onChange={(val) => setHeartsVolumeL(val === "" ? "" : Number(val))} min={0} required />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput label="Colas (L)" value={tailsVolumeL} onChange={(val) => setTailsVolumeL(val === "" ? "" : Number(val))} min={0} required />
              </Grid.Col>
            </Grid>

            <Grid>
              <Grid.Col span={4}>
                <NumberInput label="Volumen Total Destilado (L)" value={totalDistilledVolumeL} onChange={(val) => setTotalDistilledVolumeL(val === "" ? "" : Number(val))} min={0} required />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput label="Riqueza Alcohólica (% ABV)" value={alcoholContentPct} onChange={(val) => setAlcoholContentPct(val === "" ? "" : Number(val))} decimalScale={1} min={0} max={100} required />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput label="Rendimiento Real (L)" value={actualYieldL} onChange={(val) => setActualYieldL(val === "" ? "" : Number(val))} min={0} required />
              </Grid.Col>
            </Grid>

            <Grid>
              <Grid.Col span={6}>
                <NumberInput label="Temperatura Cocción (°C)" value={cookingTemperatureC} onChange={(val) => setCookingTemperatureC(val === "" ? "" : Number(val))} min={50} max={150} />
              </Grid.Col>
              <Grid.Col span={6}>
                <NumberInput label="pH de Fermentación" value={fermentationPh} onChange={(val) => setFermentationPh(val === "" ? "" : Number(val))} decimalScale={1} min={1} max={14} />
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
                  <TextInput type="date" label="Fecha de Inicio de Maduración" value={maturationStartDate} onChange={(e) => setMaturationStartDate(e.currentTarget.value)} required />
                </Grid.Col>
                <Grid.Col span={6}>
                  <NumberInput label="Días Requeridos de Maduración" value={requiredMaturationDays} onChange={(val) => setRequiredMaturationDays(val === "" ? "" : Number(val))} min={1} required />
                </Grid.Col>
              </Grid>
            )}

            <Textarea label="Notas (opcional)" value={notes} onChange={(e) => setNotes(e.currentTarget.value)} />

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => { setModalOpen(false); resetForm(); }}>Cancelar</Button>
              <Button color="orange" type="submit" loading={submitting}>Guardar Destilación</Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      {selectedBatch && (
        <Modal opened={detailModalOpen} onClose={() => setDetailModalOpen(false)} title={`Detalle de Destilación: ${selectedBatch.traceabilityCode}`} size="md">
          <Stack gap="xs">
            <Text size="xs"><b>Lotes Origen Jima:</b> {selectedBatch.sourceTraceabilityCodes.join(", ")}</Text>
            <Text size="xs"><b>Corte Cabezas:</b> {selectedBatch.headsVolumeL} L</Text>
            <Text size="xs"><b>Corte Corazón:</b> {Number(selectedBatch.heartsVolumeL).toLocaleString()} L</Text>
            <Text size="xs"><b>Corte Colas:</b> {selectedBatch.tailsVolumeL} L</Text>
            <Text size="xs"><b>Volumen Total Destilado:</b> {Number(selectedBatch.totalDistilledVolumeL).toLocaleString()} L</Text>
            <Text size="xs"><b>Graduación Alcohólica (ABV):</b> {selectedBatch.alcoholContentPct}%</Text>
            <Text size="xs"><b>Parámetros:</b> Temp: {selectedBatch.cookingTemperatureC ?? "—"}°C | pH: {selectedBatch.fermentationPh ?? "—"}</Text>
            <Text size="xs"><b>Rendimiento estimado vs. real:</b> {selectedBatch.estimatedYieldLSnapshot} L est. / {selectedBatch.actualYieldL} L real</Text>
            <Text size="xs"><b>Estado de Maduración:</b> {selectedBatch.maturationRequired ? `${selectedBatch.requiredMaturationDays} días desde ${selectedBatch.maturationStartDate}` : "Sin maduración"}</Text>
            <Text size="xs"><b>Lista para envasar desde:</b> {selectedBatch.readyForBottlingAt ? new Date(selectedBatch.readyForBottlingAt).toLocaleString() : "Inmediata"}</Text>
            <Button mt="md" color="orange" fullWidth onClick={() => setDetailModalOpen(false)}>Cerrar Detalle</Button>
          </Stack>
        </Modal>
      )}

      <Modal opened={!!cancelTarget} onClose={() => setCancelTarget(null)} title="Cancelar lote de Destilación (RB-503)" size="sm">
        <Stack gap="sm">
          <Text size="sm">Cancelar <b>{cancelTarget?.traceabilityCode}</b>. Esta acción requiere una razón y queda registrada permanentemente.</Text>
          <Textarea label="Razón de cancelación" value={cancelReason} onChange={(e) => setCancelReason(e.currentTarget.value)} required />
          <Group justify="flex-end">
            <Button variant="outline" onClick={() => setCancelTarget(null)}>Volver</Button>
            <Button color="red" onClick={handleCancelConfirm} disabled={!cancelReason.trim()}>Confirmar cancelación</Button>
          </Group>
        </Stack>
      </Modal>
    </Stack>
  );
}
