## Why

Plugins can already provide Console and UC frontend extensions from a shared `ui` bundle, but themes can only provide
frontend templates and static assets for the public site. Theme authors who need richer configuration screens, theme
detail panels, UC pages, or editor extensions currently have to ship a companion plugin even when the behavior belongs to
the theme.

Themes should be able to provide a Console/UC UI bundle using the same frontend module shape as plugins, while keeping
runtime loading scoped to the currently activated theme.

## What Changes

- Allow installed themes to package UI extension resources under a root-level `ui/` directory.
- Serve theme UI static resources from `/themes/{name}/ui/assets/**`, resolving files from `{themeRoot}/{name}/ui/**`.
- Expose `Theme.status.entry` and `Theme.status.stylesheet` when `ui/main.js` or `ui/style.css` exists.
- Load only the activated theme's UI bundle during Console and UC startup.
- Register the activated theme module using the frontend module name `theme:{themeName}`.
- Reuse the existing `PluginModule` frontend shape: `routes`, `ucRoutes`, `components`, and `extensionPoints`.

## Capabilities

### New Capabilities

- `theme-ui-resources`: Defines how Halo resolves, serves, reports, and loads theme-provided Console/UC UI resources.

### Modified Capabilities

None.

## Impact

- API model: adds optional `Theme.status.entry` and `Theme.status.stylesheet` fields.
- Backend: updates theme reconciliation, active theme bundle endpoints, and static theme UI resource handling.
- Security: adds a theme UI static resource route similar to plugin asset routes; API authorization remains unchanged.
- UI: updates startup module loading to load and register the activated theme module after plugin modules.
- OpenAPI/UI client: requires regenerating OpenAPI docs and the generated UI API client after `Theme.status` changes.
- Tooling/docs: theme-aware bundler output and developer documentation can land in follow-up PRs.
- Dependencies/database: no new dependencies and no database migration.
