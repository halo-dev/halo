# API Library Guidelines

`api/` is the public API library: the extension model, service contracts, and security abstractions consumed by the application and by plugins. Changes here affect downstream compatibility.

## Scope

- Source: `api/src/main/java/run/halo/app/` (core, extension, plugin, security, migration, theme, and related packages).
- Tests: `api/src/test/`.

## Commands

- `./gradlew :api:test` — run API library tests.
- `./gradlew spotlessApply` — format Java and Markdown.

## Conventions

- Java 21, 4-space indentation, formatted with Spotless.
- The API is a compatibility surface: prefer additive changes, and flag any breaking signature or contract change for review before merging.
- Extension definitions and schemas live in `application/src/main/resources/extensions/`; when you change them, update `application/` and `ui/` together.
- Tests: JUnit 5, named `XxxTest` (unit) or `XxxIntegrationTest` (integration).

## Boundaries

- Public API changes in `api/` require explicit approval — treat them as affecting every plugin built against Halo.

