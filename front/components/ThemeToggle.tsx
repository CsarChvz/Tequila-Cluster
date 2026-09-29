"use client";

import { useState, useEffect } from "react";
import { ActionIcon, useMantineColorScheme, useComputedColorScheme, Tooltip } from "@mantine/core";
import { IconSun, IconMoon } from "@tabler/icons-react";

export function ThemeToggle() {
  const { setColorScheme } = useMantineColorScheme();
  const computedColorScheme = useComputedColorScheme("dark", { getInitialValueInEffect: true });
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const isDark = mounted ? computedColorScheme === "dark" : true;

  return (
    <Tooltip label={isDark ? "Cambiar a Modo Claro" : "Cambiar a Modo Oscuro"}>
      <ActionIcon
        onClick={() => setColorScheme(isDark ? "light" : "dark")}
        variant="filled"
        color={isDark ? "yellow" : "dark"}
        size="lg"
        radius="md"
        aria-label="Toggle color scheme"
      >
        {isDark ? (
          <IconSun size={20} color="#111" />
        ) : (
          <IconMoon size={20} color="#ffd43b" />
        )}
      </ActionIcon>
    </Tooltip>
  );
}
