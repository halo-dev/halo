# Halo — AGENTS.md

Open-source website builder. Java 21 / Spring Boot WebFlux + R2DBC / Vue 3 + TailwindCSS.

Versions live in `gradle/libs.versions.toml`, `gradle.properties`, `ui/package.json`. Do not hard-code them.

## Subproject AGENTS.md files

This is a multi-module Gradle project. Each subproject has its own `AGENTS.md` with specialized
conventions. Load them when working in that area:

| Subproject | Path | What it is |
|---|---|---|
| API library | [`api/AGENTS.md`](api/AGENTS.md) | Extension model, reactive interfaces, security abstractions |
| Application | [`application/AGENTS.md`](application/AGENTS.md) | Spring Boot app, service implementations, routers |
| Frontend | [`ui/AGENTS.md`](ui/AGENTS.md) | pnpm workspace, Vue 3, TailwindCSS, packages |
| Platform BOMs | [`platform/application/AGENTS.md`](platform/application/AGENTS.md) | Dependency version constraints |
| Plugin BOM | [`platform/plugin/AGENTS.md`](platform/plugin/AGENTS.md) | Plugin development dependency platform |

## Build Commands (Quick Reference)

```bash
# File-scoped (fast feedback — preferred during development)
./gradlew :api:compileJava                         # Compile API module only
./gradlew :application:compileJava                 # Compile application module only
./gradlew :application:test --tests "*ClassName*"  # Run specific test class
./gradlew spotlessCheck                            # Check formatting (all modules)
./gradlew spotlessApply                            # Auto-fix formatting

# Full verification (before pushing)
./gradlew spotlessCheck build -x test              # Format + compile (skip tests)
./gradlew build                                    # Everything: format + compile + test + UI

# Frontend
cd ui && pnpm build                                # Build frontend only
```

## Code Formatting

**Spotless** is the sole formatter (Checkstyle removed in #9963).
Java: `palantirJavaFormat("2.90.0")` — 4-space indent, 120-char line limit.

The `format 'misc'` block (XML, properties, gitignore, editorconfig) MUST live in root
`build.gradle`. Putting it in a submodule alongside `java` causes a Gradle classloader conflict.

## Git Workflow

```bash
# Always start from upstream/main — never checkout local main
git fetch upstream && git checkout upstream/main && git checkout -b feat/xxx
```

- **Branch naming:** `upgrade/gradle-X`, `feat/name`, `fix/name`, `improvement/name`
- **PR template:** `.github/pull_request_template.md`
- **CI:** `.github/workflows/halo.yaml`
- **Never `git add -A` or `git add .`** — H2 database artifacts live in `application/`. Stage specific files only.
- **Stale worktrees lock `main`.** If `git checkout main` fails: `git worktree list && git worktree remove --force <path>`

## Critical Pitfalls (Project-Wide)

### 1. H2 database paths must be absolute
Use `r2dbc:h2:file:///tmp/halo-db`. Relative paths (`file:./tmp/...`) create stray `application/tmp/` directories.

### 2. `@Schema(pattern=...)` must be inline
Extracting the regex to a `static final String` puts the literal constant name into the generated
OpenAPI spec, not the regex. Keep it inline even if it exceeds the 120-char line limit.

### 3. Resilience4j rate limiter keys MUST NOT include user-controlled values
Each unique key creates an independent limiter. Including email, phone, or request body fields
allows bypass by varying those values. Use only authenticated identity or client IP.

### 4. Gradle wrapper upgrade requires TWO commands
```bash
./gradlew wrapper --gradle-version=X.Y.Z   # Updates gradle-wrapper.properties
./gradlew wrapper                           # Regenerates wrapper JAR + scripts
```

### 5. `format 'misc'` placement
Must be in root `build.gradle`, never in `application/build.gradle`. Co-locating `java` and `misc`
in the same submodule causes a Gradle 9.x implicit dependency validation error.

### 6. API client regeneration needs two commands in order
```bash
./gradlew generateOpenApiDocs    # Boots Spring, extracts OpenAPI JSON (~28s)
cd ui && pnpm api-client:gen     # Runs openapi-generator
```

## Config & Version Files

| What | Where |
|---|---|
| Dependency versions | `gradle/libs.versions.toml` |
| Project version | `gradle.properties` → `version=` |
| Spring Boot version | `gradle/libs.versions.toml` → `[plugins] spring-boot` |
| JDK version | `api/build.gradle` / `application/build.gradle` → `options.release = 21` |
| Frontend deps | `ui/package.json` |
| App configuration | `application/src/main/resources/application.yaml` |
| Extension points | `application/src/main/resources/extensions/` |
