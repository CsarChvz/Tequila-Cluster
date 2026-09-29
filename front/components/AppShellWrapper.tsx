"use client";

import React, { useState, useEffect, useCallback } from "react";
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
  Loader,
  ScrollArea,
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
  IconCheck,
  IconAlertTriangle,
  IconLogout,
  IconHome,
  IconLock,
} from "@tabler/icons-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { alertsApi, traceabilityApi, ProcessAlertResponse, BatchHistoryResponse } from "@/lib/api";
import { ThemeToggle } from "@/components/ThemeToggle";
import { useAuth } from "@/context/AuthContext";
import { roleLabel, canViewStage, StageCode, isAdministrator, isAuditor } from "@/lib/roles";

export function AppShellWrapper({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, isAuthenticated, isLoading, logout } = useAuth();

  const [searchCode, setSearchCode] = useState<string>("");
  const [searchModalOpen, setSearchModalOpen] = useState<boolean>(false);
  const [searchResult, setSearchResult] = useState<BatchHistoryResponse | null>(null);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  const [alerts, setAlerts] = useState<ProcessAlertResponse[]>([]);

  const isPublicPage = pathname === "/" || pathname === "/login" || pathname === "/register";

  useEffect(() => {
    if (!isLoading && !isAuthenticated && pathname.startsWith("/dashboard")) {
      router.push("/login");
    }
  }, [isLoading, isAuthenticated, pathname, router]);

  const loadAlerts = useCallback(() => {
    if (!isAuthenticated) return;
    alertsApi
      .list({ status: "OPEN" })
      .then(setAlerts)
      .catch(() => setAlerts([]));
  }, [isAuthenticated]);

  useEffect(() => {
    loadAlerts();
  }, [loadAlerts]);

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

  const roles = user?.roles || [];
  const currentRoleLabel = roles.map(roleLabel).join(" / ") || "Sin rol";
  const canSeeAuditoria = isAdministrator(roles) || isAuditor(roles);

  const navItems: Array<{ label: string; icon: any; href: string; stage?: StageCode; visible: boolean }> = [
    { label: "Panel Principal y Trazabilidad", icon: IconReportAnalytics, href: "/dashboard", visible: true },
    { label: "Jima y Cosecha", icon: IconPlant2, href: "/dashboard/jima", stage: "HARVEST", visible: canViewStage(roles, "HARVEST") },
    { label: "Destilación", icon: IconFlame, href: "/dashboard/destilacion", stage: "DISTILLATION", visible: canViewStage(roles, "DISTILLATION") },
    { label: "Envasado y Marbetes", icon: IconBottle, href: "/dashboard/envasado", stage: "BOTTLING", visible: canViewStage(roles, "BOTTLING") },
    { label: "Logística y Embarques", icon: IconTruckDelivery, href: "/dashboard/logistica", stage: "LOGISTICS", visible: canViewStage(roles, "LOGISTICS") },
    { label: "Calidad y Recalls", icon: IconShieldCheck, href: "/dashboard/calidad", visible: true },
    { label: "Auditoría e Historial", icon: IconHistory, href: "/dashboard/auditoria", visible: canSeeAuditoria },
  ].filter((item) => item.visible);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!searchCode.trim()) return;

    setSearchLoading(true);
    setSearchError(null);
    setSearchResult(null);
    try {
      const result = await traceabilityApi.getHistory(searchCode.trim().toUpperCase());
      setSearchResult(result);
      setSearchModalOpen(true);
    } catch (err: any) {
      setSearchError(err.message || "Código de trazabilidad no encontrado");
      setSearchModalOpen(true);
    } finally {
      setSearchLoading(false);
    }
  };

  const handleResolveAlert = async (id: string) => {
    try {
      await alertsApi.resolve(id);
      loadAlerts();
    } catch {
      // surfaced only via the alert staying in the list; the panel is best-effort here
    }
  };

  const activeAlertsCount = alerts.length;

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

          {/* Quick Traceability Search Bar (FR-37/38/43, real backend) */}
          <form onSubmit={handleSearch} style={{ width: "320px" }}>
            <TextInput
              placeholder="Buscar código TRZ-YYYY-NNNNN..."
              leftSection={searchLoading ? <Loader size={14} /> : <IconSearch size={16} />}
              value={searchCode}
              onChange={(e) => setSearchCode(e.currentTarget.value)}
              size="sm"
              radius="md"
            />
          </form>

          {/* Header Action Tools */}
          <Group gap="sm">
            <ThemeToggle />

            <Tooltip label="Ir al Landing Page">
              <ActionIcon component={Link} href="/" variant="light" color="teal" size="lg" radius="md">
                <IconHome size={20} />
              </ActionIcon>
            </Tooltip>

            {/* Notifications — real GET /api/v1/alerts?status=OPEN */}
            <Menu shadow="md" width={280}>
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
                <Menu.Label>Alertas Abiertas de Proceso (FR-44)</Menu.Label>
                {alerts.length === 0 && (
                  <Menu.Item disabled>Sin alertas abiertas</Menu.Item>
                )}
                {alerts.map((alert) => (
                  <Menu.Item
                    key={alert.id}
                    leftSection={
                      <IconAlertTriangle size={16} color={alert.severity === "CRITICAL" ? "red" : alert.severity === "WARNING" ? "orange" : "gray"} />
                    }
                    onClick={() => handleResolveAlert(alert.id)}
                  >
                    <Text size="xs" fw={700}>{alert.batchTraceabilityCode || alert.shipmentNumber || alert.alertType}</Text>
                    <Text size="xs" c="dimmed" lineClamp={2}>{alert.message}</Text>
                  </Menu.Item>
                ))}
              </Menu.Dropdown>
            </Menu>

            {/* User menu — real roles from the JWT, no simulated role switch */}
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
                        {currentRoleLabel}
                      </Text>
                    </Box>
                  </Group>
                </UnstyledButton>
              </Menu.Target>

              <Menu.Dropdown>
                <Menu.Label>Sesión JWT</Menu.Label>
                <Menu.Item disabled>{user?.email}</Menu.Item>
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

      {/* Quick Traceability Search Modal — real backend response */}
      <Modal
        opened={searchModalOpen}
        onClose={() => setSearchModalOpen(false)}
        title={
          <Group gap="xs">
            <IconSearch size={20} color="#098785" />
            <Text fw={700}>Consulta de Trazabilidad Completa (FR-37 / FR-38 / FR-43)</Text>
          </Group>
        }
        size="lg"
        radius="md"
      >
        {searchError && (
          <Alert color="red" title="No encontrado" icon={<IconAlertTriangle size={18} />}>
            {searchError}
          </Alert>
        )}

        {searchResult && (
          <Stack gap="md">
            <Alert color="teal" title={`Resultado para: ${searchResult.traceabilityCode}`} icon={<IconCheck size={18} />}>
              Etapa: {searchResult.stageName} — Estado: {searchResult.status}
            </Alert>

            <Card withBorder padding="sm" radius="md">
              <Text fw={700} size="sm" mb="xs">Datos del lote</Text>
              <Text size="xs"><b>Creado por:</b> {searchResult.createdByUsername || "—"}</Text>
              <Text size="xs"><b>Creado el:</b> {new Date(searchResult.createdAt).toLocaleString()}</Text>
              {searchResult.completedAt && (
                <Text size="xs"><b>Completado el:</b> {new Date(searchResult.completedAt).toLocaleString()}</Text>
              )}
              {searchResult.cancelledAt && (
                <Text size="xs" c="red"><b>Cancelado:</b> {searchResult.cancellationReason}</Text>
              )}
            </Card>

            {searchResult.stageDetail != null && (
              <Card withBorder padding="sm" radius="md">
                <Text fw={700} size="sm" mb="xs">Detalle de etapa</Text>
                <ScrollArea.Autosize mah={200}>
                  {Object.entries(searchResult.stageDetail as Record<string, unknown>).map(([key, value]) => (
                    <Text key={key} size="xs">
                      <b>{key}:</b> {String(value)}
                    </Text>
                  ))}
                </ScrollArea.Autosize>
              </Card>
            )}

            {searchResult.alerts.length > 0 && (
              <Card withBorder padding="sm" radius="md">
                <Text fw={700} size="sm" mb="xs">Alertas</Text>
                {searchResult.alerts.map((alert) => (
                  <Text key={alert.id} size="xs" c={alert.severity === "CRITICAL" ? "red" : undefined}>
                    [{alert.severity}] {alert.message}
                  </Text>
                ))}
              </Card>
            )}

            <Divider label="Historial de Transiciones" labelPosition="center" />
            {searchResult.transitions.map((t, idx) => (
              <Text key={idx} size="xs">
                {t.fromStatus || "—"} → {t.toStatus} · {t.changedByUsername || "—"} · {new Date(t.changedAt).toLocaleString()}
              </Text>
            ))}

            <Button color="teal" fullWidth onClick={() => setSearchModalOpen(false)}>
              Cerrar Trazabilidad
            </Button>
          </Stack>
        )}
      </Modal>
    </AppShell>
  );
}
