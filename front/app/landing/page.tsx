"use client";

import React from "react";
import {
  Title,
  Text,
  Button,
  Group,
  Container,
  Grid,
  Card,
  ThemeIcon,
  Stack,
  Badge,
  Paper,
  Box,
  SimpleGrid,
  useComputedColorScheme,
} from "@mantine/core";
import {
  IconPlant,
  IconPlant2,
  IconFlame,
  IconBottle,
  IconTruckDelivery,
  IconShieldCheck,
  IconArrowRight,
  IconQrcode,
  IconCheck,
  IconLock,
  IconCertificate,
} from "@tabler/icons-react";
import Link from "next/link";
import { ThemeToggle } from "@/components/ThemeToggle";

export default function LandingPage() {
  const computedColorScheme = useComputedColorScheme("dark", { getInitialValueInEffect: true });

  return (
    <Box style={{ minHeight: "100vh" }}>
      {/* Top Header */}
      <Box
        component="header"
        py="md"
        px="lg"
        style={(theme) => ({
          borderBottom: `1px solid ${theme.colors.gray[3]}`,
          backdropFilter: "blur(10px)",
        })}
      >
        <Container size="xl">
          <Group justify="space-between">
            <Group gap="sm">
              <ThemeIcon color="teal" size="lg" radius="md">
                <IconPlant size={22} />
              </ThemeIcon>
              <div>
                <Title order={3} style={{ fontFamily: "Playfair Display, serif" }}>
                  TEQUILA CLUSTER
                </Title>
                <Text size="10px" c="dimmed" tt="uppercase" style={{ letterSpacing: "1px" }}>
                  Trazabilidad José Cuervo
                </Text>
              </div>
            </Group>

            <Group gap="sm">
              <ThemeToggle />
              <Button component={Link} href="/login" variant="outline" color="teal">
                Iniciar Sesión
              </Button>
              <Button component={Link} href="/register" color="teal">
                Registrarse
              </Button>
            </Group>
          </Group>
        </Container>
      </Box>

      {/* Hero Section */}
      <Box py={80} style={{ textAlign: "center" }}>
        <Container size="md">
          <Badge color="amber" variant="light" size="lg" mb="md">
            Entrega Proyecto UNIVA — Tequilera José Cuervo
          </Badge>
          <Title
            order={1}
            mb="md"
            style={{
              fontFamily: "Playfair Display, serif",
              fontSize: "2.8rem",
              lineHeight: 1.2,
            }}
          >
            Sistema Web de Trazabilidad Total de Tequila
          </Title>
          <Text size="lg" c="dimmed" mb={40} style={{ maxWidth: "680px", margin: "0 auto 32px" }}>
            Registra y liga cada etapa del proceso de producción — Jima, Destilación, Envasado con Marbetes SAT y Logística de Embarque — garantizando trazabilidad inmutable hacia atrás y hacia adelante.
          </Text>

          <Group justify="center" gap="md">
            <Button
              component={Link}
              href="/"
              size="lg"
              color="teal"
              rightSection={<IconArrowRight size={20} />}
            >
              Ingresar al Panel Principal
            </Button>
            <Button
              component={Link}
              href="/login"
              size="lg"
              variant="outline"
              color="copper"
            >
              Acceso Operadores
            </Button>
          </Group>
        </Container>
      </Box>

      {/* Production Modules Grid */}
      <Container size="xl" py={60}>
        <Title order={2} style={{ textAlign: "center", fontFamily: "Playfair Display, serif" }} mb="xs">
          Módulos Integrados de Producción
        </Title>
        <Text size="sm" c="dimmed" style={{ textAlign: "center" }} mb={50}>
          Cumplimiento riguroso de Reglas de Negocio (RB-*) y Norma Oficial Mexicana del Tequila (CRT / SAT)
        </Text>

        <SimpleGrid cols={{ base: 1, sm: 2, md: 4 }} spacing="lg">
          {/* Card 1: Jima */}
          <Card padding="lg" radius="md" withBorder shadow="sm">
            <ThemeIcon color="teal" size="xl" radius="md" mb="md">
              <IconPlant2 size={28} />
            </ThemeIcon>
            <Title order={4} mb="xs">1. Jima y Agave</Title>
            <Text size="xs" c="dimmed" mb="md">
              Registro de cosecha en predios autorizados con Denominación de Origen (DO), peso en kg y permisos de transporte SAT.
            </Text>
            <Badge color="teal" variant="light" size="xs">FR-05 al FR-11</Badge>
          </Card>

          {/* Card 2: Destilacion */}
          <Card padding="lg" radius="md" withBorder shadow="sm">
            <ThemeIcon color="orange" size="xl" radius="md" mb="md">
              <IconFlame size={28} />
            </ThemeIcon>
            <Title order={4} mb="xs">2. Destilación</Title>
            <Text size="xs" c="dimmed" mb="md">
              Control de alambique por cortes (Cabezas/Corazón/Colas), graduación % ABV y seguimiento de maduración en barrica.
            </Text>
            <Badge color="orange" variant="light" size="xs">FR-12 al FR-19</Badge>
          </Card>

          {/* Card 3: Envasado */}
          <Card padding="lg" radius="md" withBorder shadow="sm">
            <ThemeIcon color="cyan" size="xl" radius="md" mb="md">
              <IconBottle size={28} />
            </ThemeIcon>
            <Title order={4} mb="xs">3. Envasado SAT</Title>
            <Text size="xs" c="dimmed" mb="md">
              Asignación de folios de marbetes fiscales SAT, reconciliación de mermas y código QR unívoco por botella.
            </Text>
            <Badge color="cyan" variant="light" size="xs">FR-20 al FR-25</Badge>
          </Card>

          {/* Card 4: Logistica */}
          <Card padding="lg" radius="md" withBorder shadow="sm">
            <ThemeIcon color="red" size="xl" radius="md" mb="md">
              <IconTruckDelivery size={28} />
            </ThemeIcon>
            <Title order={4} mb="xs">4. Logística</Title>
            <Text size="xs" c="dimmed" mb="md">
              Embarques a CEDIS y exportación con Carta Porte Digital SAT, Manifiesto CRT y confirmación de llegada.
            </Text>
            <Badge color="red" variant="light" size="xs">FR-26 al FR-32</Badge>
          </Card>
        </SimpleGrid>
      </Container>

      {/* Compliance & Security Banner */}
      <Box py={60} bg={computedColorScheme === "dark" ? "#1a242d" : "#f1f3f5"}>
        <Container size="lg">
          <SimpleGrid cols={{ base: 1, md: 3 }} spacing="xl">
            <Group gap="sm" wrap="nowrap">
              <ThemeIcon color="teal" variant="light" size="lg">
                <IconCertificate size={24} />
              </ThemeIcon>
              <div>
                <Text fw={700} size="sm">Normativa CRT y SAT</Text>
                <Text size="xs" c="dimmed">Marbetes fiscales y áreas DO validadas</Text>
              </div>
            </Group>

            <Group gap="sm" wrap="nowrap">
              <ThemeIcon color="copper" variant="light" size="lg">
                <IconQrcode size={24} />
              </ThemeIcon>
              <div>
                <Text fw={700} size="sm">Trazabilidad Inmutable</Text>
                <Text size="xs" c="dimmed">Bitácora append-only sin borrado físico</Text>
              </div>
            </Group>

            <Group gap="sm" wrap="nowrap">
              <ThemeIcon color="blue" variant="light" size="lg">
                <IconLock size={24} />
              </ThemeIcon>
              <div>
                <Text fw={700} size="sm">Seguridad RBAC JWT</Text>
                <Text size="xs" c="dimmed">Acceso restringido por etapa y rol</Text>
              </div>
            </Group>
          </SimpleGrid>
        </Container>
      </Box>

      {/* Footer */}
      <Box py="lg" style={{ textAlign: "center", borderTop: "1px solid rgba(0,0,0,0.1)" }}>
        <Text size="xs" c="dimmed">
          © 2026 Tequilera José Cuervo — Proyecto de curso UNIVA (Marcos de Trabajo).
        </Text>
      </Box>
    </Box>
  );
}
