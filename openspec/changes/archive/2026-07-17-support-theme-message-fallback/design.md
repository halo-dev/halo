## Context

`TemplateEngineManager` creates one Thymeleaf engine per theme context. Its composite template resolver selects the first matching template resource, which lets an active theme override a Halo-provided template. `ThemeMessageResolver` then merges the theme-global `i18n` bundle with messages adjacent to that selected resource.

Because `StandardMessageResolver` only derives message resources from the selected `ITemplateResource`, selecting a theme file prevents it from seeing messages adjacent to the corresponding Halo classpath template. Real themes consequently copy Halo message bundles alongside overridden login, signup, logout, password-reset, challenge, and error templates.

Thymeleaf already invokes `resolveMessagesForTemplate` independently for each template in the template stack, including nested fragments, and caches messages according to template cacheability. The change should extend that existing seam without altering template selection.

## Goals / Non-Goals

**Goals:**

- Fill missing keys in a theme template bundle from the corresponding Halo template bundle.
- Preserve theme ownership of explicitly supplied values, including values from a less-specific theme locale bundle.
- Apply the behavior uniformly to top-level templates, fragments, and nested template paths.
- Preserve current theme-global message precedence, absent-message rendering, parsing failures, and engine cache invalidation.
- Keep the implementation local to application template-message resolution.

**Non-Goals:**

- Merging templates or changing the first-match behavior of `CompositeTemplateResolver`.
- Falling back to plugin message bundles or defining a plugin/theme message override contract.
- Adding theme metadata, feature flags, public APIs, dependencies, or frontend behavior.
- Changing the locale naming and fallback rules implemented by Thymeleaf.

## Decisions

### Merge messages in `ThemeMessageResolver`

`ThemeMessageResolver.resolveMessagesForTemplate` will remain the integration point. When the selected resource is an eligible active-theme template, it will resolve three maps in ascending precedence:

1. Theme-global `i18n` messages.
2. Messages adjacent to the corresponding Halo template resource.
3. Messages adjacent to the selected theme template resource.

Maps are merged by key, with later maps overwriting earlier maps. Both template-specific maps use `StandardMessageResolver` behavior, so each source independently resolves its base, language, country, and variant bundles before source precedence is applied.

This means a value in a theme base bundle overrides a value in a more locale-specific Halo bundle. The theme base bundle is the theme author's declared default for unsupported locales.

**Alternative considered:** Modify `CompositeTemplateResolver` to expose all matching template resources. This was rejected because templates still require first-match selection, plugins are explicitly out of scope, and only message resources need composition.

### Identify active-theme resources by their concrete path

Fallback is eligible only when the selected resource:

- is a file-backed Thymeleaf template resource; and
- has a normalized absolute path within the active theme's normalized `templates` directory.

The implementation will use `Path.startsWith` rather than a raw string prefix. Plugin classloader resources and files outside the active theme directory therefore do not participate.

**Alternative considered:** Introduce a theme-specific template resource type carrying an origin marker. This was rejected because it would expand a message-only change into the template-resolution layer.

### Require a corresponding Halo template

The resolver will construct the Halo template resource at the same logical template path using the configured Thymeleaf prefix, suffix, and encoding. It will merge Halo messages only if that template resource exists.

A properties file without a corresponding Halo template is not a fallback source. Missing locale-specific properties continue to be ignored by standard Thymeleaf resolution, while malformed or unreadable existing bundles retain standard parsing-error behavior.

**Alternative considered:** Read from a hard-coded `classpath:/templates/` path. This was rejected to avoid introducing a second source of truth for template prefix, suffix, and encoding.

### Preserve Thymeleaf template-stack and cache behavior

The resolver will continue returning one merged message map per logical template and locale. Thymeleaf remains responsible for visiting outer templates and nested fragments in template-stack order and caching messages only for cacheable templates.

No cross-theme or global message cache will be added. Clearing or replacing a theme's cached template engine continues to invalidate the associated message resolver and its cached maps.

### Test through a real template engine

Focused tests will assemble the actual `HaloTemplateEngine`, `ThemeMessageResolver`, a temporary theme file resolver, and a Halo classpath resolver. They will render templates and assert resulting text rather than testing only a standalone map utility.

Fixtures will cover full fallback, partial theme overrides, source-versus-locale precedence, nested fragments, ineligible resources, absent-message markers, and invalid message resources.

## Risks / Trade-offs

- [Theme templates with accidentally omitted keys will begin showing Halo text instead of missing-message markers] → This is the intended compatibility improvement; explicit theme values remain unchanged.
- [Path-based origin detection depends on Thymeleaf's file-backed resource type] → Limit the check to the known `FileTemplateResolver` used for active themes and cover non-theme resources with regression tests.
- [Constructing the Halo resource duplicates a small part of configured resource-name handling] → Centralize suffix and resource construction in a narrow helper and test nested paths plus configured resource attributes.
- [Additional resource existence and bundle reads add work on the first render for each locale] → Reuse Thymeleaf's existing per-template message cache and avoid a separate cache layer.
- [A malformed Halo bundle can now fail rendering a theme override that previously never read it] → Preserve fail-fast parsing because packaged Halo bundles are controlled build artifacts and should not fail silently.

## Migration Plan

No data or theme migration is required. Existing theme bundles keep their precedence. Theme authors may remove duplicated Halo keys after deploying a Halo version that contains this capability, but doing so is optional.

Rollback consists of reverting the resolver change; themes that removed duplicated keys would again need to provide complete bundles on older Halo versions.

## Open Questions

None.
