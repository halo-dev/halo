## Why

Halo originally loaded plugin frontend bundles from `resources/console`, but the later UC platform shares the same plugin UI resource mechanism with Console. The canonical resource directory should therefore be `resources/ui`, while existing plugins packaged with `resources/console` must continue to work.

## What Changes

- Serve plugin static assets from both `/plugins/{name}/assets/ui/**` and the existing `/plugins/{name}/assets/console/**`.
- Prefer plugin UI bundles from `ui/main.js` and `ui/style.css` when a plugin provides the `ui` directory.
- Fall back to `console/main.js` and `console/style.css` only when the plugin does not provide the `ui` bundle directory.
- Generate `Plugin.status.entry` and `Plugin.status.stylesheet` URLs from the selected bundle directory: `/assets/ui/...` when `ui` is selected, otherwise `/assets/console/...`.
- Preserve compatibility for plugins that still package UI resources under `resources/console`.

## Capabilities

### New Capabilities

- `plugin-ui-resources`: Defines how Halo resolves, serves, bundles, and reports plugin UI resource directories.

### Modified Capabilities

None.

## Impact

- Backend plugin runtime resource handling in `application/src/main/java/run/halo/app/plugin`.
- Plugin reconciliation status population in `application/src/main/java/run/halo/app/core/reconciler/PluginReconciler.java`.
- Plugin bundle aggregation in `PluginServiceImpl`.
- Backend tests for bundle resolution, asset routing, and plugin status URLs.
- No database migration, public Java API change, or generated OpenAPI client change is expected.
