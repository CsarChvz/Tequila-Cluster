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
  Textarea,
  Select,
  Stack,
  Alert,
  Paper,
  Grid,
  Tabs,
  ActionIcon,
  Tooltip,
} from "@mantine/core";
import {
  IconShieldCheck,
  IconPlus,
  IconAlertTriangle,
  IconCheck,
  IconEye,
  IconRefresh,
  IconBuildingWarehouse,
} from "@tabler/icons-react";
import { INITIAL_ALERTS, NonConformity } from "@/lib/mockData";

export default function QualityPage() {
  const [nonConformities, setNonConformities] = useState<NonConformity[]>([
    {
      id: "nc-1",
      code: "NC-2026-001",
      batchCode: "TRZ-2026-00301",
      title: "Desviación en tono de etiqueta frontal",
      description: "Variación estética en la impresión del sello dorado de Reserva en 50 botellas.",
      severity: "LOW",
      status: "OPEN",
      createdAt: "2026-09-25 11:30",
      reportedBy: "control.calidad",
    },
    {
      id: "nc-2",
      code: "NC-2026-002",
      batchCode: "TRZ-2026-00203",
      title: "Graduación ABV fuera de rango normativo (62.5%)",
      description: "El lote de destilación superó el 60% máximo legal CRT. Bloqueado en laboratorio.",
      severity: "CRITICAL",
      status: "INVESTIGATING",
      createdAt: "2026-09-24 16:20",
      reportedBy: "destilacion.operador",
    },
  ]);

  const [recalls, setRecalls] = useState([
    {
      id: "rec-1",
      code: "REC-2026-001",
      batchCode: "TRZ-2026-00301",
      type: "PARTIAL",
      reason: "Retiro preventivo de 50 botellas con marbete SAT dañado durante empaque",
      affectedUnitsCount: 50,
      status: "IN_PROGRESS",
      createdAt: "2026-09-25 14:00",
    },
  ]);

  const [modalOpen, setModalOpen] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [batchCode, setBatchCode] = useState("TRZ-2026-00301");
  const [severity, setSeverity] = useState<"LOW" | "MEDIUM" | "HIGH" | "CRITICAL">("MEDIUM");

  const handleCreateNC = (e: React.FormEvent) => {
    e.preventDefault();
    const newNc: NonConformity = {
      id: `nc-${nonConformities.length + 1}`,
      code: `NC-2026-00${nonConformities.length + 1}`,
      batchCode,
      title,
      description,
      severity,
      status: "OPEN",
      createdAt: "2026-09-26 12:00",
      reportedBy: "auditor.calidad",
    };
    setNonConformities([newNc, ...nonConformities]);
    setModalOpen(false);
  };

  return (
    <Stack gap="lg">
      {/* Page Title */}
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Módulo de Calidad, No Conformidades y Recalls
          </Title>
          <Text size="sm" c="dimmed">
            Registro de incidencias de calidad, investigación de desviaciones y retenes/retire de producto (FR-33 / FR-34 / RB-407)
          </Text>
        </div>
        <Button
          leftSection={<IconPlus size={18} />}
          color="red"
          radius="md"
          onClick={() => setModalOpen(true)}
        >
          Reportar No Conformidad
        </Button>
      </Group>

      {/* Tabs */}
      <Tabs defaultValue="nc">
        <Tabs.List>
          <Tabs.Tab value="nc" leftSection={<IconAlertTriangle size={16} />}>
            No Conformidades ({nonConformities.length})
          </Tabs.Tab>
          <Tabs.Tab value="recalls" leftSection={<IconAlertTriangle size={16} />}>
            Recalls y Retiros de Producto ({recalls.length})
          </Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="nc" pt="md">
          <Card withBorder radius="md" p="md" shadow="xs">
            <Table highlightOnHover striped verticalSpacing="sm">
              <Table.Thead bg="gray.1">
                <Table.Tr>
                  <Table.Th>Folio Incidencia</Table.Th>
                  <Table.Th>Lote Afectado</Table.Th>
                  <Table.Th>Título y Descripción</Table.Th>
                  <Table.Th>Severidad</Table.Th>
                  <Table.Th>Reportado Por</Table.Th>
                  <Table.Th>Estado</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {nonConformities.map((nc) => (
                  <Table.Tr key={nc.id}>
                    <Table.Td>
                      <Text fw={700} size="sm" c="red.8">{nc.code}</Text>
                      <Text size="11px" c="dimmed">{nc.createdAt}</Text>
                    </Table.Td>
                    <Table.Td>
                      <Badge color="teal" variant="light" size="xs">{nc.batchCode}</Badge>
                    </Table.Td>
                    <Table.Td>
                      <Text size="xs" fw={700}>{nc.title}</Text>
                      <Text size="xs" c="dimmed">{nc.description}</Text>
                    </Table.Td>
                    <Table.Td>
                      <Badge
                        color={
                          nc.severity === "CRITICAL"
                            ? "red"
                            : nc.severity === "HIGH"
                            ? "orange"
                            : nc.severity === "MEDIUM"
                            ? "yellow"
                            : "gray"
                        }
                        size="sm"
                      >
                        {nc.severity}
                      </Badge>
                    </Table.Td>
                    <Table.Td>
                      <Text size="xs">{nc.reportedBy}</Text>
                    </Table.Td>
                    <Table.Td>
                      <Badge color={nc.status === "OPEN" ? "red" : nc.status === "INVESTIGATING" ? "orange" : "green"} size="sm">
                        {nc.status}
                      </Badge>
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          </Card>
        </Tabs.Panel>

        <Tabs.Panel value="recalls" pt="md">
          <Card withBorder radius="md" p="md" shadow="xs">
            <Table highlightOnHover striped verticalSpacing="sm">
              <Table.Thead bg="gray.1">
                <Table.Tr>
                  <Table.Th>Folio Recall</Table.Th>
                  <Table.Th>Lote Origen</Table.Th>
                  <Table.Th>Tipo de Retiro</Table.Th>
                  <Table.Th>Causa / Razón</Table.Th>
                  <Table.Th>Unidades Afectadas</Table.Th>
                  <Table.Th>Estado</Table.Th>
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {recalls.map((rec) => (
                  <Table.Tr key={rec.id}>
                    <Table.Td>
                      <Text fw={700} size="sm" c="red.9">{rec.code}</Text>
                      <Text size="11px" c="dimmed">{rec.createdAt}</Text>
                    </Table.Td>
                    <Table.Td>
                      <Badge color="cyan" variant="light" size="xs">{rec.batchCode}</Badge>
                    </Table.Td>
                    <Table.Td>
                      <Badge color="orange" size="xs">{rec.type}</Badge>
                    </Table.Td>
                    <Table.Td>
                      <Text size="xs">{rec.reason}</Text>
                    </Table.Td>
                    <Table.Td>
                      <Text size="xs" fw={700}>{rec.affectedUnitsCount} botellas → RECALLED</Text>
                    </Table.Td>
                    <Table.Td>
                      <Badge color="orange" size="sm">{rec.status}</Badge>
                    </Table.Td>
                  </Table.Tr>
                ))}
              </Table.Tbody>
            </Table>
          </Card>
        </Tabs.Panel>
      </Tabs>

      {/* Modal to Create NC */}
      <Modal
        opened={modalOpen}
        onClose={() => setModalOpen(false)}
        title={
          <Group gap="xs">
            <IconAlertTriangle color="#d9480f" size={20} />
            <Text fw={700}>Reportar No Conformidad de Calidad (FR-33)</Text>
          </Group>
        }
        size="md"
        radius="md"
      >
        <form onSubmit={handleCreateNC}>
          <Stack gap="sm">
            <Select
              label="Lote Involucrado"
              data={["TRZ-2026-00301", "TRZ-2026-00203", "TRZ-2026-00201", "TRZ-2026-00101"]}
              value={batchCode}
              onChange={(val) => setBatchCode(val || "")}
              required
            />

            <TextInput
              label="Título de la No Conformidad"
              placeholder="Ej. Fuga en sello de botella"
              value={title}
              onChange={(e) => setTitle(e.currentTarget.value)}
              required
            />

            <Select
              label="Severidad de la Incidencia"
              data={["LOW", "MEDIUM", "HIGH", "CRITICAL"]}
              value={severity}
              onChange={(val) => setSeverity((val as any) || "MEDIUM")}
              required
            />

            <Textarea
              label="Descripción Detallada y Causa Raíz"
              placeholder="Explicación técnica de la desviación encontrada..."
              value={description}
              onChange={(e) => setDescription(e.currentTarget.value)}
              rows={4}
              required
            />

            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => setModalOpen(false)}>
                Cancelar
              </Button>
              <Button color="red" type="submit">
                Guardar No Conformidad
              </Button>
            </Group>
          </Stack>
        </form>
      </Modal>
    </Stack>
  );
}
