# API Module — AGENTS.md

`java-library` module at `api/`. Defines the extension model, reactive interfaces, security
abstractions, and service contracts used by the `application` module and plugins.

## Build

```bash
./gradlew :api:compileJava              # Compile only
./gradlew :api:test                     # Run all tests
./gradlew :api:test --tests "*Xxx*"     # Run specific test
./gradlew :api:spotlessApply            # Format only API module
```

## Key Packages

| Package | Purpose |
|---|---|
| `run.halo.app.extension` | Kubernetes-style extension model: `Extension`, `Metadata`, `Scheme`, `ReactiveExtensionClient`, `Unstructured` |
| `run.halo.app.extension.router` | REST router framework for extensions: `IListRequest`, `SortableRequest`, `QueryParamBuildUtil` |
| `run.halo.app.extension.controller` | Controller pattern for reconciling extensions |
| `run.halo.app.core.extension` | Core extensions: `Post`, `Category`, `Tag`, `Comment`, `User`, `Role`, `Plugin`, `Theme`, `Setting` |
| `run.halo.app.core.extension.service` | Core service interfaces: `UserService`, `RoleService`, `PostService`, etc. |
| `run.halo.app.infra` | Infrastructure: `SystemSetting`, `SystemConfigurableEnvironmentFetcher` |
| `run.halo.app.security` | Security abstractions: `HaloOAuth2AuthenticationToken`, authorization interfaces |
| `run.halo.app.plugin` | Plugin API: `PluginApplicationContext`, extension points |
| `run.halo.app.theme` | Theme API: `ThemeSetting`, template interfaces |
| `run.halo.app.content` | Content abstractions: `Snapshot`, `ContentWrapper`, `Contributor` |
| `run.halo.app.event` | Event types (Spring `ApplicationEvent` subclasses) |
| `run.halo.app.search` | Search abstractions |
| `run.halo.app.migration` | Migration framework |
| `run.halo.app.notification` | Notification abstractions: `Reason`, `ReasonType`, `NotificationCenter` |

## Extension Model

Halo's core data model uses a Kubernetes-style extension system:

- **`Extension`** interface — all domain objects implement this. Provides `getMetadata()`, `getApiVersion()`, `getKind()`.
- **`Metadata`** — contains `name`, `labels`, `annotations`, `version`, `creationTimestamp`.
- **`Scheme`** — registers an Extension type with its `GroupVersionKind`, Java type, and JSON schema.
- **`ReactiveExtensionClient`** — the primary CRUD API: `create()`, `fetch()`, `update()`, `delete()`, `list()`.
- **`Unstructured`** — generic representation when the concrete type is unknown (e.g., plugin config).

### Custom Resource Patterns

```java
// Most extensions follow this pattern
@GVK(group = "content.halo.run", version = "v1alpha1", kind = "Post",
     singular = "post", plural = "posts")
public class Post extends AbstractExtension { ... }
```

## Service Interfaces

Services are defined in `api` as interfaces, implemented in `application`.
Key ones:

- `UserService` — user CRUD, role assignment, password management. Prefer over raw `ReactiveExtensionClient` for user operations (handles duplicate checking, extensions hooks).
- `RoleService` — role CRUD and dependency resolution.
- `PostService` / `SinglePageService` — content publishing pipeline.
- `NotificationCenter` — notification dispatch.
- `ThemeService` / `PluginService` — theme/plugin lifecycle.

## Pitfalls

- **`@Schema(pattern=...)` must be inline** in DTOs/extensions. Extracting the regex to a `static final String` puts the literal constant name (not the regex) into the generated OpenAPI spec.
- **Rate limiter keys** in `api` module abstractions must not include user-controlled parameters. Each unique key is an independent limiter. Use authenticated identity or client IP only.
- **`Extension` metadata name convention:** use `setName("prefix-" + UUID)` not `setGenerateName()`. Service implementations like `UserServiceImpl` call `grantRoles(user.getMetadata().getName())` on the original reference — `generateName` returns null until persisted.
