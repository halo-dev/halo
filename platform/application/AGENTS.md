# Platform Application BOM — AGENTS.md

`java-platform` module at `platform/application/`. Bill of Materials that declares version
constraints for all application dependencies. Published as `run.halo.tools.platform:platform-application`.

## Build

```bash
./gradlew :platform:application:compileJava    # Compile only
```

This module has no source code — it's purely dependency constraint declarations.

## Purpose

Centralizes dependency versions so `api` and `application` modules don't need to repeat
version numbers. Applies the Spring Boot BOM via `SpringBootPlugin.BOM_COORDINATES`.

## Key Dependencies

See `platform/application/build.gradle` for the full constraint list. Key ones:

| Dependency | Purpose |
|---|---|
| Spring Boot BOM | Framework baseline (`org.springframework.boot:spring-boot-dependencies`) |
| Lucene bundles | Search engine |
| Resilience4j | Rate limiting / circuit breaker |
| PF4J | Plugin framework |
| Guava | Utility library |
| SpringDoc OpenAPI | API documentation generation |
| Bouncy Castle | Cryptographic operations |
| jsoup | HTML parsing |
| Thumbnailator | Image thumbnail generation |
| UA Parser | User-agent parsing |

## Pitfalls

- **Version constraints go here, not in `api/build.gradle` or `application/build.gradle`.**
  The `api` module uses `api platform(project(':platform:application'))` to import these constraints.
- **Adding a new dependency?** If it's used by both `api` and `application`, add the version
  constraint here first, then use the versionless dependency in `api/build.gradle`.
- **Upgrading a version?** If the version is pinned in `gradle/libs.versions.toml`, update it there.
  If only in this BOM, update the constraint directly.
