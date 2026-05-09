# AGENTS.md — Halo Project Conventions

## Project Overview

Halo is an open-source website building tool — Java 21, Spring Boot WebFlux + R2DBC backend, Vue 3 + TailwindCSS frontend. 5 Gradle submodules: `api`, `application`, `platform:application`, `platform:plugin`, `ui`. Key source: `application/src/main/java/run/halo/app/`. Versions: see `gradle/libs.versions.toml`, `gradle.properties`, `ui/package.json`.

## Build & Formatting

- **Spotless** is the sole formatter. Java uses `palantirJavaFormat("2.90.0")` (4-space indent, 120-char line limit).
- The `format 'misc'` block (XML, properties, gitignore, editorconfig) MUST live in root `build.gradle` — putting it in a submodule alongside `java` causes a Gradle classloader conflict.
- Use `leadingTabsToSpaces()` in misc format — `indentWithSpaces()` is deprecated in Spotless 8.x.
- Pre-push check: `./gradlew spotlessCheck build -x test`
- Apply formatting: `./gradlew spotlessApply`

## Critical Pitfalls

1. **Never `git add -A` or `git add .`** — H2 database artifacts in `application/` will pollute your commit. Stage specific files only.

2. **H2 database paths must be absolute.** Use `r2dbc:h2:file:///tmp/halo-db`, not relative paths like `file:./tmp/...`. The latter resolves relative to the working directory and creates stray `application/tmp/` directories.

3. **`@Schema(pattern=...)` must be inline.** The OpenAPI generator reads annotations at compile time — extracting the regex to a `static final String` constant results in the literal constant name in the generated spec, not the actual regex.

4. **Resilience4j rate limiter keys must NOT include user-controlled values.** Each unique key creates an independent limiter instance — if the key includes email, phone, or any request field, the rate limit can be bypassed by varying that input. Use only authenticated identity (username) or client IP.

5. **Gradle wrapper upgrade requires TWO commands:**
   ```bash
   ./gradlew wrapper --gradle-version=X.Y.Z
   ./gradlew wrapper
   ```
   The first updates `gradle-wrapper.properties`; the second regenerates the wrapper JAR and scripts using the new Gradle version. Skipping the second step leaves the old wrapper JAR in place.

6. **Stale worktrees can lock `main`.** If `git checkout main` fails with "already used by worktree", run `git worktree list` and `git worktree remove --force <path>` to clean up.

## Git Workflow

- **Branch naming:** `upgrade/gradle-X`, `feat/name`, `fix/name`, `improvement/name`
- **Always start from upstream/main:**
  ```bash
  git fetch upstream && git checkout upstream/main && git checkout -b feat/xxx
  ```
  Never `git checkout main && git pull` — avoids stale local main and worktree lock issues.
- **PR template:** see `.github/pull_request_template.md` (do not duplicate here).
- **CI:** see `.github/workflows/halo.yaml` and adjacent workflow files.

## Key Files

| Purpose | Path |
|---|---|
| Dependency catalog | `gradle/libs.versions.toml` |
| Gradle properties (version, etc.) | `gradle.properties` |
| Root build config | `build.gradle`, `settings.gradle` |
| Java module build configs | `api/build.gradle`, `application/build.gradle` |
| App entry & config | `application/src/main/resources/application.yaml` |
| Frontend | `ui/` (pnpm, see `ui/package.json`) |
| PR template | `.github/pull_request_template.md` |
