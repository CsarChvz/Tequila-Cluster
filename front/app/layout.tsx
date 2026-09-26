import "@mantine/core/styles.css";
import React from "react";
import {
  MantineProvider,
  ColorSchemeScript,
  mantineHtmlProps,
} from "@mantine/core";
import { theme } from "../theme";
import { AppShellWrapper } from "@/components/AppShellWrapper";
import { AuthProvider } from "@/context/AuthContext";

export const metadata = {
  title: "Tequila Cluster — Sistema de Trazabilidad (José Cuervo)",
  description: "Sistema web de trazabilidad de tequila para Jima, Destilación, Envasado y Logística",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es" {...mantineHtmlProps}>
      <head>
        <ColorSchemeScript defaultColorScheme="dark" />
        <link rel="shortcut icon" href="/favicon.svg" />
        <meta
          name="viewport"
          content="minimum-scale=1, initial-scale=1, width=device-width, user-scalable=no"
        />
      </head>
      <body>
        <MantineProvider theme={theme} defaultColorScheme="dark">
          <AuthProvider>
            <AppShellWrapper>{children}</AppShellWrapper>
          </AuthProvider>
        </MantineProvider>
      </body>
    </html>
  );
}
