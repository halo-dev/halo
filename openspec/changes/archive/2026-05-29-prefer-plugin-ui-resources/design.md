## Context

Plugin frontend bundles are currently discovered and served from the legacy `console` resource directory. That name matched the original Console-only integration, but Halo now also supports the UC platform and both platforms share plugin UI resources. The runtime needs to recognize `ui` as the preferred shared resource directory without breaking existing plugins that still package `console/main.js` and `console/style.css`.

The existing flow has three relevant surfaces:

- static asset serving under `/plugins/{name}/assets/console/**`;
- aggregated plugin bundles produced from each started plugin's `main.js` and `style.css`;
- `Plugin.status.entry` and `Plugin.status.stylesheet` links populated during reconciliation.

## Goals / Non-Goals

**Goals:**

- Add default static asset serving for plugin resources under `/plugins/{name}/assets/ui/**`.
- Prefer `ui` bundle resources over `console` bundle resources for aggregation.
- Keep the fallback to `console` for existing plugin packages.
- Keep bundle-directory selection consistent across JS, CSS, and plugin status URLs.

**Non-Goals:**

- Change the public plugin extension API or the `Plugin` schema.
- Regenerate OpenAPI clients.
- Change plugin build tooling defaults in this change.
- Alias `/assets/console/**` to `ui/**`; the existing route continues to serve legacy `console` resources.

## Decisions

### Directory selection is per plugin, not per file

Halo will select the first available bundle directory from this priority order:

1. `ui`
2. `console`

A directory is considered available when the plugin provides at least one known UI bundle resource in that directory, either `main.js` or `style.css`. Once `ui` is selected for a plugin, Halo will not aggregate or report `console` bundle files for that plugin.

This avoids mixed results such as `ui/main.js` with `console/style.css`, which would make runtime behavior depend on accidental partial packaging.

### Asset routes remain directory-specific

The existing `/plugins/{name}/assets/console/**` route will continue to resolve resources from `console/**`. A new `/plugins/{name}/assets/ui/**` route will resolve resources from `ui/**`.

This keeps old URLs stable and makes new `ui` URLs explicit. It also avoids surprising behavior where a URL containing `console` silently returns `ui` resources.

### Status URLs use the selected bundle directory

`Plugin.status.entry` and `Plugin.status.stylesheet` will be generated from the same selected directory used by bundle aggregation. If `ui` is selected, status URLs use `/plugins/{name}/assets/ui/main.js` and `/plugins/{name}/assets/ui/style.css`; otherwise they use the legacy `/assets/console/...` paths.

This makes status output reflect the actual runtime source and gives administrators and plugin tooling a clear signal about which resource layout is active.

## Risks / Trade-offs

- Partial `ui` packaging can hide legacy `console` resources for the same plugin. Mitigation: define selection at the directory level and cover it with tests so plugin authors get deterministic behavior.
- Plugins with dynamic chunks must emit URLs that match their packaged directory. Mitigation: this change adds `/assets/ui/**` serving, but does not rewrite old chunk public paths.
- Existing tests may assume only `console` bundle URLs. Mitigation: update focused tests to cover both `ui` priority and `console` fallback.

## Migration Plan

No data migration is required. Existing plugins continue to work through the `console` fallback. Plugins can migrate by packaging shared frontend resources under `resources/ui`; once `ui/main.js` or `ui/style.css` is present, Halo will use the `ui` directory for that plugin.

Rollback is straightforward: reverting the runtime change restores the old Console-only directory behavior, while plugins still packaged with `console` remain unaffected.
