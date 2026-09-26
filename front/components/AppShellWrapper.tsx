"use client";

import React, { useState } from "react";
import {
  AppShell,
  Group,
  Title,
  Text,
  UnstyledButton,
  Badge,
  TextInput,
  ActionIcon,
  Menu,
  Avatar,
  Box,
  NavLink,
  Modal,
  Card,
  Divider,
  Button,
  Stack,
  Alert,
  Tooltip,
  ThemeIcon,
} from "@mantine/core";
import {
  IconPlant,
  IconPlant2,
  IconFlame,
  IconBottle,
  IconTruckDelivery,
  IconShieldCheck,
  IconHistory,
  IconSearch,
  IconBell,
  IconUserCheck,
  IconReportAnalytics,
  IconChevronRight,
  IconCheck,
  IconAlertTriangle,
  IconMapPin,
  IconBuildingWarehouse,
} from "@tabler/icons-react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { INITIAL_ALERTS } from "@/lib/mockData";

export function AppShellWrapper({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const [currentRole, setCurrentRole] = useState<string>("Administrator");
  const [searchCode, setSearchCode] = useState<string>("");
  const [searchModalOpen, setSearchModalOpen] = useState<boolean>(false);
  const [searchResult, setSearchResult] = useState<any>(null);

  const navItems = [
    { label: "Panel Principal y Trazabilidad", icon: IconReportAnalytics, href: "/", roleAccess: "Todas" },
    { label: "Jima y Cosecha", icon: IconPlant2, href: "/jima", roleAccess: "Harvest" },
    { label: "Destilación", icon: IconFlame, href: "/destilacion", roleAccess: "Distillation" },
    { label: "Envasado y Marbetes", icon: IconBottle, href: "/envasado", roleAccess: "Bottling" },
    { label: "Logística y Embarques", icon: IconTruckDelivery, href: "/logistica", roleAccess: "Logistics" },
    { label: "Calidad y Recalls", icon: IconShieldCheck, href: "/calidad", roleAccess: "Quality" },
    { label: "Auditoría e Historial", icon: IconHistory, href: "/auditoria", roleAccess: "Auditor" },
  ];

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchCode.trim()) return;
    
    // Simulate backward & forward traceability lookup
    const code = searchCode.trim().toUpperCase();
    setSearchResult({
      code,
      type: code.startsWith("EMB") ? "Shipment" : code.startsWith("SAT") ? "Tax Label" : "Batch",
      harvest: {
        code: "TRZ-2026-00101",
        field: "Rancho Tequileño Sector Norte",
        supplier: "Agaves del Valle de Amatitán S.A.",
        weight: "28,500 kg (712 piñas)",
        status: "COMPLETED",
        doArea: "DO-JALISCO-AMATITAN-01 (Vigente)",
      },
      distillation: {
        code: "TRZ-2026-00201",
        volume: "3,900 L (Hearts: 3,200 L)",
        abv: "54.5% ABV",
        maturation: "Reposado (60 días requeridos)",
        status: "MATURING",
      },
      bottling: {
        code: "TRZ-2026-00301",
        brand: "José Cuervo Tradicional",
        units: "4,500 botellas (750ml)",
        marbeteRange: "SAT-MARB-2026-770001 al 774515",
        status: "COMPLETED",
      },
      logistics: {
        code: "EMB-2026-00501",
        carrier: "Transportes Tequileros del Occidente",
        destination: "CEDIS Cuervo Guadalajara",
        status: "IN_TRANSIT",
      }
    });
    setSearchModalOpen(true);
  };

  const activeAlertsCount = INITIAL_ALERTS.filter(a => !a.resolved).length;

  return (
    <AppShell
      header={{ height: 64 }}
      navbar={{ width: 280, breakpoint: "sm" }}
      padding="md"
    >
      {/* Header */}
      <AppShell.Header style={{ backgroundColor: "#1a252c", borderBottom: "2px solid #ffb300", color: "#fff" }}>
        <Group h="100%" px="md" justify="space-between">
          <Group gap="sm">
            <ThemeIcon color="amber" variant="filled" size="lg" radius="md">
              <IconPlant size={22} color="#1a252c" />
            </ThemeIcon>
            <Box>
              <Title order={4} style={{ color: "#ffc107", fontFamily: "Playfair Display, serif", lineHeight: 1.1 }}>
                TEQUILA CLUSTER
              </Title>
              <Text size="xs" c="dimmed" style={{ letterSpacing: "1px", textTransform: "uppercase" }}>
                Sistema de Trazabilidad — José Cuervo
              </Text>
            </Box>
          </Group>

          {/* Quick Traceability Search Bar */}
          <form onSubmit={handleSearch} style={{ width: "340px" }}>
            <TextInput
              placeholder="Buscar código TRZ-YYYY-NNNNN o Folio SAT..."
              leftSection={<IconSearch size={16} />}
              value={searchCode}
              onChange={(e) => setSearchCode(e.currentTarget.value)}
              size="sm"
              radius="md"
              styles={{
                input: {
                  backgroundColor: "#2c3b47",
                  color: "#ffffff",
                  borderColor: "#3e5264",
                }
              }}
            />
          </form>

          {/* Role selector & Notifications */}
          <Group gap="md">
            <Menu shadow="md" width={220}>
              <Menu.Target>
                <Tooltip label="Alerta Activas de Proceso">
                  <ActionIcon variant="light" color="amber" size="lg" radius="md" style={{ position: "relative" }}>
                    <IconBell size={20} />
                    {activeAlertsCount > 0 && (
                      <Badge
                        color="red"
                        size="xs"
                        circle
                        style={{ position: "absolute", top: -4, right: -4 }}
                      >
                        {activeAlertsCount}
                      </Badge>
                    )}
                  </ActionIcon>
                </Tooltip>
              </Menu.Target>

              <Menu.Dropdown>
                <Menu.Label>Alertas Críticas de Proceso (FR-44)</Menu.Label>
                {INITIAL_ALERTS.map((alert) => (
                  <Menu.Item
                    key={alert.id}
                    leftSection={
                      alert.severity === "CRITICAL" ? (
                        <IconAlertTriangle size={16} color="red" />
                      ) : (
                        <IconAlertTriangle size={16} color="orange" />
                      )
                    }
                  >
                    <Text size="xs" fw={700}>{alert.batchCode}</Text>
                    <Text size="xs" c="dimmed" lineClamp={2}>{alert.message}</Text>
                  </Menu.Item>
                ))}
              </Menu.Dropdown>
            </Menu>

            <Menu shadow="md" width={240}>
              <Menu.Target>
                <UnstyledButton style={{ padding: "4px 8px", borderRadius: "6px", backgroundColor: "#2c3b47" }}>
                  <Group gap="xs">
                    <Avatar color="amber" radius="xl" size="sm">
                      <IconUserCheck size={16} />
                    </Avatar>
                    <Box style={{ textAlign: "left" }}>
                      <Text size="xs" fw={700} c="white">
                        Rol: {currentRole}
                      </Text>
                      <Text size="10px" c="amber">
                        Cambiar permisos
                      </Text>
                    </Box>
                  </Group>
                </UnstyledButton>
              </Menu.Target>

              <Menu.Dropdown>
                <Menu.Label>Seleccionar Rol Simulado (RBAC FR-02)</Menu.Label>
                {[
                  "Administrator",
                  "Jima Operator",
                  "Distillation Operator",
                  "Bottling Operator",
                  "Logistics Operator",
                  "Auditor",
                ].map((role) => (
                  <Menu.Item
                    key={role}
                    onClick={() => setCurrentRole(role)}
                    rightSection={currentRole === role ? <IconCheck size={14} color="#4bcbc9" /> : null}
                  >
                    {role}
                  </Menu.Item>
                ))}
              </Menu.Dropdown>
            </Menu>
          </Group>
        </Group>
      </AppShell.Header>

      {/* Navbar */}
      <AppShell.Navbar p="xs" style={{ backgroundColor: "#1e2c37", borderRight: "1px solid #2d3e4e" }}>
        <Text size="xs" fw={700} c="dimmed" tt="uppercase" px="xs" py="xs" style={{ letterSpacing: "0.5px" }}>
          Módulos de Producción
        </Text>
        <Stack gap="4px">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = pathname === item.href;
            return (
              <NavLink
                key={item.href}
                component={Link}
                href={item.href}
                label={item.label}
                leftSection={<Icon size={20} color={isActive ? "#ffc107" : "#a6b7c6"} />}
                active={isActive}
                styles={{
                  root: {
                    borderRadius: "8px",
                    color: isActive ? "#ffffff" : "#c1d1e0",
                    backgroundColor: isActive ? "#2c3e50" : "transparent",
                    fontWeight: isActive ? 600 : 400,
                    "&:hover": {
                      backgroundColor: "#283747",
                    },
                  },
                }}
              />
            );
          })}
        </Stack>

        <Box style={{ marginTop: "auto" }} p="xs">
          <Card padding="xs" radius="md" style={{ backgroundColor: "#141f27", border: "1px solid #2a3c4a" }}>
            <Group justify="space-between">
              <Text size="xs" c="dimmed">Status del Sistema</Text>
              <Badge color="green" size="xs" variant="dot">PostgreSQL Conectado</Badge>
            </Group>
            <Text size="11px" c="gray.4" mt="4px">
              Fecha límite entrega: 2026-09-29
            </Text>
          </Card>
        </Box>
      </AppShell.Navbar>

      {/* Main Content */}
      <AppShell.Main style={{ backgroundColor: "#f8f9fa", minHeight: "calc(100vh - 64px)" }}>
        {children}
      </AppShell.Main>

      {/* Quick Traceability Search Modal */}
      <Modal
        opened={searchModalOpen}
        onClose={() => setSearchModalOpen(false)}
        title={
          <Group gap="xs">
            <IconSearch size={20} color="#098785" />
            <Text fw={700}>Consulta de Trazabilidad Completa (FR-37 / FR-38)</Text>
          </Group>
        }
        size="lg"
        radius="md"
      >
        {searchResult && (
          <Stack gap="md">
            <Alert color="teal" title={`Resultado para: ${searchResult.code}`} icon={<IconCheck size={18} />}>
              Trazabilidad enlazada correctamente hacia atrás y hacia adelante para el código consultado.
            </Alert>

            <Divider label="Línea de Vida del Lote (Lineage)" labelPosition="center" />

            {/* Harvest */}
            <Card withBorder padding="sm" radius="md">
              <Group justify="space-between" mb="xs">
                <Group gap="xs">
                  <IconPlant2 color="#209b99" size={18} />
                  <Text fw={700} size="sm">Etapa 1: Jima / Cosecha</Text>
                </Group>
                <Badge color="green">{searchResult.harvest.status}</Badge>
              </Group>
              <Text size="xs"><b>Código:</b> {searchResult.harvest.code}</Text>
              <Text size="xs"><b>Predio:</b> {searchResult.harvest.field}</Text>
              <Text size="xs"><b>Proveedor:</b> {searchResult.harvest.supplier}</Text>
              <Text size="xs"><b>Área Autorizada DO:</b> {searchResult.harvest.doArea}</Text>
              <Text size="xs"><b>Peso Total:</b> {searchResult.harvest.weight}</Text>
            </Card>

            <Box style={{ textAlign: "center" }}>
              <IconChevronRight size={20} style={{ transform: "rotate(90deg)" }} color="#87dddb" />
            </Box>

            {/* Distillation */}
            <Card withBorder padding="sm" radius="md">
              <Group justify="space-between" mb="xs">
                <Group gap="xs">
                  <IconFlame color="#ff8f00" size={18} />
                  <Text fw={700} size="sm">Etapa 2: Destilación y Maduración</Text>
                </Group>
                <Badge color="blue">{searchResult.distillation.status}</Badge>
              </Group>
              <Text size="xs"><b>Código:</b> {searchResult.distillation.code}</Text>
              <Text size="xs"><b>Volumen Obtenido:</b> {searchResult.distillation.volume}</Text>
              <Text size="xs"><b>Graduación Alcohólica:</b> {searchResult.distillation.abv}</Text>
              <Text size="xs"><b>Maduración:</b> {searchResult.distillation.maturation}</Text>
            </Card>

            <Box style={{ textAlign: "center" }}>
              <IconChevronRight size={20} style={{ transform: "rotate(90deg)" }} color="#87dddb" />
            </Box>

            {/* Bottling */}
            <Card withBorder padding="sm" radius="md">
              <Group justify="space-between" mb="xs">
                <Group gap="xs">
                  <IconBottle color="#2daeac" size={18} />
                  <Text fw={700} size="sm">Etapa 3: Envasado y Marbetes SAT</Text>
                </Group>
                <Badge color="teal">{searchResult.bottling.status}</Badge>
              </Group>
              <Text size="xs"><b>Código:</b> {searchResult.bottling.code}</Text>
              <Text size="xs"><b>Marca:</b> {searchResult.bottling.brand}</Text>
              <Text size="xs"><b>Unidades:</b> {searchResult.bottling.units}</Text>
              <Text size="xs"><b>Rango Marbetes SAT:</b> {searchResult.bottling.marbeteRange}</Text>
            </Card>

            <Box style={{ textAlign: "center" }}>
              <IconChevronRight size={20} style={{ transform: "rotate(90deg)" }} color="#87dddb" />
            </Box>

            {/* Shipment */}
            <Card withBorder padding="sm" radius="md">
              <Group justify="space-between" mb="xs">
                <Group gap="xs">
                  <IconTruckDelivery color="#ff6f00" size={18} />
                  <Text fw={700} size="sm">Etapa 4: Logística y Embarque</Text>
                </Group>
                <Badge color="orange">{searchResult.logistics.status}</Badge>
              </Group>
              <Text size="xs"><b>Embarque N°:</b> {searchResult.logistics.code}</Text>
              <Text size="xs"><b>Transportista:</b> {searchResult.logistics.carrier}</Text>
              <Text size="xs"><b>Destino:</b> {searchResult.logistics.destination}</Text>
            </Card>

            <Button color="teal" fullWidth onClick={() => setSearchModalOpen(false)}>
              Cerrar Trazabilidad
            </Button>
          </Stack>
        )}
      </Modal>
    </AppShell>
  );
}
