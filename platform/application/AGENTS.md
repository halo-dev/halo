# Application BOM Guidelines

`platform/application/` is a Java platform BOM constraining dependencies shared by the backend.

- Declare shared dependency versions in `gradle/libs.versions.toml` and reference them from `build.gradle`; add constraints here only when a dependency is used across backend modules.
- Changes ripple through the whole backend build — verify with `./gradlew build`.
- New shared dependencies require approval before merging.

