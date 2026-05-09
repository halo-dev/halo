# Platform Plugin BOM — AGENTS.md

`java-platform` module at `platform/plugin/`. This BOM is published as `run.halo.tools.platform:platform-plugin` and defines the dependency baseline that external Halo plugins consume.

Read this file together with the root [`AGENTS.md`](../../AGENTS.md). If a change affects the public API surface that plugin authors use, also load [`api/AGENTS.md`](../../api/AGENTS.md).

## Quick commands

```bash
./gradlew :platform:plugin:compileJava
```

This module has no source code; it only declares dependency constraints.

## Purpose

Plugin developers import this BOM so they get compatible versions automatically:

```groovy
dependencies {
    implementation platform('run.halo.tools.platform:platform-plugin:<version>')
    implementation 'run.halo.app:api'
}
```

## Key dependencies

|         Dependency         |                    Purpose                     |
|----------------------------|------------------------------------------------|
| `platform:application` BOM | Inherits the application-side version baseline |
| `project(':api')`          | Core Halo API consumed by plugins              |
| future plugin API modules  | Reserved for additional plugin-facing APIs     |

## Rules

- This BOM extends `:platform:application`, so upstream version changes flow into plugin builds.
- New plugin-facing API modules should be constrained here so downstream plugin builds stay versionless.
- Treat changes here as ecosystem changes, not local build tweaks.

## Boundaries

- ✅ **Always:** Preserve plugin compatibility and keep dependency declarations centralized.
- ⚠️ **Ask first:** Version or dependency changes that alter the plugin development contract.
- 🚫 **Never:** Add implementation-style dependencies or source logic to this `java-platform` module.

