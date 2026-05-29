## 1. Resource Resolution

- [x] 1.1 Add shared bundle-directory selection logic that prefers `ui` and falls back to `console` per plugin.
- [x] 1.2 Update plugin bundle resource lookup to resolve `main.js` and `style.css` from the selected directory.
- [x] 1.3 Add default static asset serving for `/plugins/{name}/assets/ui/**` while preserving `/plugins/{name}/assets/console/**`.

## 2. Plugin Status

- [x] 2.1 Update plugin reconciliation to detect the selected bundle directory before populating UI bundle status fields.
- [x] 2.2 Generate `Plugin.status.entry` and `Plugin.status.stylesheet` with `/assets/ui/...` when `ui` is selected and `/assets/console/...` when falling back.

## 3. Tests

- [x] 3.1 Add or update bundle resource tests for `ui` priority, `console` fallback, and directory-level skipping of `console`.
- [x] 3.2 Add or update route tests for serving plugin assets from `/assets/ui/**` and preserving `/assets/console/**`.
- [x] 3.3 Add or update reconciler tests for status entry and stylesheet URLs from both selected directories.

## 4. Validation

- [x] 4.1 Run `./gradlew :application:spotlessApply`.
- [x] 4.2 Run focused application tests for plugin bundle resources, plugin asset routes, and plugin reconciliation.
- [x] 4.3 Run `openspec validate prefer-plugin-ui-resources --strict`.
