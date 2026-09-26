"use client";

import React, { useState, useEffect } from "react";
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
  Center,
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
  IconLogout,
  IconHome,
  IconLock,
} from "@tabler/icons-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { INITIAL_ALERTS } from "@/lib/mockData";
import { ThemeToggle } from "@/components/ThemeToggle";
import { useAuth } from "@/context/AuthContext";

export function AppShellWrapper({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, isAuthenticated, isLoading, logout, setSimulatedRole } = useAuth();

  const [searchCode, setSearchCode] = useState<string>("");
  const [searchModalOpen, setSearchModalOpen] = useState<boolean>(false);
  const [searchResult, setSearchResult] = useState<any>(null);

  // If on public standalone pages (Landing /, Login /login, Register /register)
  const isPublicPage = pathname === "/" || pathname === "/login" || pathname === "/register";

  // Redirect unauthenticated user trying to access /dashboard routes
  useEffect(() => {
    if (!isLoading && !isAuthenticated && pathname.startsWith("/dashboard")) {
      router.push("/login");
    }
  }, [isLoading, isAuthenticated, pathname, router]);

  if (isPublicPage) {
    return (
      <Box
        style={{
          backgroundColor: "var(--mantine-color-body)",
          color: "var(--mantine-color-text)",
          minHeight: "100vh",
        }}
      >
        {children}
      </Box>
    );
  }

  // Protection Gate if accessing /dashboard unauthenticated
  if (!isAuthenticated && pathname.startsWith("/dashboard")) {
    return (
      <Center style={{ minHeight: "100vh", padding: "20px" }}>
        <Card withBorder padding="xl" radius="md" style={{ maxWidth: 420, textAlign: "center" }}>
          <ThemeIcon color="red" size={48} radius="md" mb="md" mx="auto">
            <IconLock size={28} />
          </ThemeIcon>
          <Title order={3} mb="xs">Acceso Restringido (JWT Required)</Title>
          <Text size="sm" c="dimmed" mb="lg">
            Debes iniciar sesión con una cuenta de operador para acceder al Dashboard y módulos de producción.
          </Text>
          <Button component={Link} href="/login" color="teal" fullWidth>
            Iniciar Sesión
          </Button>
        </Card>
      </Center>
    );
  }

  const currentRole = user?.roles[0] || "Administrator";

  const navItems = [
    { label: "Panel Principal y Trazabilidad", icon: IconReportAnalytics, href: "/dashboard" },
    { label: "Jima y Cosecha", icon: IconPlant2, href: "/dashboard/jima" },
    { label: "Destilación", icon: IconFlame, href: "/dashboard/destilacion" },
    { label: "Envasado y Marbetes", icon: IconBottle, href: "/dashboard/envasado" },
    { label: "Logística y Embarques", icon: IconTruckDelivery, href: "/dashboard/logistica" },
    { label: "Calidad y Recalls", icon: IconShieldCheck, href: "/dashboard/calidad" },
    { label: "Auditoría e Historial", icon: IconHistory, href: "/dashboard/auditoria" },
  ];

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchCode.trim()) return;
    
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
      <AppShell.Header
        style={{
          backgroundColor: "var(--mantine-color-body)",
          borderBottom: "2px solid var(--mantine-color-teal-filled)",
          color: "var(--mantine-color-text)",
        }}
      >
        <Group h="100%" px="md" justify="space-between">
          <Group gap="sm">
            <ThemeIcon color="teal" variant="filled" size="lg" radius="md">
              <IconPlant size={22} />
            </ThemeIcon>
            <Box>
              <Title
                order={4}
                style={{
                  color: "var(--mantine-color-teal-filled)",
                  fontFamily: "Playfair Display, serif",
                  lineHeight: 1.1,
                }}
              >
                TEQUILA CLUSTER
              </Title>
              <Text size="xs" c="dimmed" style={{ letterSpacing: "1px", textTransform: "uppercase" }}>
                Sistema de Trazabilidad — José Cuervo
              </Text>
            </Box>
          </Group>

          {/* Quick Traceability Search Bar */}
          <form onSubmit={handleSearch} style={{ width: "320px" }}>
            <TextInput
              placeholder="Buscar código TRZ-YYYY-NNNNN..."
              leftSection={<IconSearch size={16} />}
              value={searchCode}
              onChange={(e) => setSearchCode(e.currentTarget.value)}
              size="sm"
              radius="md"
            />
          </form>

          {/* Header Action Tools */}
          <Group gap="sm">
            {/* Dark / Light Mode Toggle */}
            <ThemeToggle />

            {/* Public Landing Link */}
            <Tooltip label="Ir al Landing Page">
              <ActionIcon component={Link} href="/" variant="light" color="teal" size="lg" radius="md">
                <IconHome size={20} />
              </ActionIcon>
            </Tooltip>

            {/* Notifications */}
            <Menu shadow="md" width={240}>
              <Menu.Target>
                <Tooltip label="Alertas Activas de Proceso">
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

            {/* User Role Switcher & Profile */}
            <Menu shadow="md" width={240}>
              <Menu.Target>
                <UnstyledButton
                  style={{
                    padding: "4px 8px",
                    borderRadius: "6px",
                    backgroundColor: "var(--mantine-color-default-hover)",
                  }}
                >
                  <Group gap="xs">
                    <Avatar color="amber" radius="xl" size="sm">
                      <IconUserCheck size={16} />
                    </Avatar>
                    <Box style={{ textAlign: "left" }}>
                      <Text size="xs" fw={700}>
                        {user ? user.username : "Operador"}
                      </Text>
                      <Text size="10px" c="teal" fw={600}>
                        {currentRole}
                      </Text>
                    </Box>
                  </Group>
                </UnstyledButton>
              </Menu.Target>

              <Menu.Dropdown>
                <Menu.Label>Cambiar Rol Simulado (RBAC FR-02)</Menu.Label>
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
                    onClick={() => setSimulatedRole(role)}
                    rightSection={currentRole === role ? <IconCheck size={14} color="#098785" /> : null}
                  >
                    {role}
                  </Menu.Item>
                ))}
                <Menu.Divider />
                <Menu.Item color="red" leftSection={<IconLogout size={14} />} onClick={logout}>
                  Cerrar Sesión (JWT)
                </Menu.Item>
              </Menu.Dropdown>
            </Menu>
          </Group>
        </Group>
      </AppShell.Header>

      {/* Navbar */}
      <AppShell.Navbar
        p="xs"
        style={{
          backgroundColor: "var(--mantine-color-body)",
          borderRight: "1px solid var(--mantine-color-default-border)",
        }}
      >
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
                leftSection={<Icon size={20} color={isActive ? "#098785" : "gray"} />}
                active={isActive}
                styles={{
                  root: {
                    borderRadius: "8px",
                    fontWeight: isActive ? 600 : 400,
                  },
                }}
              />
            );
          })}
        </Stack>

        <Box style={{ marginTop: "auto" }} p="xs">
          <Card
            padding="xs"
            radius="md"
            style={{
              backgroundColor: "var(--mantine-color-default-hover)",
              border: "1px solid var(--mantine-color-default-border)",
            }}
          >
            <Group justify="space-between">
              <Text size="xs" c="dimmed">Status JWT API</Text>
              <Badge color="green" size="xs" variant="dot">Spring Boot Ready</Badge>
            </Group>
            <Text size="11px" c="dimmed" mt="4px">
              Token: {user ? "Sesión Activa" : "Invitado"}
            </Text>
          </Card>
        </Box>
      </AppShell.Navbar>

      {/* Main Content */}
      <AppShell.Main
        style={{
          backgroundColor: "var(--mantine-color-body)",
          color: "var(--mantine-color-text)",
          minHeight: "calc(100vh - 64px)",
        }}
      >
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
