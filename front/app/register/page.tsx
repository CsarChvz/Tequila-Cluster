"use client";

import React, { useState } from "react";
import {
  Card,
  Title,
  Text,
  TextInput,
  PasswordInput,
  Select,
  Button,
  Group,
  Stack,
  Alert,
  Container,
  Box,
  ThemeIcon,
  Anchor,
} from "@mantine/core";
import {
  IconLock,
  IconUser,
  IconMail,
  IconPlant,
  IconArrowLeft,
  IconAlertTriangle,
} from "@tabler/icons-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";
import { ThemeToggle } from "@/components/ThemeToggle";

export default function RegisterPage() {
  const router = useRouter();
  const { register } = useAuth();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("Jima Operator");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      await register({ username, email, password, role });
      router.push("/");
    } catch (err: any) {
      setError(err.message || "Error al registrar usuario");
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
                Registro de Nuevo Operador
              </Title>
              <Text size="xs" c="dimmed" mt="4px">
                Crea una cuenta con asignación de rol RBAC
              </Text>
            </Box>
          </Stack>

          {error && (
            <Alert color="red" icon={<IconAlertTriangle size={18} />} title="Error de Registro" mb="md">
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <Stack gap="md">
              <TextInput
                label="Nombre de Usuario"
                placeholder="ej. operador.jose"
                leftSection={<IconUser size={16} />}
                value={username}
                onChange={(e) => setUsername(e.currentTarget.value)}
                required
              />

              <TextInput
                label="Correo Electrónico Corporativo"
                placeholder="ej. operador@tequilacuervo.com"
                leftSection={<IconMail size={16} />}
                value={email}
                onChange={(e) => setEmail(e.currentTarget.value)}
                required
              />

              <Select
                label="Rol / Permiso Solicitado (RBAC)"
                data={[
                  "Jima Operator",
                  "Distillation Operator",
                  "Bottling Operator",
                  "Logistics Operator",
                  "Auditor",
                  "Administrator",
                ]}
                value={role}
                onChange={(val) => setRole(val || "Jima Operator")}
                required
              />

              <PasswordInput
                label="Contraseña"
                placeholder="Contraseña segura"
                leftSection={<IconLock size={16} />}
                value={password}
                onChange={(e) => setPassword(e.currentTarget.value)}
                required
              />

              <Button type="submit" color="teal" fullWidth loading={loading} mt="xs">
                Crear Cuenta de Operador
              </Button>
            </Stack>
          </form>

          <Group justify="center" mt="lg">
            <Text size="xs" c="dimmed">
              ¿Ya tienes una cuenta registrada?{" "}
              <Anchor component={Link} href="/login" fw={700} c="teal">
                Inicia sesión aquí
              </Anchor>
            </Text>
          </Group>
        </Card>
      </Container>
    </Box>
  );
}
