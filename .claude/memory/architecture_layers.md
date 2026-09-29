---
name: architecture-layers
description: Decisión de arquitectura en capas planas con subpaquetes por dominio, y ubicación de docker
metadata:
  type: project
---

El PM eligió una arquitectura sencilla en capas planas (no un patrón con nombre formal, ni
módulos Maven/Gradle separados por etapa): un único módulo Spring Boot con paquetes de nivel
superior `controllers/ dtos/ exceptions/ models/ repositories/ services/ utils/` bajo
`org.dev.tequilacluster`.

Dentro de cada una de esas capas, el código se organiza en subpaquetes por dominio para
reflejar NFR-15 (módulos por etapa) sin romper la carpeta plana que el PM ya creó:
`security`, `shared` (traceability code, batch lifecycle, alertas, historial de transición,
audit log — usados por todas las etapas), `catalogs` (CRUD genérico de catálogos simples),
`harvest`, `distillation`, `bottling`, `logistics`, `quality`, `inventory`. No se crearon
paquetes de nivel superior nuevos: la infraestructura de JWT/RBAC vive repartida entre
`services/security` (lógica: `JwtService`, `AppUserDetailsService`, `StagePermissionService`)
y `utils/security` (infraestructura/config: `JwtAuthenticationFilter`, `AppUserPrincipal`,
`SecurityConfig`, `StageAction`), siguiendo la misma regla de "subpaquete por dominio dentro de
la capa existente" que el resto del proyecto.

**Why:** el PM ya había creado las carpetas vacías `controllers/dtos/exceptions/models/
repositories/services/utils` antes de pedir ayuda — es una decisión tomada, no una propuesta.
Imponer módulos Gradle separados por etapa habría contradicho lo que el PM ya construyó.

**How to apply:** cualquier clase nueva va en `<capa>/<dominio>/NombreClase.java`, ej.
`services/harvest/JimaBatchService.java`, `controllers/security/AuthController.java`. El nombre
de paquete raíz es `org.dev.tequilacluster` (el diagrama de paquetes del ERS decía
`com.dev.tequilacluster`; se resolvió a favor del código ya existente en el repo, igual que el
Appendix C del ERS resuelve sus propias inconsistencias).

La infraestructura de contenedores vive en la raíz del repo: `Dockerfile` (build multi-stage con
Gradle wrapper + runtime JRE 21) y `compose.yml` (`db` = postgres:16-alpine que aplica el DDL en
`docker-entrypoint-initdb.d`, `app` = build del Dockerfile con `depends_on: db: condition:
service_healthy`). Comando único: `docker compose up --build -d`.
