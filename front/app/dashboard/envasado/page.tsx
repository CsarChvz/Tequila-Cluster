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
  MultiSelect,
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
  IconBottle,
  IconPlus,
  IconAlertTriangle,
  IconCheck,
  IconEye,
  IconInfoCircle,
  IconBan,
} from "@tabler/icons-react";
import {
  bottlingApi,
  distillationApi,
  catalogsApi,
  BottlingBatchResponse,
  DistillationBatchResponse,
  TaxLabelResponse,
} from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { canWriteStage } from "@/lib/roles";

export default function BottlingPage() {
  const { user } = useAuth();
  const canWrite = canWriteStage(user?.roles || [], "BOTTLING");

  const [batches, setBatches] = useState<BottlingBatchResponse[]>([]);
  const [readyDistillationBatches, setReadyDistillationBatches] = useState<DistillationBatchResponse[]>([]);
  const [brands, setBrands] = useState<Array<{ id: string; name: string }>>([]);
  const [categories, setCategories] = useState<Array<{ id: string; code: string; name: string; minimumMaturationDays: number }>>([]);
  const [labelRequirements, setLabelRequirements] = useState<Array<{ id: string; code: string; displayName: string; required: boolean }>>([]);
  const [availableTaxLabels, setAvailableTaxLabels] = useState<TaxLabelResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState<BottlingBatchResponse | null>(null);
  const [cancelTarget, setCancelTarget] = useState<BottlingBatchResponse | null>(null);
  const [cancelReason, setCancelReason] = useState("");

  const [distillationBatchId, setDistillationBatchId] = useState<string | null>(null);
  const [brandId, setBrandId] = useState<string | null>(null);
  const [categoryId, setCategoryId] = useState<string | null>(null);
  const [bottlingDate, setBottlingDate] = useState("");
  const [bottleCapacityMl, setBottleCapacityMl] = useState<number | "">(750);
  const [unitsBottled, setUnitsBottled] = useState<number | "">("");
  const [registeredLossesUnits, setRegisteredLossesUnits] = useState<number | "">(0);
  const [productionLotNumber, setProductionLotNumber] = useState("");
  const [assignedTaxLabelIds, setAssignedTaxLabelIds] = useState<string[]>([]);
  const [labelValues, setLabelValues] = useState<Record<string, string>>({});
  const [notes, setNotes] = useState("");
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const totalVolumeL = typeof unitsBottled === "number" && typeof bottleCapacityMl === "number"
    ? Number(((unitsBottled * bottleCapacityMl) / 1000).toFixed(3))
    : 0;

  const loadAll = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [bottlingList, distillationList, brandList, categoryList, labelReqList, taxLabels] = await Promise.all([
        bottlingApi.list(),
        distillationApi.list(),
        catalogsApi.brands(),
        catalogsApi.tequilaCategories(),
        catalogsApi.labelRequirements(),
        bottlingApi.availableTaxLabels(),
      ]);
      setBatches(bottlingList);
      setReadyDistillationBatches(
        distillationList.filter(
          (d) => d.status === "COMPLETED" && d.readyForBottlingAt && new Date(d.readyForBottlingAt) <= new Date()
        )
      );
      setBrands(brandList);
      setCategories(categoryList);
      setLabelRequirements(labelReqList.filter((r) => r.active !== false));
      setAvailableTaxLabels(taxLabels);
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
    setDistillationBatchId(null);
    setBrandId(null);
    setCategoryId(null);
    setBottlingDate("");
    setBottleCapacityMl(750);
    setUnitsBottled("");
    setRegisteredLossesUnits(0);
    setProductionLotNumber("");
    setAssignedTaxLabelIds([]);
    setLabelValues({});
    setNotes("");
    setFormError(null);
  };

  const handleCreateBatch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!distillationBatchId || !brandId || !categoryId || !bottlingDate || !productionLotNumber || unitsBottled === "") return;

    const missingRequired = labelRequirements.filter((r) => r.required && !labelValues[r.id]?.trim());
    if (missingRequired.length > 0) {
      setFormError(`Falta capturar el requisito de etiqueta obligatorio: ${missingRequired.map((r) => r.displayName).join(", ")} (RB-305)`);
      return;
    }
    if (assignedTaxLabelIds.length < Number(unitsBottled)) {
      setFormError(`Se necesitan al menos ${unitsBottled} marbetes asignados, hay ${assignedTaxLabelIds.length} seleccionados (RB-302)`);
      return;
    }

    setFormError(null);
    setSubmitting(true);
    try {
      await bottlingApi.create({
        distillationBatchId,
        brandId,
        categoryId,
        bottlingDate,
        bottleCapacityMl: Number(bottleCapacityMl),
        totalVolumeL,
        productionLotNumber,
        unitsBottled: Number(unitsBottled),
        registeredLossesUnits: Number(registeredLossesUnits) || 0,
        assignedTaxLabelIds,
        labelValues,
        notes: notes || undefined,
      });
      setModalOpen(false);
      resetForm();
      await loadAll();
    } catch (err: any) {
      setFormError(err.message || "Error al registrar el envasado");
    } finally {
      setSubmitting(false);
    }
  };

  const handleComplete = async (batch: BottlingBatchResponse) => {
    try {
      await bottlingApi.complete(batch.batchId);
      await loadAll();
    } catch (err: any) {
      alert(err.message || "No se pudo completar el lote (revisa requisitos de etiqueta, RB-307)");
    }
  };

  const handleCancelConfirm = async () => {
    if (!cancelTarget || !cancelReason.trim()) return;
    try {
      await bottlingApi.cancel(cancelTarget.batchId, cancelReason.trim());
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
        <Loader color="cyan" />
      </Center>
    );
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Etapa 3: Envasado y Marbetes Fiscales SAT
          </Title>
          <Text size="sm" c="dimmed">
            Control de embotellado, folios de marbetes SAT, código único por botella y reconciliación de mermas (FR-20 a FR-25)
          </Text>
        </div>
        {canWrite && (
          <Button leftSection={<IconPlus size={18} />} color="cyan" radius="md" onClick={() => setModalOpen(true)}>
            Registrar Nuevo Envasado
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
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #2daeac" }}>
            <Text size="xs" c="dimmed" fw={700}>BOTELLAS PRODUCIDAS</Text>
            <Text size="xl" fw={700} mt="4px">{batches.reduce((acc, b) => acc + b.unitsBottled, 0).toLocaleString()} Unidades</Text>
          </Paper>
        </Grid.Col>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #209b99" }}>
            <Text size="xs" c="dimmed" fw={700}>MARBETES SAT DISPONIBLES</Text>
            <Text size="xl" fw={700} mt="4px">{availableTaxLabels.length} Folios</Text>
            <Text size="xs" c="teal" mt="4px">Estado AVAILABLE (RB-302)</Text>
          </Paper>
        </Grid.Col>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ff8f00" }}>
            <Text size="xs" c="dimmed" fw={700}>LOTES CON DISCREPANCIA DE RECONCILIACIÓN</Text>
            <Text size="xl" fw={700} mt="4px">{batches.filter((b) => b.taxLabelReconciliationWarning).length}</Text>
            <Text size="xs" c="dimmed" mt="4px">Tolerancia permitida: ≤ 2.0% (RB-306)</Text>
          </Paper>
        </Grid.Col>
      </Grid>

      <Card withBorder radius="md" p="md" shadow="xs">
        <Group justify="space-between" mb="md">
          <Title order={4}>Lotes de Envasado Registrados</Title>
          <Badge color="cyan" size="lg">{batches.length} Lotes</Badge>
        </Group>

        <Table highlightOnHover striped verticalSpacing="sm">
          <Table.Thead bg="gray.1">
            <Table.Tr>
              <Table.Th>Código Trazabilidad</Table.Th>
              <Table.Th>Lote Destilación Origen</Table.Th>
              <Table.Th>Marca y Categoría</Table.Th>
              <Table.Th>Presentación</Table.Th>
              <Table.Th>Unidades / Mermas</Table.Th>
              <Table.Th>Marbetes Usados</Table.Th>
              <Table.Th>Estado</Table.Th>
              <Table.Th>Acciones</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {batches.map((b) => (
              <Table.Tr key={b.batchId}>
                <Table.Td>
                  <Text fw={700} size="sm" c="cyan.9">{b.traceabilityCode}</Text>
                  <Text size="11px" c="dimmed">{b.productionLotNumber}</Text>
                </Table.Td>
                <Table.Td><Badge color="orange" variant="light" size="xs">{b.sourceDistillationTraceabilityCode || "—"}</Badge></Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700}>{b.brandName || "—"}</Text>
                  <Badge color="amber" variant="dot" size="xs">{b.categoryName || "—"}</Badge>
                </Table.Td>
                <Table.Td><Text size="xs">{b.bottleCapacityMl} ml ({Number(b.totalVolumeL).toLocaleString()} L)</Text></Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700}>{b.unitsBottled.toLocaleString()} botellas</Text>
                  <Text size="11px" c="dimmed">Merma: {b.registeredLossesUnits} unidades</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={600}>{b.taxLabelsAssigned} usados</Text>
                  {b.taxLabelReconciliationWarning && <Badge color="orange" size="xs" mt={4}>Discrepancia</Badge>}
                </Table.Td>
                <Table.Td>
                  <Badge color={b.status === "COMPLETED" ? "green" : b.status === "CANCELLED" ? "red" : "blue"} size="sm">{b.status}</Badge>
                </Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    <Tooltip label="Ver detalle del lote">
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
        title={<Group gap="xs"><IconBottle color="#2daeac" size={20} /><Text fw={700}>Registrar Nuevo Envasado (FR-20 / FR-21)</Text></Group>}
        size="lg"
        radius="md"
      >
        <form onSubmit={handleCreateBatch}>
          <Stack gap="sm">
            <Alert color="cyan" title="Requisitos de Marbete SAT (RB-302/303)" icon={<IconInfoCircle size={18} />}>
              Se necesita 1 marbete AVAILABLE por cada botella + merma registrada. Si no hay suficientes disponibles, un Administrator debe cargarlos primero (bulk-load vía Postman).
            </Alert>

            {formError && <Alert color="red" title="Error" icon={<IconAlertTriangle size={18} />}>{formError}</Alert>}

            <Select
              label="Lote de Destilación Origen (listo para envasar)"
              data={readyDistillationBatches.map((d) => ({
                value: d.batchId,
                label: `${d.traceabilityCode} — Hearts ${Number(d.heartsVolumeL).toLocaleString()}L (${d.alcoholContentPct}% ABV)`,
              }))}
              value={distillationBatchId}
              onChange={setDistillationBatchId}
              searchable
              required
            />

            <Grid>
              <Grid.Col span={6}>
                <Select
                  label="Marca Comercial"
                  data={brands.map((b) => ({ value: b.id, label: b.name }))}
                  value={brandId}
                  onChange={setBrandId}
                  searchable
                  required
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <Select
                  label="Categoría Tequila"
                  data={categories.map((c) => ({ value: c.id, label: `${c.name} (mín. ${c.minimumMaturationDays} días)` }))}
                  value={categoryId}
                  onChange={setCategoryId}
                  required
                />
              </Grid.Col>
            </Grid>

            <Grid>
              <Grid.Col span={6}>
                <TextInput type="date" label="Fecha de Envasado" value={bottlingDate} onChange={(e) => setBottlingDate(e.currentTarget.value)} required />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput label="Lote de Producción (único)" value={productionLotNumber} onChange={(e) => setProductionLotNumber(e.currentTarget.value)} required />
              </Grid.Col>
            </Grid>

            <Grid>
              <Grid.Col span={4}>
                <NumberInput label="Capacidad Botella (ml)" value={bottleCapacityMl} onChange={(val) => setBottleCapacityMl(val === "" ? "" : Number(val))} min={1} required />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput label="Unidades a Embotellar" value={unitsBottled} onChange={(val) => setUnitsBottled(val === "" ? "" : Number(val))} min={1} required />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput label="Mermas Registradas" value={registeredLossesUnits} onChange={(val) => setRegisteredLossesUnits(val === "" ? "" : Number(val))} min={0} />
              </Grid.Col>
            </Grid>

            <Text size="xs" c="dimmed">Volumen total calculado: {totalVolumeL.toLocaleString()} L</Text>

            <MultiSelect
              label={`Marbetes SAT a asignar (mínimo ${unitsBottled || 0})`}
              placeholder="Selecciona folios AVAILABLE"
              data={availableTaxLabels.map((t) => ({ value: t.id, label: t.folio }))}
              value={assignedTaxLabelIds}
              onChange={setAssignedTaxLabelIds}
              searchable
              required
            />

            {labelRequirements.length > 0 && (
              <Card withBorder padding="sm" radius="md">
                <Text size="xs" fw={700} mb="xs">Requisitos de Etiqueta (FR-24)</Text>
                <Stack gap="xs">
                  {labelRequirements.map((req) => (
                    <TextInput
                      key={req.id}
                      label={`${req.displayName}${req.required ? " *" : ""}`}
                      value={labelValues[req.id] || ""}
                      onChange={(e) => setLabelValues((prev) => ({ ...prev, [req.id]: e.currentTarget.value }))}
                      required={req.required}
                    />
                  ))}
                </Stack>
              </Card>
            )}

            <Textarea label="Notas (opcional)" value={notes} onChange={(e) => setNotes(e.currentTarget.value)} />

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => { setModalOpen(false); resetForm(); }}>Cancelar</Button>
              <Button color="cyan" type="submit" loading={submitting}>Registrar Envasado</Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      {selectedBatch && (
        <Modal opened={detailModalOpen} onClose={() => setDetailModalOpen(false)} title={`Detalle de Envasado: ${selectedBatch.traceabilityCode}`} size="md">
          <Stack gap="xs">
            <Text size="xs"><b>Lote Destilación Origen:</b> {selectedBatch.sourceDistillationTraceabilityCode || "—"}</Text>
            <Text size="xs"><b>Marca / Categoría:</b> {selectedBatch.brandName} ({selectedBatch.categoryName})</Text>
            <Text size="xs"><b>Volumen Total Envasado:</b> {Number(selectedBatch.totalVolumeL).toLocaleString()} Litros</Text>
            <Text size="xs"><b>Unidades Embotelladas:</b> {selectedBatch.unitsBottled.toLocaleString()} botellas</Text>
            <Text size="xs"><b>Mermas Registradas:</b> {selectedBatch.registeredLossesUnits} unidades</Text>
            <Text size="xs"><b>Marbetes Usados:</b> {selectedBatch.taxLabelsAssigned}</Text>
            <Text size="xs"><b>Unidades disponibles en inventario:</b> {selectedBatch.bottledUnitsCreated}</Text>
            <Text size="xs"><b>Reconciliación:</b> {selectedBatch.taxLabelReconciliationWarning ? "⚠ Discrepancia sobre la tolerancia (RB-306)" : "✓ Dentro de tolerancia"}</Text>
            <Button mt="md" color="cyan" fullWidth onClick={() => setDetailModalOpen(false)}>Cerrar Detalle</Button>
          </Stack>
        </Modal>
      )}

      <Modal opened={!!cancelTarget} onClose={() => setCancelTarget(null)} title="Cancelar lote de Envasado (RB-503)" size="sm">
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
