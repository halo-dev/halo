## Why

Plugins can already provide Console and UC frontend extensions from a shared `ui` bundle, but themes can only provide
frontend templates and static assets for the public site. Theme authors who need richer configuration screens, theme
detail panels, UC pages, or editor extensions currently have to ship a companion plugin even when the behavior belongs to
the theme.

Themes should be able to provide a Console/UC UI bundle using the same frontend module shape as plugins, while keeping
runtime loading scoped to the currently activated theme.

## What Changes

- Allow installed themes to package UI extension build output under `ui-plugin/dist/`.
- Serve theme UI static resources from `/themes/{name}/ui-plugin/assets/**`, resolving files from
  `{themeRoot}/{name}/ui-plugin/dist/**`.
- Expose `Theme.status.entry` and `Theme.status.stylesheet` when `ui-plugin/dist/main.js` or
  `ui-plugin/dist/style.css` exists.
- Load started plugin UI bundles and the activated theme UI bundle from a single `ui-plugins` aggregate endpoint during
  Console and UC startup.
- Register the activated theme module using the frontend module name `theme:{themeName}`.
- Reuse the existing `PluginModule` frontend shape: `routes`, `ucRoutes`, `components`, and `extensionPoints`.

## Capabilities

### New Capabilities

- `theme-ui-resources`: Defines how Halo resolves, serves, reports, and loads theme-provided Console/UC UI resources.

### Modified Capabilities

None.

## Impact

- API model: adds optional `Theme.status.entry` and `Theme.status.stylesheet` fields.
- Backend: updates theme reconciliation, aggregated UI plugin bundle endpoints, and static theme UI resource handling.
- Security: adds a theme UI static resource route similar to plugin asset routes; API authorization remains unchanged.
- UI: updates startup module loading to load and register started plugin modules and the activated theme module from the
  aggregated bundle.
- OpenAPI/UI client: requires regenerating OpenAPI docs and the generated UI API client after `Theme.status` changes.
- Tooling/docs: theme-aware bundler output and developer documentation can land in follow-up PRs.
- Dependencies/database: no new dependencies and no database migration.
