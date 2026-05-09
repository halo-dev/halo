# API Module — AGENTS.md

`java-library` module at `api/`. It defines the extension model, reactive interfaces, security abstractions, and service contracts used by `application` and external plugins.

Read this file together with the root [`AGENTS.md`](../AGENTS.md). If a task changes both contracts and implementations, also load [`application/AGENTS.md`](../application/AGENTS.md). If API or OpenAPI-visible DTOs affect the console or user center, also load [`ui/AGENTS.md`](../ui/AGENTS.md).

## Quick commands

```bash
./gradlew :api:compileJava
./gradlew :api:test
./gradlew :api:test --tests "*Xxx*"
./gradlew :api:spotlessApply
```

## When to work here

- service interfaces implemented in `application`
- extension model types and metadata contracts
- public security abstractions
- plugin-facing APIs and extension points
- shared DTOs or annotations that affect generated OpenAPI

Because plugins depend on this module, changes here are higher risk than ordinary backend refactors.

## Key packages

|                Package                |                                                    Purpose                                                     |
|---------------------------------------|----------------------------------------------------------------------------------------------------------------|
| `run.halo.app.extension`              | Kubernetes-style extension model: `Extension`, `Metadata`, `Scheme`, `ReactiveExtensionClient`, `Unstructured` |
| `run.halo.app.extension.router`       | REST router framework for extensions: `IListRequest`, `SortableRequest`, `QueryParamBuildUtil`                 |
| `run.halo.app.extension.controller`   | Controller pattern for reconciling extensions                                                                  |
| `run.halo.app.core.extension`         | Core extensions: `Post`, `Category`, `Tag`, `Comment`, `User`, `Role`, `Plugin`, `Theme`, `Setting`            |
| `run.halo.app.core.extension.service` | Core service interfaces such as `UserService`, `RoleService`, `PostService`                                    |
| `run.halo.app.infra`                  | Infrastructure abstractions                                                                                    |
| `run.halo.app.security`               | Security abstractions including `HaloOAuth2AuthenticationToken`                                                |
| `run.halo.app.plugin`                 | Plugin API and extension points                                                                                |
| `run.halo.app.theme`                  | Theme API                                                                                                      |
| `run.halo.app.content`                | Content abstractions                                                                                           |
| `run.halo.app.event`                  | Event types                                                                                                    |
| `run.halo.app.search`                 | Search abstractions                                                                                            |
| `run.halo.app.migration`              | Migration framework                                                                                            |
| `run.halo.app.notification`           | Notification abstractions                                                                                      |

## Extension model

Halo's core data model uses a Kubernetes-style extension system:

- **`Extension`** — all domain objects implement this and expose metadata, API version, and kind.
- **`Metadata`** — holds `name`, labels, annotations, version, and timestamps.
- **`Scheme`** — registers an extension type with its `GroupVersionKind`, Java type, and JSON schema.
- **`ReactiveExtensionClient`** — the generic CRUD entry point.
- **`Unstructured`** — fallback representation when the concrete type is unknown.

Example:

```java
@GVK(group = "content.halo.run", version = "v1alpha1", kind = "Post",
    singular = "post", plural = "posts")
public class Post extends AbstractExtension { ... }
```

## Service interfaces

Interfaces live here; implementations belong in `application`.

- `UserService` — prefer this over raw `ReactiveExtensionClient` for user creation and role wiring.
- `RoleService` — role CRUD and dependency resolution.
- `PostService` / `SinglePageService` — content publishing pipeline.
- `NotificationCenter` — notification dispatch.
- `ThemeService` / `PluginService` — theme and plugin lifecycle.

If an interface signature changes, expect follow-on changes in `application` and often in `ui`.

## OpenAPI and downstream impact

Changes in `api` can surface in generated docs and the UI API client. If you touch OpenAPI-visible DTOs, schema annotations, or route-facing types, regenerate the client from the repo root:

```bash
./gradlew generateOpenApiDocs
pnpm -C ui api-client:gen
```

## Test patterns

- Run focused tests with `./gradlew :api:test --tests "*NamePattern*"`.
- Add or update tests for any changed contract behavior.
- Prefer contract-level assertions that make downstream breakage obvious.

## Pitfalls

- **`@Schema(pattern = ...)` must stay inline.** Extracting the regex into a constant makes the generated OpenAPI contain the constant name instead of the pattern.
- **Rate limiter keys must not include user-controlled input.** Each unique key becomes its own limiter; use authenticated identity or client IP only.
- **Use `setName("prefix-" + UUID)`, not `setGenerateName()`.** Service implementations often read `metadata.name` before persistence; `generateName` is still null at that point.

## Boundaries

- ✅ **Always:** Keep API contracts minimal and stable; update downstream implementations and tests together; think about plugin compatibility before changing signatures.
- ⚠️ **Ask first:** Breaking changes to public APIs, plugin extension points, public annotations, or serialization shape.
- 🚫 **Never:** Hide a breaking API change behind silent defaults; change public contracts without updating `application`; edit generated UI client files instead of regenerating them.

