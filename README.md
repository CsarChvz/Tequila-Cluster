# Tequila Cluster — Traceability System (Backend)

Backend en Spring Boot del sistema de trazabilidad de tequila para Tequilera José Cuervo
(proyecto de curso UNIVA, *Marcos de Trabajo*). Registra y liga cada etapa de producción —
Jima (cosecha), Destilación, Envasado y Logística de embarque — con trazabilidad completa hacia
atrás y hacia adelante. El resumen completo de requerimientos, reglas de negocio y modelo de
estados vive en [`CLAUDE.md`](./CLAUDE.md); este README es solo para levantar y ubicarte en el
proyecto.

## Qué necesitas

- [Docker](https://www.docker.com/) con Docker Compose v2 (`docker compose`, no `docker-compose`).
- Para desarrollo local sin Docker: JDK 21 y PostgreSQL 16 (el wrapper de Gradle ya está
  incluido, no necesitas instalar Gradle).
- El frontend (React) vive en otro repositorio, a cargo de otro equipo — no es parte de este.

## Cómo levantar el proyecto

### Opción A — Docker (recomendada)

```bash
cp .env.example .env
# edita .env: al menos POSTGRES_PASSWORD y JWT_SECRET

docker compose up --build -d
```

Esto levanta dos servicios, en orden:

1. **`db`** — PostgreSQL 16, aplica automáticamente `src/main/resources/sql/DDL_Tequila_Traceability.sql`
   la primera vez que se crea el volumen (vía `docker-entrypoint-initdb.d`).
2. **`app`** — el backend Spring Boot, compilado con `Dockerfile` (multi-stage: build con Gradle,
   runtime con JRE). Espera a que `db` esté `healthy` antes de arrancar (`depends_on: condition:
   service_healthy`), así que nunca truena por falta de base de datos.

La API queda disponible en `http://localhost:${SERVER_PORT}` (por defecto `8080`).

Para bajar todo: `docker compose down` (agrega `-v` si además quieres borrar el volumen de datos
de Postgres). Para ver logs: `docker compose logs -f app`.

### Opción B — Desarrollo local sin Docker

```bash
# 1. Levanta solo la base de datos con Docker...
docker compose up -d db

# ...o crea manualmente una base PostgreSQL 16 y aplica el DDL tú mismo:
psql -U <usuario> -d <bd> -f src/main/resources/sql/DDL_Tequila_Traceability.sql

# 2. Copia y ajusta las variables de entorno del backend
cp src/main/resources/static/backend.env.example .env
# edita .env con la URL/usuario/password de tu Postgres local

# 3. Corre la app (Windows)
.\gradlew.bat bootRun

# 3. Corre la app (Linux/Mac)
./gradlew bootRun
```

### Variables de entorno

Hay dos archivos de ejemplo, no los confundas:

- **`.env.example`** (raíz del repo): variables que usa `compose.yml` para configurar *ambos*
  contenedores (`db` y `app`) — incluye credenciales de Postgres (`POSTGRES_DB/USER/PASSWORD`).
- **`src/main/resources/static/backend.env.example`**: variables que consume directamente
  Spring Boot (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, etc.) — úsalo cuando corras
  el backend fuera de Docker apuntando a una base de datos propia.

En ambos casos: copia el archivo a `.env`, ajusta los valores reales y **nunca lo commitees**
(ya está en `.gitignore`).

## Arquitectura del proyecto

Arquitectura simple en capas (sin nombre de patrón formal): un único módulo Spring Boot,
`controllers → services → repositories → models`, con `dtos/`, `exceptions/` y `utils/` como
capas de soporte. Dentro de cada capa, el código se organiza en subpaquetes por dominio para
reflejar las etapas del proceso (NFR-15) sin salir de esa estructura plana:

```
security    → autenticación, JWT, permisos por rol+etapa (FR-01 a FR-04)
shared      → traceability code, ciclo de vida de batch, alertas, historial, audit log
catalogs    → catálogos de administración (proveedores, áreas autorizadas, marcas, etc.)
harvest     → Jima: lotes de cosecha, permisos de transporte (FR-05 a FR-11)
distillation→ lotes de destilación, cortes, maduración (FR-12 a FR-19)
bottling    → envasado, marbetes, unidades embotelladas (FR-20 a FR-25)
logistics   → embarques, documentos, entregas (FR-26 a FR-32)
quality     → no conformidades y recalls (FR-33, FR-34)
inventory   → movimientos de inventario (FR-35, FR-36)
```

### Qué hay en cada directorio

```
Tequila-Cluster/
├── CLAUDE.md                    # ERS resumido: FR, RB, roles, estados, glosario — léelo antes de tocar reglas de negocio
├── compose.yml                  # Orquesta "db" (Postgres) + "app" (esta API), en ese orden
├── Dockerfile                   # Build multi-stage de la API (Gradle → JRE 21)
├── .env.example                 # Variables para docker compose (raíz del repo)
├── build.gradle / settings.gradle / gradlew(.bat)  # Build del backend (Gradle, Java 21)
├── .claude/memory/               # Memoria de proyecto para Claude Code (decisiones, limitaciones conocidas)
├── src/main/resources/
│   ├── sql/DDL_Tequila_Traceability.sql   # Fuente de verdad del modelo de datos (PostgreSQL)
│   ├── static/backend.env.example         # Variables de entorno del backend (fuera de Docker)
│   └── application.properties             # Config de Spring Boot, lee las variables de entorno de arriba
└── src/main/java/org/dev/tequilacluster/
    ├── controllers/<dominio>/   # Endpoints REST (@RestController), uno por caso de uso
    ├── dtos/<dominio>/          # Records de entrada/salida de los controllers (no exponer entidades JPA directo)
    ├── exceptions/              # Excepciones de dominio + manejador global (@RestControllerAdvice) → códigos HTTP estándar
    ├── models/<dominio>/        # Entidades JPA (1:1 con las tablas del DDL) + enums en models/<dominio>/enums/
    ├── repositories/<dominio>/  # Interfaces Spring Data JPA (una por entidad)
    ├── services/<dominio>/      # Lógica de negocio y reglas (RB-*) — los controllers no validan nada por sí mismos
    │   └── security/            # JwtService, AppUserDetailsService, StagePermissionService (RBAC por rol+etapa)
    └── utils/                   # Utilidades transversales sin estado de negocio
        └── security/            # Filtro JWT, principal de Spring Security, SecurityConfig, enum StageAction
```

Los módulos `distillation`, `bottling`, `logistics`, `quality` e `inventory` tienen solo
esqueletos (`services`/`controllers` con `// TODO`) — la lógica de negocio completa está
asignada a los dos devs, ver el reporte de tareas en la carpeta compartida `mds/`
(`TODO_TequilaCluster.md`). El módulo `harvest` (Jima) está implementado de punta a punta y sirve
como patrón de referencia para replicar en las demás etapas.

## Verificar que compila

```bash
.\gradlew.bat compileJava   # Windows
./gradlew compileJava       # Linux/Mac
```
