## MODIFIED Requirements

### Requirement: Plugin provider compatibility

The plugin provider SHALL preserve legacy output behavior when IIFE format is selected and SHALL select modern ESM output from the plugin manifest's Halo version requirement when automatic format selection is used.

#### Scenario: Plugin manifest default is preserved

- **WHEN** a caller uses the plugin provider without `manifestPath`
- **THEN** the helper SHALL read `../src/main/resources/plugin.yaml`

#### Scenario: Plugin automatically targets ESM

- **WHEN** a caller uses automatic format selection and `plugin.yaml` `spec.requires` is a simple stable version or `>=MAJOR.MINOR.PATCH` requirement whose minimum is 2.26.0 or newer
- **THEN** the helper SHALL configure ESM provider output

#### Scenario: Plugin automatically targets IIFE

- **WHEN** a caller uses automatic format selection and `spec.requires` is absent, invalid, wildcard, composite, otherwise unsupported, or permits a stable Halo version older than 2.26.0
- **THEN** the helper SHALL configure legacy IIFE provider output
- **THEN** an unparseable or unsupported range SHALL produce a warning explaining the automatic fallback

#### Scenario: Plugin selects IIFE explicitly

- **WHEN** a caller explicitly selects IIFE format
- **THEN** the helper SHALL keep existing plugin output, global name, externals, globals, and bundle location compatibility behavior

### Requirement: Theme provider manifest

The theme provider SHALL read the theme manifest by default and use its metadata name and required Halo version to derive bundler defaults.

#### Scenario: Theme manifest default is used

- **WHEN** a caller uses the theme provider without `manifestPath`
- **THEN** the helper SHALL read `../theme.yaml`

#### Scenario: Theme manifest path can be overridden

- **WHEN** a caller uses the theme provider with `manifestPath`
- **THEN** the helper SHALL read the manifest at the provided path

#### Scenario: Theme module name is derived from metadata name

- **WHEN** the theme manifest has `metadata.name` equal to `earth`
- **THEN** the helper SHALL configure the provider module name as `theme:earth`

#### Scenario: Theme format uses required Halo version

- **WHEN** the theme provider uses automatic format selection
- **THEN** the helper SHALL apply the same `spec.requires` threshold rules used by the plugin provider

### Requirement: Theme provider build output

The theme provider SHALL configure IIFE or ESM build output to match the active-theme UI resource runtime contract.

#### Scenario: Theme Vite output defaults are generated

- **WHEN** a caller uses `viteConfig` with `provider: "theme"`
- **THEN** the helper SHALL configure production output to `dist`
- **THEN** IIFE output SHALL configure the Vite base URL as `/themes/{metadata.name}/ui-plugin/assets/`
- **THEN** ESM output SHALL emit relocatable provider-relative resource URLs
- **THEN** the helper SHALL emit the primary entry, styles, chunks, and provider manifest required by the selected format

#### Scenario: Theme Rsbuild output defaults are generated

- **WHEN** a caller uses `rsbuildConfig` with `provider: "theme"`
- **THEN** the helper SHALL configure the output root to `dist`
- **THEN** IIFE output SHALL configure the public path as `/themes/{metadata.name}/ui-plugin/assets/`
- **THEN** ESM output SHALL derive asynchronous resource URLs from the loaded entry URL
- **THEN** the helper SHALL emit the primary entry, styles, chunks, and provider manifest required by the selected format

#### Scenario: Theme builds with the supported Rsbuild toolchain

- **WHEN** a theme uses the Rsbuild 2 helper without adding a duplicate Vue plugin to caller configuration
- **THEN** the preset SHALL compile the theme with an Rsbuild Vue plugin compatible with that Rsbuild major
- **THEN** linked workspace development SHALL NOT inject an Rsbuild 1 preset plugin into the Rsbuild 2 compilation

#### Scenario: Theme IIFE output reuses legacy globals

- **WHEN** the theme provider selects IIFE format
- **THEN** the helper SHALL apply the legacy external dependency and global variable mappings used by plugin IIFE bundles

#### Scenario: Theme ESM output reuses shared specifiers

- **WHEN** the theme provider selects ESM format
- **THEN** the helper SHALL apply the same host runtime snapshot and ESM externalization rules used by plugin ESM bundles

#### Scenario: User config overrides theme defaults consistently

- **WHEN** a caller provides Vite or Rsbuild configuration that does not conflict with the selected provider format, manifest, resource root, or shared dependency contract
- **THEN** the helper SHALL merge the caller configuration after provider defaults

#### Scenario: User config conflicts with generated output contract

- **WHEN** a caller override causes the final bundle format, entry, public path, external set, or emitted manifest to disagree with the selected provider contract
- **THEN** the build SHALL fail with a diagnostic describing the conflicting setting

## ADDED Requirements

### Requirement: Provider format selection

The modern Vite and Rsbuild helpers SHALL support `auto`, `iife`, and `esm` provider format selection with `auto` as the default.

#### Scenario: IIFE is selected for a compatible modern target

- **WHEN** a caller explicitly selects `iife` and the provider requires Halo 2.26.0 or newer
- **THEN** the helper SHALL emit legacy IIFE output as a migration escape hatch

#### Scenario: Explicit ESM metadata permits an older target

- **WHEN** a caller explicitly selects `esm` but the provider's Halo version range permits a release that does not support ESM UI providers
- **THEN** the helper SHALL emit a strong compatibility warning
- **THEN** the helper SHALL NOT rewrite the provider manifest or silently select IIFE

#### Scenario: Explicit ESM target cannot be derived

- **WHEN** a caller explicitly selects `esm` and `spec.requires` does not yield a supported target Halo version
- **THEN** the helper SHALL require an explicit `targetHaloVersion` build option before selecting a host runtime snapshot

#### Scenario: Explicit IIFE ignores target parsing

- **WHEN** a caller explicitly selects `iife`
- **THEN** the helper SHALL preserve legacy output without requiring `spec.requires` or a target snapshot

#### Scenario: ESM is tested against a Halo prerelease

- **WHEN** a caller explicitly selects `esm` for a 2.26 prerelease that supplies the ESM runtime
- **THEN** the helper SHALL allow the build after validating the explicit target snapshot
- **THEN** automatic selection SHALL continue using the stable 2.26.0 threshold

### Requirement: Target Halo shared dependency validation

The bundler SHALL validate a provider project's actually resolved shared dependency packages against the sparse immutable host runtime snapshot selected for its target Halo version before externalizing them.

#### Scenario: Target snapshot is selected automatically

- **WHEN** automatic format selection chooses ESM
- **THEN** the bundler SHALL select the latest snapshot whose Halo baseline is not newer than the minimum Halo version derived from a simple stable version or `>=MAJOR.MINOR.PATCH` requirement

#### Scenario: Target is newer than the bundled snapshots

- **WHEN** the installed bundler does not contain a snapshot with the exact target Halo version but contains an older eligible snapshot
- **THEN** the bundler SHALL use the latest eligible older snapshot
- **THEN** it SHALL warn that newly introduced target exports require a bundler update

#### Scenario: Prerelease target reuses a snapshot

- **WHEN** the selected target is a prerelease whose stable core version has an exact snapshot
- **THEN** the bundler SHALL reuse that same-core snapshot without describing it as an older release
- **WHEN** the prerelease stable core version is newer than the selected snapshot baseline
- **THEN** the bundler SHALL warn that the older eligible snapshot is being reused

#### Scenario: No eligible ESM snapshot exists

- **WHEN** the installed bundler contains no snapshot whose baseline is compatible with the selected ESM target
- **THEN** the build SHALL fail with a diagnostic that identifies the target version and recommends a bundler update or IIFE output

#### Scenario: Resolved dependency matches the expected root

- **WHEN** the package resolved from the provider project root has the expected package name and owning package root
- **THEN** the bundler SHALL allow the shared package to remain external after validating its statically imported exports

#### Scenario: Resolved dependency version differs from the host baseline

- **WHEN** a shared package's actual version differs from the exact host version recorded by the snapshot
- **THEN** the bundler SHALL allow externalization when concrete root and export checks pass
- **THEN** it SHALL warn for a newer provider version and emit a stronger warning when the major version differs

#### Scenario: Resolved dependency identity is invalid

- **WHEN** a shared dependency is missing, resolves through an alias or fork, or bypasses the expected package root
- **THEN** the ESM build SHALL fail and identify the dependency, resolved source and version when available, exact host version, target snapshot, and IIFE remediation

#### Scenario: Package declaration differs from actual resolution

- **WHEN** a package declaration range or lockfile text differs from the package actually resolved from the provider project root
- **THEN** the bundler SHALL validate the actually resolved package
- **THEN** it SHALL use the declaration and lockfile only as diagnostic context

#### Scenario: Shared dependency exposes only an exports map

- **WHEN** a shared dependency has no `module`, `main`, or root `index.js` but provides a valid package-root `exports` entry
- **THEN** the bundler SHALL resolve its installed package metadata using maintained Node-compatible package resolution
- **THEN** it SHALL NOT infer or require a conventional package entry file

#### Scenario: Provider imports an unsupported export

- **WHEN** an ESM provider imports a package subpath or static runtime export that is absent from the target snapshot
- **THEN** the build SHALL fail before externalizing that import

#### Scenario: Source contains import-like text

- **WHEN** a provider source comment or string literal contains text that resembles a static, re-export, side-effect, or dynamic import
- **THEN** the bundler SHALL ignore that text instead of treating it as a runtime dependency

#### Scenario: Provider uses a namespace import

- **WHEN** an ESM provider uses a namespace import or dynamically reads properties from a shared package namespace
- **THEN** the bundler SHALL allow the build after validating the package root
- **THEN** it SHALL warn that individual runtime properties could not be proven against the snapshot

#### Scenario: Provider imports FormKit Core

- **WHEN** an ESM provider imports the `@formkit/core` package root directly or through a bundled FormKit subpackage
- **THEN** the bundler SHALL externalize that root to the same host graph used by `@formkit/vue`

#### Scenario: Provider imports another FormKit package

- **WHEN** an ESM provider imports an `@formkit/*` package other than `@formkit/vue` or `@formkit/core`
- **THEN** the bundler SHALL include that package in provider output while preserving external resolution of its `@formkit/core` runtime import

#### Scenario: Provider imports a non-shared dependency

- **WHEN** an ESM provider imports VueUse or another dependency not listed in the target Halo snapshot
- **THEN** the bundler SHALL include that dependency in the provider output

#### Scenario: Caller externalizes a non-shared dependency

- **WHEN** caller Vite or Rsbuild configuration externalizes a dependency that is not a Halo shared root
- **THEN** the ESM build SHALL fail with a diagnostic that identifies the unsupported external
- **THEN** the build SHALL NOT emit a manifest containing a browser-unresolvable bare import

#### Scenario: Provider imports editor internals directly

- **WHEN** an ESM provider imports `@tiptap/*` or `prosemirror-*` at runtime while also using `@halo-dev/richtext-editor`
- **THEN** the bundler SHALL keep the direct dependency private and warn that editor class identity and cross-version behavior are best-effort

#### Scenario: ESM validation fails after target selection

- **WHEN** automatic or explicit selection has chosen ESM and dependency, import, or output validation fails
- **THEN** the build SHALL fail
- **THEN** it SHALL NOT silently emit IIFE output

### Requirement: Provider build diagnostics

The bundler SHALL make format and shared dependency decisions visible without adding build metadata to the runtime manifest.

#### Scenario: Provider build completes

- **WHEN** a plugin or theme provider build completes format selection
- **THEN** the build output SHALL identify the selected format and whether it was explicit, automatic, or an automatic IIFE fallback
- **THEN** an ESM build SHALL identify the target Halo version, selected snapshot baseline, and each shared package's provider and exact host versions

#### Scenario: Best-effort compatibility diagnostics are summarized

- **WHEN** a successful ESM build encounters version drift, namespace or dynamic imports, or an editor identity boundary that cannot be fully validated
- **THEN** the bundler SHALL emit at most one deterministic compatibility-note block after validation
- **THEN** version drift SHALL be grouped by shared dependency without an earlier duplicate warning
- **THEN** namespace and dynamic-import diagnostics SHALL be grouped by shared dependency and deduplicated by source
- **THEN** source labels SHALL be provider-relative or dependency-relative rather than absolute filesystem paths

### Requirement: ESM provider output manifest

The bundler SHALL generate the provider manifest consumed by Halo whenever ESM output is selected.

#### Scenario: ESM manifest is generated

- **WHEN** a Vite or Rsbuild ESM provider build succeeds
- **THEN** the output SHALL include a minimal manifest containing the ESM format, entry module, and at most one optional startup stylesheet consumed by the Halo runtime
- **THEN** the output manifest SHALL NOT duplicate the build target or resolved shared dependency versions already used by bundler validation

#### Scenario: Provider emits asynchronous CSS chunks

- **WHEN** CSS belongs only to an asynchronously imported JavaScript chunk
- **THEN** the bundler SHALL NOT list that CSS in the provider manifest
- **THEN** the emitted JavaScript runtime SHALL load it on demand from a provider-root-safe URL

#### Scenario: ESM provider uses dynamic imports

- **WHEN** provider source contains a dynamic import
- **THEN** the bundler SHALL emit an independently addressable ESM chunk under the provider resource root
- **THEN** the entry and chunk SHALL use relative or provider-root URLs that work for both plugins and themes

#### Scenario: ESM provider is served from a fallback resource directory

- **WHEN** a plugin build target prefers the `ui` resource directory but Halo discovers the complete ESM artifact through the legacy `console` fallback
- **THEN** module preloads, dynamic JavaScript, asynchronous CSS, and emitted assets SHALL resolve from the directory containing the loaded entry or referencing stylesheet
- **THEN** the emitted ESM runtime SHALL NOT hard-code the preferred `ui` directory
- **THEN** an existing provider build script SHALL NOT need to change its output copy directory

#### Scenario: ESM entry is produced

- **WHEN** an ESM provider build succeeds
- **THEN** the primary entry SHALL export the provider's `PluginModule` as its default export
- **THEN** it SHALL retain imports for the supported shared package roots for host Import Map resolution

#### Scenario: IIFE provider is built

- **WHEN** IIFE output is selected
- **THEN** the bundler SHALL NOT emit an ESM provider manifest that could cause Halo to misclassify the artifact

### Requirement: Vite and Rsbuild ESM equivalence

Vite and Rsbuild provider helpers SHALL implement the same externally observable ESM provider contract for plugin and theme providers.

#### Scenario: Equivalent provider is built with both helpers

- **WHEN** equivalent provider source is built through Vite and Rsbuild
- **THEN** both outputs SHALL use the same manifest schema, host runtime snapshot, import restrictions, entry export contract, resource URL rules, and format selection semantics

#### Scenario: Default production ESM output is optimized

- **WHEN** a provider is built as ESM in production without caller optimization overrides
- **THEN** Vite and Rsbuild SHALL apply their native production JavaScript and CSS minimization
- **THEN** the output SHALL retain one startup JavaScript entry with the default `PluginModule` export
- **THEN** code requested through dynamic imports SHALL remain independently addressable and non-inlined assets SHALL use provider-root-safe URLs

#### Scenario: Vite emits a deployable ESM entry

- **WHEN** Vite builds an ESM provider
- **THEN** it SHALL treat the provider as a final browser entry rather than whitespace-preserving library distribution output
- **THEN** it SHALL preserve the entry module's export signature and honor caller-compatible asset optimization settings

#### Scenario: ESM optimization preserves the IIFE contract

- **WHEN** the ESM production defaults are applied to the modern provider lane
- **THEN** Vite IIFE library output and Rsbuild IIFE window-library output SHALL retain their existing globals, filenames, startup CSS, and chunk behavior
- **THEN** an existing caller SHALL NOT need to change its `viteConfig` or `rsbuildConfig` invocation

#### Scenario: Bundler-specific override bypasses validation

- **WHEN** a Vite or Rsbuild-specific override would bypass dependency validation or change the final output contract
- **THEN** that helper SHALL fail the build instead of emitting a misleading provider manifest

#### Scenario: Caller removes secondary resource content hashes

- **WHEN** the final Vite or Rsbuild output contains an asynchronous chunk, asynchronous stylesheet, or browser-loaded emitted asset without a content hash in its filename
- **THEN** the ESM build SHALL fail with an actionable diagnostic
- **THEN** the stable entry and startup stylesheet names SHALL remain cache-keyed by the provider descriptor

#### Scenario: Output contains build sidecars or filename functions

- **WHEN** the final output contains non-runtime sidecars such as source maps, legal-comment files, or the provider manifest
- **THEN** the content-hash invariant SHALL NOT reject those sidecars merely because their filenames are stable
- **WHEN** caller filename functions produce content-hashed runtime resource names
- **THEN** the ESM build SHALL accept the final output without requiring a particular configuration value shape

#### Scenario: Rsbuild override changes the ESM output contract

- **WHEN** caller Rsbuild configuration conflicts with module output, IIFE mode, module chunk format or loading, the required entry filename, or Halo-controlled externals
- **THEN** the helper SHALL fail before compilation with an actionable diagnostic

#### Scenario: Rsbuild watches a development ESM provider

- **WHEN** a plugin or theme runs `rsbuild build --watch --env-mode=development`
- **THEN** the helper SHALL preserve the automatic runtime public path required by relocatable ESM chunks and assets instead of accepting the development server base default
- **THEN** the initial build and subsequent watched rebuilds SHALL emit the entry, chunks, startup style, and provider manifest without changing the caller invocation
