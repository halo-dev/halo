# Halo — AI Agent Guide

> **Halo** ([halo-dev/halo](https://github.com/halo-dev/halo)) is an open-source blog/CMS built with Java 21, Spring Boot WebFlux + R2DBC, Gradle, and Vue 3 + TailwindCSS. This file is read automatically by Claude Code, Cursor, Copilot, and other AI coding agents placed at the project root.

## Tech Stack

- **Java 21**, Spring Boot (WebFlux + R2DBC reactive stack)
- **Gradle** multi-module build (see `gradle/libs.versions.toml` and `gradle.properties` for exact versions)
- **Vue 3.5** + **TailwindCSS** frontend in `ui/` (see `ui/package.json` for exact versions)
- **pnpm** for UI package management
- **Databases:** H2 (dev/test), PostgreSQL, MySQL, MariaDB (production)

## Build & Test

```bash
# Full build (compile + UI + tests)
./gradlew build

# Fast verification (format check + compile, skip tests)
./gradlew spotlessCheck build -x test

# Module-specific
./gradlew :application:compileJava
./gradlew :application:test --tests "*PostService*"

# UI only
cd ui && pnpm build && cd ..

# OpenAPI → TypeScript client
./gradlew generateOpenApiDocs && make -C ui api-client-gen
```

Tests use **JUnit 5** + Spring Boot Test + WebTestClient (reactive).

## Lint & Formatting

**Spotless** with `googleJavaFormat()` (2-space indent, 100-char line limit) + misc/json/flexmark formatters.

```bash
./gradlew spotlessCheck   # Check formatting (CI gate)
./gradlew spotlessApply   # Auto-fix
```

Checkstyle was removed (PR #9963). Spotless is the sole formatting tool.

Pre-commit hooks (Husky + lint-staged) run on `git commit`. If `ui/node_modules` is missing, use `git commit --no-verify`.

## Module Structure

```
halo/
├── api/                     # Public API interfaces & DTOs
├── application/             # Core implementation
│   └── src/main/java/run/halo/app/
│       ├── core/            # User, post, comment, role
│       ├── security/        # Auth (OAuth2, form login)
│       ├── content/         # Posts, pages, single pages
│       ├── theme/           # Theme engine
│       └── infrastructure/  # Rate limiting, caching
├── platform/
│   ├── application/         # Platform application support
│   └── plugin/              # Plugin framework & SPI
└── ui/                      # Vue 3 SPA
    ├── console-src/         # Admin console
    ├── uc-src/              # User center
    └── packages/            # Shared components (@halo-dev/components)
```

## REST API Conventions

- **Functional endpoints** (Spring WebFlux `RouterFunction`), not `@RestController`
- **API groups:** Console (`console.api.halo.run`), UC (`uc.api.halo.run`), Theme (public), MP (`mp.api.halo.run`)
- **Versioning:** `/apis/<group>/v1alpha1/`

## Coding Conventions

- **Reactive style:** Use `Mono<T>` / `Flux<T>`. Prefer `.flatMap()`, `.switchIfEmpty()`, `.filter()` chains.
- **R2DBC:** Admin impls use `R2dbcEntityTemplate`; user-facing impls use `R2dbcEntityOperations`. Do not mix them.
- **Always stage specific files:** Never `git add -A` or `git add .` — the `application/` directory may contain H2 database files and runtime artifacts.
- **Branch naming:** `feat/<name>`, `fix/<name>`, `improvement/<name>`, `upgrade/gradle-<version>`.

## PR Submission

Use the PR template at `.github/pull_request_template.md`. Default target is upstream `halo-dev/halo`.

```bash
# Create from a clean main
git fetch origin && git checkout origin/main
git checkout -b feat/my-feature

# Push & create PR
git push --set-upstream origin feat/my-feature
gh pr create --repo halo-dev/halo --base main --head feat/my-feature
```

## Pitfalls

### 1. Resilience4j Rate Limiter Key Granularity

Each unique key passed to `rateLimiterRegistry.rateLimiter(key, config)` creates an independent limiter instance. Including user-controlled values (email, phone, request body fields) in the key creates a bypass:

```java
// BROKEN — attacker changes email to get a fresh limiter
String key = "send-email-" + username + ":" + email;

// CORRECT — use only non-user-controlled values
String key = "send-email-" + username;
```

**Rule:** Rate limiter keys must use only authenticated identity or client IP — never request body fields, query params, or path variables.

### 2. `@Schema(pattern=...)` Must Be Inline

Springdoc reads annotations at compile time. Extracting a regex to a `static final` constant puts the constant name (not the regex) into the generated OpenAPI spec:

```java
// BROKEN — spec contains "SEMVER_PATTERN"
static final String SEMVER_PATTERN = "^(0|[1-9]\\d*)...";
@Schema(pattern = SEMVER_PATTERN)

// CORRECT — inline the regex
@Schema(pattern = "^(0|[1-9]\\d*)...")
```

If the inline regex exceeds 100 chars, use `// spotless:off` / `// spotless:on`.

### 3. Spotless Must Be Applied in Root `build.gradle`

For multi-module projects, apply Spotless in the root `build.gradle`, not in sibling submodules — otherwise Gradle throws a classloader conflict on `SpotlessTaskService`. `format 'misc'` must also stay in root, not in `application/build.gradle`, to avoid Gradle 9.x implicit dependency validation errors.

### 4. H2 Temp Files Use Absolute Paths

Relative paths like `r2dbc:h2:file:./tmp/halo-dark` create unwanted directories under `application/`. Use absolute: `r2dbc:h2:file:///tmp/halo-dark`. Do NOT add `application/tmp/` to `.gitignore` — fix the root cause.

### 5. Gradle Wrapper Upgrade: Always Two Steps

```bash
# Step 1: update properties + download new Gradle
./gradlew wrapper --gradle-version=<VERSION>
# Step 2: regenerate wrapper JAR + scripts (uses new Gradle)
./gradlew wrapper
```

Skipping step 2 leaves the old wrapper JAR. Verify with `sha256sum`, not just `git diff`.

### 6. OpenAPI Client Gen Requires Two Commands in Order

`./gradlew generateOpenApiDocs` first (boots Spring to extract OpenAPI JSON, ~28s), then `make -C ui api-client-gen`. Skipping the first step generates a stale client.

### 7. Pre-commit Hooks Need `pnpm install`

`git commit` triggers lint-staged which needs `ui/node_modules`. Either run `cd ui && pnpm install` first, or use `git commit --no-verify`.
