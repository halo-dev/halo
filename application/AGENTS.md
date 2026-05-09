# Application Module — AGENTS.md

Executable Spring Boot WebFlux application at `application/`. Implements all service interfaces
from `api`, REST routers, security filters, and extension controllers.

## Build

```bash
./gradlew :application:compileJava                      # Compile only
./gradlew :application:test --tests "*PostService*"     # Run specific test class
./gradlew :application:test                             # Run all tests
./gradlew :application:spotlessApply                    # Format only application module
./gradlew :application:bootRun                          # Start dev server (port 8090)
```

## Key Packages

| Package | Purpose |
|---|---|
| `run.halo.app.core.extension.service.impl` | Service implementations: `UserServiceImpl`, `PostServiceImpl`, `RoleServiceImpl` |
| `run.halo.app.content` | Content management: post/page publishing, snapshots, contributors |
| `run.halo.app.security` | Security: authentication filters, OAuth2, authorization |
| `run.halo.app.security.authentication.oauth2` | OAuth2 login: `MapOAuth2AuthenticationFilter`, login handlers |
| `run.halo.app.extension.controller` | Extension controllers (reconcilers) |
| `run.halo.app.infra` | Infrastructure: system config, theme/plugin management |
| `run.halo.app.notification` | Notification implementation: `DefaultNotificationCenter` |
| `run.halo.app.plugin` | Plugin lifecycle: `PluginApplicationContextFactory`, extension point registration |
| `run.halo.app.theme` | Theme engine: template resolution, theme routes |
| `run.halo.app.search` | Search implementation (Lucene) |
| `run.halo.app.migration` | Database migration (r2dbc-migrate) |

## Service Implementation Pattern

All service implementations follow the same reactive pattern:

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

### Key service classes:
- `UserServiceImpl` — handles duplicate checking, `UserPreCreatingHandler`/`UserPostCreatingHandler` extension hooks, `grantRoles()`. Always use this for user creation, not raw `ReactiveExtensionClient`.
- `UserConnectionServiceImpl` — OAuth2 connection management: `createUserConnection()`.
- `DefaultNotificationCenter` — notification dispatch via `ReasonType` resolution.

## Security

### OAuth2 Authentication Flow

1. `MapOAuth2AuthenticationFilter` maps OAuth2 `OAuth2User` to Halo `UserDetails`
2. Auto-registration creates a new Halo user from OAuth2 attributes
3. `DefaultOAuth2LoginHandlerEnhancer` creates/updates `UserConnection`

### OAuth2 key classes:
- `MapOAuth2AuthenticationFilter.java` — central filter, maps OAuth2 to Halo user
- `OAuth2SecurityConfigurer.java` — registers filter in Spring Security chain
- `HaloOAuth2AuthenticationToken.java` (in `api`) — wraps OAuth2 token + UserDetails

### Pitfalls:
- Pre-auth check in `MapOAuth2AuthenticationFilter` MUST exclude `OAuth2AuthenticationToken` instances. Without `!(preAuth instanceof OAuth2AuthenticationToken)`, the OAuth2-only flow tries to bind to the OAuth2 user's "name" attribute as a Halo username.
- Use `UserService.createUser()`, not `ReactiveExtensionClient.create()` followed by manual `RoleBinding.create()`.
- Do NOT use `setGenerateName()` with `createUser()` — it returns null from `getName()`.

## REST Router Pattern

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
- `run.halo.app.core.extension.router` — core extension CRUD routes
- `run.halo.app.content.router` — content management endpoints
- `run.halo.app.security.router` — auth endpoints (login, token refresh)

## Config & Resources

| What | Path |
|---|---|
| Application YAML | `application/src/main/resources/application.yaml` |
| Extension point files | `application/src/main/resources/extensions/` |
| Role templates | `application/src/main/resources/extensions/role-template-*.yaml` |
| i18n resources | `application/src/main/resources/i18n/` |

## Test Patterns

```bash
# Run specific test class
./gradlew :application:test --tests "*ClassName*"

# Run tests matching a pattern (e.g., all OAuth2 tests)
./gradlew :application:test --tests "*oauth2*"

# Run multiple patterns
./gradlew :application:test --tests "*PostService*" --tests "*ContentService*"
```

Tests use JUnit 5 + Spring Boot Test + Mockito + `StepVerifier` for reactive chains.
Lombok available in test scope.

When testing OAuth2 auto-registration:
- Mock `UserService` with `when(userService.createUser(any(User.class), anySet()))`
- Use `ArgumentCaptor` to inspect created User fields
- Use `@MockitoSettings(strictness = Strictness.LENIENT)` to handle reactive chain stubbing
