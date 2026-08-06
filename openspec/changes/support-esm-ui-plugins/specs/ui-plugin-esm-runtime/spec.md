## ADDED Requirements

### Requirement: Provider UI module manifest

Halo SHALL use a generated provider manifest as the authoritative description of an ESM UI module supplied by a plugin or theme.

#### Scenario: ESM plugin manifest is discovered

- **WHEN** a started plugin provides a valid UI module manifest with `format` set to `esm`
- **THEN** Halo SHALL discover its entry module and styles from that manifest
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

### Requirement: Halo shared dependency inventory

Halo SHALL publish sparse immutable inventories that record the actual shared runtime supplied by a Halo baseline and the best-effort provider build ranges it accepts.

#### Scenario: Inventory records the shared dependency set

- **WHEN** Halo generates a release inventory
- **THEN** the inventory SHALL identify the exact resolved host version, accepted provider build range, and actual package-root runtime exports for `vue`, `vue-router`, `pinia`, `axios`, `@formkit/vue`, `@formkit/core`, `@halo-dev/ui-shared`, `@halo-dev/components`, `@halo-dev/api-client`, and `@halo-dev/richtext-editor`

#### Scenario: Inventory range expresses best-effort replacement

- **WHEN** a provider build version falls inside an inventory's accepted range
- **THEN** the range SHALL mean that Halo permits replacement with its host runtime after export validation
- **THEN** the range SHALL NOT be represented as proof of complete type or behavioral compatibility

#### Scenario: Initial ESM inventory ranges are generated

- **WHEN** Halo publishes the initial ESM provider inventory
- **THEN** it SHALL admit provider build versions `>=3.2.0 <4` for Vue, `>=4 <6` for Vue Router, `>=2 <4` for Pinia, `>=1 <2` for Axios, `>=1 <3` for FormKit Vue/Core, and `>=2 <3` for the four Halo-owned shared packages
- **THEN** stable ranges SHALL NOT implicitly admit prerelease versions

#### Scenario: Halo release reuses an inventory

- **WHEN** a Halo release does not change the shared version facts, accepted ranges, root exports, or runtime bridge behavior
- **THEN** it SHALL be allowed to reuse the latest earlier immutable inventory instead of publishing a duplicate

#### Scenario: Inventory excludes non-shared dependencies

- **WHEN** Halo generates a release inventory
- **THEN** the inventory SHALL NOT expose VueUse, Tiptap, ProseMirror, other FormKit subpackages, or arbitrary third-party dependencies as shared runtime specifiers

#### Scenario: Inventory is generated from actual resolution

- **WHEN** a declared dependency range, workspace link, or lockfile resolution differs from a package's resolved version
- **THEN** the inventory SHALL record the version obtained from the actual Halo UI build resolution
- **THEN** it SHALL NOT record a declaration range, peer suffix, workspace protocol, or link protocol as the resolved version

#### Scenario: Inventory exports are generated

- **WHEN** Halo generates an inventory from its browser runtime artifacts
- **THEN** the recorded root exports SHALL be derived from and checked against those actual artifacts
- **THEN** the manually reviewed accepted range SHALL remain separate from the generated host facts

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

Halo SHALL describe current UI providers through one authenticated response that reuses existing static resource mappings and supplies a version query for cache invalidation.

#### Scenario: Provider descriptor is requested

- **WHEN** an authenticated Console or User Center session requests its provider descriptor
- **THEN** Halo SHALL classify the currently started plugins and activated theme
- **THEN** the response SHALL contain one version, versioned legacy script/style URLs, valid ESM descriptors, and invalid-provider diagnostics from that classification

#### Scenario: ESM plugin resource is described

- **WHEN** a plugin is classified as ESM
- **THEN** its entry and style URLs SHALL use the existing `/plugins/{name}/assets/ui/` mapping or the selected legacy `/assets/console/` fallback
- **THEN** each URL SHALL include the descriptor version as a query parameter

#### Scenario: ESM theme resource is described

- **WHEN** the activated theme is classified as ESM
- **THEN** its entry and style URLs SHALL use the existing `/themes/{name}/ui-plugin/assets/` mapping
- **THEN** each URL SHALL include the descriptor version as a query parameter

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
- **THEN** consumers SHALL NOT need to depend on provider evaluation or registration order

#### Scenario: Provider loading or registration fails

- **WHEN** an enabled provider fails manifest validation, style loading, entry loading, export validation, or registration
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

#### Scenario: ESM entries are loaded

- **WHEN** one or more discovered providers are classified as ESM providers
- **THEN** Halo SHALL import each provider's independent entry URL
- **THEN** the provider SHALL be able to load its own relative asynchronous chunks and emitted assets

#### Scenario: ESM entry exports a plugin module

- **WHEN** an ESM entry finishes evaluating
- **THEN** its default export MUST conform to the existing `PluginModule` shape
- **THEN** Halo SHALL pass it through the existing plugin module initialization flow

#### Scenario: Providers finish loading in different orders

- **WHEN** independent ESM imports settle in a nondeterministic order
- **THEN** Halo SHALL initialize the accepted provider modules in a stable provider order
- **THEN** no provider SHALL rely on ESM evaluation order or direct imports from another provider

#### Scenario: Provider styles settle in different orders

- **WHEN** legacy and ESM provider styles finish loading in a nondeterministic order
- **THEN** their stylesheet elements SHALL retain descriptor order
- **THEN** one ESM provider style failure SHALL be attributed to that provider without discarding unrelated providers

### Requirement: ESM provider failure isolation

Halo SHALL isolate observable ESM provider discovery, import, evaluation, export, style, registration, and delayed-chunk failures from other providers and the core UI where the host lifecycle exposes an isolation boundary.

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
- **THEN** entry, style, and aggregate URLs SHALL include the current provider descriptor version as a cache key
- **THEN** asynchronous chunks and assets SHALL use provider-relative content-hashed URLs where supported
- **THEN** provider discovery metadata SHALL be revalidated so it reflects currently enabled providers

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
