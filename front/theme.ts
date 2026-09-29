"use client";

import { createTheme, MantineColorsTuple } from "@mantine/core";

const agaveTeal: MantineColorsTuple = [
  "#eefbfb",
  "#dcf5f5",
  "#b4eae9",
  "#87dddb",
  "#62d3d1",
  "#4bcbc9",
  "#3ec6c4",
  "#2daeac",
  "#209b99",
  "#098785"
];

const copperGold: MantineColorsTuple = [
  "#fff8e1",
  "#ffefb3",
  "#ffe082",
  "#ffd54f",
  "#ffca28",
  "#ffc107",
  "#ffb300",
  "#ffa000",
  "#ff8f00",
  "#ff6f00"
];

export const theme = createTheme({
  primaryColor: "teal",
  colors: {
    teal: agaveTeal,
    copper: copperGold,
  },
  fontFamily: "Inter, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
  headings: {
    fontFamily: "Playfair Display, Georgia, serif",
  },
  defaultRadius: "md",
  cursorType: "pointer",
});
