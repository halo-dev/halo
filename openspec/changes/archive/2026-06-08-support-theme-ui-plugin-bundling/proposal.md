## Why

Themes can now provide Console and User Center UI plugin bundles at runtime, but
`@halo-dev/ui-plugin-bundler-kit` still only provides plugin-oriented build
defaults. Theme authors need the same build helper to emit bundles that match
the theme runtime resource contract without hand-writing Vite or Rsbuild
details.

## What Changes

- Add a `provider?: "plugin" | "theme"` option to the modern Vite and Rsbuild
  config helpers, defaulting to `"plugin"` for compatibility.
- Keep existing plugin provider behavior unchanged, including plugin manifest
  defaults and legacy bundle location compatibility.
- Add theme provider defaults that read `../theme.yaml`, emit to `dist`, expose
  assets from `/themes/{name}/ui-plugin/assets/`, and publish the module under
  `theme:{name}`.
- Keep the deprecated `HaloUIPluginBundlerKit` helper unchanged and unsupported
  for theme providers.
- Document theme UI plugin project layout and minimal Vite/Rsbuild usage.
- Add focused tests for provider defaults and generated config shape.

## Capabilities

### New Capabilities

- `ui-plugin-bundler-provider`: Defines provider-specific build defaults for
  Halo UI plugin bundles produced by `@halo-dev/ui-plugin-bundler-kit`.

### Modified Capabilities

- None.

## Impact

- Affected package: `ui/packages/ui-plugin-bundler-kit`.
- Public API impact: non-breaking optional `provider` config on `viteConfig`
  and `rsbuildConfig`; default remains plugin behavior.
- Documentation impact: README examples for theme UI plugin projects.
- Test impact: package-level unit tests for Vite/Rsbuild config generation.
- No backend, database, OpenAPI, or generated API client changes are expected.
