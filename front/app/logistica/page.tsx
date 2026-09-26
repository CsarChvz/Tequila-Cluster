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
  IconTruckDelivery,
  IconPlus,
  IconAlertTriangle,
  IconCheck,
  IconEye,
  IconFileCheck,
  IconInfoCircle,
} from "@tabler/icons-react";
import { INITIAL_SHIPMENTS, INITIAL_BOTTLING_BATCHES, Shipment } from "@/lib/mockData";

export default function LogisticsPage() {
  const [shipments, setShipments] = useState<Shipment[]>(INITIAL_SHIPMENTS);
  const [modalOpen, setModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedShipment, setSelectedShipment] = useState<Shipment | null>(null);

  // Form state
  const [carrier, setCarrier] = useState("Transportes Tequileros del Occidente S.A.");
  const [driverName, setDriverName] = useState("Carlos Mendoza");
  const [licensePlate, setLicensePlate] = useState("JV-884-91");
  const [destination, setDestination] = useState("CEDIS Cuervo Guadalajara - Bodega Central");
  const [unitsToShip, setUnitsToShip] = useState<number | "">(2400);
  const [selectedBottlingCode, setSelectedBottlingCode] = useState("TRZ-2026-00301");

  const [validationError, setValidationError] = useState<string | null>(null);

  const handleCreateShipment = (e: React.FormEvent) => {
    e.preventDefault();
    setValidationError(null);

    const units = Number(unitsToShip) || 0;

    // FR-27 / RB-401: Validate units <= available units
    if (units > 4500) {
      setValidationError(
        `Error RB-401: Las unidades a embarcar (${units}) superan las disponibles en el lote de envasado (4,500).`
      );
      return;
    }

    const nextIdNumber = shipments.length + 501;
    const newShipment: Shipment = {
      id: `s-${shipments.length + 1}`,
      shipmentNumber: `EMB-2026-00${nextIdNumber}`,
      carrier,
      driverName,
      licensePlate,
      destination,
      departureDate: "2026-09-26 10:00",
      estimatedArrivalDate: "2026-09-26 16:00",
      bottlingBatchCodes: [{ code: selectedBottlingCode, units }],
      status: "IN_TRANSIT",
      documentsComplete: true,
      documentsList: [
        { name: "Carta Porte Digital SAT (CFDI)", required: true, uploaded: true, valid: true },
        { name: "Manifiesto de Carga de Alcohol", required: true, uploaded: true, valid: true },
        { name: "Póliza de Seguro de Transporte", required: true, uploaded: true, valid: true },
        { name: "Certificado Fitosanitario CRT", required: true, uploaded: true, valid: true },
      ],
    };

    setShipments([newShipment, ...shipments]);
    setModalOpen(false);
  };

  const handleConfirmDelivery = (shipmentId: string) => {
    setShipments((prev) =>
      prev.map((s) => (s.id === shipmentId ? { ...s, status: "DELIVERED" } : s))
    );
  };

  return (
    <Stack gap="lg">
      {/* Header & Actions */}
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Etapa 4: Logística y Embarques de Tequila
          </Title>
          <Text size="sm" c="dimmed">
            Despacho de embarques, validación de Carta Porte SAT, documentos de transporte y confirmación de entrega (FR-26 a FR-32)
          </Text>
        </div>
        <Button
          leftSection={<IconPlus size={18} />}
          color="orange"
          radius="md"
          onClick={() => setModalOpen(true)}
        >
          Crear Nuevo Embarque
        </Button>
      </Group>

      {/* Summary Cards */}
      <Grid>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ff6f00" }}>
            <Text size="xs" c="dimmed" fw={700}>EMBARQUES EN TRÁNSITO</Text>
            <Text size="xl" fw={700} mt="4px">
              {shipments.filter((s) => s.status === "IN_TRANSIT").length} Activos
            </Text>
            <Text size="xs" c="orange" mt="4px">
              Carta Porte CFDI y CRT Aprobados (FR-29)
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #209b99" }}>
            <Text size="xs" c="dimmed" fw={700}>ENTREGAS CONFIRMADAS</Text>
            <Text size="xl" fw={700} mt="4px">
              {shipments.filter((s) => s.status === "DELIVERED").length} Entregados
            </Text>
            <Text size="xs" c="teal" mt="4px">
              Actualización de inventarios a DELIVERED
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ffb300" }}>
            <Text size="xs" c="dimmed" fw={700}>CUMPLIMIENTO DE DOCUMENTOS</Text>
            <Text size="xl" fw={700} mt="4px">100% Válidos</Text>
            <Text size="xs" c="dimmed" mt="4px">
              Bloqueo automático si falta documento (RB-402)
            </Text>
          </Paper>
        </Grid.Col>
      </Grid>

      {/* Shipments Table */}
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
              <Table.Th>Transportista y Conductor</Table.Th>
              <Table.Th>Placas / Unidad</Table.Th>
              <Table.Th>Destino</Table.Th>
              <Table.Th>Documentos SAT</Table.Th>
              <Table.Th>Estado</Table.Th>
              <Table.Th>Acciones</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {shipments.map((s) => (
              <Table.Tr key={s.id}>
                <Table.Td>
                  <Text fw={700} size="sm" c="orange.9">
                    {s.shipmentNumber}
                  </Text>
                  <Text size="11px" c="dimmed">Salida: {s.departureDate}</Text>
                </Table.Td>
                <Table.Td>
                  {s.bottlingBatchCodes.map((b) => (
                    <Badge key={b.code} color="cyan" variant="light" size="xs">
                      {b.code} ({b.units.toLocaleString()} botellas)
                    </Badge>
                  ))}
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700}>{s.carrier}</Text>
                  <Text size="xs" c="dimmed">Chofer: {s.driverName}</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color="gray" size="xs">{s.licensePlate}</Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs">{s.destination}</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color={s.documentsComplete ? "green" : "red"} size="xs">
                    {s.documentsComplete ? "✓ Carta Porte & CRT" : "Pendiente"}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Badge
                    color={s.status === "DELIVERED" ? "green" : s.status === "IN_TRANSIT" ? "orange" : "blue"}
                    size="sm"
                  >
                    {s.status}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    {s.status === "IN_TRANSIT" && (
                      <Button
                        size="xs"
                        color="teal"
                        variant="light"
                        onClick={() => handleConfirmDelivery(s.id)}
                      >
                        Confirmar Entrega
                      </Button>
                    )}
                    <Tooltip label="Ver documentos de transporte">
                      <ActionIcon
                        variant="light"
                        color="orange"
                        onClick={() => {
                          setSelectedShipment(s);
                          setDetailModalOpen(true);
                        }}
                      >
                        <IconEye size={16} />
                      </ActionIcon>
                    </Tooltip>
                  </Group>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Card>

      {/* Modal to Create Shipment */}
      <Modal
        opened={modalOpen}
        onClose={() => setModalOpen(false)}
        title={
          <Group gap="xs">
            <IconTruckDelivery color="#ff6f00" size={20} />
            <Text fw={700}>Crear Nuevo Embarque (FR-26 / FR-28)</Text>
          </Group>
        }
        size="lg"
        radius="md"
      >
        <form onSubmit={handleCreateShipment}>
          <Stack gap="sm">
            <Alert color="orange" title="Regla de Salida RB-402" icon={<IconInfoCircle size={18} />}>
              El embarque no podrá salir del estado <b>PLANNED</b> a <b>IN_TRANSIT</b> sin tener cargados y validados todos los documentos normativos (Carta Porte SAT y Manifiesto CRT).
            </Alert>

            {validationError && (
              <Alert color="red" title="Error de Capacidad RB-401" icon={<IconAlertTriangle size={18} />}>
                {validationError}
              </Alert>
            )}

            <Select
              label="Lote de Envasado Apto (COMPLETED)"
              data={INITIAL_BOTTLING_BATCHES.map((b) => ({
                value: b.traceabilityCode,
                label: `${b.traceabilityCode} - ${b.brand} (${b.unitsBottled.toLocaleString()} botellas disponibles)`,
              }))}
              value={selectedBottlingCode}
              onChange={(val) => setSelectedBottlingCode(val || "")}
              required
            />

            <NumberInput
              label="Unidades a Asignar a este Embarque"
              value={unitsToShip}
              onChange={(val) => setUnitsToShip(val === "" ? "" : Number(val))}
              min={1}
              required
            />

            <TextInput
              label="Empresa Transportista Autorizada"
              value={carrier}
              onChange={(e) => setCarrier(e.currentTarget.value)}
              required
            />

            <Grid>
              <Grid.Col span={6}>
                <TextInput
                  label="Nombre del Conductor"
                  value={driverName}
                  onChange={(e) => setDriverName(e.currentTarget.value)}
                  required
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <TextInput
                  label="Placas del Vehículo / Remolque"
                  value={licensePlate}
                  onChange={(e) => setLicensePlate(e.currentTarget.value)}
                  required
                />
              </Grid.Col>
            </Grid>

            <TextInput
              label="Destino Final / CEDIS"
              value={destination}
              onChange={(e) => setDestination(e.currentTarget.value)}
              required
            />

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button color="orange" type="submit">
                Crear Embarque y Generar Documentos
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      {/* Shipment Detail Modal */}
      {selectedShipment && (
        <Modal
          opened={detailModalOpen}
          onClose={() => setDetailModalOpen(false)}
          title={`Documentos del Embarque: ${selectedShipment.shipmentNumber}`}
          size="md"
        >
          <Stack gap="sm">
            <Text size="xs"><b>Transportista:</b> {selectedShipment.carrier}</Text>
            <Text size="xs"><b>Chofer / Placas:</b> {selectedShipment.driverName} ({selectedShipment.licensePlate})</Text>
            <Text size="xs"><b>Destino:</b> {selectedShipment.destination}</Text>

            <Text size="xs" fw={700} mt="xs">Documentación de Transporte Requerida (RB-402):</Text>
            {selectedShipment.documentsList.map((doc, idx) => (
              <Paper key={idx} p="xs" radius="sm" withBorder bg="gray.0">
                <Group justify="space-between">
                  <Text size="xs">{doc.name}</Text>
                  <Badge color={doc.valid ? "green" : "red"} size="xs">
                    {doc.valid ? "✓ VÁLIDO" : "PENDIENTE"}
                  </Badge>
                </Group>
              </Paper>
            ))}

            <Button mt="md" color="orange" fullWidth onClick={() => setDetailModalOpen(false)}>
              Cerrar Vista de Documentos
            </Button>
          </Stack>
        </Modal>
      )}
    </Stack>
  );
}
