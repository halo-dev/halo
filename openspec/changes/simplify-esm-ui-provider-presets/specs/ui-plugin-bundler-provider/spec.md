## MODIFIED Requirements

### Requirement: Theme provider build output

The theme provider SHALL configure IIFE or ESM build defaults to match the active-theme UI resource runtime contract while preserving native caller configuration as an advanced escape hatch.

#### Scenario: Theme Vite output defaults are generated

- **WHEN** a caller uses `viteConfig` with `provider: "theme"`
- **THEN** the helper SHALL configure production output to `dist`
- **THEN** IIFE output SHALL configure the Vite base URL as `/themes/{metadata.name}/ui-plugin/assets/`
- **THEN** ESM output SHALL emit relocatable provider-relative resource URLs
- **THEN** the helper SHALL emit the primary entry, styles, chunks, and provider manifest required by the selected format when the relevant defaults are not overridden

#### Scenario: Theme Rsbuild output defaults are generated

- **WHEN** a caller uses `rsbuildConfig` with `provider: "theme"`
- **THEN** the helper SHALL configure the output root to `dist`
- **THEN** IIFE output SHALL configure the public path as `/themes/{metadata.name}/ui-plugin/assets/`
- **THEN** ESM output SHALL derive asynchronous resource URLs from the loaded entry URL
- **THEN** the helper SHALL emit the primary entry, styles, chunks, and provider manifest required by the selected format when the relevant defaults are not overridden

#### Scenario: Theme builds with the supported Rsbuild toolchain

- **WHEN** a theme uses the Rsbuild 2 helper without adding a duplicate Vue plugin to caller configuration
- **THEN** the preset SHALL compile the theme with an Rsbuild Vue plugin compatible with that Rsbuild major
- **THEN** linked workspace development SHALL NOT inject an Rsbuild 1 preset plugin into the Rsbuild 2 compilation

#### Scenario: Theme IIFE output reuses legacy globals

- **WHEN** the theme provider selects IIFE format
- **THEN** the helper SHALL apply the legacy external dependency and global variable mappings used by plugin IIFE bundles

#### Scenario: Theme ESM output reuses shared specifiers

- **WHEN** the theme provider selects ESM format without caller overrides to the shared external configuration
- **THEN** the helper SHALL apply the same host runtime snapshot and ESM externalization defaults used by plugin ESM bundles

#### Scenario: User config overrides theme defaults

- **WHEN** a caller supplies native Vite or Rsbuild configuration
- **THEN** the helper SHALL merge the caller configuration after provider defaults using the bundler's normal merge semantics
- **THEN** the helper SHALL NOT reject, rewrite, or warn about the override merely because it conflicts with the default provider output contract

#### Scenario: User config changes the generated output contract

- **WHEN** caller configuration or a raw bundler hook changes the final format, entry, public path, external set, filenames, resources, or manifest consistency
- **THEN** correctness of the resulting artifact SHALL be the provider developer's responsibility
- **THEN** the bundler-kit SHALL NOT claim that the overridden output retains the supported default preset contract

### Requirement: Target Halo shared dependency validation

The bundler SHALL discover imported shared package roots and provide best-effort installed-version diagnostics against the sparse immutable host runtime snapshot selected for the target Halo version. Version diagnostics SHALL NOT be an admission gate for ESM output.

#### Scenario: Target snapshot is selected automatically

- **WHEN** automatic format selection chooses ESM
- **THEN** the bundler SHALL select the latest snapshot whose Halo baseline is not newer than the minimum Halo version derived from a simple stable version or `>=MAJOR.MINOR.PATCH` requirement

#### Scenario: Target is newer than the bundled snapshots

- **WHEN** the installed bundler does not contain a snapshot with the exact target Halo version but contains an older eligible snapshot
- **THEN** the bundler SHALL use the latest eligible older snapshot
- **THEN** it SHALL warn that the installed bundler contains only an older host dependency baseline

#### Scenario: Prerelease target reuses a snapshot

- **WHEN** the selected target is a prerelease whose stable core version has an exact snapshot
- **THEN** the bundler SHALL reuse that same-core snapshot without describing it as an older release
- **WHEN** the prerelease stable core version is newer than the selected snapshot baseline
- **THEN** the bundler SHALL warn that the older eligible snapshot is being reused

#### Scenario: No eligible ESM snapshot exists

- **WHEN** the installed bundler contains no snapshot whose baseline is compatible with the selected ESM target
- **THEN** the build SHALL fail with a diagnostic that identifies the target version and recommends a bundler update or IIFE output

#### Scenario: Imported shared root has an installed version

- **WHEN** syntax-aware import discovery observes a supported shared root and its package metadata can be resolved from the provider project
- **THEN** the bundler SHALL report the installed provider version and exact host version recorded by the selected snapshot
- **THEN** the supported root SHALL remain external according to the default preset

#### Scenario: Provider version is not newer within the host major

- **WHEN** the provider package and host snapshot have the same major version and the provider version is not newer than the host version
- **THEN** version difference alone SHALL NOT produce a compatibility note or fail the build

#### Scenario: Provider version is newer within the host major

- **WHEN** the provider package and host snapshot have the same major version and the provider version is newer than the host version
- **THEN** the bundler SHALL emit one best-effort compatibility note identifying the newer provider version
- **THEN** the version difference SHALL NOT fail the build or change externalization

#### Scenario: Provider and host major versions differ

- **WHEN** the provider package and host snapshot have different major versions
- **THEN** the bundler SHALL emit one best-effort compatibility note identifying the major-version difference
- **THEN** it SHALL NOT emit a second provider-newer note for the same package
- **THEN** the version difference SHALL NOT fail the build or change externalization

#### Scenario: Shared dependency exposes only an exports map

- **WHEN** a shared dependency has no `module`, `main`, or root `index.js` but provides valid package metadata through its package exports
- **THEN** the bundler SHALL use maintained Node-compatible package resolution when reading the installed version
- **THEN** it SHALL NOT infer or require a conventional package entry file

#### Scenario: Source contains import-like text

- **WHEN** a provider source comment or string literal contains text that resembles a static, re-export, side-effect, or dynamic import
- **THEN** the bundler SHALL ignore that text instead of treating it as a runtime dependency

#### Scenario: Provider imports a shared root namespace

- **WHEN** an ESM provider uses a namespace import or dynamically reads properties from a supported shared package root
- **THEN** the bundler SHALL treat it like any other import of that shared root
- **THEN** the bundler SHALL NOT attempt to prove individual namespace properties against the snapshot

#### Scenario: Provider imports a shared-package subpath

- **WHEN** an ESM provider imports a subpath below one of Halo's supported shared roots
- **THEN** the build SHALL fail because the host Import Map exposes only the exact root specifier

#### Scenario: Provider imports FormKit Core

- **WHEN** an ESM provider imports the `@formkit/core` package root directly or through a bundled FormKit subpackage
- **THEN** the default preset SHALL externalize that root to the same host graph used by `@formkit/vue`

#### Scenario: Provider imports another FormKit package

- **WHEN** an ESM provider imports an `@formkit/*` package other than `@formkit/vue` or `@formkit/core`
- **THEN** the default preset SHALL include that package in provider output while preserving external resolution of its `@formkit/core` runtime import

#### Scenario: Provider imports a non-shared dependency

- **WHEN** an ESM provider imports VueUse or another dependency not listed in the target Halo snapshot
- **THEN** the default preset SHALL include that dependency in the provider output

#### Scenario: Caller changes dependency resolution

- **WHEN** caller Vite or Rsbuild configuration aliases a shared dependency, changes externals, externalizes a non-shared dependency, or otherwise changes final bundler resolution
- **THEN** the helper SHALL preserve the caller configuration without validating the final resolved identity or browser mapping
- **THEN** compatibility and browser resolution of that overridden output SHALL be the provider developer's responsibility

#### Scenario: Provider imports editor internals directly

- **WHEN** an ESM provider imports `@tiptap/*` or `prosemirror-*` at runtime
- **THEN** the default preset SHALL keep those dependencies private even when the provider also uses `@halo-dev/richtext-editor`

#### Scenario: Required provider structure validation fails after target selection

- **WHEN** automatic or explicit selection has chosen ESM and a shared-root subpath or required manifest structure is invalid
- **THEN** the build SHALL fail
- **THEN** it SHALL NOT silently emit IIFE output

### Requirement: Provider build diagnostics

The bundler SHALL make format and best-effort shared dependency version decisions visible without adding build metadata to the runtime manifest.

#### Scenario: Provider build completes

- **WHEN** a plugin or theme provider build completes format selection
- **THEN** the build output SHALL identify the selected format and whether it was explicit, automatic, or an automatic IIFE fallback
- **THEN** an ESM build SHALL identify the target Halo version, selected snapshot baseline, and the provider and exact host versions available for imported shared roots

#### Scenario: Best-effort compatibility diagnostics are summarized

- **WHEN** a successful ESM build encounters a provider-newer or different-major shared dependency version
- **THEN** the bundler SHALL emit at most one deterministic compatibility-note block
- **THEN** version differences SHALL be grouped by shared dependency without duplicate warnings
- **THEN** the bundler SHALL NOT emit source-by-source namespace, static-export, editor-identity, alias, fork, or final-resolution compatibility diagnostics

### Requirement: Vite and Rsbuild ESM equivalence

Vite and Rsbuild provider helpers SHALL implement equivalent supported defaults for plugin and theme ESM providers. Equivalence SHALL NOT be claimed after caller overrides change those defaults.

#### Scenario: Equivalent provider is built with both default helpers

- **WHEN** equivalent provider source is built through Vite and Rsbuild without conflicting caller overrides
- **THEN** both outputs SHALL use the same manifest schema, host runtime snapshot, shared-root defaults, entry export contract, resource URL rules, and format selection semantics

#### Scenario: Default production ESM output is optimized

- **WHEN** a provider is built as ESM in production without caller optimization overrides
- **THEN** Vite and Rsbuild SHALL apply their native production JavaScript and CSS minimization
- **THEN** the output SHALL retain one startup JavaScript entry with the default `PluginModule` export
- **THEN** code requested through dynamic imports SHALL remain independently addressable and non-inlined assets SHALL use provider-root-safe URLs
- **THEN** asynchronous JavaScript, CSS, and non-inlined assets emitted by the default preset SHALL use content-hashed filenames

#### Scenario: Vite emits a deployable ESM entry

- **WHEN** Vite builds an ESM provider with its preset defaults
- **THEN** it SHALL treat the provider as a final browser entry rather than whitespace-preserving library distribution output
- **THEN** it SHALL preserve the entry module's export signature and apply the preset's relative resource and content-hash defaults

#### Scenario: ESM optimization preserves the IIFE contract

- **WHEN** the ESM production defaults are applied to the modern provider lane
- **THEN** Vite IIFE library output and Rsbuild IIFE window-library output SHALL retain their existing globals, filenames, startup CSS, and chunk behavior
- **THEN** an existing caller SHALL NOT need to change its `viteConfig` or `rsbuildConfig` invocation

#### Scenario: Caller overrides ESM preset output

- **WHEN** a caller changes dependency resolution, format, entry, public path, resource naming, optimization, or output through native configuration or bundler hooks
- **THEN** the helper SHALL merge that configuration after the preset without attempting to prove or restore the default ESM contract
- **THEN** the caller SHALL be responsible for manifest consistency, browser resolution, runtime identity, resource relocation, and cache invalidation

#### Scenario: Default Vite secondary resources are content hashed

- **WHEN** the Vite ESM preset emits an asynchronous chunk, asynchronous stylesheet, or non-inlined asset without caller filename overrides
- **THEN** its configured output pattern SHALL include a content-derived hash
- **THEN** the bundler-kit SHALL NOT inspect final assets or re-emit them to prove their filenames were content-derived

#### Scenario: Default Rsbuild secondary resources are content hashed

- **WHEN** the Rsbuild ESM preset emits an asynchronous chunk, asynchronous stylesheet, or non-inlined asset without caller filename overrides
- **THEN** its configured output pattern SHALL include a content-derived hash
- **THEN** the bundler-kit SHALL NOT inspect later Rspack compilation stages to prove the final resource name

#### Scenario: Rsbuild watches a development ESM provider

- **WHEN** a plugin or theme runs `rsbuild build --watch --env-mode=development` without overriding the preset resource path
- **THEN** the default helper SHALL preserve the automatic runtime public path required by relocatable ESM chunks and assets
- **THEN** the initial build and subsequent watched rebuilds SHALL emit the entry, chunks, startup style, and provider manifest without changing the caller invocation
