"use client";

import React, { useState, useEffect, useCallback } from "react";
import {
  Title,
  Text,
  Group,
  Table,
  Badge,
  Card,
  Stack,
  Alert,
  TextInput,
  Loader,
  Center,
} from "@mantine/core";
import { IconLock, IconSearch, IconDatabase, IconAlertTriangle } from "@tabler/icons-react";
import { auditLogApi, AuditLogResponse } from "@/lib/api";

export default function AuditPage() {
  const [logs, setLogs] = useState<AuditLogResponse[]>([]);
  const [filterText, setFilterText] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const loadLogs = useCallback(async () => {
    setLoading(true);
    setLoadError(null);
    try {
      setLogs(await auditLogApi.list());
    } catch (err: any) {
      setLoadError(err.message || "No se pudo conectar con el backend (¿tu rol tiene acceso? Solo Administrator/Auditor)");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadLogs();
  }, [loadLogs]);

  const filteredLogs = logs.filter(
    (l) =>
      (l.entityId || "").toLowerCase().includes(filterText.toLowerCase()) ||
      (l.username || "").toLowerCase().includes(filterText.toLowerCase()) ||
      l.action.toLowerCase().includes(filterText.toLowerCase()) ||
      l.entityType.toLowerCase().includes(filterText.toLowerCase())
  );

  if (loading) {
    return (
      <Center style={{ minHeight: 300 }}>
        <Loader color="gray" />
      </Center>
    );
  }

  return (
    <Stack gap="lg">
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Bitácora de Auditoría e Historial Inmutable
          </Title>
          <Text size="sm" c="dimmed">
            Registro inalterable (Append-only NFR-10) de todas las transacciones y cambios de datos (FR-41 / FR-42)
          </Text>
        </div>
        <Badge color="gray" variant="filled" size="lg" leftSection={<IconLock size={14} />}>
          NFR-10: Modo Append-Only Activo
        </Badge>
      </Group>

      {loadError && (
        <Alert color="red" title="Error de conexión con el backend" icon={<IconAlertTriangle size={18} />}>
          {loadError}
        </Alert>
      )}

      <Alert color="teal" title="Garantía de Inmutabilidad e Integridad de Registro" icon={<IconDatabase size={20} />}>
        Ningún registro de auditoría puede ser modificado o eliminado (NFR-09 / NFR-10). Toda operación relevante queda firmada con timestamp, usuario e IP de origen.
      </Alert>

      <Card withBorder radius="md" p="md" shadow="xs">
        <Group justify="space-between" mb="md">
          <TextInput
            placeholder="Filtrar por entidad, usuario o acción..."
            leftSection={<IconSearch size={16} />}
            value={filterText}
            onChange={(e) => setFilterText(e.currentTarget.value)}
            style={{ width: "380px" }}
          />
          <Text size="xs" c="dimmed">Mostrando {filteredLogs.length} eventos registrados</Text>
        </Group>

        <Table highlightOnHover striped verticalSpacing="sm">
          <Table.Thead bg="gray.1">
            <Table.Tr>
              <Table.Th>Timestamp</Table.Th>
              <Table.Th>Usuario</Table.Th>
              <Table.Th>Acción</Table.Th>
              <Table.Th>Entidad Involucrada</Table.Th>
              <Table.Th>IP Origen</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {filteredLogs.map((log) => (
              <Table.Tr key={log.id}>
                <Table.Td><Text size="xs" fw={700}>{new Date(log.occurredAt).toLocaleString()}</Text></Table.Td>
                <Table.Td><Text size="xs" fw={600}>{log.username || "sistema"}</Text></Table.Td>
                <Table.Td>
                  <Badge color={log.action.includes("CANCEL") ? "red" : log.action.includes("CREATE") ? "green" : "blue"} size="xs">
                    {log.action}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700} c="teal.9">{log.entityId || "—"}</Text>
                  <Text size="11px" c="dimmed">{log.entityType}</Text>
                </Table.Td>
                <Table.Td><Badge color="dark" variant="light" size="xs">{log.ipAddress || "—"}</Badge></Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Card>
    </Stack>
  );
}
