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
  ActionIcon,
  Tooltip,
} from "@mantine/core";
import {
  IconPlant2,
  IconPlus,
  IconCheck,
  IconAlertCircle,
  IconTractor,
  IconFileText,
  IconEye,
  IconInfoCircle,
} from "@tabler/icons-react";
import { INITIAL_HARVEST_BATCHES, HarvestBatch } from "@/lib/mockData";

export default function JimaPage() {
  const [batches, setBatches] = useState<HarvestBatch[]>(INITIAL_HARVEST_BATCHES);
  const [modalOpen, setModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState<HarvestBatch | null>(null);

  // Form State
  const [field, setField] = useState("Rancho Tequileño Sector Norte");
  const [supplier, setSupplier] = useState("Agaves del Valle de Amatitán S.A.");
  const [harvestDate, setHarvestDate] = useState("2026-09-26");
  const [totalWeightKg, setTotalWeightKg] = useState<number | "">(25000);
  const [pinasCount, setPinasCount] = useState<number | "">(625);
  const [doArea, setDoArea] = useState("DO-JALISCO-AMATITAN-01");
  const [supplierError, setSupplierError] = useState<string | null>(null);
  const [weightWarning, setWeightWarning] = useState<string | null>(null);

  const estimatedYieldL = typeof totalWeightKg === "number" ? Math.round(totalWeightKg * 0.12) : 0;

  const handleCreateBatch = (e: React.FormEvent) => {
    e.preventDefault();
    setSupplierError(null);

    // FR-08: Validation check for supplier
    if (supplier.includes("Inactivo")) {
      setSupplierError("El proveedor seleccionado está INACTIVO. Operación bloqueada (RB-102).");
      return;
    }

    // FR-11: Warning if weight exceeds plant capacity (e.g. 50,000 kg)
    if (typeof totalWeightKg === "number" && totalWeightKg > 50000) {
      setWeightWarning("Atención: El peso excede la capacidad nominal diaria de recepción (50,000 kg).");
    }

    const nextIdNumber = batches.length + 101;
    const newBatch: HarvestBatch = {
      id: `h-${batches.length + 1}`,
      traceabilityCode: `TRZ-2026-00${nextIdNumber}`,
      field,
      supplier,
      harvestDate,
      totalWeightKg: Number(totalWeightKg),
      pinasCount: Number(pinasCount),
      estimatedYieldL,
      transportPermitCode: `TP-SAT-2026-09${batches.length + 20}`,
      transportPermitStatus: "GENERATED",
      status: "COMPLETED",
      authorizedArea: doArea,
      areaValidUntil: "2028-12-31",
    };

    setBatches([newBatch, ...batches]);
    setModalOpen(false);
  };

  return (
    <Stack gap="lg">
      {/* Page Title & Actions */}
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Etapa 1: Jima y Cosecha de Agave
          </Title>
          <Text size="sm" c="dimmed">
            Registro de lotes de agave, validación de Denominación de Origen y generación de permisos de transporte (FR-05 a FR-11)
          </Text>
        </div>
        <Button
          leftSection={<IconPlus size={18} />}
          color="teal"
          radius="md"
          onClick={() => setModalOpen(true)}
        >
          Registrar Nueva Jima
        </Button>
      </Group>

      {/* Summary Cards */}
      <Grid>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #209b99" }}>
            <Text size="xs" c="dimmed" fw={700}>TOTAL REGISTRADO EN JIMA</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.reduce((acc, b) => acc + b.totalWeightKg, 0).toLocaleString()} kg
            </Text>
            <Text size="xs" c="teal" mt="4px">
              Rendimiento Estimado Total: {batches.reduce((acc, b) => acc + b.estimatedYieldL, 0).toLocaleString()} L
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ff8f00" }}>
            <Text size="xs" c="dimmed" fw={700}>PERMISOS DE TRANSPORTE SAT</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.length} Generados
            </Text>
            <Text size="xs" c="dimmed" mt="4px">
              Formato automático TP-SAT-YYYY-XXXX (FR-10)
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #4bcbc9" }}>
            <Text size="xs" c="dimmed" fw={700}>ZONAS DE ORIGEN DO VIGENTES</Text>
            <Text size="xl" fw={700} mt="4px">100% Verificadas</Text>
            <Text size="xs" c="teal" mt="4px">
              Validación contra catálogo de áreas (RB-101)
            </Text>
          </Paper>
        </Grid.Col>
      </Grid>

      {/* Batches Table */}
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
              <Table.Th>Área DO</Table.Th>
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
              <Table.Tr key={b.id}>
                <Table.Td>
                  <Text fw={700} size="sm" c="teal.8">
                    {b.traceabilityCode}
                  </Text>
                  <Text size="11px" c="dimmed">{b.harvestDate}</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={600}>{b.field}</Text>
                  <Text size="xs" c="dimmed">{b.supplier}</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color="gray" variant="light" size="xs">
                    {b.authorizedArea}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700}>{b.totalWeightKg.toLocaleString()} kg</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs">{b.pinasCount} piñas</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700} c="teal">{b.estimatedYieldL.toLocaleString()} L</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color={b.transportPermitStatus === "COMPLETED" ? "green" : "orange"} size="xs">
                    {b.transportPermitCode} ({b.transportPermitStatus})
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Badge color={b.status === "COMPLETED" ? "green" : "blue"} size="sm">
                    {b.status}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Tooltip label="Ver detalle de trazabilidad">
                    <ActionIcon
                      variant="light"
                      color="teal"
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

      {/* Modal to Create Harvest Batch */}
      <Modal
        opened={modalOpen}
        onClose={() => setModalOpen(false)}
        title={
          <Group gap="xs">
            <IconPlant2 color="#209b99" size={20} />
            <Text fw={700}>Registrar Nuevo Lote de Jima (FR-05 / FR-06)</Text>
          </Group>
        }
        size="lg"
        radius="md"
      >
        <form onSubmit={handleCreateBatch}>
          <Stack gap="sm">
            <Alert color="blue" title="Generación Automática de Trazabilidad (FR-05)" icon={<IconInfoCircle size={18} />}>
              Al guardar, se asignará automáticamente el código <b>TRZ-2026-00104</b> y se expedirá el permiso de transporte SAT en estado <b>GENERATED</b>.
            </Alert>

            {supplierError && (
              <Alert color="red" title="Error de Regla de Negocio RB-102" icon={<IconAlertCircle size={18} />}>
                {supplierError}
              </Alert>
            )}

            <Select
              label="Predio de Cosecha (Obligatorio)"
              data={[
                "Rancho Tequileño Sector Norte",
                "Predio El Volcán Parcela 4",
                "Agaves Cuervo - Tablón 12",
                "Finca Santa María Amatitán",
              ]}
              value={field}
              onChange={(val) => setField(val || "")}
              required
            />

            <Select
              label="Proveedor de Agave (RB-102 - Debe estar Activo)"
              data={[
                "Agaves del Valle de Amatitán S.A.",
                "Cooperativa Agavera Los Altos",
                "Agrícola José Cuervo Directo",
                "Proveedor Test Inactivo (Simula error RB-102)",
              ]}
              value={supplier}
              onChange={(val) => setSupplier(val || "")}
              required
            />

            <Select
              label="Área Autorizada de Denominación de Origen (RB-101)"
              data={[
                "DO-JALISCO-AMATITAN-01 (Vigente)",
                "DO-JALISCO-ARANDAS-04 (Vigente)",
                "DO-JALISCO-TEQUILA-02 (Vigente)",
              ]}
              value={doArea}
              onChange={(val) => setDoArea(val || "")}
              required
            />

            <Grid>
              <Grid.Col span={6}>
                <NumberInput
                  label="Peso Total de Agave (kg)"
                  value={totalWeightKg}
                  onChange={(val) => setTotalWeightKg(val === "" ? "" : Number(val))}
                  min={100}
                  step={500}
                  required
                />
              </Grid.Col>

              <Grid.Col span={6}>
                <NumberInput
                  label="Número de Piñas"
                  value={pinasCount}
                  onChange={(val) => setPinasCount(val === "" ? "" : Number(val))}
                  min={1}
                  required
                />
              </Grid.Col>
            </Grid>

            <Card withBorder bg="teal.0" padding="xs" radius="md">
              <Group justify="space-between">
                <Text size="xs" fw={700} c="teal.9">Rendimiento Teórico Estimado (FR-09):</Text>
                <Text size="sm" fw={800} c="teal.9">
                  {estimatedYieldL.toLocaleString()} Litros (Factor 0.12)
                </Text>
              </Group>
            </Card>

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button color="teal" type="submit">
                Registrar Jima y Generar TRZ
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      {/* Batch Detail Modal */}
      {selectedBatch && (
        <Modal
          opened={detailModalOpen}
          onClose={() => setDetailModalOpen(false)}
          title={`Detalle de Lote: ${selectedBatch.traceabilityCode}`}
          size="md"
        >
          <Stack gap="xs">
            <Text size="xs"><b>Predio:</b> {selectedBatch.field}</Text>
            <Text size="xs"><b>Proveedor:</b> {selectedBatch.supplier}</Text>
            <Text size="xs"><b>Fecha de Cosecha:</b> {selectedBatch.harvestDate}</Text>
            <Text size="xs"><b>Peso Agave:</b> {selectedBatch.totalWeightKg.toLocaleString()} kg</Text>
            <Text size="xs"><b>Número de Piñas:</b> {selectedBatch.pinasCount}</Text>
            <Text size="xs"><b>Rendimiento Estimado:</b> {selectedBatch.estimatedYieldL.toLocaleString()} L</Text>
            <Text size="xs"><b>Área Autorizada DO:</b> {selectedBatch.authorizedArea}</Text>
            <Text size="xs"><b>Permiso de Transporte SAT:</b> {selectedBatch.transportPermitCode} ({selectedBatch.transportPermitStatus})</Text>
            <Button mt="md" color="teal" fullWidth onClick={() => setDetailModalOpen(false)}>
              Cerrar Detalle
            </Button>
          </Stack>
        </Modal>
      )}
    </Stack>
  );
}
