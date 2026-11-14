# Backend Application Guidelines

`application/` is the WebFlux + R2DBC backend: services, routers, security, migrations, and packaging. Everything here is reactive — never block threads.

## Structure

- `application/src/main/java/` — services, routers, endpoints, and configuration.
- `application/src/main/resources/application*.yaml` — environment configuration.
- `application/src/main/resources/db/migration/{h2,mariadb,mysql,postgresql}/` — per-dialect migrations.
- `application/src/main/resources/extensions/` — extension schemas and role templates.
- `application/src/test/` — backend tests.

## Commands

- `./gradlew :application:test` — run backend tests.
- `./gradlew :application:bootRun` — start the backend dev server.
- `./gradlew :application:copyUiDist` — embed the built UI into the JAR.
- `./gradlew generateOpenApiDocs` — emit the OpenAPI spec used to regenerate the UI client.

## Conventions

- Keep reactive flows non-blocking; no blocking I/O in request paths.
- Database migrations are versioned per dialect — never auto-generate them, and review them with the `application/` guide in mind.
- Tests: JUnit 5 with Spring Boot test support; name unit tests `XxxTest` and integration tests `XxxIntegrationTest`.
- After DTO, route, or schema changes, regenerate the UI client (`./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen`) and update `ui/` usage.

## Boundaries

- Ask before adding database migrations, changing security configuration, or introducing new dependencies.

