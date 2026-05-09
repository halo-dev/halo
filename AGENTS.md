# Halo — AI Agent Guide

> **Halo** ([halo-dev/halo](https://github.com/halo-dev/halo)) is an open-source blog and content management system (CMS) built with a modern reactive Java stack and Vue 3 frontend. This guide helps AI coding agents (Claude Code, Cursor, Copilot, etc.) work effectively with the Halo codebase.

## Project Overview

Halo is a full-featured blog/CMS platform written in **Java 21** with **Spring Boot 4.1.0-RC1** (WebFlux + R2DBC reactive stack). The frontend is a **Vue 3.5** SPA styled with **TailwindCSS 3.4**. The build system uses **Gradle 9.5** for Java and **pnpm 10.30.3** for the UI.

- **Repository:** `github.com/halo-dev/halo`
- **Version:** 2.24.0-SNAPSHOT (latest stable: v2.24.2)
- **Java source level:** 21
- **Spring Boot:** 4.1.0-RC1 (reactive)
- **Database support:** H2, PostgreSQL, MySQL, MariaDB
- **License:** GPL v3

## Build Commands

### Java Build

```bash
# Full build (Java compile + UI build + tests)
./gradlew build

# Compile Java only (skip tests, skip UI)
./gradlew build -x test -x :ui:build

# Compile a specific module
./gradlew :application:compileJava
./gradlew :api:compileJava

# Fast verification (format + compile, no tests)
./gradlew spotlessCheck build -x test
```

### UI Build

```bash
# Build the UI module
cd ui && pnpm build && cd ..

# Build from root (Gradle calls pnpm internally)
./gradlew :ui:build
```

### OpenAPI Docs & API Client

```bash
# Generate OpenAPI JSON spec (boots Spring context — ~28s)
./gradlew generateOpenApiDocs

# Generate TypeScript API client from the spec
make -C ui api-client-gen
```

## Test Commands

```bash
# Run all tests
./gradlew test

# Run tests in a specific module
./gradlew :application:test

# Run a specific test class
./gradlew :application:test --tests "run.halo.app.service.PostServiceTest"

# Run tests matching a pattern
./gradlew :application:test --tests "*ecommerce*"
./gradlew :application:test --tests "*OAuth2*"
```

Test framework: **JUnit 5** with Spring Boot Test and WebTestClient (for reactive integration tests). There are **304 Java test files** across the project.

## Lint and Format

### Spotless (Google Java Format)

Halo uses **Spotless** with **`googleJavaFormat()`** — Google Java Style defaults:

- 2-space indentation
- 100-character line limit

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix all formatting issues
./gradlew spotlessApply
```

`googleJavaFormat()` was chosen over `palantirJavaFormat()` for maintenance safety — Google's active maintenance provides more reliable long-term support than Palantir's.

Spotless is configured in `build.gradle` (root) and covers **3 formatters**:

| Format | Target Files | Options |
|--------|-------------|---------|
| `misc` | `*.xml`, `*.properties`, `*.gitignore`, `*.editorconfig` | `trimTrailingWhitespace()`, `leadingTabsToSpaces()`, `endWithNewline()` |
| `json` | `*.json` | `gson().sortByKeys()` |
| `flexmark` | `*.md` | `flexmark()` |

Java formatting (Google Java Format) is applied per-submodule in each submodule's `build.gradle`.

### Lint-Staged (Pre-commit Hooks)

Husky + lint-staged runs formatters on staged files at `git commit` time. If `ui/node_modules` is missing, skip hooks with:

```bash
git commit --no-verify
```

## Project Structure

Halo follows a multi-module Gradle layout with **5 submodules**:

```
halo/
├── api/                        # Public API interfaces & DTOs
│   └── src/main/java/run/halo/app/
├── application/                # Core implementation (main app)
│   └── src/main/java/run/halo/app/
│       ├── core/               # Core domain: user, post, comment, role
│       ├── security/           # Authentication (OAuth2, form login)
│       ├── config/             # Spring Boot configuration
│       ├── content/            # Content management (posts, pages)
│       ├── theme/              # Theme engine
│       └── infrastructure/     # Infrastructure (rate limiting, caching)
├── platform/
│   ├── application/            # Platform application support
│   └── plugin/                 # Plugin framework & SPI
├── ui/                         # Vue 3 frontend SPA
│   ├── console-src/            # Admin console UI
│   ├── uc-src/                 # User center UI
│   └── packages/               # Shared UI components (@halo-dev/components)
├── build.gradle                # Root build (Spotless misc/json/flexmark config)
├── gradle/
│   ├── libs.versions.toml      # Version catalog (all dependencies)
│   └── wrapper/                # Gradle wrapper
└── settings.gradle             # Module declarations
```

## Technology Stack

| Component | Version |
|-----------|---------|
| Java | 21 |
| Spring Boot | 4.1.0-RC1 (WebFlux + R2DBC) |
| Gradle | 9.5.0 |
| Node.js | 24 |
| pnpm | 10.30.3 |
| Vue | 3.5.27 |
| TailwindCSS | 3.4.17 |
| Flux | Reactive streams |
| Spotless | 8.4.0 |

## Coding Conventions

### Java

- **Formatting:** Google Java Style (2-space indent, 100-char line limit) enforced by Spotless + `googleJavaFormat()`
- **Reactive style:** Use `Mono<T>` / `Flux<T>` from Project Reactor. Prefer `Mono.just()`, `.flatMap()`, `.switchIfEmpty()`, `.filter()` chains.
- **R2DBC:** Use `R2dbcEntityTemplate` (admin impls) or `R2dbcEntityOperations` (user-facing impls) — do not mix them
- **Annotations:** `@Schema(pattern=...)` values must be inline string literals, not constants (see pitfalls)

### REST API

- **API groups:** Console (`console.api.halo.run`), UC (`uc.api.halo.run`), Theme (public), Mini Program (`mp.api.halo.run`)
- **Endpoint naming:** Resource-oriented, versioned (`/v1alpha1/`)
- **Router functions:** Spring WebFlux functional endpoints, not `@RestController`

### Version

- Project version is set in `gradle.properties`: `version=2.24.0-SNAPSHOT`

## Common Pitfalls

### 1. Never `git add -A` or `git add .`

The `application/` directory may contain H2 database files, screenshots, and other runtime artifacts. Always stage specific files:

```bash
git add path/to/file1 path/to/file2
git status   # Check what's untracked first
```

### 2. H2 Temp Files Use Absolute Paths

When running with H2, `r2dbc:h2:file:./tmp/halo-dark` resolves to the working directory, creating an unwanted `application/tmp/` directory. Use an absolute path:

```
r2dbc:h2:file:///tmp/halo-dark
```

Do NOT add `application/tmp/` to `.gitignore` — fix the root cause.

### 3. Resilience4j Rate Limiter Key Granularity

**Each unique key creates a fresh rate limiter instance.** Including user-controlled values (email, phone, request body fields) in the key creates a bypass:

```java
// BROKEN — attacker changes email to get a fresh limiter
String key = "send-email-" + username + ":" + email;

// FIXED — only non-user-controlled values
String key = "send-email-" + username;
```

Rule: Use only authenticated username, client IP, or session ID — never request body fields, query params, or path variables.

### 4. `@Schema(pattern=...)` Must Be Inline

Springdoc reads `@Schema(pattern=...)` at compile time. Extracting the regex to a `static final` constant causes the OpenAPI spec to contain the constant name instead of the actual regex:

```java
// BROKEN — spec gets "SEMVER_PATTERN"
static final String SEMVER_PATTERN = "^(0|[1-9]\\\\d*)...";
@Schema(pattern = SEMVER_PATTERN)

// CORRECT — inline the regex
@Schema(pattern = "^(0|[1-9]\\\\d*)...")
```

If the inline regex exceeds 100 chars, use `// spotless:off` / `// spotless:on` guards.

### 5. Spotless Requires Root `build.gradle`

For multi-module setup, Spotless must be applied in the **root** `build.gradle`. Applying it to sibling submodules directly causes a Gradle classloader conflict with `SpotlessTaskService`.

Additionally, putting `format 'misc'` in `application/build.gradle` instead of root causes a Gradle 9.x implicit dependency validation error, as both `java` and `format 'misc'` share the same `SpotlessTaskService`.

### 6. Pre-commit Hooks Need `pnpm install`

If `ui/node_modules` is missing, `git commit` hooks fail. Either run `cd ui && pnpm install` first, or use `git commit --no-verify`.

## Git Workflow

### Branch Naming

```
feat/<feature-name>          # New features
fix/<bug-description>        # Bug fixes
improvement/<description>    # Tooling/config changes (Spotless, CI, etc.)
upgrade/gradle-<VERSION>     # Gradle wrapper upgrades
```

### Branch Creation

Always create branches from a clean `main`:

```bash
git fetch origin && git checkout origin/main
git checkout -b feat/my-feature
```

Never branch from other branches — this leaks dirty commits into the PR diff.

### Commit Author

```bash
git config user.name "John Niang"
git config user.email "johnniang@foxmail.com"
```

### Pre-Push Verification

```bash
# Required checks before pushing
./gradlew spotlessCheck build -x test
```

This verifies: Spotless formatting and compilation. Both MUST pass.

### PR Template

Halo uses a structured PR template at `.github/pull_request_template.md`. Every PR body must follow this format:

```markdown
#### What type of PR is this?

/kind improvement

#### What this PR does / why we need it:

...

#### Which issue(s) this PR fixes:

Fixes #

#### Special notes for your reviewer:

...

#### Does this PR introduce a user-facing change?

```release-note
NONE
```
```

**Kind labels:** `/kind bug`, `/kind cleanup`, `/kind documentation`, `/kind feature`, `/kind improvement`

### PR Submission

Default target: upstream `halo-dev/halo` (not personal fork `JohnNiang/halo`).

```bash
# Create PR against upstream
gh pr create --repo halo-dev/halo --base main --head <branch>

# Create draft PR
gh pr create --repo halo-dev/halo --base main --head <branch> --draft
```

Fork sync:
```bash
git fetch johnniang && git rebase johnniang/main
```

## Gradle Upgrade Flow

When upgrading the Gradle wrapper, use the official two-step method — do NOT manually edit the properties file.

```bash
# Two-step upgrade (OFFICIAL method)
./gradlew wrapper --gradle-version=<NEW_VERSION> && ./gradlew wrapper

# Verify ALL files changed
sha256sum gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.properties gradlew gradlew.bat
```

**Pitfall:** Both steps are required. The first updates `gradle-wrapper.properties` and downloads the new Gradle; the second regenerates the wrapper JAR and scripts using the NEW Gradle. Skipping the second step leaves an old wrapper JAR in place. Verify with `sha256sum` rather than `git diff` — the binary can change without a clear text diff.

## CI/CD Workflows

Halo has 5 CI workflows (defined in `.github/workflows/`):

| Workflow | Trigger | What it does |
|----------|---------|-------------|
| `halo.yaml` | PR + push to main | Full build + test matrix |
| `openapi-check.yaml` | PR | Verifies OpenAPI spec is current |
| `packages-preview-release.yaml` | PR | Previews package changes |
| `release-ui-packages.yaml` | Release | Publishes UI packages |
| `stale-issues.yaml` | Scheduled | Manages stale issues |

---

*This guide is automatically discovered by AI coding agents (Claude Code, Cursor, Copilot, etc.) placed at the project root.*
