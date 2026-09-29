---
name: diagrams-limitation
description: Los PDF de diagramas UML no se pudieron rasterizar/ver en este entorno; qué se extrajo en su lugar
metadata:
  type: reference
---

La carpeta `TequilaCluster_ERS&Diagrams/` tiene 6 PDF (Class Diagram, Package Diagrams,
API & Services Diagrams, Use Case Diagrams, State Diagrams, y el SRS) exportados desde draw.io.
El contenido de los diagramas en sí está embebido como gráficos vectoriales sin capa de texto
legible; este entorno de trabajo no tenía instalado `poppler-utils` (`pdftoppm`), `ghostscript`,
`imagemagick` ni `soffice`/LibreOffice para rasterizar las páginas a imagen y poder leerlas
visualmente con el Read tool. Solo `pdftotext` estaba disponible, y solo devuelve las portadas e
índices de cada PDF (no el contenido gráfico de las páginas de diagrama).

Lo que sí se confirmó por esas portadas: 34 clases + 13 enumeraciones derivadas del DDL (Class
Diagram), paquete base `com.dev.tequilacluster` con vistas de capas/dependencias/clases por
módulo (Package Diagrams — el código real usa `org.dev.tequilacluster`, ver
[[architecture_layers]]), API REST `/api/v1` con catálogo de endpoints por rol/caso de
uso/requerimiento/código HTTP (API & Services Diagrams), 39 casos de uso mapeados a los 44 FR
(Use Case Diagrams), y estados por entidad (State Diagrams) que coinciden 1:1 con el Appendix A
del ERS ya transcrito en `CLAUDE.md`.

**Why:** como el ERS (texto) y el DDL ya especifican el mismo modelo de datos, estados y reglas
de negocio que los diagramas visualizan, no fue necesario bloquear el trabajo esperando poder
verlos — `CLAUDE.md` ya cubre esa información en forma de texto/tablas.

**How to apply:** si en el futuro se necesita ver el layout visual real de un diagrama (por
ejemplo para una presentación o para revisar una relación específica no obvia en el DDL), instalar
`poppler-utils` (`pdftoppm`) y volver a intentar el Read tool sobre el PDF, o abrir el archivo
`.drawio` editable mencionado en cada PDF. No asumir que el contenido visual coincide 100% con
lo transcrito aquí sin verificarlo si aparece una discrepancia con el ERS o el DDL.
