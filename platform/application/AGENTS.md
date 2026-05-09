# Platform Application BOM — AGENTS.md

`java-platform` module at `platform/application/`. This BOM declares shared dependency constraints for the application-side modules and is published as `run.halo.tools.platform:platform-application`.

Read this file together with the root [`AGENTS.md`](../../AGENTS.md). If the dependency change affects code in `api` or `application`, also load the corresponding nested `AGENTS.md`.

## Quick commands

```bash
./gradlew :platform:application:compileJava
```

This module has no source code; changes here are dependency-constraint changes only.

## Purpose

- centralize dependency versions used by `api` and `application`
- import the Spring Boot BOM baseline
- keep downstream build files versionless wherever possible

## Key dependencies

See `platform/application/build.gradle` for the full constraint list. Important families include:

|    Dependency     |           Purpose            |
|-------------------|------------------------------|
| Spring Boot BOM   | Framework baseline           |
| Lucene            | Search engine                |
| Resilience4j      | Rate limiting and resilience |
| PF4J              | Plugin framework             |
| Guava             | Shared utilities             |
| SpringDoc OpenAPI | API documentation generation |
| Bouncy Castle     | Cryptography                 |
| jsoup             | HTML parsing                 |
| Thumbnailator     | Thumbnail generation         |
| UA Parser         | User-agent parsing           |

## Rules

- Put shared versions here instead of repeating them in `api/build.gradle` or `application/build.gradle`.
- If a version is already managed in `gradle/libs.versions.toml`, update it there instead of duplicating it here.
- If a dependency is shared by `api` and `application`, add the constraint here first, then use the dependency without an inline version downstream.

## Boundaries

- ✅ **Always:** Keep version management centralized and consistent with `gradle/libs.versions.toml`.
- ⚠️ **Ask first:** New shared libraries, major upgrades with compatibility risk, or BOM changes that affect plugin consumers indirectly.
- 🚫 **Never:** Scatter shared versions back into module build files; add implementation-style logic to this `java-platform` module.

