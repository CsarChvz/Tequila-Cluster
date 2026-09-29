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
  NumberInput,
  Select,
  Checkbox,
  Stack,
  Alert,
  Paper,
  Grid,
  ActionIcon,
  Tooltip,
  Loader,
  Center,
  Divider,
} from "@mantine/core";
import {
  IconTruckDelivery,
  IconPlus,
  IconAlertTriangle,
  IconEye,
  IconInfoCircle,
  IconBan,
  IconFileText,
} from "@tabler/icons-react";
import { logisticsApi, bottlingApi, catalogsApi, ShipmentResponse, BottlingBatchResponse } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { canWriteStage } from "@/lib/roles";

export default function LogisticsPage() {
  const { user } = useAuth();
  const canWrite = canWriteStage(user?.roles || [], "LOGISTICS");

  const [shipments, setShipments] = useState<ShipmentResponse[]>([]);
  const [completedBottlingBatches, setCompletedBottlingBatches] = useState<BottlingBatchResponse[]>([]);
  const [carriers, setCarriers] = useState<Array<{ id: string; name: string }>>([]);
  const [shipmentTypes, setShipmentTypes] = useState<Array<{ id: string; code: string; name: string }>>([]);
  const [documentTypes, setDocumentTypes] = useState<Array<{ id: string; code: string; name: string }>>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [modalOpen, setModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedShipment, setSelectedShipment] = useState<ShipmentResponse | null>(null);
  const [cancelTarget, setCancelTarget] = useState<ShipmentResponse | null>(null);
  const [cancelReason, setCancelReason] = useState("");

  const [shipmentNumber, setShipmentNumber] = useState("");
  const [shipmentTypeId, setShipmentTypeId] = useState<string | null>(null);
  const [carrierId, setCarrierId] = useState<string | null>(null);
  const [vehicleLicensePlate, setVehicleLicensePlate] = useState("");
  const [destination, setDestination] = useState("");
  const [departureAt, setDepartureAt] = useState("");
  const [estimatedArrivalAt, setEstimatedArrivalAt] = useState("");
  const [bottlingBatchId, setBottlingBatchId] = useState<string | null>(null);
  const [quantityUnits, setQuantityUnits] = useState<number | "">("");
  const [formError, setFormError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [docTypeId, setDocTypeId] = useState<string | null>(null);
  const [docNumber, setDocNumber] = useState("");
  const [docValid, setDocValid] = useState(true);
  const [docSubmitting, setDocSubmitting] = useState(false);
  const [docError, setDocError] = useState<string | null>(null);

  const selectedBatch = completedBottlingBatches.find((b) => b.batchId === bottlingBatchId);

  const loadAll = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [shipmentList, bottlingList, carrierList, shipmentTypeList, documentTypeList] = await Promise.all([
        logisticsApi.list(),
        bottlingApi.list(),
        catalogsApi.carriers(),
        catalogsApi.shipmentTypes(),
        catalogsApi.documentTypes(),
      ]);
      setShipments(shipmentList);
      setCompletedBottlingBatches(bottlingList.filter((b) => b.status === "COMPLETED" && b.bottledUnitsCreated > 0));
      setCarriers(carrierList);
      setShipmentTypes(shipmentTypeList);
      setDocumentTypes(documentTypeList);
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
    setShipmentNumber("");
    setShipmentTypeId(null);
    setCarrierId(null);
    setVehicleLicensePlate("");
    setDestination("");
    setDepartureAt("");
    setEstimatedArrivalAt("");
    setBottlingBatchId(null);
    setQuantityUnits("");
    setFormError(null);
  };

  const handleCreateShipment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!shipmentTypeId || !carrierId || !bottlingBatchId || quantityUnits === "" || !departureAt || !estimatedArrivalAt) return;

    if (selectedBatch && Number(quantityUnits) > selectedBatch.bottledUnitsCreated) {
      setFormError(`RB-401: solo hay ${selectedBatch.bottledUnitsCreated} unidades disponibles en ese lote de envasado.`);
      return;
    }

    setFormError(null);
    setSubmitting(true);
    try {
      await logisticsApi.create({
        shipmentNumber,
        shipmentTypeId,
        carrierId,
        vehicleLicensePlate,
        destination,
        departureAt: new Date(departureAt).toISOString(),
        estimatedArrivalAt: new Date(estimatedArrivalAt).toISOString(),
        items: [{ bottlingBatchId, quantityUnits: Number(quantityUnits) }],
      });
      setModalOpen(false);
      resetForm();
      await loadAll();
    } catch (err: any) {
      setFormError(err.message || "Error al crear el embarque");
    } finally {
      setSubmitting(false);
    }
  };

  const handleAddDocument = async () => {
    if (!selectedShipment || !docTypeId) return;
    setDocSubmitting(true);
    setDocError(null);
    try {
      await logisticsApi.addDocument(selectedShipment.id, {
        documentTypeId: docTypeId,
        documentNumber: docNumber || undefined,
        valid: docValid,
      });
      setDocTypeId(null);
      setDocNumber("");
      setDocValid(true);
      const refreshed = await logisticsApi.list();
      setShipments(refreshed);
      setSelectedShipment(refreshed.find((s) => s.id === selectedShipment.id) || null);
    } catch (err: any) {
      setDocError(err.message || "No se pudo agregar el documento");
    } finally {
      setDocSubmitting(false);
    }
  };

  const handleStartTransit = async (shipment: ShipmentResponse) => {
    try {
      await logisticsApi.startTransit(shipment.id);
      await loadAll();
      setDetailModalOpen(false);
    } catch (err: any) {
      alert(err.message || "No se puede iniciar tránsito (revisa documentos requeridos, RB-402)");
    }
  };

  const handleDeliver = async (shipment: ShipmentResponse) => {
    try {
      await logisticsApi.deliver(shipment.id);
      await loadAll();
    } catch (err: any) {
      alert(err.message || "No se pudo confirmar la entrega");
    }
  };

  const handleCancelConfirm = async () => {
    if (!cancelTarget || !cancelReason.trim()) return;
    try {
      await logisticsApi.cancel(cancelTarget.id, cancelReason.trim());
      setCancelTarget(null);
      setCancelReason("");
      await loadAll();
    } catch (err: any) {
      alert(err.message || "No se pudo cancelar el embarque");
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
            Etapa 4: Logística y Embarques de Tequila
          </Title>
          <Text size="sm" c="dimmed">
            Despacho de embarques, documentos de transporte y confirmación de entrega (FR-26 a FR-32)
          </Text>
        </div>
        {canWrite && (
          <Button leftSection={<IconPlus size={18} />} color="orange" radius="md" onClick={() => setModalOpen(true)}>
            Crear Nuevo Embarque
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
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ff6f00" }}>
            <Text size="xs" c="dimmed" fw={700}>EMBARQUES EN TRÁNSITO</Text>
            <Text size="xl" fw={700} mt="4px">{shipments.filter((s) => s.status === "IN_TRANSIT").length} Activos</Text>
          </Paper>
        </Grid.Col>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #209b99" }}>
            <Text size="xs" c="dimmed" fw={700}>ENTREGAS CONFIRMADAS</Text>
            <Text size="xl" fw={700} mt="4px">{shipments.filter((s) => s.status === "DELIVERED").length} Entregados</Text>
          </Paper>
        </Grid.Col>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ffb300" }}>
            <Text size="xs" c="dimmed" fw={700}>PLANIFICADOS PENDIENTES DE DOCUMENTOS</Text>
            <Text size="xl" fw={700} mt="4px">{shipments.filter((s) => s.status === "PLANNED").length}</Text>
            <Text size="xs" c="dimmed" mt="4px">Bloqueo automático si falta documento (RB-402)</Text>
          </Paper>
        </Grid.Col>
      </Grid>

      <Card withBorder radius="md" p="md" shadow="xs">
        <Group justify="space-between" mb="md">
          <Title order={4}>Registro de Embarques de Salida</Title>
          <Badge color="orange" size="lg">{shipments.length} Embarques</Badge>
        </Group>

        <Table highlightOnHover striped verticalSpacing="sm">
          <Table.Thead bg="gray.1">
            <Table.Tr>
              <Table.Th>N° Embarque</Table.Th>
              <Table.Th>Lotes Embotellados Incluidos</Table.Th>
              <Table.Th>Transportista</Table.Th>
              <Table.Th>Placas</Table.Th>
              <Table.Th>Destino</Table.Th>
              <Table.Th>Documentos</Table.Th>
              <Table.Th>Estado</Table.Th>
              <Table.Th>Acciones</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {shipments.map((s) => (
              <Table.Tr key={s.id}>
                <Table.Td>
                  <Text fw={700} size="sm" c="orange.9">{s.shipmentNumber}</Text>
                  <Text size="11px" c="dimmed">Salida: {new Date(s.departureAt).toLocaleString()}</Text>
                </Table.Td>
                <Table.Td>
                  {s.items.map((it) => (
                    <Badge key={it.bottlingBatchId} color="cyan" variant="light" size="xs">
                      {it.traceabilityCode} ({it.quantityUnits.toLocaleString()} botellas)
                    </Badge>
                  ))}
                </Table.Td>
                <Table.Td><Text size="xs" fw={700}>{s.carrierName}</Text></Table.Td>
                <Table.Td><Badge color="gray" size="xs">{s.vehicleLicensePlate}</Badge></Table.Td>
                <Table.Td><Text size="xs">{s.destination}</Text></Table.Td>
                <Table.Td><Badge color={s.documents.length > 0 ? "green" : "red"} size="xs">{s.documents.length} documento(s)</Badge></Table.Td>
                <Table.Td>
                  <Badge color={s.status === "DELIVERED" ? "green" : s.status === "IN_TRANSIT" ? "orange" : s.status === "CANCELLED" ? "red" : "blue"} size="sm">
                    {s.status}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    {canWrite && s.status === "IN_TRANSIT" && (
                      <Button size="xs" color="teal" variant="light" onClick={() => handleDeliver(s)}>Confirmar Entrega</Button>
                    )}
                    <Tooltip label="Ver / agregar documentos">
                      <ActionIcon variant="light" color="orange" onClick={() => { setSelectedShipment(s); setDetailModalOpen(true); }}>
                        <IconEye size={16} />
                      </ActionIcon>
                    </Tooltip>
                    {canWrite && s.status === "PLANNED" && (
                      <Tooltip label="Cancelar embarque">
                        <ActionIcon variant="light" color="red" onClick={() => setCancelTarget(s)}>
                          <IconBan size={16} />
                        </ActionIcon>
                      </Tooltip>
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
        title={<Group gap="xs"><IconTruckDelivery color="#ff6f00" size={20} /><Text fw={700}>Crear Nuevo Embarque (FR-26 / FR-28)</Text></Group>}
        size="lg"
        radius="md"
      >
        <form onSubmit={handleCreateShipment}>
          <Stack gap="sm">
            <Alert color="orange" title="Regla de Salida RB-402" icon={<IconInfoCircle size={18} />}>
              El embarque se crea en estado <b>PLANNED</b>. No podrá pasar a <b>IN_TRANSIT</b> sin todos los documentos requeridos por su tipo de embarque, agregados y válidos.
            </Alert>

            {formError && <Alert color="red" title="Error" icon={<IconAlertTriangle size={18} />}>{formError}</Alert>}

            <TextInput label="Número de Embarque (único)" value={shipmentNumber} onChange={(e) => setShipmentNumber(e.currentTarget.value)} required />

            <Select
              label="Lote de Envasado (COMPLETED, con unidades disponibles)"
              data={completedBottlingBatches.map((b) => ({
                value: b.batchId,
                label: `${b.traceabilityCode} — ${b.brandName} (${b.bottledUnitsCreated.toLocaleString()} disponibles)`,
              }))}
              value={bottlingBatchId}
              onChange={setBottlingBatchId}
              searchable
              required
            />

            <NumberInput
              label="Unidades a Asignar a este Embarque"
              value={quantityUnits}
              onChange={(val) => setQuantityUnits(val === "" ? "" : Number(val))}
              min={1}
              max={selectedBatch?.bottledUnitsCreated}
              required
            />

            <Grid>
              <Grid.Col span={6}>
                <Select label="Tipo de Embarque" data={shipmentTypes.map((t) => ({ value: t.id, label: t.name }))} value={shipmentTypeId} onChange={setShipmentTypeId} required />
              </Grid.Col>
              <Grid.Col span={6}>
                <Select label="Transportista" data={carriers.map((c) => ({ value: c.id, label: c.name }))} value={carrierId} onChange={setCarrierId} searchable required />
              </Grid.Col>
            </Grid>

            <TextInput label="Placas del Vehículo / Remolque" value={vehicleLicensePlate} onChange={(e) => setVehicleLicensePlate(e.currentTarget.value)} required />
            <TextInput label="Destino Final / CEDIS" value={destination} onChange={(e) => setDestination(e.currentTarget.value)} required />

            <Grid>
              <Grid.Col span={6}>
                <TextInput type="datetime-local" label="Fecha/Hora de Salida" value={departureAt} onChange={(e) => setDepartureAt(e.currentTarget.value)} required />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput type="datetime-local" label="Llegada Estimada" value={estimatedArrivalAt} onChange={(e) => setEstimatedArrivalAt(e.currentTarget.value)} required />
              </Grid.Col>
            </Grid>

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => { setModalOpen(false); resetForm(); }}>Cancelar</Button>
              <Button color="orange" type="submit" loading={submitting}>Crear Embarque</Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      {selectedShipment && (
        <Modal opened={detailModalOpen} onClose={() => setDetailModalOpen(false)} title={`Documentos del Embarque: ${selectedShipment.shipmentNumber}`} size="md">
          <Stack gap="sm">
            <Text size="xs"><b>Transportista:</b> {selectedShipment.carrierName}</Text>
            <Text size="xs"><b>Placas:</b> {selectedShipment.vehicleLicensePlate}</Text>
            <Text size="xs"><b>Destino:</b> {selectedShipment.destination}</Text>
            <Text size="xs"><b>Estado:</b> {selectedShipment.status}</Text>

            <Text size="xs" fw={700} mt="xs">Documentos cargados (RB-402):</Text>
            {selectedShipment.documents.length === 0 && <Text size="xs" c="dimmed">Sin documentos cargados aún.</Text>}
            {selectedShipment.documents.map((doc) => (
              <Paper key={doc.id} p="xs" radius="sm" withBorder bg="gray.0">
                <Group justify="space-between">
                  <Text size="xs">{doc.documentTypeName} {doc.documentNumber ? `— ${doc.documentNumber}` : ""}</Text>
                  <Badge color={doc.valid ? "green" : "red"} size="xs">{doc.valid ? "✓ VÁLIDO" : "NO VÁLIDO"}</Badge>
                </Group>
              </Paper>
            ))}

            {canWrite && selectedShipment.status === "PLANNED" && (
              <Card withBorder padding="sm" radius="md" mt="xs">
                <Text size="xs" fw={700} mb="xs">Agregar documento</Text>
                {docError && <Alert color="red" mb="xs">{docError}</Alert>}
                <Stack gap="xs">
                  <Select label="Tipo de documento" data={documentTypes.map((d) => ({ value: d.id, label: d.name }))} value={docTypeId} onChange={setDocTypeId} />
                  <TextInput label="Número / folio (opcional)" value={docNumber} onChange={(e) => setDocNumber(e.currentTarget.value)} />
                  <Checkbox label="Documento válido" checked={docValid} onChange={(e) => setDocValid(e.currentTarget.checked)} />
                  <Button size="xs" leftSection={<IconFileText size={14} />} onClick={handleAddDocument} loading={docSubmitting} disabled={!docTypeId}>
                    Agregar documento
                  </Button>
                </Stack>
              </Card>
            )}

            {canWrite && selectedShipment.status === "PLANNED" && (
              <Button mt="sm" color="green" fullWidth onClick={() => handleStartTransit(selectedShipment)}>
                Iniciar Tránsito
              </Button>
            )}

            <Divider my="xs" />
            <Button color="orange" fullWidth onClick={() => setDetailModalOpen(false)}>Cerrar</Button>
          </Stack>
        </Modal>
      )}

      <Modal opened={!!cancelTarget} onClose={() => setCancelTarget(null)} title="Cancelar embarque (RB-503)" size="sm">
        <Stack gap="sm">
          <Text size="sm">Cancelar <b>{cancelTarget?.shipmentNumber}</b>. Esta acción requiere una razón y queda registrada permanentemente.</Text>
          <TextInput label="Razón de cancelación" value={cancelReason} onChange={(e) => setCancelReason(e.currentTarget.value)} required />
          <Group justify="flex-end">
            <Button variant="outline" onClick={() => setCancelTarget(null)}>Volver</Button>
            <Button color="red" onClick={handleCancelConfirm} disabled={!cancelReason.trim()}>Confirmar cancelación</Button>
          </Group>
        </Stack>
      </Modal>
    </Stack>
  );
}
