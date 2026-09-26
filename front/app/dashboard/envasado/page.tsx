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
  IconBottle,
  IconPlus,
  IconAlertTriangle,
  IconCheck,
  IconEye,
  IconBarcode,
  IconInfoCircle,
  IconQrcode,
} from "@tabler/icons-react";
import { INITIAL_BOTTLING_BATCHES, INITIAL_DISTILLATION_BATCHES, BottlingBatch } from "@/lib/mockData";

export default function BottlingPage() {
  const [batches, setBatches] = useState<BottlingBatch[]>(INITIAL_BOTTLING_BATCHES);
  const [modalOpen, setModalOpen] = useState(false);
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [qrModalOpen, setQrModalOpen] = useState(false);
  const [selectedBatch, setSelectedBatch] = useState<BottlingBatch | null>(null);

  // Form State
  const [parentDistillationCode, setParentDistillationCode] = useState("TRZ-2026-00202");
  const [brand, setBrand] = useState("José Cuervo Tradicional");
  const [category, setCategory] = useState<"Blanco" | "Reposado" | "Añejo" | "Extra Añejo">("Blanco");
  const [bottleCapacityMl, setBottleCapacityMl] = useState<number | "">(750);
  const [unitsBottled, setUnitsBottled] = useState<number | "">(4500);
  const [lossesRecorded, setLossesRecorded] = useState<number | "">(15);
  const [startFolio, setStartFolio] = useState("SAT-MARB-2026-880001");

  const [validationError, setValidationError] = useState<string | null>(null);

  const handleCreateBottlingBatch = (e: React.FormEvent) => {
    e.preventDefault();
    setValidationError(null);

    const units = Number(unitsBottled) || 0;
    const losses = Number(lossesRecorded) || 0;
    const capacity = Number(bottleCapacityMl) || 750;
    const totalVolumeL = (units * capacity) / 1000;

    const nextIdNumber = batches.length + 301;
    const startNum = 880001;
    const endNum = startNum + units + losses - 1;

    const newBatch: BottlingBatch = {
      id: `b-${batches.length + 1}`,
      traceabilityCode: `TRZ-2026-00${nextIdNumber}`,
      parentDistillationCode,
      bottlingDate: "2026-09-26",
      brand,
      category,
      bottleCapacityMl: capacity,
      totalVolumeL,
      productionLotCode: `LOT-JCT-2026-${batches.length + 10}B`,
      unitsBottled: units,
      lossesRecorded: losses,
      satMarbeteStartFolio: startFolio,
      satMarbeteEndFolio: `SAT-MARB-2026-${endNum}`,
      marbetesAssigned: units + losses,
      marbeteStatus: "USED",
      labelRequirementsComplete: true,
      status: "COMPLETED",
    };

    setBatches([newBatch, ...batches]);
    setModalOpen(false);
  };

  return (
    <Stack gap="lg">
      {/* Page Title & Action */}
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Etapa 3: Envasado y Marbetes Fiscales SAT
          </Title>
          <Text size="sm" c="dimmed">
            Control de embotellado, folios de marbetes SAT, generación de QR único por botella y reconciliación de mermas (FR-20 a FR-25)
          </Text>
        </div>
        <Button
          leftSection={<IconPlus size={18} />}
          color="cyan"
          radius="md"
          onClick={() => setModalOpen(true)}
        >
          Registrar Nuevo Envasado
        </Button>
      </Group>

      {/* Summary Cards */}
      <Grid>
        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #2daeac" }}>
            <Text size="xs" c="dimmed" fw={700}>BOTELLAS PRODUCIDAS</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.reduce((acc, b) => acc + b.unitsBottled, 0).toLocaleString()} Unidades
            </Text>
            <Text size="xs" c="teal" mt="4px">
              Con código QR / Barcode asignado (FR-23)
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #209b99" }}>
            <Text size="xs" c="dimmed" fw={700}>MARBETES SAT ASIGNADOS</Text>
            <Text size="xl" fw={700} mt="4px">
              {batches.reduce((acc, b) => acc + b.marbetesAssigned, 0).toLocaleString()} Folios
            </Text>
            <Text size="xs" c="teal" mt="4px">
              Estado: USED / Conciliado (RB-302)
            </Text>
          </Paper>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 4 }}>
          <Paper p="md" radius="md" withBorder style={{ borderLeft: "4px solid #ff8f00" }}>
            <Text size="xs" c="dimmed" fw={700}>RECONCILIACIÓN DE MERMAS</Text>
            <Text size="xl" fw={700} mt="4px">0.33% Merma Promedio</Text>
            <Text size="xs" c="dimmed" mt="4px">
              Tolerancia permitida: ≤ 2.0% (RB-306)
            </Text>
          </Paper>
        </Grid.Col>
      </Grid>

      {/* Table */}
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
              <Table.Th>Rango Marbetes SAT</Table.Th>
              <Table.Th>Etiquetado</Table.Th>
              <Table.Th>Acciones</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {batches.map((b) => (
              <Table.Tr key={b.id}>
                <Table.Td>
                  <Text fw={700} size="sm" c="cyan.9">
                    {b.traceabilityCode}
                  </Text>
                  <Text size="11px" c="dimmed">{b.productionLotCode}</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color="orange" variant="light" size="xs">
                    {b.parentDistillationCode}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700}>{b.brand}</Text>
                  <Badge color="amber" variant="dot" size="xs">{b.category}</Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs">{b.bottleCapacityMl} ml ({b.totalVolumeL.toLocaleString()} L)</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700}>{b.unitsBottled.toLocaleString()} botellas</Text>
                  <Text size="11px" c="dimmed">Merma: {b.lossesRecorded} unidades</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={600}>{b.satMarbeteStartFolio}</Text>
                  <Text size="11px" c="dimmed">al {b.satMarbeteEndFolio}</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color={b.labelRequirementsComplete ? "green" : "red"} size="xs">
                    {b.labelRequirementsComplete ? "✓ Completo" : "Incompleto"}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    <Tooltip label="Generar / Ver Código QR Unitario">
                      <ActionIcon
                        variant="light"
                        color="cyan"
                        onClick={() => {
                          setSelectedBatch(b);
                          setQrModalOpen(true);
                        }}
                      >
                        <IconQrcode size={16} />
                      </ActionIcon>
                    </Tooltip>
                    <Tooltip label="Ver detalle del lote">
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
                  </Group>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Card>

      {/* Create Modal */}
      <Modal
        opened={modalOpen}
        onClose={() => setModalOpen(false)}
        title={
          <Group gap="xs">
            <IconBottle color="#2daeac" size={20} />
            <Text fw={700}>Registrar Nuevo Envasado (FR-20 / FR-21)</Text>
          </Group>
        }
        size="lg"
        radius="md"
      >
        <form onSubmit={handleCreateBottlingBatch}>
          <Stack gap="sm">
            <Alert color="cyan" title="Requisitos de Marbete SAT (RB-302)" icon={<IconInfoCircle size={18} />}>
              1 Marbete SAT asignado por cada botella producida + merma. Los marbetes quedan vinculados de forma unívoca a la botella.
            </Alert>

            <Select
              label="Lote de Destilación Origen (Ready for Bottling)"
              data={INITIAL_DISTILLATION_BATCHES.filter((d) => d.status === "COMPLETED" || d.status === "MATURING").map((d) => ({
                value: d.traceabilityCode,
                label: `${d.traceabilityCode} - Hearts ${d.heartsVolumeL.toLocaleString()}L (${d.abvPercentage}% ABV)`,
              }))}
              value={parentDistillationCode}
              onChange={(val) => setParentDistillationCode(val || "")}
              required
            />

            <Grid>
              <Grid.Col span={6}>
                <Select
                  label="Marca Comercial"
                  data={["José Cuervo Tradicional", "Reserva de la Familia", "Cuervo Especial", "1800 Cristalino"]}
                  value={brand}
                  onChange={(val) => setBrand(val || "")}
                  required
                />
              </Grid.Col>
              <Grid.Col span={6}>
                <Select
                  label="Categoría Tequila"
                  data={["Blanco", "Reposado", "Añejo", "Extra Añejo"]}
                  value={category}
                  onChange={(val) => setCategory((val as any) || "Blanco")}
                  required
                />
              </Grid.Col>
            </Grid>

            <Grid>
              <Grid.Col span={4}>
                <NumberInput
                  label="Capacidad Botella (ml)"
                  value={bottleCapacityMl}
                  onChange={(val) => setBottleCapacityMl(val === "" ? "" : Number(val))}
                  required
                />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput
                  label="Unidades a Embotellar"
                  value={unitsBottled}
                  onChange={(val) => setUnitsBottled(val === "" ? "" : Number(val))}
                  min={1}
                  required
                />
              </Grid.Col>
              <Grid.Col span={4}>
                <NumberInput
                  label="Mermas Estimadas"
                  value={lossesRecorded}
                  onChange={(val) => setLossesRecorded(val === "" ? "" : Number(val))}
                  min={0}
                />
              </Grid.Col>
            </Grid>

            <TextInput
              label="Folio Inicial Marbetes SAT"
              value={startFolio}
              onChange={(e) => setStartFolio(e.currentTarget.value)}
              required
            />

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button color="cyan" type="submit">
                Completar Envasado y Generar QR
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      {/* QR Simulation Modal */}
      {selectedBatch && (
        <Modal
          opened={qrModalOpen}
          onClose={() => setQrModalOpen(false)}
          title={`Código QR de Unidad Embotellada (FR-23)`}
          size="sm"
        >
          <Stack align="center" gap="sm">
            <Card withBorder padding="md" radius="md" style={{ textAlign: "center", backgroundColor: "#fff" }}>
              <IconQrcode size={120} color="#1a252c" />
              <Text size="xs" fw={700} mt="xs">BOT-QR-{selectedBatch.traceabilityCode}-0001</Text>
              <Badge color="green" size="xs" mt="4px">AVAILABLE</Badge>
            </Card>

            <Stack gap="2px" style={{ width: "100%" }}>
              <Text size="xs"><b>Lote:</b> {selectedBatch.traceabilityCode}</Text>
              <Text size="xs"><b>Marca:</b> {selectedBatch.brand} ({selectedBatch.category})</Text>
              <Text size="xs"><b>Marbete SAT:</b> {selectedBatch.satMarbeteStartFolio}</Text>
              <Text size="xs"><b>Lote de Producción:</b> {selectedBatch.productionLotCode}</Text>
            </Stack>

            <Button fullWidth color="cyan" onClick={() => setQrModalOpen(false)}>
              Cerrar Vista QR
            </Button>
          </Stack>
        </Modal>
      )}

      {/* Batch Detail Modal */}
      {selectedBatch && (
        <Modal
          opened={detailModalOpen}
          onClose={() => setDetailModalOpen(false)}
          title={`Detalle de Envasado: ${selectedBatch.traceabilityCode}`}
          size="md"
        >
          <Stack gap="xs">
            <Text size="xs"><b>Lote Destilación Origen:</b> {selectedBatch.parentDistillationCode}</Text>
            <Text size="xs"><b>Marca / Categoría:</b> {selectedBatch.brand} ({selectedBatch.category})</Text>
            <Text size="xs"><b>Volumen Total Envasado:</b> {selectedBatch.totalVolumeL.toLocaleString()} Litros</Text>
            <Text size="xs"><b>Unidades Embotelladas:</b> {selectedBatch.unitsBottled.toLocaleString()} botellas</Text>
            <Text size="xs"><b>Mermas Registradas:</b> {selectedBatch.lossesRecorded} unidades</Text>
            <Text size="xs"><b>Marbetes SAT Asignados:</b> {selectedBatch.marbetesAssigned} ({selectedBatch.satMarbeteStartFolio} al {selectedBatch.satMarbeteEndFolio})</Text>
            <Text size="xs"><b>Requisitos de Etiqueta:</b> {selectedBatch.labelRequirementsComplete ? "✓ Cumplidos en su totalidad" : "Pendiente"}</Text>
            <Button mt="md" color="cyan" fullWidth onClick={() => setDetailModalOpen(false)}>
              Cerrar Detalle
            </Button>
          </Stack>
        </Modal>
      )}
    </Stack>
  );
}
