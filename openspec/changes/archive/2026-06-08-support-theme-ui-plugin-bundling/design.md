## Context

The runtime support for theme-provided Console and User Center UI plugin bundles
uses the same frontend `PluginModule` shape as plugins, but serves theme build
output from a different resource model:

```text
{themeRoot}/{themeName}/ui-plugin/dist/**
```

and exposes those assets from:

```text
/themes/{themeName}/ui-plugin/assets/**
```

`@halo-dev/ui-plugin-bundler-kit` currently assumes plugin projects. It reads a
plugin manifest, emits plugin bundle files, and configures plugin asset public
paths. Theme authors can work around this manually today, but the package should
provide first-class defaults that match the runtime contract.

## Goals / Non-Goals

**Goals:**

- Support plugin and theme providers through one modern configuration API.
- Keep existing plugin helper behavior compatible by default.
- Provide theme defaults for manifest path, output directory, module name, and
  asset public path.
- Support both Vite and Rsbuild helpers.
- Cover the provider defaults with focused unit tests and README examples.

**Non-Goals:**

- Update the deprecated `HaloUIPluginBundlerKit` Vite plugin.
- Auto-detect provider type from nearby manifest files.
- Support arbitrary theme project layouts beyond existing manifest and bundler
  override hooks.
- Change the runtime theme UI resource endpoints or frontend module contract.
- Add new dependencies.

## Decisions

### Use `provider?: "plugin" | "theme"`

The modern Vite and Rsbuild config helpers will accept an optional `provider`
field. The default is `"plugin"` so existing calls remain valid:

```ts
viteConfig({ vite: {} });
rsbuildConfig({ rsbuild: {} });
```

Theme projects opt in explicitly:

```ts
viteConfig({ provider: "theme", vite: {} });
rsbuildConfig({ provider: "theme", rsbuild: {} });
```

Alternative considered: separate `themeViteConfig` and `themeRsbuildConfig`
exports. Separate exports are explicit, but they split what is still one UI
plugin bundling API. The provider field better reflects that plugin and theme
are two providers for the same frontend module contract.

### Do not auto-detect the provider

The helper will not switch behavior based on whether `theme.yaml` or
`plugin.yaml` exists. Auto-detection would make builds sensitive to directory
layout and could surprise existing plugin projects. Explicit provider selection
keeps behavior predictable.

### Keep legacy helper unchanged

The deprecated `HaloUIPluginBundlerKit` helper will not gain theme support. It
still carries old production output compatibility for plugin bundles, and
extending it would increase the compatibility matrix while encouraging new
theme projects to adopt deprecated API.

### Use provider-specific manifest defaults

Plugin provider keeps the existing default:

```text
../src/main/resources/plugin.yaml
```

Theme provider defaults to:

```text
../theme.yaml
```

This assumes the recommended theme UI plugin project layout:

```text
theme-root/
├── theme.yaml
└── ui-plugin/
    ├── package.json
    ├── src/index.ts
    └── vite.config.ts
```

The existing `manifestPath` option remains available for non-standard layouts.

### Define a minimal theme manifest type

The bundler only needs the theme metadata name to generate the module name and
asset public path. It should parse theme manifests using a small internal type
instead of depending on a full generated API-client `Theme` model.

### Theme provider uses fixed runtime paths

Theme provider does not inspect `spec.requires` or use plugin bundle location
compatibility. Theme UI support is a new runtime feature with one resource
location: `ui-plugin/dist/**`.

Theme provider defaults:

- output root: `dist`
- module/global name: `theme:{metadata.name}`
- Vite `base`: `/themes/{metadata.name}/ui-plugin/assets/`
- Rsbuild `output.publicPath`: `/themes/{metadata.name}/ui-plugin/assets/`

User config still merges after presets, so advanced projects can override these
values. The README should make clear that overriding the public path can break
dynamic chunks and emitted assets.

### Reuse existing externals and globals

Theme UI bundles run in the same Console/UC environment and use the same
`PluginModule` contract as plugins. They should reuse the existing external
dependency and global mapping defaults to avoid duplicate Vue/runtime copies and
keep output size consistent with plugin UI bundles.

## Risks / Trade-offs

- Public path can be overridden incorrectly -> Document the expected theme
  public path and cover defaults with tests.
- Provider defaults increase config branching -> Keep provider-specific logic in
  small helpers and leave legacy API untouched.
- Theme manifest parsing may accept malformed manifests until build config
  generation fails -> Validate the minimal fields needed by the bundler and let
  Halo runtime continue to own full theme compatibility checks.
- Package-level tests may require adding a test script to this workspace package
  -> Use the existing workspace Vitest tooling, not a new dependency.
