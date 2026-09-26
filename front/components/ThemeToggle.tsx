"use client";

import { ActionIcon, useMantineColorScheme, useComputedColorScheme, Tooltip } from "@mantine/core";
import { IconSun, IconMoon } from "@tabler/icons-react";

export function ThemeToggle() {
  const { setColorScheme } = useMantineColorScheme();
  const computedColorScheme = useComputedColorScheme("dark", { getInitialValueInEffect: true });

  return (
    <Tooltip label={computedColorScheme === "dark" ? "Cambiar a Modo Claro" : "Cambiar a Modo Oscuro"}>
      <ActionIcon
        onClick={() => setColorScheme(computedColorScheme === "light" ? "dark" : "light")}
        variant="filled"
        color={computedColorScheme === "dark" ? "yellow" : "dark"}
        size="lg"
        radius="md"
        aria-label="Toggle color scheme"
      >
        {computedColorScheme === "dark" ? (
          <IconSun size={20} color="#111" />
        ) : (
          <IconMoon size={20} color="#ffd43b" />
        )}
      </ActionIcon>
    </Tooltip>
  );
}
