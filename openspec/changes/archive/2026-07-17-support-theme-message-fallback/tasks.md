## 1. Regression Coverage

- [x] 1.1 Add classpath Halo template/message fixtures and a focused `HaloTemplateEngine` test that reproduces the missing-message result when a temporary active theme overrides only the template.
- [x] 1.2 Add engine-level cases for key-level merging, theme-template precedence, theme-global precedence, and theme-base-bundle precedence over a locale-specific Halo value.
- [x] 1.3 Add engine-level cases for nested fragments, absent-message markers, missing Halo templates, non-theme resources, and malformed applicable message bundles.

## 2. Theme Message Fallback

- [x] 2.1 Pass the configured Thymeleaf template prefix, suffix, encoding, and resource-loading support from `TemplateEngineManager` into `ThemeMessageResolver`.
- [x] 2.2 Add normalized file-resource boundary detection so only templates selected from the active theme's `templates` directory are eligible.
- [x] 2.3 Resolve the same logical Halo template with the configured resource settings, require that template to exist, and load its template-adjacent locale bundles.
- [x] 2.4 Merge theme-global, Halo template, and selected theme template message maps in the specified key-level precedence order without adding a separate cache.

## 3. Verification

- [x] 3.1 Run the focused theme message resolver and template-engine tests, including the existing `ThemeMessageResolverIntegrationTest`.
- [x] 3.2 Run `./gradlew :application:spotlessCheck` and the relevant application test suite, then confirm `git diff --check` passes and only planned files changed.
