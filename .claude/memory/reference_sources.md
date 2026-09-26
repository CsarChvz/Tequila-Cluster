---
name: reference-sources
description: Rutas a los documentos fuente del proyecto (ERS, diagramas, DDL, reportes de TODO)
metadata:
  type: reference
---

- ERS completo (Word): `C:\Users\Edgardo Jr\Documents\cuatris\cuatri 7\MarcosDeTrabajo\SRS_TequilaCluster_Versión1-4.docx`
  — resumido en `CLAUDE.md` del repo, no hace falta reabrirlo salvo para revisar redacción exacta.
- Diagramas UML (PDF + fuente `.drawio` mencionada en cada uno): `C:\Users\Edgardo Jr\Documents\cuatris\cuatri 7\MarcosDeTrabajo\TequilaCluster_ERS&Diagrams\`
  (ver limitación en [[diagrams_limitation]]).
- DDL de PostgreSQL (fuente de verdad del modelo de datos): `src/main/resources/sql/DDL_Tequila_Traceability.sql`
  dentro del repo `Tequila-Cluster`.
- Variables de entorno de ejemplo: `src/main/resources/static/backend.env.example`.
- Reportes de TODO para los 2 devs: `C:\Users\Edgardo Jr\Documents\cuatris\cuatri 7\MarcosDeTrabajo\mds\`
  (ej. `TODO_TequilaCluster.md`).

**Why:** el PM trabaja con archivos fuera del repo git (docx, PDFs, carpeta `mds/`) además de
los que sí están versionados; conviene tener las rutas a mano en vez de volver a preguntar.

**How to apply:** usar estas rutas directamente en vez de buscarlas de nuevo; si un archivo no
existe donde se espera, avisar en vez de asumir que se movió.
