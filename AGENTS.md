# AGENTS.md — Halo

Open-source website builder — Java 21, Spring Boot WebFlux + R2DBC, Vue 3 + TailwindCSS. Versions: `gradle/libs.versions.toml`, `gradle.properties`, `ui/package.json`.

## Commands

### File-scoped (preferred — fast feedback)

```bash
./gradlew :api:compileJava                        # Compile API module
./gradlew :application:compileJava                # Compile application module
./gradlew :application:test --tests "*PostService*"    # Run specific test class
./gradlew spotlessCheck                           # Check formatting only
./gradlew spotlessApply                           # Auto-fix formatting
cd ui && pnpm build                               # Build frontend only
```

### Full build (before pushing)

```bash
./gradlew spotlessCheck build -x test             # Format + compile (no tests)
./gradlew build                                   # Everything: format + compile + test + UI
```

## Project Structure

5 Gradle submodules: `api`, `application`, `platform:application`, `platform:plugin`, `ui`. Key source: `application/src/main/java/run/halo/app/`.

## Code Formatting

- **Spotless** is the sole formatter (Checkstyle removed in #9963).
- Java: `palantirJavaFormat("2.90.0")` — 4-space indent, 120-char line limit.
- The `format 'misc'` block (XML, properties, gitignore, editorconfig) MUST live in root `build.gradle` — putting it in a submodule alongside `java` causes a Gradle classloader conflict.
- Use `leadingTabsToSpaces()` — `indentWithSpaces()` is deprecated in Spotless 8.x.

### ✅ Good patterns

- `application/src/main/java/run/halo/app/security/authentication/oauth2/MapOAuth2AuthenticationFilter.java` — reactive filter with proper null-safety
- `application/src/main/java/run/halo/app/core/user/service/impl/UserServiceImpl.java` — service pattern with extension hooks, duplicate checking
- `application/src/main/resources/extensions/role-template-authenticated.yaml` — extension resource registration

## Critical Pitfalls

1. **Never `git add -A` or `git add .`** — H2 database artifacts in `application/`. Stage specific files only.

2. **H2 database paths must be absolute.** Use `r2dbc:h2:file:///tmp/halo-db`. Relative paths (`file:./tmp/...`) create stray `application/tmp/` directories.

3. **`@Schema(pattern=...)` must be inline.** Extracting the regex to a `static final String` constant puts the literal constant name (not the regex) into the generated OpenAPI spec.

4. **Resilience4j rate limiter keys must NOT include user-controlled values.** Each unique key creates an independent limiter — including email, phone, or request body fields allows bypass by varying those values. Use only authenticated identity or client IP.

5. **Gradle wrapper upgrade requires TWO commands:**
   ```bash
   ./gradlew wrapper --gradle-version=X.Y.Z
   ./gradlew wrapper
   ```
   First updates `gradle-wrapper.properties`; second regenerates the wrapper JAR and scripts.

6. **Stale worktrees lock `main`.** If `git checkout main` fails with "already used by worktree":
   ```bash
   git worktree list
   git worktree remove --force <path>
   ```

## Git Workflow

```bash
# Always start from upstream/main — never checkout local main
git fetch upstream && git checkout upstream/main && git checkout -b feat/xxx
```

- **Branch naming:** `upgrade/gradle-X`, `feat/name`, `fix/name`, `improvement/name`
- **PR template:** `.github/pull_request_template.md`
- **CI:** `.github/workflows/halo.yaml`

## Key Files

| Purpose | Path |
|---|---|
| Dependency catalog | `gradle/libs.versions.toml` |
| Gradle properties (version) | `gradle.properties` |
| Root build config | `build.gradle`, `settings.gradle` |
| Module build configs | `api/build.gradle`, `application/build.gradle` |
| App config | `application/src/main/resources/application.yaml` |
| Frontend | `ui/package.json` (pnpm, Vue 3, TailwindCSS) |
| Extension points | `application/src/main/resources/extensions/` |
