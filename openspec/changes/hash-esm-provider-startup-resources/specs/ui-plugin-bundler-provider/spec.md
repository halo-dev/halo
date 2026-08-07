## MODIFIED Requirements

### Requirement: ESM provider output manifest

The bundler SHALL generate the provider manifest consumed by Halo whenever ESM output is selected.

#### Scenario: ESM manifest is generated

- **WHEN** a Vite or Rsbuild ESM provider build succeeds
- **THEN** the output SHALL include a minimal manifest containing the ESM format, actual emitted entry module path, and at most one optional actual emitted startup stylesheet path consumed by the Halo runtime
- **THEN** the output manifest SHALL NOT duplicate the build target or resolved shared dependency versions already used by bundler diagnostics

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

- **WHEN** an ESM provider build succeeds with the default resource naming preset
- **THEN** the primary entry filename SHALL contain a content-derived hash
- **THEN** the primary entry SHALL export the provider's `PluginModule` as its default export
- **THEN** it SHALL retain imports for the supported shared package roots for host Import Map resolution

#### Scenario: IIFE provider is built

- **WHEN** IIFE output is selected
- **THEN** the bundler SHALL retain the stable legacy entry filename
- **THEN** the bundler SHALL NOT emit an ESM provider manifest that could cause Halo to misclassify the artifact

### Requirement: Vite and Rsbuild ESM equivalence

Vite and Rsbuild provider helpers SHALL implement equivalent supported defaults for plugin and theme ESM providers. Equivalence SHALL NOT be claimed after caller overrides change those defaults.

#### Scenario: Equivalent provider is built with both default helpers

- **WHEN** equivalent provider source is built through Vite and Rsbuild without conflicting caller overrides
- **THEN** both outputs SHALL use the same manifest schema, host runtime snapshot, shared-root defaults, entry export contract, resource URL rules, and format selection semantics

#### Scenario: Default production ESM output is optimized

- **WHEN** a provider is built as ESM in production without caller optimization overrides
- **THEN** Vite and Rsbuild SHALL apply their native production JavaScript and CSS minimization
- **THEN** the output SHALL retain one content-hashed startup JavaScript entry with the default `PluginModule` export
- **THEN** the default startup stylesheet, when present, SHALL use a content-hashed filename
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

#### Scenario: Default Vite startup and secondary resources are content hashed

- **WHEN** the Vite ESM preset emits its entry, a startup stylesheet, an asynchronous chunk, an asynchronous stylesheet, or a non-inlined asset without caller filename overrides
- **THEN** its configured output pattern SHALL include a content-derived hash
- **THEN** the bundler-kit SHALL record the actual startup filenames but SHALL NOT inspect final assets or re-emit them to prove their filenames were content-derived

#### Scenario: Default Rsbuild startup and secondary resources are content hashed

- **WHEN** the Rsbuild ESM preset emits its entry, a startup stylesheet, an asynchronous chunk, an asynchronous stylesheet, or a non-inlined asset without caller filename overrides
- **THEN** its configured output pattern SHALL include a content-derived hash
- **THEN** the bundler-kit SHALL record the actual startup filenames but SHALL NOT inspect later Rspack compilation stages to prove the final resource name

#### Scenario: Rsbuild watches a development ESM provider

- **WHEN** a plugin or theme runs `rsbuild build --watch --env-mode=development` without overriding the preset resource path
- **THEN** the default helper SHALL preserve the automatic runtime public path required by relocatable ESM chunks and assets
- **THEN** the initial build and subsequent watched rebuilds SHALL emit the content-hashed entry, chunks, startup style, and provider manifest without changing the caller invocation
