"use client";

import React, { useState } from "react";
import {
  Title,
  Text,
  Group,
  Table,
  Badge,
  Card,
  Stack,
  Alert,
  Paper,
  TextInput,
  Select,
} from "@mantine/core";
import {
  IconHistory,
  IconLock,
  IconSearch,
  IconDatabase,
} from "@tabler/icons-react";
import { INITIAL_AUDIT_LOGS, AuditLogItem } from "@/lib/mockData";

export default function AuditPage() {
  const [logs] = useState<AuditLogItem[]>(INITIAL_AUDIT_LOGS);
  const [filterText, setFilterText] = useState("");

  const filteredLogs = logs.filter(
    (l) =>
      l.entityId.toLowerCase().includes(filterText.toLowerCase()) ||
      l.user.toLowerCase().includes(filterText.toLowerCase()) ||
      l.action.toLowerCase().includes(filterText.toLowerCase())
  );

  return (
    <Stack gap="lg">
      {/* Title */}
      <Group justify="space-between" align="center">
        <div>
          <Title order={2} style={{ fontFamily: "Playfair Display, serif", color: "#1a252c" }}>
            Bitácora de Auditoría e Historial Inmutable
          </Title>
          <Text size="sm" c="dimmed">
            Registro inalterable (Append-only NFR-10) de todas las transacciones, transiciones de lotes y cambios de datos (FR-41 / FR-42)
          </Text>
        </div>
        <Badge color="gray" variant="filled" size="lg" leftSection={<IconLock size={14} />}>
          NFR-10: Modo Append-Only Activo
        </Badge>
      </Group>

      <Alert color="teal" title="Garantía de Inmutabilidad e Integridad de Registro" icon={<IconDatabase size={20} />}>
        Ningún registro de auditoría puede ser modificado o eliminado (NFR-09 / NFR-10). Todas las operaciones de creación, transición de lote o intento fallido de validación quedan firmadas con timestamp, usuario, rol e IP de origen.
      </Alert>

      {/* Filter Bar */}
      <Card withBorder radius="md" p="md" shadow="xs">
        <Group justify="space-between" mb="md">
          <TextInput
            placeholder="Filtrar por código de lote, usuario o acción..."
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
              <Table.Th>Timestamp (UTC-6)</Table.Th>
              <Table.Th>Usuario y Rol</Table.Th>
              <Table.Th>Acción Realizada</Table.Th>
              <Table.Th>Entidad Involucrada</Table.Th>
              <Table.Th>Detalle de la Operación</Table.Th>
              <Table.Th>IP Origen</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {filteredLogs.map((log) => (
              <Table.Tr key={log.id}>
                <Table.Td>
                  <Text size="xs" fw={700}>{log.timestamp}</Text>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={600}>{log.user}</Text>
                  <Badge color="gray" variant="dot" size="xs">{log.role}</Badge>
                </Table.Td>
                <Table.Td>
                  <Badge
                    color={
                      log.action.includes("FAIL")
                        ? "red"
                        : log.action.includes("CREATE")
                        ? "green"
                        : "blue"
                    }
                    size="xs"
                  >
                    {log.action}
                  </Badge>
                </Table.Td>
                <Table.Td>
                  <Text size="xs" fw={700} c="teal.9">{log.entityId}</Text>
                  <Text size="11px" c="dimmed">{log.entity}</Text>
                </Table.Td>
                <Table.Td style={{ maxWidth: "300px" }}>
                  <Text size="xs">{log.details}</Text>
                </Table.Td>
                <Table.Td>
                  <Badge color="dark" variant="light" size="xs">{log.ipAddress}</Badge>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Card>
    </Stack>
  );
}
