---
name: project-tequila-cluster
description: Qué es Tequila Cluster Traceability System, deadline, equipo y fuente de verdad del modelo de datos
metadata:
  type: project
---

Tequila Cluster Traceability System es un proyecto de curso (UNIVA, "Marcos de Trabajo") para
Tequilera José Cuervo: web app de trazabilidad de tequila (Jima → Destilación → Envasado →
Logística), stack React + Spring Boot + PostgreSQL. Entrega final: **2026-09-29**.

Equipo: Vania Guzmán Maldonado (Product Owner), Edgardo Franco Farías (Project Manager, el
usuario de estas sesiones), César Chávez Rodríguez, Paul Angello García Arceo y Juan Manuel
Rodríguez Rivera (Software Developers).

**Why:** el PM dejó una base de proyecto (2026-09-26) para que los devs completen el resto
durante el fin de semana antes de la entrega. El resumen completo del ERS (44 FR, 45 RB, 18
NFR) vive en `CLAUDE.md` en la raíz del repo — no releer el `.docx` original, `CLAUDE.md` es la
fuente resumida y `DDL_Tequila_Traceability.sql` es la fuente de verdad del modelo de datos.

**How to apply:** antes de proponer cambios al modelo de datos o a los FR/RB, verificar contra
`CLAUDE.md` y el DDL, no contra suposiciones. Ver [[architecture_layers]] para dónde va el
código nuevo y [[reference_sources]] para las rutas completas de los documentos fuente.
