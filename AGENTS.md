# Repository Guidelines

Halo is a full-stack monorepo: Java 21 / Spring Boot WebFlux + R2DBC backend and a Vue 3 + TypeScript + TailwindCSS frontend. Work from the repo root and load **every relevant module guide** for the areas you touch.

## Modules

|       Module        |                           Path                            |                              Guide                               |
|---------------------|-----------------------------------------------------------|------------------------------------------------------------------|
| API library         | `api/` (extension model, contracts, security)             | [api/AGENTS.md](api/AGENTS.md)                                   |
| Backend application | `application/` (services, routers, migrations, packaging) | [application/AGENTS.md](application/AGENTS.md)                   |
| Frontend            | `ui/` (console, user center, workspace packages)          | [ui/AGENTS.md](ui/AGENTS.md)                                     |
| Application BOM     | `platform/application/` (shared dependency constraints)   | [platform/application/AGENTS.md](platform/application/AGENTS.md) |
| Plugin BOM          | `platform/plugin/` (plugin-facing constraints)            | [platform/plugin/AGENTS.md](platform/plugin/AGENTS.md)           |

## Quick Commands

Run from the repo root:

- `./gradlew build` — full backend build and tests.
- `./gradlew spotlessApply` — format Java, Markdown, JSON, and properties.
- `./gradlew :application:test` — backend tests.
- `pnpm -C ui typecheck && pnpm -C ui lint` — frontend validation.
- `pnpm -C ui test:unit` — frontend unit tests.
- `./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen` — regenerate the UI client after contract changes.

## Cross-Module Rules

- Keep API contracts, backend handlers, and UI usage in sync; never hand-edit `ui/packages/api-client/src/` — regenerate it after contract changes.
- Versions live in `gradle/libs.versions.toml`, `gradle.properties`, or `ui/package.json`; never hard-code them.
- Branch naming: `feat/`, `fix/`, `improvement/`, `upgrade/`; PRs target `upstream` and follow `.github/pull_request_template.md`.
- Stage specific files only; never `git add -A` (H2 artifacts and build outputs appear under `application/`).
- Add tests for changed behavior; keep CI green.
- Ask before public API changes, new dependencies, database migrations, security configuration, or CI workflow changes.
- Never commit secrets or introduce blocking I/O into reactive flows.

