## ADDED Requirements

### Requirement: Provider UI module manifest

Halo SHALL use a generated provider manifest as the authoritative description of an ESM UI module supplied by a plugin or theme.

#### Scenario: ESM plugin manifest is discovered

- **WHEN** a started plugin provides a valid UI module manifest with `format` set to `esm`
- **THEN** Halo SHALL discover its entry module and at most one optional startup stylesheet from that manifest
- **THEN** Halo SHALL obtain provider identity, activation state, installed version, and `spec.requires` from Halo-managed metadata

#### Scenario: ESM theme manifest is discovered

- **WHEN** the activated theme provides a valid UI module manifest with `format` set to `esm`
- **THEN** Halo SHALL apply the same manifest schema and validation used for plugin providers

#### Scenario: Provider has no manifest

- **WHEN** a plugin or activated theme provides UI bundle resources without a provider manifest
- **THEN** Halo SHALL classify the provider as a legacy IIFE provider

#### Scenario: Provider manifest is invalid

- **WHEN** a provider manifest exists but has invalid JSON, an unsupported format, missing runtime fields, or invalid resource paths
- **THEN** Halo SHALL report that provider as invalid
- **THEN** Halo SHALL NOT fall back to executing its resources as a legacy IIFE bundle

#### Scenario: Manifest resource path escapes provider root

- **WHEN** an ESM provider manifest contains an absolute, cross-origin, or provider-root-escaping resource path
- **THEN** Halo SHALL reject the provider manifest

### Requirement: Halo host runtime snapshot

Halo SHALL publish sparse immutable snapshots that record the actual shared runtime supplied by a Halo baseline without claiming a supported provider dependency range.

#### Scenario: Snapshot records the shared dependency set

- **WHEN** Halo generates a host runtime snapshot
- **THEN** the snapshot SHALL identify the exact resolved host version and actual package-root runtime exports for `vue`, `vue-router`, `pinia`, `axios`, `@formkit/vue`, `@formkit/core`, `@halo-dev/ui-shared`, `@halo-dev/components`, `@halo-dev/api-client`, and `@halo-dev/richtext-editor`
- **THEN** it SHALL NOT contain a manually maintained accepted provider version range

#### Scenario: Provider version differs from the snapshot

- **WHEN** a provider resolves a shared package version different from the snapshot's exact host version
- **THEN** the difference SHALL be reported as best-effort diagnostic context rather than proof of compatibility or an automatic build rejection
- **THEN** a newer provider version SHALL warn and a different major version SHALL produce a stronger warning

#### Scenario: Halo release reuses a snapshot

- **WHEN** a Halo release does not change the shared version facts, root exports, or runtime bridge behavior
- **THEN** it SHALL be allowed to reuse the latest earlier immutable snapshot instead of publishing a duplicate

#### Scenario: Host runtime selects a sparse snapshot

- **WHEN** Halo builds its host runtime bridges for a release without an exact snapshot file
- **THEN** it SHALL select the latest snapshot whose Halo baseline is not newer than the current release
- **THEN** a prerelease SHALL compare snapshot age by its stable core version so a same-core snapshot is not treated as an older release

#### Scenario: Snapshot excludes non-shared dependencies

- **WHEN** Halo generates a host runtime snapshot
- **THEN** the snapshot SHALL NOT expose VueUse, Tiptap, ProseMirror, other FormKit subpackages, or arbitrary third-party dependencies as shared runtime specifiers

#### Scenario: Snapshot is generated from actual resolution

- **WHEN** a declared dependency range, workspace link, or lockfile resolution differs from a package's resolved version
- **THEN** the snapshot SHALL record the version obtained from the actual Halo UI build resolution
- **THEN** it SHALL NOT record a declaration range, peer suffix, workspace protocol, or link protocol as the resolved version

#### Scenario: Snapshot exports are generated

- **WHEN** Halo generates a snapshot from its browser runtime artifacts
- **THEN** the recorded root exports SHALL be derived from and checked against those actual artifacts
- **THEN** the snapshot SHALL NOT retain a synthetic package export that the browser artifact does not expose

#### Scenario: Snapshot output version is selected

- **WHEN** the bundler-kit snapshot generator runs
- **THEN** it SHALL derive the Halo baseline and versioned output path from `@halo-dev/ui-plugin-bundler-kit`'s package version
- **THEN** this change SHALL NOT require snapshot generation or checking to run automatically during every package build or CI job

### Requirement: Host-owned shared dependency resolution

Halo SHALL own the browser mapping from supported shared package roots to Halo-provided runtime modules.

#### Scenario: Import Map precedes provider modules

- **WHEN** Console or User Center starts
- **THEN** Halo SHALL install its Import Map before any host or provider module that consumes a shared specifier is resolved

#### Scenario: Provider imports shared package root

- **WHEN** an ESM provider imports a supported shared package root
- **THEN** the browser SHALL resolve it to the Halo-provided runtime URL for the current Halo release

#### Scenario: Provider attempts to override shared mapping

- **WHEN** a plugin or theme supplies its own Import Map or mapping for a shared specifier
- **THEN** Halo SHALL ignore or reject the provider-owned mapping

#### Scenario: Identity-sensitive dependencies are shared

- **WHEN** the host and ESM providers use Vue, Vue Router, Pinia, FormKit Vue, or FormKit Core
- **THEN** they SHALL use runtime modules backed by the same host-owned module identity and state

#### Scenario: FormKit Vue and Core are imported at runtime

- **WHEN** an ESM provider imports `@formkit/vue` or `@formkit/core`
- **THEN** both roots SHALL resolve to one Halo-built FormKit module graph
- **THEN** operations including node lookup, form submission, and reset SHALL observe the same Core registry as the host

#### Scenario: Private FormKit package uses Core

- **WHEN** a provider bundles another `@formkit/*` package that retains a runtime import of `@formkit/core`
- **THEN** that Core import SHALL resolve to the host-owned FormKit graph rather than a private Core copy

#### Scenario: Provider imports VueUse

- **WHEN** an ESM provider imports VueUse in its source
- **THEN** the provider build SHALL include VueUse in the provider's own output instead of resolving it through the Halo Import Map

#### Scenario: Provider imports Axios

- **WHEN** an ESM provider imports `axios`
- **THEN** it SHALL receive the standard Halo-supplied Axios module
- **THEN** it SHALL NOT implicitly receive the configured Axios instance used internally by `@halo-dev/api-client`

#### Scenario: Provider uses the Halo API client

- **WHEN** an ESM provider imports `axiosInstance` from `@halo-dev/api-client`
- **THEN** it SHALL receive the separately created shared Halo API instance with host authentication and error handling
- **THEN** ordinary provider clients SHALL be created with `axios.create()` instead of mutating shared defaults or interceptors

### Requirement: Versioned provider descriptor

Halo SHALL describe current UI providers through one authenticated response that reuses existing static resource mappings and supplies stable catalog and provider cache keys for cache invalidation.

#### Scenario: Provider descriptor is requested

- **WHEN** an authenticated Console or User Center session requests its provider descriptor
- **THEN** Halo SHALL classify the currently started plugins and activated theme
- **THEN** the response SHALL contain one ordered provider list in which every discovered provider appears exactly once with its Halo-owned identity, type, installed version, classification kind, and kind-specific entry, startup style, or invalid reason
- **THEN** the response SHALL contain a catalog-versioned legacy script URL only when at least one provider is classified as legacy
- **THEN** the provider list SHALL be the authoritative discovery, startup-style precedence, and registration order
- **THEN** the response SHALL NOT expose a separate catalog version, registration list, stylesheet list, ESM-provider list, or invalid-provider list

#### Scenario: Provider descriptor is consumed by the Halo UI

- **WHEN** Halo generates its OpenAPI document and TypeScript API client
- **THEN** the document SHALL include the provider descriptor endpoint and its complete response schema
- **THEN** required descriptor and provider fields and the `plugin` or `theme` provider type SHALL remain represented in the generated models
- **THEN** Console and User Center SHALL use the generated API method and models instead of a parallel hand-written descriptor contract

#### Scenario: ESM plugin resource is described

- **WHEN** a plugin is classified as ESM
- **THEN** its entry URL SHALL use the existing `/plugins/{name}/assets/ui/` mapping or the selected legacy `/assets/console/` fallback
- **THEN** the entry URL SHALL include a stable plugin-specific cache key as a query parameter

#### Scenario: ESM theme resource is described

- **WHEN** the activated theme is classified as ESM
- **THEN** its entry URL SHALL use the existing `/themes/{name}/ui-plugin/assets/` mapping
- **THEN** the entry URL SHALL include a stable theme-specific cache key as a query parameter

#### Scenario: Startup stylesheets are described

- **WHEN** the descriptor contains legacy providers or valid ESM providers with a main stylesheet
- **THEN** the descriptor SHALL expose the direct provider-keyed stylesheet URL on its owning provider record
- **THEN** each URL SHALL use the existing provider static resource mapping so relative CSS assets resolve from their owning provider
- **THEN** provider records SHALL NOT expose CSS emitted only for asynchronous chunks

#### Scenario: Legacy aggregate stylesheet is requested

- **WHEN** a Halo 2.x client requests the existing aggregate CSS endpoint
- **THEN** the endpoint SHALL remain available as a compatibility bridge
- **THEN** it SHALL emit ordered CSS `@import` rules that reference the direct provider-keyed stylesheet URLs instead of concatenating their source under the aggregate API URL
- **THEN** the implementation SHALL include an adjacent comment identifying removal after legacy IIFE support ends in Halo 3

#### Scenario: Provider state changes during startup

- **WHEN** a plugin or theme changes after a descriptor response is created
- **THEN** Halo SHALL NOT claim immutable content for the previous response or retain copied provider resources
- **THEN** a full page reload SHALL obtain a newly versioned current descriptor as the supported recovery boundary

### Requirement: Shared UI provider registration store

Halo SHALL expose format-neutral UI provider availability and registration metadata through `stores.uiPlugins()` exported by `@halo-dev/ui-shared`.

#### Scenario: Provider descriptor initializes the store

- **WHEN** Console or User Center receives a provider descriptor
- **THEN** Halo SHALL populate the shared store before evaluating legacy or ESM provider entries
- **THEN** each discovered plugin or activated-theme provider SHALL have Halo-owned name, type, version, and lifecycle metadata
- **THEN** valid providers SHALL start as `pending` and providers already invalidated by descriptor discovery SHALL start as `failed`

#### Scenario: Enabled provider is queried

- **WHEN** provider code calls `isEnabled(name)`
- **THEN** the result SHALL reactively indicate whether that UI provider is present in the current descriptor regardless of IIFE or ESM format

#### Scenario: Registered provider is queried

- **WHEN** provider code calls `isRegistered(name)` or observes the corresponding registration record
- **THEN** the result SHALL become true only after that provider's module registration commits successfully in the current page
- **THEN** a discovered legacy provider that has no UI module SHALL be treated as a successful compatible no-op and reported as registered
- **THEN** consumers SHALL NOT need to depend on provider evaluation or registration order

#### Scenario: Provider loading or registration fails

- **WHEN** an enabled provider fails manifest validation, entry loading, export validation, or registration
- **THEN** its shared registration record SHALL transition to `failed`
- **THEN** it SHALL NOT be reported as registered

#### Scenario: Provider reads another provider's record

- **WHEN** provider code obtains registration metadata from the shared store
- **THEN** it SHALL receive only provider identity, type, version, and lifecycle status
- **THEN** the public store SHALL NOT expose another provider's `PluginModule`, routes, components, or callable implementation

#### Scenario: Provider attempts to mutate registration metadata

- **WHEN** provider code accesses the shared store
- **THEN** the registration collection and host lifecycle actions SHALL be documented and typed for read-only provider consumption
- **THEN** client-side mutation escape hatches SHALL NOT be treated as a supported API or security boundary

### Requirement: Mixed legacy and ESM provider loading

Halo SHALL load legacy IIFE and ESM UI providers in the same Console or User Center session without requiring legacy providers to be rebuilt.

#### Scenario: Provider descriptors are requested

- **WHEN** an authenticated Console or User Center session initializes UI providers
- **THEN** Halo SHALL return the current provider-neutral descriptor containing the enabled plugin providers and the activated theme provider
- **THEN** inactive themes SHALL NOT be included

#### Scenario: Legacy aggregate is loaded

- **WHEN** one or more discovered providers are classified as legacy IIFE providers
- **THEN** Halo SHALL load them through the descriptor's versioned aggregate bundle behavior
- **THEN** the aggregate SHALL exclude providers classified as ESM
- **THEN** its legacy enabled-provider metadata SHALL include every valid enabled provider regardless of IIFE or ESM format

#### Scenario: ESM entries are loaded

- **WHEN** one or more discovered providers are classified as ESM providers
- **THEN** Halo SHALL start all independent entry imports in parallel and await them with all-settled semantics
- **THEN** the provider SHALL be able to load its own relative asynchronous chunks and emitted assets
- **THEN** Halo SHALL NOT reload the page after an individual entry import settles

#### Scenario: Many provider startup resources are loaded

- **WHEN** the descriptor contains many provider styles and ESM entries
- **THEN** Halo SHALL start every style load, every ESM entry import, and the legacy script load before waiting for any individual startup resource to settle
- **THEN** style elements SHALL retain provider-list order independent of network completion order
- **THEN** Halo SHALL NOT introduce application-level request batching that serializes provider startup

#### Scenario: ESM entry exports a plugin module

- **WHEN** an ESM entry finishes evaluating
- **THEN** its default export MUST conform to the existing `PluginModule` shape
- **THEN** Halo SHALL pass it through the existing plugin module initialization flow

#### Scenario: Providers finish loading in different orders

- **WHEN** independent ESM imports settle in a nondeterministic order
- **THEN** Halo SHALL initialize the accepted provider modules in a stable provider order
- **THEN** no provider SHALL rely on ESM evaluation order or direct imports from another provider

#### Scenario: Startup provider styles are loaded

- **WHEN** provider startup begins
- **THEN** Halo SHALL insert and load each provider record's startup stylesheet directly in provider-list order
- **THEN** a stylesheet failure SHALL fail only its owning provider and SHALL NOT prevent unrelated providers or the core UI from continuing

#### Scenario: Provider loads an asynchronous CSS chunk

- **WHEN** a provider later imports a JavaScript chunk with associated CSS
- **THEN** the bundler runtime SHALL load that CSS on demand from the provider's static resource mapping
- **THEN** Halo SHALL NOT eagerly include that chunk CSS in the startup aggregate

### Requirement: ESM provider failure isolation

Halo SHALL isolate observable ESM provider discovery, startup-style, import, evaluation, export, registration, and delayed-chunk failures from other providers and the core UI where the host lifecycle exposes an isolation boundary.

#### Scenario: One ESM entry fails

- **WHEN** an ESM provider entry fails to fetch, link, evaluate, or export a valid `PluginModule`
- **THEN** Halo SHALL skip that provider
- **THEN** Halo SHALL continue loading and initializing other valid providers
- **THEN** Halo SHALL continue core Console or User Center startup

#### Scenario: Provider requires an incompatible Halo version

- **WHEN** an ESM provider's `spec.requires` is incompatible with the running Halo version
- **THEN** Halo SHALL reject that provider before importing its entry
- **THEN** the rejection SHALL identify the provider, its required Halo range, and the current Halo version

#### Scenario: Multiple providers fail

- **WHEN** multiple providers fail during one startup
- **THEN** Halo SHALL present a single user-facing summary notification
- **THEN** Halo SHALL retain provider-specific structured diagnostics for logs and management UI

#### Scenario: Provider registration fails synchronously

- **WHEN** a provider fails while registering routes, components, stores, extensions, or other host integrations
- **THEN** Halo SHALL invoke recorded removal or restoration handles in reverse order for mutations that support undo
- **THEN** a named route replaced by the failing provider SHALL be restored to the previously registered route
- **THEN** Halo-managed anonymous parent routes SHALL have an internal identity that permits their replaced named children to be restored
- **THEN** a route already registered under an unidentifiable anonymous parent SHALL retain legacy last-registration-wins behavior when replacement succeeds
- **THEN** if that replacement is followed by a failed transaction and cannot be restored reliably, Halo SHALL diagnose an incomplete route rollback and require a page reload instead of having rejected the provider before mutation
- **THEN** Halo SHALL continue registering later providers
- **THEN** any mutation that could not be reversed SHALL be diagnosed and SHALL use page reload as the final recovery boundary

#### Scenario: Provider lazy route chunk fails later

- **WHEN** an accepted provider's route or asynchronous component chunk fails after startup
- **THEN** Halo SHALL attribute the error to that provider through router or component error handling
- **THEN** it SHALL show a provider-specific failure state where possible without disabling unrelated routes or providers

#### Scenario: Provider creates non-transactional side effects

- **WHEN** provider evaluation creates top-level side effects, timers, event listeners, or later asynchronous work outside host registries
- **THEN** Halo SHALL NOT claim those effects can be rolled back
- **THEN** observable failures SHALL be attributed and diagnosed with full page reload as the recovery boundary

### Requirement: Legacy UI provider compatibility

Halo SHALL preserve the existing IIFE UI provider protocol throughout Halo 2.x.

#### Scenario: Existing plugin artifact runs on a new Halo 2.x release

- **WHEN** an existing IIFE plugin whose `spec.requires` accepts the current Halo version is started
- **THEN** Halo SHALL load it without requiring a rebuild or new manifest
- **THEN** Halo SHALL preserve existing bundle endpoints, global module registration, shared global names, enabled-provider metadata, and `ui` to `console` resource fallback

#### Scenario: Existing theme UI artifact runs on a new Halo 2.x release

- **WHEN** the activated theme provides an existing IIFE UI module
- **THEN** Halo SHALL continue loading it through the legacy aggregate and global module protocol

#### Scenario: Legacy shared dependency is upgraded

- **WHEN** Halo upgrades a shared dependency used by legacy provider globals
- **THEN** Halo SHALL run frozen legacy fixtures and maintained ecosystem usage samples before release
- **THEN** Halo SHALL retain known-used exports or add focused compatibility behavior where practical
- **THEN** Halo SHALL document that upstream behavioral compatibility remains best-effort rather than guaranteed for every provider

#### Scenario: Legacy provider uses VueUse global

- **WHEN** a legacy IIFE provider references `window.VueUse`
- **THEN** Halo SHALL retain the compatibility global for Halo 2.x
- **THEN** that compatibility behavior SHALL NOT make VueUse a shared ESM specifier

#### Scenario: Legacy-only compatibility code is retained

- **WHEN** implementation code is added or changed solely to preserve IIFE provider globals, metadata globals, aggregate aliases, resource fallback, bundler global mappings, or global-backed ESM bridges
- **THEN** the source SHALL include an adjacent comment identifying removal after legacy IIFE support ends in Halo 3
- **THEN** exported legacy declarations SHALL also use the language's deprecation marker where applicable

#### Scenario: Provider migrates a global presence check

- **WHEN** provider code needs a replacement for `window[providerName]` or `window.enabledUiPlugins` presence checks
- **THEN** documentation SHALL direct it to the shared provider registration store
- **THEN** direct access to another provider's module implementation SHALL remain unsupported

### Requirement: UI provider lifecycle and cache boundary

Halo SHALL treat a full Console or User Center page load as the supported module replacement boundary.

#### Scenario: Provider is installed, upgraded, enabled, disabled, or activated

- **WHEN** a plugin or theme UI provider changes after the page module graph has started
- **THEN** Halo SHALL require or prompt a full page reload
- **THEN** Halo SHALL NOT hot-unload or hot-replace the running provider module

#### Scenario: Production ESM assets are cached

- **WHEN** production provider ESM resources are emitted
- **THEN** entry and direct startup-style URLs SHALL include a cache key derived from that provider's Halo-managed identity and installed version
- **THEN** legacy aggregate URLs SHALL include the current catalog version as a cache key
- **THEN** asynchronous chunks and assets SHALL use provider-relative content-hashed URLs where supported
- **THEN** provider discovery metadata SHALL be revalidated so it reflects currently enabled providers

#### Scenario: Development provider assets are cached

- **WHEN** a development plugin or theme provider is described repeatedly without changing its directly loaded build output
- **THEN** its entry and startup-style URLs SHALL retain the same provider-specific cache key
- **WHEN** its manifest, entry, or startup stylesheet changes
- **THEN** its provider-specific cache key and the catalog version SHALL change
- **THEN** another unchanged provider SHALL retain its existing direct resource URLs

### Requirement: Development and packaged runtime parity

Halo SHALL support ESM UI providers in both the proxied UI development topology and the packaged production topology.

#### Scenario: Proxied development Console loads an ESM provider

- **WHEN** a developer opens a Console or User Center route through the Halo development server while the UI development server is running independently
- **THEN** the proxied HTML SHALL contain the development Import Map before module entries
- **THEN** Halo UI runtime modules SHALL load from the UI development server
- **THEN** APIs and plugin or theme provider resources SHALL continue loading from the Halo backend

#### Scenario: Development page uses a deep route

- **WHEN** a developer opens a nested Console or User Center route through the Halo development server
- **THEN** the proxied entry, Import Map, shared runtime modules, provider entries, and provider chunks SHALL resolve successfully without relying on the UI development server as the document origin

#### Scenario: Packaged application loads an ESM provider

- **WHEN** Halo runs from a built application artifact without a UI development server
- **THEN** the packaged Console and User Center HTML SHALL map shared dependencies to packaged content-hashed runtime assets
- **THEN** all host and provider runtime resources SHALL load without development-server URLs
