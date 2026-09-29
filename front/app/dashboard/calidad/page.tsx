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
  Select,
  Stack,
  Alert,
  Tabs,
  Menu,
  ActionIcon,
  Tooltip,
  Loader,
  Center,
} from "@mantine/core";
import { IconAlertTriangle, IconPlus, IconChevronDown } from "@tabler/icons-react";
import {
  qualityApi,
  harvestApi,
  distillationApi,
  bottlingApi,
  NonConformityResponse,
  RecallResponse,
  NonConformityStatus,
  RecallStatus,
} from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { isAdministrator, isAuditor } from "@/lib/roles";

const NC_STATUS_FLOW: NonConformityStatus[] = ["OPEN", "INVESTIGATING", "RESOLVED", "CLOSED"];
const RECALL_STATUS_FLOW: RecallStatus[] = ["OPEN", "IN_PROGRESS", "COMPLETED"];

export default function QualityPage() {
  const { user } = useAuth();
  const canWrite = !isAuditor(user?.roles || []); // Auditor is view-only everywhere (RB-504)

  const [nonConformities, setNonConformities] = useState<NonConformityResponse[]>([]);
  const [recalls, setRecalls] = useState<RecallResponse[]>([]);
  const [batchOptions, setBatchOptions] = useState<Array<{ value: string; label: string }>>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [ncModalOpen, setNcModalOpen] = useState(false);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [ncBatchId, setNcBatchId] = useState<string | null>(null);
  const [severity, setSeverity] = useState<"LOW" | "MEDIUM" | "HIGH" | "CRITICAL">("MEDIUM");
  const [ncFormError, setNcFormError] = useState<string | null>(null);
  const [ncSubmitting, setNcSubmitting] = useState(false);

  const [recallModalOpen, setRecallModalOpen] = useState(false);
  const [recallBatchId, setRecallBatchId] = useState<string | null>(null);
  const [recallType, setRecallType] = useState<"PARTIAL" | "COMPLETE">("PARTIAL");
  const [recallReason, setRecallReason] = useState("");
  const [recallNcId, setRecallNcId] = useState<string | null>(null);
  const [recallFormError, setRecallFormError] = useState<string | null>(null);
  const [recallSubmitting, setRecallSubmitting] = useState(false);

  const loadAll = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      const [ncList, recallList, harvestList, distillationList, bottlingList] = await Promise.all([
        qualityApi.listNonConformities(),
        qualityApi.listRecalls(),
        harvestApi.list(),
        distillationApi.list(),
        bottlingApi.list(),
      ]);
      setNonConformities(ncList);
      setRecalls(recallList);
      setBatchOptions([
        ...harvestList.map((b) => ({ value: b.batchId, label: `${b.traceabilityCode} (Jima)` })),
        ...distillationList.map((b) => ({ value: b.batchId, label: `${b.traceabilityCode} (Destilación)` })),
        ...bottlingList.map((b) => ({ value: b.batchId, label: `${b.traceabilityCode} (Envasado)` })),
      ]);
    } catch (err: any) {
      setLoadError(err.message || "No se pudo conectar con el backend");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAll();
  }, [loadAll]);

  const handleCreateNC = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!ncBatchId || !title.trim() || !description.trim()) return;
    setNcFormError(null);
    setNcSubmitting(true);
    try {
      await qualityApi.createNonConformity({ batchId: ncBatchId, title, description, severity });
      setNcModalOpen(false);
      setTitle("");
      setDescription("");
      setNcBatchId(null);
      setSeverity("MEDIUM");
      await loadAll();
    } catch (err: any) {
      setNcFormError(err.message || "Error al registrar la no conformidad");
    } finally {
      setNcSubmitting(false);
    }
  };

  const handleCreateRecall = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!recallBatchId || !recallReason.trim()) return;
    setRecallFormError(null);
    setRecallSubmitting(true);
    try {
      await qualityApi.createRecall({
        sourceBatchId: recallBatchId,
        recallType,
        reason: recallReason,
        nonConformityId: recallNcId || undefined,
      });
      setRecallModalOpen(false);
      setRecallBatchId(null);
      setRecallReason("");
      setRecallNcId(null);
      await loadAll();
    } catch (err: any) {
      setRecallFormError(err.message || "Error al registrar el recall");
    } finally {
      setRecallSubmitting(false);
    }
  };

  const advanceNcStatus = async (nc: NonConformityResponse, next: NonConformityStatus) => {
    try {
      await qualityApi.updateNonConformityStatus(nc.id, next);
      await loadAll();
    } catch (err: any) {
      alert(err.message || "No se pudo actualizar el estado");
    }
  };

  const advanceRecallStatus = async (recall: RecallResponse, next: RecallStatus) => {
    try {
      await qualityApi.updateRecallStatus(recall.id, next);
      await loadAll();
    } catch (err: any) {
      alert(err.message || "No se pudo actualizar el estado");
    }
  };

  if (loading) {
    return (
      <Center style={{ minHeight: 300 }}>
        <Loader color="red" />
      </Center>
    );
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Módulo de Calidad, No Conformidades y Recalls
          </Title>
          <Text size="sm" c="dimmed">
            Registro de incidencias de calidad, investigación de desviaciones y retenes/retiro de producto (FR-33 / FR-34 / RB-407)
          </Text>
        </div>
        {canWrite && (
          <Group>
            <Button leftSection={<IconPlus size={18} />} color="red" radius="md" onClick={() => setNcModalOpen(true)}>
              Reportar No Conformidad
            </Button>
            <Button leftSection={<IconPlus size={18} />} color="orange" radius="md" variant="outline" onClick={() => setRecallModalOpen(true)}>
              Iniciar Recall
            </Button>
          </Group>
        )}
      </Group>

      {loadError && (
        <Alert color="red" title="Error de conexión con el backend" icon={<IconAlertTriangle size={18} />}>
          {loadError}
        </Alert>
      )}

      <Tabs defaultValue="nc">
        <Tabs.List>
          <Tabs.Tab value="nc" leftSection={<IconAlertTriangle size={16} />}>No Conformidades ({nonConformities.length})</Tabs.Tab>
          <Tabs.Tab value="recalls" leftSection={<IconAlertTriangle size={16} />}>Recalls y Retiros de Producto ({recalls.length})</Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="nc" pt="md">
          <Card withBorder radius="md" p="md" shadow="xs">
            <Table highlightOnHover striped verticalSpacing="sm">
              <Table.Thead bg="gray.1">
                <Table.Tr>
                  <Table.Th>Lote Afectado</Table.Th>
                  <Table.Th>Título y Descripción</Table.Th>
                  <Table.Th>Severidad</Table.Th>
                  <Table.Th>Reportado Por</Table.Th>
                  <Table.Th>Estado</Table.Th>
                  {canWrite && <Table.Th>Acción</Table.Th>}
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {nonConformities.map((nc) => {
                  const nextStatuses = NC_STATUS_FLOW.slice(NC_STATUS_FLOW.indexOf(nc.status) + 1);
                  return (
                    <Table.Tr key={nc.id}>
                      <Table.Td><Badge color="teal" variant="light" size="xs">{nc.batchTraceabilityCode}</Badge></Table.Td>
                      <Table.Td>
                        <Text size="xs" fw={700}>{nc.title}</Text>
                        <Text size="xs" c="dimmed">{nc.description}</Text>
                      </Table.Td>
                      <Table.Td>
                        <Badge color={nc.severity === "CRITICAL" ? "red" : nc.severity === "HIGH" ? "orange" : nc.severity === "MEDIUM" ? "yellow" : "gray"} size="sm">
                          {nc.severity}
                        </Badge>
                      </Table.Td>
                      <Table.Td><Text size="xs">{nc.reportedByUsername || "—"}</Text></Table.Td>
                      <Table.Td>
                        <Badge color={nc.status === "OPEN" ? "red" : nc.status === "INVESTIGATING" ? "orange" : nc.status === "RESOLVED" ? "blue" : "green"} size="sm">
                          {nc.status}
                        </Badge>
                      </Table.Td>
                      {canWrite && (
                        <Table.Td>
                          {nextStatuses.length > 0 && (
                            <Menu>
                              <Menu.Target>
                                <Button size="xs" variant="light" rightSection={<IconChevronDown size={14} />}>Avanzar estado</Button>
                              </Menu.Target>
                              <Menu.Dropdown>
                                {nextStatuses.map((s) => (
                                  <Menu.Item key={s} onClick={() => advanceNcStatus(nc, s)}>{s}</Menu.Item>
                                ))}
                              </Menu.Dropdown>
                            </Menu>
                          )}
                        </Table.Td>
                      )}
                    </Table.Tr>
                  );
                })}
              </Table.Tbody>
            </Table>
          </Card>
        </Tabs.Panel>

        <Tabs.Panel value="recalls" pt="md">
          <Card withBorder radius="md" p="md" shadow="xs">
            <Table highlightOnHover striped verticalSpacing="sm">
              <Table.Thead bg="gray.1">
                <Table.Tr>
                  <Table.Th>Lote Origen</Table.Th>
                  <Table.Th>Tipo de Retiro</Table.Th>
                  <Table.Th>Causa / Razón</Table.Th>
                  <Table.Th>Unidades Afectadas</Table.Th>
                  <Table.Th>Estado</Table.Th>
                  {canWrite && <Table.Th>Acción</Table.Th>}
                </Table.Tr>
              </Table.Thead>
              <Table.Tbody>
                {recalls.map((rec) => {
                  const nextStatuses = rec.status === "CANCELLED" ? [] : RECALL_STATUS_FLOW.slice(RECALL_STATUS_FLOW.indexOf(rec.status) + 1);
                  return (
                    <Table.Tr key={rec.id}>
                      <Table.Td><Badge color="cyan" variant="light" size="xs">{rec.sourceBatchTraceabilityCode}</Badge></Table.Td>
                      <Table.Td><Badge color="orange" size="xs">{rec.recallType}</Badge></Table.Td>
                      <Table.Td><Text size="xs">{rec.reason}</Text></Table.Td>
                      <Table.Td><Text size="xs" fw={700}>{rec.affectedUnitsCount} botellas → RECALLED</Text></Table.Td>
                      <Table.Td><Badge color={rec.status === "COMPLETED" ? "green" : rec.status === "CANCELLED" ? "red" : "orange"} size="sm">{rec.status}</Badge></Table.Td>
                      {canWrite && (
                        <Table.Td>
                          {nextStatuses.length > 0 && (
                            <Menu>
                              <Menu.Target>
                                <Button size="xs" variant="light" rightSection={<IconChevronDown size={14} />}>Avanzar estado</Button>
                              </Menu.Target>
                              <Menu.Dropdown>
                                {nextStatuses.map((s) => (
                                  <Menu.Item key={s} onClick={() => advanceRecallStatus(rec, s)}>{s}</Menu.Item>
                                ))}
                              </Menu.Dropdown>
                            </Menu>
                          )}
                        </Table.Td>
                      )}
                    </Table.Tr>
                  );
                })}
              </Table.Tbody>
            </Table>
          </Card>
        </Tabs.Panel>
      </Tabs>

      <Modal
        opened={ncModalOpen}
        onClose={() => setNcModalOpen(false)}
        title={<Group gap="xs"><IconAlertTriangle color="#d9480f" size={20} /><Text fw={700}>Reportar No Conformidad de Calidad (FR-33)</Text></Group>}
        size="md"
        radius="md"
      >
        <form onSubmit={handleCreateNC}>
          <Stack gap="sm">
            {ncFormError && <Alert color="red">{ncFormError}</Alert>}
            <Select label="Lote Involucrado" data={batchOptions} value={ncBatchId} onChange={setNcBatchId} searchable required />
            <TextInput label="Título de la No Conformidad" placeholder="Ej. Fuga en sello de botella" value={title} onChange={(e) => setTitle(e.currentTarget.value)} required />
            <Select label="Severidad de la Incidencia" data={["LOW", "MEDIUM", "HIGH", "CRITICAL"]} value={severity} onChange={(val) => setSeverity((val as any) || "MEDIUM")} required />
            <Textarea label="Descripción Detallada y Causa Raíz" value={description} onChange={(e) => setDescription(e.currentTarget.value)} rows={4} required />
            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => setNcModalOpen(false)}>Cancelar</Button>
              <Button color="red" type="submit" loading={ncSubmitting}>Guardar No Conformidad</Button>
            </Group>
          </Stack>
        </form>
      </Modal>

      <Modal
        opened={recallModalOpen}
        onClose={() => setRecallModalOpen(false)}
        title={<Group gap="xs"><IconAlertTriangle color="#d9480f" size={20} /><Text fw={700}>Iniciar Recall de Producto (FR-34 / RB-407)</Text></Group>}
        size="md"
        radius="md"
      >
        <form onSubmit={handleCreateRecall}>
          <Stack gap="sm">
            {recallFormError && <Alert color="red">{recallFormError}</Alert>}
            <Alert color="blue" variant="light">
              Un recall <b>COMPLETE</b> marca todas las unidades embotelladas descendientes del lote como RECALLED. Un <b>PARTIAL</b> sobre unidades específicas se administra vía backend (Postman) si no se listan aquí.
            </Alert>
            <Select label="Lote Origen" data={batchOptions} value={recallBatchId} onChange={setRecallBatchId} searchable required />
            <Select label="Tipo de Retiro" data={["PARTIAL", "COMPLETE"]} value={recallType} onChange={(val) => setRecallType((val as any) || "PARTIAL")} required />
            <Select
              label="No Conformidad relacionada (opcional)"
              data={nonConformities.map((nc) => ({ value: nc.id, label: `${nc.title} (${nc.batchTraceabilityCode})` }))}
              value={recallNcId}
              onChange={setRecallNcId}
              clearable
            />
            <Textarea label="Razón del Recall" value={recallReason} onChange={(e) => setRecallReason(e.currentTarget.value)} rows={3} required />
            <Group justify="flex-end" mt="md">
              <Button variant="outline" onClick={() => setRecallModalOpen(false)}>Cancelar</Button>
              <Button color="orange" type="submit" loading={recallSubmitting}>Iniciar Recall</Button>
            </Group>
          </Stack>
        </form>
      </Modal>
    </Stack>
  );
}
