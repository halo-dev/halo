---
name: gradle-dependency-updates
description: >-
  Upgrade outdated Gradle dependencies in this project. Use this skill whenever
  the user asks to check for dependency updates, show what's outdated, or
  upgrade/bump/update Gradle dependencies, libraries, or plugins — including
  mentions of "dependencyUpdates", the versions plugin, or "outdated
  dependencies". Backend only (api/application/platform); frontend dependencies
  (ui/, pnpm) are out of scope.
---

# Gradle Dependency Updates

This project manages backend dependencies through a Gradle version catalog
(`gradle/libs.versions.toml`) plus the Spring Boot BOM. The ben-manes Versions
plugin is already applied to `:api` and `:application`.

## 1. Find what's outdated

```bash
./gradlew dependencyUpdates -DoutputFormatter=plain,json --console=plain
```

Read the reports at `api/build/dependencyUpdates/report.{txt,json}` and
`application/build/dependencyUpdates/report.{txt,json}`; merge and dedupe
across the two modules.

The plugin's default revision includes stable releases, so "later milestone
versions" does not mean pre-releases — only versions with a qualifier
(alpha/beta/rc) are pre-releases.

## 2. Decide what can be bumped

- **Catalog library** — declared in `[libraries]` of `gradle/libs.versions.toml`
  → bumpable in the catalog.
- **Gradle plugin** — declared in `[plugins]` → bumpable in the catalog.
- **BOM-managed** — pinned by the Spring Boot BOM in `platform/application`
  (Spring Framework, r2dbc drivers, postgresql, byte-buddy, jspecify,
  junit-platform-launcher, ...) → never bump individually; they arrive with the
  next Spring Boot BOM bump.
- **Gradle wrapper** — `gradle/wrapper/gradle-wrapper.properties`.

## 3. Project constraints (do not cross)

- **thymeleaf**: the `runtimeOnly ':thymeleaf:...'` flat-dir jars in
  `application/build.gradle` are a pinned workaround (halo-dev/halo#7289) —
  never touch them.
- **lombok**: keep aligned with the `io.freefair.lombok` plugin; do not add a
  `lombok { version = ... }` override.
- **ben-manes Versions plugin itself**: stay on 0.54.0 — 0.59.0 fails to apply
  with a classloader error.
- **Pre-releases** (e.g. tika-core 4.0.0-beta-1): skip unless the user asks.
- Versions live only in `gradle/libs.versions.toml`, `gradle.properties`, or
  `ui/package.json` — never hard-code them (AGENTS.md).

## 4. Show the update plan

Group by category, current → latest with the semver delta (major/minor/patch),
state where each bump lands, and list excluded items with reasons.

## 5. Apply and verify

Edit `gradle/libs.versions.toml` — one line per bump — only after the user
approves the plan, then confirm the build:

```bash
./gradlew build
```

Re-run `dependencyUpdates` and check that the bumped items no longer appear as
outdated; what remains should be exactly the out-of-scope set.
