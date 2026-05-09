# Application Module — AGENTS.md

Executable Spring Boot WebFlux application at `application/`. This module implements the contracts from `api`, owns routers, security, migrations, plugin/theme runtime behavior, and packages the UI into the backend artifact.

Read this file together with the root [`AGENTS.md`](../AGENTS.md). If a change affects public contracts, also load [`api/AGENTS.md`](../api/AGENTS.md). If a change affects console or user-center payloads, auth flows, or generated OpenAPI, also load [`ui/AGENTS.md`](../ui/AGENTS.md).

## Quick commands

```bash
./gradlew :application:compileJava
./gradlew :application:test
./gradlew :application:test --tests "*PostService*"
./gradlew :application:spotlessApply
./gradlew :application:bootRun
./gradlew generateOpenApiDocs
```

## Cross-stack touchpoints

- `generateOpenApiDocs` is driven from this module and feeds the generated UI API client.
- `copyUiDist` pulls `ui/build/dist` into the backend resources during packaging.
- If request/response shape, auth behavior, or route semantics change here, assume the UI may need changes too.

## Key packages

|                    Package                    |                                         Purpose                                         |
|-----------------------------------------------|-----------------------------------------------------------------------------------------|
| `run.halo.app.core.extension.service.impl`    | Service implementations such as `UserServiceImpl`, `PostServiceImpl`, `RoleServiceImpl` |
| `run.halo.app.content`                        | Content management: publishing, snapshots, contributors                                 |
| `run.halo.app.security`                       | Security filters, OAuth2, authorization                                                 |
| `run.halo.app.security.authentication.oauth2` | OAuth2 login flow                                                                       |
| `run.halo.app.extension.controller`           | Extension reconcilers and controllers                                                   |
| `run.halo.app.infra`                          | System config, theme/plugin management                                                  |
| `run.halo.app.notification`                   | `DefaultNotificationCenter` and related behavior                                        |
| `run.halo.app.plugin`                         | Plugin lifecycle and extension registration                                             |
| `run.halo.app.theme`                          | Theme engine and route integration                                                      |
| `run.halo.app.search`                         | Lucene-backed search                                                                    |
| `run.halo.app.migration`                      | `r2dbc-migrate` integration                                                             |

## Service implementation pattern

Most services follow the same reactive shape:

```java
@Component
public class XxxServiceImpl implements XxxService {
    private final ReactiveExtensionClient client;

    @Override
    public Mono<Xxx> doSomething(String name) {
        return client.fetch(Xxx.class, name)
            .switchIfEmpty(Mono.error(new ExtensionNotFoundException(name, Xxx.class)))
            .flatMap(xxx -> {
                // mutate
                return client.update(xxx);
            });
    }
}
```

Key service reminders:

- `UserServiceImpl` owns duplicate checks, create hooks, and role grants. Use it instead of raw `ReactiveExtensionClient` for user creation.
- `UserConnectionServiceImpl` owns OAuth2 connection persistence.
- `DefaultNotificationCenter` resolves `ReasonType` and dispatches notifications.

## Security

### OAuth2 authentication flow

1. `MapOAuth2AuthenticationFilter` maps `OAuth2User` to Halo `UserDetails`.
2. Auto-registration creates the Halo user from OAuth2 attributes.
3. `DefaultOAuth2LoginHandlerEnhancer` creates or updates `UserConnection`.

Key classes:

- `MapOAuth2AuthenticationFilter.java`
- `OAuth2SecurityConfigurer.java`
- `HaloOAuth2AuthenticationToken.java` in `api`

Pitfalls:

- The pre-auth check in `MapOAuth2AuthenticationFilter` must exclude `OAuth2AuthenticationToken` instances.
- Use `UserService.createUser()`, not raw extension creation plus manual role binding.
- Do not use `setGenerateName()` with `createUser()` because the name is still null when downstream code reads it.

## REST router pattern

Routers use Spring WebFlux functional endpoints:

```java
@Component
public class PostRouter implements RouterFunction<ServerResponse> {
    @Override
    public RouterFunction<ServerResponse> route() {
        return RouterFunctions.route()
            .GET("/posts", this::list)
            .POST("/posts", this::create)
            .build();
    }
}
```

Router packages:

- `run.halo.app.core.extension.router`
- `run.halo.app.content.router`
- `run.halo.app.security.router`

## Config and resources

|       What        |                               Path                               |
|-------------------|------------------------------------------------------------------|
| Application YAML  | `application/src/main/resources/application.yaml`                |
| Extension schemas | `application/src/main/resources/extensions/`                     |
| Role templates    | `application/src/main/resources/extensions/role-template-*.yaml` |
| i18n resources    | `application/src/main/resources/i18n/`                           |
| DB migrations     | `application/src/main/resources/db/migration/`                   |

## Test patterns

```bash
./gradlew :application:test --tests "*ClassName*"
./gradlew :application:test --tests "*oauth2*"
./gradlew :application:test --tests "*PostService*" --tests "*ContentService*"
```

Tests use JUnit 5, Spring Boot Test, Mockito, and `StepVerifier`.

When testing OAuth2 auto-registration:

- mock `UserService.createUser(any(User.class), anySet())`
- inspect created users with `ArgumentCaptor`
- use lenient Mockito settings only when the reactive setup truly requires it

## Boundaries

- ✅ **Always:** Preserve reactive, non-blocking behavior; update API contracts and UI consumers together when backend behavior changes; run focused backend tests for the touched area.
- ⚠️ **Ask first:** Security model changes, migration scripts, plugin preset behavior, or anything that breaks public API assumptions.
- 🚫 **Never:** Add blocking I/O in the request path; bypass `UserService` for user creation flows; change generated OpenAPI behavior without considering UI regeneration.

