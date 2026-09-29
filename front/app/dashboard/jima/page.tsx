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
  ActionIcon,
  Tooltip,
  Loader,
  Center,
} from "@mantine/core";
import {
  IconPlant2,
  IconPlus,
  IconAlertCircle,
  IconEye,
  IconInfoCircle,
  IconCheck,
  IconBan,
} from "@tabler/icons-react";
import { harvestApi, catalogsApi, JimaBatchResponse } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { canWriteStage } from "@/lib/roles";

export default function JimaPage() {
  const { user } = useAuth();
  const canWrite = canWriteStage(user?.roles || [], "HARVEST");

  const [batches, setBatches] = useState<JimaBatchResponse[]>([]);
  const [fields, setFields] = useState<Array<{ id: string; fieldCode: string; name: string }>>([]);
  const [suppliers, setSuppliers] = useState<Array<{ id: string; supplierCode: string; legalName: string }>>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState<JimaBatchResponse | null>(null);
  const [cancelTarget, setCancelTarget] = useState<JimaBatchResponse | null>(null);
  const [cancelReason, setCancelReason] = useState("");

  const [fieldId, setFieldId] = useState<string | null>(null);
  const [supplierId, setSupplierId] = useState<string | null>(null);
  const [harvestDate, setHarvestDate] = useState("");
  const [totalWeightKg, setTotalWeightKg] = useState<number | "">("");
  const [agaveHeartsCount, setAgaveHeartsCount] = useState<number | "">("");
  const [notes, setNotes] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const estimatedYieldL = typeof totalWeightKg === "number" ? Math.round(totalWeightKg * 0.12) : 0;

  const loadAll = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [batchList, fieldList, supplierList] = await Promise.all([
        harvestApi.list(),
        catalogsApi.agaveFields(),
        catalogsApi.suppliers(),
      ]);
      setBatches(batchList);
      setFields(fieldList);
      setSuppliers(supplierList);
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
    setFieldId(null);
    setSupplierId(null);
    setHarvestDate("");
    setTotalWeightKg("");
    setAgaveHeartsCount("");
    setNotes("");
    setFormError(null);
  };

  const handleCreateBatch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fieldId || !supplierId || !harvestDate || totalWeightKg === "" || agaveHeartsCount === "") return;
    setSubmitting(true);
    setFormError(null);
    try {
      await harvestApi.create({
        fieldId,
        supplierId,
        harvestDate,
        totalWeightKg: Number(totalWeightKg),
        agaveHeartsCount: Number(agaveHeartsCount),
        notes: notes || undefined,
      });
      setModalOpen(false);
      resetForm();
      await loadAll();
    } catch (err: any) {
      setFormError(err.message || "Error al registrar el lote de Jima");
    } finally {
      setSubmitting(false);
    }
  };

  const handleComplete = async (batch: JimaBatchResponse) => {
    try {
      await harvestApi.complete(batch.batchId);
      await loadAll();
    } catch (err: any) {
      alert(err.message || "No se pudo completar el lote");
    }
  };

  const handleCancelConfirm = async () => {
    if (!cancelTarget || !cancelReason.trim()) return;
    try {
      await harvestApi.cancel(cancelTarget.batchId, cancelReason.trim());
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
        <Loader color="teal" />
      </Center>
    );
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Etapa 1: Jima y Cosecha de Agave
          </Title>
          <Text size="sm" c="dimmed">
            Registro de lotes de agave, validación de Denominación de Origen y generación de permisos de transporte (FR-05 a FR-11)
          </Text>
        </div>
        {canWrite && (
          <Button leftSection={<IconPlus size={18} />} color="teal" radius="md" onClick={() => setModalOpen(true)}>
            Registrar Nueva Jima
          </Button>
        )}
      </Group>

      {loadError && (
        <Alert color="red" title="Error de conexión con el backend" icon={<IconAlertCircle size={18} />}>
          {loadError}
        </Alert>
      )}

      <Grid>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #209b99" }}>
            <Text size="xs" c="dimmed" fw={700}>TOTAL REGISTRADO EN JIMA</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.reduce((acc, b) => acc + Number(b.totalWeightKg), 0).toLocaleString()} kg
            </Text>
            <Text size="xs" c="teal" mt="4px">
              Rendimiento Estimado Total: {batches.reduce((acc, b) => acc + Number(b.estimatedYieldL), 0).toLocaleString()} L
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ff8f00" }}>
            <Text size="xs" c="dimmed" fw={700}>PERMISOS DE TRANSPORTE SAT</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.filter((b) => b.transportPermitNumber).length} Generados
            </Text>
            <Text size="xs" c="dimmed" mt="4px">Auto-generados al crear el lote (FR-10)</Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #4bcbc9" }}>
            <Text size="xs" c="dimmed" fw={700}>LOTES CON ADVERTENCIA DE CAPACIDAD</Text>
            <Text size="xl" fw={700} mt="4px">{batches.filter((b) => b.capacityWarning).length}</Text>
            <Text size="xs" c="teal" mt="4px">Peso sobre capacidad máx. de planta (RB-104)</Text>
          </Paper>
        </Grid.Col>
      </Grid>

      <Card withBorder radius="md" p="md" shadow="xs">
        <Group justify="space-between" mb="md">
          <Title order={4}>Lotes de Cosecha de Agave Registrados</Title>
          <Badge color="teal" size="lg">{batches.length} Lotes</Badge>
        </Group>

        <Table highlightOnHover striped verticalSpacing="sm">
          <Table.Thead bg="gray.1">
            <Table.Tr>
              <Table.Th>Código Trazabilidad</Table.Th>
              <Table.Th>Predio y Proveedor</Table.Th>
              <Table.Th>Peso Total (kg)</Table.Th>
              <Table.Th>Piñas</Table.Th>
              <Table.Th>Rendimiento Est. (L)</Table.Th>
              <Table.Th>Permiso Transporte</Table.Th>
              <Table.Th>Estado</Table.Th>
              <Table.Th>Acción</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {batches.map((b) => (
              <Table.Tr key={b.batchId}>
                <Table.Td>
                  <Text fw={700} size="sm" c="teal.8">{b.traceabilityCode}</Text>
                  <Text size="11px" c="dimmed">{b.harvestDate}</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={600}>{b.fieldCode}</Text>
                  <Text size="xs" c="dimmed">{b.supplierCode}</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700}>{Number(b.totalWeightKg).toLocaleString()} kg</Text>
                  {b.capacityWarning && <Badge color="orange" size="xs" mt={4}>Excede capacidad</Badge>}
                </Table.Td>
                <Table.Td><Text size="xs">{b.agaveHeartsCount} piñas</Text></Table.Td>
                <Table.Td><Text size="xs" fw={700} c="teal">{Number(b.estimatedYieldL).toLocaleString()} L</Text></Table.Td>
                <Table.Td>
                  <Badge color={b.transportPermitNumber ? "green" : "gray"} size="xs">
                    {b.transportPermitNumber || "—"}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Badge color={b.status === "COMPLETED" ? "green" : b.status === "CANCELLED" ? "red" : "blue"} size="sm">
                    {b.status}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    <Tooltip label="Ver detalle">
                      <ActionIcon variant="light" color="teal" onClick={() => { setSelectedBatch(b); setDetailModalOpen(true); }}>
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
        title={<Group gap="xs"><IconPlant2 color="#209b99" size={20} /><Text fw={700}>Registrar Nuevo Lote de Jima (FR-05 / FR-06)</Text></Group>}
        size="lg"
        radius="md"
      >
        <form onSubmit={handleCreateBatch}>
          <Stack gap="sm">
            <Alert color="blue" title="Generación Automática de Trazabilidad (FR-05)" icon={<IconInfoCircle size={18} />}>
              Al guardar, el backend asigna el código <b>TRZ-YYYY-NNNNN</b> y expide el permiso de transporte SAT en estado <b>GENERATED</b>.
            </Alert>

            {formError && (
              <Alert color="red" title="Error" icon={<IconAlertCircle size={18} />}>{formError}</Alert>
            )}

            <Select
              label="Predio de Cosecha (Obligatorio)"
              data={fields.map((f) => ({ value: f.id, label: `${f.fieldCode} — ${f.name}` }))}
              value={fieldId}
              onChange={setFieldId}
              searchable
              required
            />

            <Select
              label="Proveedor de Agave (RB-102 — debe estar activo)"
              data={suppliers.map((s) => ({ value: s.id, label: `${s.supplierCode} — ${s.legalName}` }))}
              value={supplierId}
              onChange={setSupplierId}
              searchable
              required
            />

            <TextInput
              type="date"
              label="Fecha de Cosecha"
              value={harvestDate}
              onChange={(e) => setHarvestDate(e.currentTarget.value)}
              required
            />

            <Grid>
              <Grid.Col span={6}>
                <NumberInput
                  label="Peso Total de Agave (kg)"
                  value={totalWeightKg}
                  onChange={(val) => setTotalWeightKg(val === "" ? "" : Number(val))}
                  min={1}
                  step={500}
                  required
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <NumberInput
                  label="Número de Piñas"
                  value={agaveHeartsCount}
                  onChange={(val) => setAgaveHeartsCount(val === "" ? "" : Number(val))}
                  min={1}
                  required
                />
              </Grid.Col>
            </Grid>

            <Textarea label="Notas (opcional)" value={notes} onChange={(e) => setNotes(e.currentTarget.value)} />

            <Card withBorder bg="teal.0" padding="xs" radius="md">
              <Group justify="space-between">
                <Text size="xs" fw={700} c="teal.9">Rendimiento Teórico Estimado (FR-09):</Text>
                <Text size="sm" fw={800} c="teal.9">{estimatedYieldL.toLocaleString()} Litros (Factor 0.12)</Text>
              </Group>
            </Card>

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => { setModalOpen(false); resetForm(); }}>Cancelar</Button>
              <Button color="teal" type="submit" loading={submitting}>Registrar Jima y Generar TRZ</Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      {selectedBatch && (
        <Modal opened={detailModalOpen} onClose={() => setDetailModalOpen(false)} title={`Detalle de Lote: ${selectedBatch.traceabilityCode}`} size="md">
          <Stack gap="xs">
            <Text size="xs"><b>Predio:</b> {selectedBatch.fieldCode}</Text>
            <Text size="xs"><b>Proveedor:</b> {selectedBatch.supplierCode}</Text>
            <Text size="xs"><b>Fecha de Cosecha:</b> {selectedBatch.harvestDate}</Text>
            <Text size="xs"><b>Peso Agave:</b> {Number(selectedBatch.totalWeightKg).toLocaleString()} kg</Text>
            <Text size="xs"><b>Número de Piñas:</b> {selectedBatch.agaveHeartsCount}</Text>
            <Text size="xs"><b>Rendimiento Estimado:</b> {Number(selectedBatch.estimatedYieldL).toLocaleString()} L</Text>
            <Text size="xs"><b>Permiso de Transporte SAT:</b> {selectedBatch.transportPermitNumber || "—"}</Text>
            <Text size="xs"><b>Estado:</b> {selectedBatch.status}</Text>
            <Button mt="md" color="teal" fullWidth onClick={() => setDetailModalOpen(false)}>Cerrar Detalle</Button>
          </Stack>
        </Modal>
      )}

      <Modal opened={!!cancelTarget} onClose={() => setCancelTarget(null)} title="Cancelar lote de Jima (RB-503)" size="sm">
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
