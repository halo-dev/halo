## 1. Backend Theme UI Resources

- [x] 1.1 Add shared theme UI resource lookup for `ui/main.js`, `ui/style.css`, and arbitrary `ui/**` resources.
- [x] 1.2 Serve `/themes/{name}/ui/assets/**` from `{themeRoot}/{name}/ui/**` with directory traversal protection.
- [x] 1.3 Keep `/themes/{name}/assets/**` behavior unchanged for `templates/assets/**`.

## 2. Theme Status

- [x] 2.1 Add optional `entry` and `stylesheet` fields to `Theme.status`.
- [x] 2.2 Populate theme status bundle URLs from readable `ui/main.js` and `ui/style.css`.
- [x] 2.3 Include theme version cache parameters on status URLs when the theme version is available.
- [x] 2.4 Regenerate OpenAPI docs and the UI API client after the `Theme.status` schema update.

## 3. UI Plugin Bundle Loading

- [x] 3.1 Add aggregated UI plugin JS and CSS bundle endpoints under the Console API route.
- [x] 3.2 Include started plugin bundles and only the currently activated theme bundle.
- [x] 3.3 Keep the existing plugin bundle endpoints as compatibility aliases for the aggregated bundle.
- [x] 3.4 Generate `enabledUiPlugins` metadata that distinguishes `plugin` and `theme` entries.

## 4. Frontend Startup

- [x] 4.1 Load the aggregated UI plugin JS bundle.
- [x] 4.2 Register the active theme module as `theme:{themeName}` using the existing module initialization path.
- [x] 4.3 Load the aggregated UI plugin CSS bundle without failing startup when the bundle is empty or missing.
- [x] 4.4 Ensure theme activation entry points reload the page when the active theme can change.

## 5. Tests

- [x] 5.1 Add backend tests for theme UI static resource serving, missing resources, and traversal rejection.
- [x] 5.2 Add reconciler tests for `Theme.status.entry` and `Theme.status.stylesheet`.
- [x] 5.3 Add service and endpoint tests for aggregated UI plugin JS/CSS bundle loading and alias behavior.
- [x] 5.4 Add frontend tests for UI plugin module registration and startup error handling.

## 6. Validation

- [x] 6.1 Run `./gradlew spotlessApply`.
- [x] 6.2 Run focused backend tests for theme resource routing, reconciliation, and bundle endpoints.
- [x] 6.3 Run `pnpm -C ui typecheck && pnpm -C ui lint` after frontend changes.
- [x] 6.4 Run `openspec validate support-theme-ui-resources --strict`.
