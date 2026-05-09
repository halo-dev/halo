# Halo — AGENTS.md

Open-source CMS/blogging platform. Java 21 / Spring Boot WebFlux + R2DBC + Vue 3 + TypeScript + TailwindCSS.

Versions live in `gradle/libs.versions.toml`, `gradle.properties`, and `ui/package.json`. Do not hard-code them.

## Start here

- This repository is usually opened and worked from the **repo root**, even for frontend-only changes. Prefer root-scoped commands such as `./gradlew :application:test` and `pnpm -C ui lint`.
- Backend and frontend changes are often part of the same task. Treat this as a **full-stack repo**, not two isolated projects.
- This root file is the **repo-wide baseline**. Nested `AGENTS.md` files add local rules. When a task touches multiple areas, load **every relevant nested file**, not just the one closest to the first edited file.

## Quick commands (run from repo root)

```bash
./gradlew build                                # full repo build: backend + UI packaging + tests
./gradlew spotlessApply                        # format Java and repo-level markdown/json/properties
./gradlew :api:test                            # API module tests
./gradlew :application:test                    # backend tests
./gradlew :application:bootRun                 # backend dev server
pnpm -C ui install                             # install frontend deps
pnpm -C ui build                               # frontend build
pnpm -C ui typecheck && pnpm -C ui lint        # frontend validation
pnpm -C ui test:unit                           # frontend unit tests
./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen   # refresh OpenAPI-generated UI client
```

## How work is split in this repo

|         Area         |          Path           |                           Use it for                           |                        Also load                         |
|----------------------|-------------------------|----------------------------------------------------------------|----------------------------------------------------------|
| Public API contracts | `api/`                  | Extension model, service interfaces, security abstractions     | `application/AGENTS.md` when implementations also change |
| Backend application  | `application/`          | WebFlux services, routers, security, migrations, packaging     | `ui/AGENTS.md` when payloads or UX also change           |
| Frontend             | `ui/`                   | Console, user center, workspace packages, generated API client | `application/AGENTS.md` for API or OpenAPI work          |
| Application BOM      | `platform/application/` | Shared dependency constraints                                  | root only unless API/application also change             |
| Plugin BOM           | `platform/plugin/`      | Plugin-facing dependency constraints                           | `api/AGENTS.md` when plugin API changes                  |

## Nested AGENTS.md files

Use the root file plus the relevant nested file(s):

|  Subproject  |                                Path                                |                           What it adds                            |
|--------------|--------------------------------------------------------------------|-------------------------------------------------------------------|
| API library  | [`api/AGENTS.md`](api/AGENTS.md)                                   | Public API boundaries, extension model, service contract pitfalls |
| Application  | [`application/AGENTS.md`](application/AGENTS.md)                   | WebFlux implementation patterns, routers, security, tests         |
| Frontend     | [`ui/AGENTS.md`](ui/AGENTS.md)                                     | pnpm workspace commands, module layout, UI conventions            |
| Platform BOM | [`platform/application/AGENTS.md`](platform/application/AGENTS.md) | Dependency constraint rules                                       |
| Plugin BOM   | [`platform/plugin/AGENTS.md`](platform/plugin/AGENTS.md)           | Plugin BOM rules and downstream compatibility notes               |

## Cross-module workflows

### Full-stack feature work

1. Start at the repo root.
2. Load the nested `AGENTS.md` for **every** touched module.
3. Keep API contracts, backend handlers, and UI usage in sync in the same change.

### Backend contract or OpenAPI changes

If you change any of these, also consider UI impact:

- public request/response DTOs
- REST routes or router contracts
- OpenAPI annotations or generated schema
- auth/session behavior consumed by console or user center

Then regenerate the client:

```bash
./gradlew generateOpenApiDocs
pnpm -C ui api-client:gen
```

Do not edit generated files in `ui/packages/api-client/src/` directly.

### UI packaging

The UI is embedded into the backend JAR. `application:copyUiDist` copies `ui/build/dist` into the application resources during the backend build. If the UI output changes, assume the backend packaging path matters too.

## Repository structure

- **`api/`** — Public API library published as `run.halo.app`; changes here affect external plugins.
- **`application/`** — Main Spring Boot application; all business logic lives here.
- **`platform/application/`** — Application BOM for dependency versions.
- **`platform/plugin/`** — Plugin BOM for downstream plugin development.
- **`ui/`** — pnpm workspace for admin console (`/console`) and user center (`/uc`).
- **`docs/`** — Human-facing developer documentation.

## Code style and examples

**Java:** Spotless + Palantir Java Format (`2.90.0`) with 4-space indentation and 120-character lines. Spotless is the source of truth.

**Frontend:** `vp fmt` (`pnpm -C ui format`) + ESLint. See [`ui/AGENTS.md`](ui/AGENTS.md) for module conventions.

Example validation paths:

```bash
# backend-only change
./gradlew spotlessApply
./gradlew :application:test

# frontend-only change
pnpm -C ui format
pnpm -C ui typecheck && pnpm -C ui lint

# cross-stack contract change
./gradlew generateOpenApiDocs
pnpm -C ui api-client:gen
pnpm -C ui typecheck && pnpm -C ui lint
./gradlew :application:test
```

## Key development notes

- Preset plugins are downloaded at build time via `downloadPluginPresets`.
- Database migrations use `r2dbc-migrate`, not Flyway/Liquibase.
- Migration scripts live in `application/src/main/resources/db/migration/{h2,mariadb,mysql,postgresql}/`.
- Extension type schemas live in `application/src/main/resources/extensions/`; runtime data is stored as JSON in the database.
- Profile-specific configs include `application-local.yaml`, `application-dev.yaml`, and `application-postgresql.yaml`.
- Default work directory is `~/.halo2` unless overridden by `halo.work-dir`.
- Virtual threads are enabled. Do not introduce blocking I/O in reactive paths.
- AI-assisted contributions are allowed, but material usage should be disclosed in PR descriptions.

## Repository & upstream context

This AGENTS.md is shared by two repositories:

|      Repository       |         Purpose         |
|-----------------------|-------------------------|
| `halo-dev/halo`       | Open-source edition     |
| `lxware-dev/halo-pro` | Pro/proprietary edition |

Always identify the current upstream before pushing or opening a PR:

```bash
git remote -v
```

PRs must target the `upstream` remote of the current repo. Opening against the wrong upstream is costly to unwind.

## Git workflow

```bash
git fetch upstream && git checkout upstream/main && git checkout -b feat/xxx
```

- **Branch naming:** `upgrade/gradle-X`, `feat/name`, `fix/name`, `improvement/name`
- **PR template:** `.github/pull_request_template.md`
- **CI:** `.github/workflows/halo.yaml`
- **Never `git add -A` or `git add .`** — H2 artifacts and build outputs can appear under `application/`; stage specific files only.
- **Stale worktrees can lock `main`.** If `git checkout main` fails, inspect with `git worktree list` and remove the stale worktree explicitly.

## Before you submit

Run the checks that match the touched area, and do not leave red CI behind:

```bash
./gradlew spotlessApply
./gradlew :application:test
pnpm -C ui typecheck && pnpm -C ui lint
```

Add or update tests for the code you change.

## Boundaries

- ✅ **Always:** Work from the repo root for command orchestration; load all touched nested `AGENTS.md` files; stage files explicitly; regenerate the UI API client after OpenAPI contract changes; add or update tests for changed behavior.
- ⚠️ **Ask first:** Public API changes in `api/`; new Gradle or npm dependencies; database migration scripts; security configuration; CI workflow changes; destructive changes to generated client output.
- 🚫 **Never:** Commit secrets or credentials; hard-code versions; introduce blocking I/O into reactive flows; edit generated API client files directly; open a PR without verifying the correct upstream.

## Critical pitfalls

### 1. H2 database paths must be absolute

Use `r2dbc:h2:file:///tmp/halo-db`. Relative paths such as `file:./tmp/...` create stray `application/tmp/` directories.

### 2. Gradle wrapper upgrades require two commands

```bash
./gradlew wrapper --gradle-version=X.Y.Z
./gradlew wrapper
```

### 3. `format 'misc'` must stay in the root `build.gradle`

Do not move it into `application/build.gradle`. Putting `java` and `misc` formatting in the same submodule triggers a Gradle 9.x implicit dependency validation error.

## Config and version files

|            What            |                                  Where                                  |
|----------------------------|-------------------------------------------------------------------------|
| Dependency versions        | `gradle/libs.versions.toml`                                             |
| Project version            | `gradle.properties`                                                     |
| Spring Boot plugin version | `gradle/libs.versions.toml` → `[plugins] spring-boot`                   |
| JDK release                | `api/build.gradle`, `application/build.gradle` → `options.release = 21` |
| Frontend dependencies      | `ui/package.json`                                                       |
| App configuration          | `application/src/main/resources/application.yaml`                       |
| Extension schemas          | `application/src/main/resources/extensions/`                            |

