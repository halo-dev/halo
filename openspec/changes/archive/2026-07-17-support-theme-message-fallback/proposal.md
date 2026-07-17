## Why

Themes can override Halo-provided Thymeleaf templates, but message resolution only reads bundles adjacent to the selected theme resource. A theme must therefore copy every Halo-provided message bundle even when it changes only the markup or a single message, otherwise unresolved message expressions appear in the rendered page.

## What Changes

- Add key-level message fallback for active-theme templates that override a Halo-provided template at the same logical path.
- Merge template messages so theme template bundles override Halo template bundles, while missing theme keys fall back to Halo.
- Apply the behavior independently to page templates, fragments, and templates in nested directories.
- Preserve existing theme-global message precedence, locale fallback behavior, absent-message markers, and template-engine cache lifecycle.
- Exclude plugin templates and unrelated template resolvers from the fallback contract.

## Capabilities

### New Capabilities

- `theme-message-fallback`: Defines message lookup and precedence when an active theme overrides a Halo-provided Thymeleaf template.

### Modified Capabilities

None.

## Impact

- Affects the application theme engine, primarily `ThemeMessageResolver` and its construction in `TemplateEngineManager`.
- Adds focused template-engine tests and classpath template/message fixtures.
- Does not change public APIs, theme metadata, plugin contracts, database schemas, security behavior, frontend code, or dependencies.
- Existing themes remain compatible; duplicated message keys continue to win, while previously missing keys begin using Halo-provided values.
