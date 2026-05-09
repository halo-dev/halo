# Platform Plugin BOM — AGENTS.md

`java-platform` module at `platform/plugin/`. Bill of Materials for plugin development.
Published as `run.halo.tools.platform:platform-plugin`. Plugins import this BOM to get
compatible versions of the Halo API and other plugin dependencies.

## Build

```bash
./gradlew :platform:plugin:compileJava    # Compile only
```

This module has no source code — it's purely dependency constraint declarations.

## Purpose

Plugin developers import this BOM in their `build.gradle`:

```groovy
dependencies {
    implementation platform('run.halo.tools.platform:platform-plugin:<version>')
    implementation 'run.halo.app:api'
}
```

## Key Dependencies

| Dependency | Purpose |
|---|---|
| `platform:application` BOM | Inherits all application dependency versions |
| `project(':api')` | The Halo API library — core dependency for all plugins |
| (future) Plugin APIs | Reserved for other plugin-specific API dependencies |

## Pitfalls

- **Extends `platform:application`.** This BOM inherits from `:platform:application`, so any version
  change in the parent automatically applies to plugin builds.
- **New plugin APIs go here.** When adding a new plugin-related API module (e.g., `links-api`),
  add the version constraint here so plugin developers don't need to specify versions.
- **This is a `javaPlatform`, not a library.** It declares constraints, not implementations.
  Do not add `implementation`-style dependencies.
