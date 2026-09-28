"use client";

import React, { useState } from "react";
import {
  Card,
  Title,
  Text,
  TextInput,
  PasswordInput,
  Button,
  Group,
  Stack,
  Alert,
  Container,
  Box,
  ThemeIcon,
  Divider,
  Anchor,
} from "@mantine/core";
import {
  IconLock,
  IconUser,
  IconAlertTriangle,
  IconPlant,
  IconArrowLeft,
  IconUserCheck,
} from "@tabler/icons-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { ThemeToggle } from "@/components/ThemeToggle";

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();

  const [username, setUsername] = useState("admin.jcuervo");
  const [password, setPassword] = useState("password123");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await login(username, password);
      router.push("/");
    } catch (err: any) {
      setError(err.message || "Error al iniciar sesión");
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = async (roleName: string, mockUser: string) => {
    setUsername(mockUser);
    setPassword("password123");
    setError(null);
    setLoading(true);

    try {
      await login(mockUser, "password123");
      router.push("/");
    } catch (err: any) {
      setError(err.message || "Error al iniciar sesión");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      style={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        justifyContent: "center",
        padding: "20px",
      }}
    >
      <Container size="xs">
        <Group justify="space-between" mb="lg">
          <Button
            component={Link}
            href="/landing"
            variant="subtle"
            color="gray"
            leftSection={<IconArrowLeft size={16} />}
          >
            Volver al Landing
          </Button>
          <ThemeToggle />
        </Group>

        <Card withBorder shadow="md" padding="xl" radius="md">
          <Stack align="center" mb="md">
            <ThemeIcon color="teal" size={48} radius="md">
              <IconPlant size={30} />
            </ThemeIcon>
            <Box style={{ textAlign: "center" }}>
              <Title order={3} style={{ fontFamily: "Playfair Display, serif" }}>
                Iniciar Sesión en Tequila Cluster
              </Title>
              <Text size="xs" c="dimmed" mt="4px">
                Acceso con autenticación JWT integrada a la API de Spring Boot
              </Text>
            </Box>
          </Stack>

          {error && (
            <Alert color="red" icon={<IconAlertTriangle size={18} />} title="Error de Autenticación" mb="md">
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <Stack gap="md">
              <TextInput
                label="Nombre de Usuario o Correo"
                placeholder="ej. admin.jcuervo"
                leftSection={<IconUser size={16} />}
                value={username}
                onChange={(e) => setUsername(e.currentTarget.value)}
                required
              />

              <PasswordInput
                label="Contraseña"
                placeholder="Ingresa tu contraseña"
                leftSection={<IconLock size={16} />}
                value={password}
                onChange={(e) => setPassword(e.currentTarget.value)}
                required
              />

              <Button type="submit" color="teal" fullWidth loading={loading} mt="xs">
                Ingresar al Sistema
              </Button>
            </Stack>
          </form>

          <Divider label="Acceso Rápido por Rol (Demostración)" labelPosition="center" my="lg" />

          <Stack gap="xs">
            {[
              { role: "Administrator", user: "admin.jcuervo", color: "teal" },
              { role: "Jima Operator", user: "operador.jima", color: "green" },
              { role: "Distillation Operator", user: "operador.destilacion", color: "orange" },
              { role: "Bottling Operator", user: "operador.envasado", color: "cyan" },
              { role: "Logistics Operator", user: "operador.logistica", color: "blue" },
              { role: "Auditor", user: "auditor.calidad", color: "gray" },
            ].map((item) => (
              <Button
                key={item.role}
                size="xs"
                variant="light"
                color={item.color}
                leftSection={<IconUserCheck size={14} />}
                onClick={() => handleQuickLogin(item.role, item.user)}
              >
                Ingresar como {item.role} ({item.user})
              </Button>
            ))}
          </Stack>

          <Group justify="center" mt="lg">
            <Text size="xs" c="dimmed">
              ¿No tienes una cuenta de operador?{" "}
              <Anchor component={Link} href="/register" fw={700} c="teal">
                Regístrate aquí
              </Anchor>
            </Text>
          </Group>
        </Card>
      </Container>
    </Box>
  );
}
