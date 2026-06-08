## ADDED Requirements

### Requirement: Provider selection

`@halo-dev/ui-plugin-bundler-kit` SHALL allow modern Vite and Rsbuild config
helpers to select the UI plugin provider type with `provider?: "plugin" |
"theme"`.

#### Scenario: Default provider remains plugin

- **WHEN** a caller invokes `viteConfig` or `rsbuildConfig` without `provider`
- **THEN** the helper SHALL use plugin provider defaults

#### Scenario: Theme provider is selected explicitly

- **WHEN** a caller invokes `viteConfig` or `rsbuildConfig` with `provider:
  "theme"`
- **THEN** the helper SHALL use theme provider defaults

#### Scenario: Legacy helper remains unchanged

- **WHEN** a caller uses the deprecated `HaloUIPluginBundlerKit` helper
- **THEN** the helper SHALL NOT provide theme provider behavior

### Requirement: Plugin provider compatibility

The plugin provider SHALL preserve existing Vite and Rsbuild helper behavior.

#### Scenario: Plugin manifest default is preserved

- **WHEN** a caller uses the plugin provider without `manifestPath`
- **THEN** the helper SHALL read `../src/main/resources/plugin.yaml`

#### Scenario: Plugin bundle defaults are preserved

- **WHEN** a caller uses the plugin provider
- **THEN** the helper SHALL keep existing plugin output, global name, externals,
  globals, and bundle location compatibility behavior

### Requirement: Theme provider manifest

The theme provider SHALL read the theme manifest by default and use its metadata
name to derive bundler defaults.

#### Scenario: Theme manifest default is used

- **WHEN** a caller uses the theme provider without `manifestPath`
- **THEN** the helper SHALL read `../theme.yaml`

#### Scenario: Theme manifest path can be overridden

- **WHEN** a caller uses the theme provider with `manifestPath`
- **THEN** the helper SHALL read the manifest at the provided path

#### Scenario: Theme module name is derived from metadata name

- **WHEN** the theme manifest has `metadata.name` equal to `earth`
- **THEN** the helper SHALL configure the bundle global module name as
  `theme:earth`

### Requirement: Theme provider build output

The theme provider SHALL configure build output to match the theme UI resource
runtime contract.

#### Scenario: Theme Vite output defaults are generated

- **WHEN** a caller uses `viteConfig` with `provider: "theme"`
- **THEN** the helper SHALL configure production output to `dist`
- **THEN** the helper SHALL configure the Vite base URL as
  `/themes/{metadata.name}/ui-plugin/assets/`
- **THEN** the helper SHALL emit `main.js` and `style.css` as the primary bundle
  files

#### Scenario: Theme Rsbuild output defaults are generated

- **WHEN** a caller uses `rsbuildConfig` with `provider: "theme"`
- **THEN** the helper SHALL configure the output root to `dist`
- **THEN** the helper SHALL configure the public path as
  `/themes/{metadata.name}/ui-plugin/assets/`
- **THEN** the helper SHALL emit `main.js` and `style.css` as the primary bundle
  files

#### Scenario: Theme provider reuses UI plugin externals

- **WHEN** a caller uses the theme provider
- **THEN** the helper SHALL apply the same external dependency and global
  variable mappings used by plugin UI bundles

#### Scenario: User config can override theme defaults

- **WHEN** a caller provides Vite or Rsbuild config that overrides provider
  defaults
- **THEN** the helper SHALL merge the caller config after provider defaults
