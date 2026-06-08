## Context

Halo currently has two separate resource models that this change should keep distinct:

- Plugin UI resources are loaded from plugin bundle directories and served from `/plugins/{name}/assets/ui/**`.
- Theme public-site assets are served from `/themes/{name}/assets/**`, which maps to `templates/assets/**`.

Issue #9993 asks for themes to provide Console and UC extensions like plugins do. The theme capability should reuse the
existing frontend module contract, but it should not make every installed theme participate in Console/UC startup. Theme
UI is part of the active theme runtime, so only the activated theme should be loaded automatically.

## Goals / Non-Goals

**Goals:**

- Let themes package Console/UC UI bundles in a root-level `ui/` directory.
- Serve theme UI resources from a route that does not collide with existing theme public-site assets.
- Load only the activated theme's UI bundle during Console and UC startup.
- Reuse the existing `PluginModule` frontend module shape.
- Support dynamic chunks and other emitted assets by serving the entire `ui/**` resource tree.

**Non-Goals:**

- Load UI bundles from inactive themes automatically.
- Introduce separate Console and UC bundles for themes.
- Add a new frontend module contract distinct from `PluginModule`.
- Implement hot-swapping of theme UI modules after theme activation; page reload remains the activation boundary.
- Add theme-specific extension points such as `theme:self:tabs:create` in the initial runtime resource change.
- Update theme build tooling defaults in the same PR as the runtime support.

## Decisions

### Theme UI resources live in root-level `ui`

Theme packages will place UI extension resources under the theme root:

```text
ui/main.js
ui/style.css
ui/chunks/*.js
ui/assets/*
```

This mirrors the preferred plugin bundle directory while avoiding the theme `templates/assets/**` directory used by
public-site templates.

### Theme UI static resources use `/themes/{name}/ui/assets/**`

Halo will serve root-level theme UI files through:

```text
/themes/{name}/ui/assets/{*resource}
```

The route resolves to:

```text
{themeRoot}/{name}/ui/{resource}
```

This keeps the route stable for dynamic imports and emitted assets without occupying `/themes/{name}/assets/**`.

### Runtime loading is active-theme only through the UI plugin bundle

Console and UC startup will load one aggregated UI plugin bundle that includes started plugin bundles and, if present,
the activated theme's bundle. Inactive themes may have UI static files available by name, but they are not registered or
executed unless the theme is activated and the aggregated bundle includes it.

This keeps installed themes from injecting Console/UC routes, components, or extension points while they are not active.

The primary aggregated bundle endpoints will be:

```text
/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.js
/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle.css
```

The existing plugin bundle endpoints remain as compatibility aliases for the same aggregated content.

### One bundle serves both Console and UC

Themes will use the same `PluginModule` shape as plugins. A single theme UI bundle may export:

- `routes` for Console;
- `ucRoutes` for UC;
- `components`;
- `extensionPoints`.

Console and UC already select platform-specific routes from the same module shape, so a separate theme module contract is
unnecessary. The aggregated bundle exposes module metadata through `this.enabledUiPlugins`.

### Frontend registration uses `theme:{themeName}`

The activated theme module will be registered under `theme:{themeName}`. This prevents collisions with plugins and gives
future theme-specific extension points a stable lookup key for the current theme module.

### Bundle URLs are reported on theme status

`Theme.status.entry` and `Theme.status.stylesheet` will point to:

```text
/themes/{name}/ui/assets/main.js?v={version}
/themes/{name}/ui/assets/style.css?v={version}
```

Only existing readable files should be reported. Missing `main.js` or `style.css` should leave the corresponding field
unset.

### Page reload remains the theme activation boundary

Theme activation should keep using a page reload for Console/UC module replacement. Runtime unloading of the previously
active theme's routes, components, and extension points is out of scope.

## Risks / Trade-offs

- UI resources from inactive themes are addressable by name through the static route. This matches the plugin asset model
  and enables stable chunk URLs, but it means frontend code should not contain secrets. API authorization remains the real
  permission boundary.
- A theme can provide only JS or only CSS. Reporting and loading should handle either file independently.
- Dynamic chunks require the theme bundler public path to match `/themes/{name}/ui/assets/`. Runtime support can land
  before the bundler-kit update, but documentation should make the expected public path clear.
- Activation without reload can leave stale modules in memory. Mitigation: keep page reload as the supported activation
  behavior for theme UI bundles.

## Migration Plan

No data migration is required. Existing themes continue to work because the new `ui/` directory and routes are additive.

Theme authors can adopt the capability by packaging a root-level `ui/` directory and setting the bundler public path to
`/themes/{themeName}/ui/assets/`.

Rollback is straightforward: reverting the runtime loading and route changes leaves existing public-site theme resources
unchanged.
