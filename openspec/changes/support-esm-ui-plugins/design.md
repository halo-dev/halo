## Context

Halo currently externalizes UI dependencies to classic global scripts, concatenates every started plugin's `main.js` plus the activated theme's `main.js`, and discovers the resulting `PluginModule` objects from `window`. Vite emits IIFE output, Rsbuild emits a window library, and both share the same global mapping. This preserves a single Vue-family runtime but couples providers to global load order, prevents standard ESM chunk graphs, and gives a provider no reliable way to verify that its build-time dependencies match its target Halo release.

Several foundations already exist:

- plugin and theme resource routes serve arbitrary files below their UI output roots, including asynchronous chunks;
- `Plugin.status` and `Theme.status` already expose primary entry and stylesheet URLs, although the aggregate loader does not consume them;
- plugin and theme providers use the same `PluginModule` contract and modern bundler helpers;
- Console and User Center share one UI project but have distinct HTML entries;
- development proxies only Console/User Center HTML from Halo on port 8090 to the UI development server on port 3000, while APIs and provider resources remain on Halo;
- production copies generated UI HTML and assets into the application artifact.

The change spans the UI build, bundler kit, backend resource discovery, runtime loading, dependency compatibility policy, and real-browser validation. It must remain additive for existing IIFE artifacts and must not introduce a second user-facing compatibility version beside `spec.requires`.

## Goals / Non-Goals

**Goals:**

- Load plugin and activated-theme UI modules as native ESM with independent entries, styles, asynchronous chunks, and caches.
- Keep existing IIFE plugins and themes working without rebuilds throughout Halo 2.x.
- Make `spec.requires` the only provider-facing compatibility declaration.
- Publish sparse host runtime snapshots that record exact shared-package versions, root exports, and runtime bridge facts for a Halo baseline.
- Resolve shared dependencies through one host-owned Import Map and preserve identity for stateful frameworks.
- Apply the same ESM contract to Vite and Rsbuild, plugin and theme providers, Console and User Center.
- Reuse the existing plugin and theme static resource mappings and add stable provider-specific cache keys for cache invalidation.
- Isolate ESM provider loading and registration failures without preventing the core UI or other providers from starting.
- Validate both the proxied development topology and the packaged BootJar topology in real browsers.

**Non-Goals:**

- Module Federation, SystemJS, import-map polyfills, or provider-owned Import Maps.
- Multiple simultaneous Vue, Vue Router, Pinia, or FormKit versions.
- Remote or CDN provider entries and chunks.
- Direct JavaScript dependencies between providers or a provider evaluation-order contract.
- Production hot replacement, runtime module unloading, or cross-Halo HMR.
- Automatic dual IIFE/ESM artifacts or fallback to IIFE after ESM evaluation starts.
- Sharing VueUse or arbitrary third-party packages through Halo's Import Map.
- Introducing an `@halo-dev/ui-plugin-api` facade in this change.
- Adding ESM diagnostics to the public `Plugin.status` or `Theme.status` API in the first version.
- Full Yarn Plug'n'Play resolution support without a dedicated resolver adapter.
- Guaranteeing semantic compatibility between a provider's installed dependencies and the Halo-hosted replacements.
- Fully undoing ESM top-level side effects, arbitrary event handlers, timers, or asynchronous work started by a provider.

## Decisions

### Use `spec.requires` to select a build-time host runtime snapshot

Providers continue to declare only `spec.requires`; there is no independent UI runtime version. When the bundler can derive a minimum stable Halo version, it selects the latest immutable host runtime snapshot whose Halo baseline is not newer than that target. The selected target and snapshot are build input and diagnostic context, not fields in the emitted runtime manifest.

For example, `requires: ">=2.26.0"` selects the 2.26.0 snapshot. A provider that needs an export introduced by Halo 2.28 raises its minimum required version and builds with a bundler kit containing a corresponding snapshot.

Snapshots are sparse: Halo publishes a new one only when a shared version, root export surface, or runtime bridge changes. Multiple Halo releases may reuse one snapshot. Each entry records the exact package name and version resolved by the baseline Halo UI build, package-root runtime exports derived from the actual host runtime artifact, and the runtime asset and identity requirements. It does not contain a hand-maintained accepted version range.

For example:

```json
{
  "vue": {
    "version": "3.5.40",
    "exports": ["computed", "defineComponent", "ref"]
  }
}
```

The snapshot uses these public specifiers:

```text
vue
vue-router
pinia
axios
@formkit/vue
@formkit/core
@halo-dev/ui-shared
@halo-dev/components
@halo-dev/api-client
@halo-dev/richtext-editor
```

The current snapshot is emitted with the Halo UI runtime. Immutable historical snapshots are embedded in published bundler-kit releases; builds never fetch mutable online metadata. Both the provider bundler and Halo's host runtime bridge select the latest snapshot not newer than the target Halo release. If a target is newer than the installed bundler knows, the bundler reuses that eligible snapshot and warns only when the selected baseline is from an earlier stable core release; a prerelease of the same core release does not make that same-core snapshot older. If no eligible ESM snapshot exists, the ESM build fails and points to a bundler update or IIFE output.

The snapshot generator derives its Halo baseline and output file name from `@halo-dev/ui-plugin-bundler-kit`'s own package version. The exact moment at which Halo maintainers generate or check a new snapshot is intentionally not coupled to package build or CI in this change; publication workflow integration can be decided separately.

Alternative considered: an independently versioned `uiRuntime` contract. It adds a second provider-facing version and implies a stronger ABI guarantee than Halo can make. `spec.requires`, exact host diagnostics, root-export checks, and compatibility fixtures provide a simpler best-effort contract.

### Validate the provider's resolved packages, not declaration ranges

The bundler resolves each shared package from the provider project root and validates the package actually used for type checking and compilation. A declaration such as `^3.5.0` or lockfile text is diagnostic context, not the source of truth.

Resolution is anchored at the provider project or manifest path, not the installed bundler-kit package. The resolver:

1. resolves the package from the provider project using Node-compatible project resolution;
2. locates the owning package root even when `package.json` is not exported;
3. resolves symlinks/workspace links to the real package root;
4. reads and normalizes the exact package name and version;
5. cross-checks the bundler's final resolved entry so aliases or conditional-entry changes cannot bypass validation.

The bundler SHALL NOT infer a package entry from `module`, `main`, or a
conventional root `index.js`. Vite's configured resolver remains authoritative
when it exposes a resolved module ID. When externalization prevents Rsbuild
from exposing that final resource, the bundler kit uses `pkg-types` to resolve
the package from the importing provider and locate the nearest owning
`package.json`; this keeps exports-only packages and pnpm symlinks within a
maintained Node-compatible resolver instead of duplicating entry-point rules.

The bundler rejects missing shared packages, deep imports, unsupported statically named exports, aliases, forks, and configuration that bypasses externalization. Only after those checks does it externalize a shared root import. Caller configuration cannot externalize arbitrary non-shared packages because the browser Import Map supplies only Halo's declared shared roots. A namespace import or dynamic namespace property cannot be proven completely, so it is allowed with a best-effort warning. Any version difference is diagnostic rather than an admission gate: a provider version newer than the host warns, and a different major version emits a stronger warning, but the build proceeds when all concrete root identity and static export checks pass.

Other dependencies, including VueUse, remain private. Direct `@tiptap/*` or `prosemirror-*` runtime imports also remain private, but produce a compatibility warning when the provider uses `@halo-dev/richtext-editor`; authors should prefer the editor package's re-exports to avoid class-identity and version skew.

Error messages identify the provider root, resolved source, selected Halo snapshot, and remediation. A successful ESM build emits one aligned shared-dependency version table and, when needed, one deterministic compatibility-note block. Version drift is summarized by dependency instead of being emitted once during validation and again in the final table. Namespace or dynamic-import diagnostics are grouped by shared root, deduplicated, and use provider-relative or dependency-relative source labels rather than absolute filesystem paths. Root-export validation catches many concrete incompatibilities, but cannot prove overload, type, default-export-property, or behavioral equivalence.

Alternative considered: exact or range-based version rejection. Local ecosystem sampling showed that either policy would exclude providers even when their actual imported surface is available. Warning-only version drift plus concrete identity/export checks and an IIFE escape hatch is a more practical best-effort boundary.

### Let the bundler select format from `spec.requires`, but let the artifact declare format

Modern Vite and Rsbuild helpers accept `format: "auto" | "iife" | "esm"`, defaulting to `auto`.

- `auto` recognizes a simple stable version or `>=MAJOR.MINOR.PATCH` lower bound and emits ESM when that minimum is at least 2.26.0.
- Missing, wildcard, composite, or otherwise unparseable `spec.requires` values warn and fall back to IIFE. This avoids duplicating Halo's broader Java version-range grammar in Node merely to select an optional output format.
- `iife` ignores target parsing and always remains available as a migration escape hatch.
- `esm` uses the derived target when available; otherwise the author supplies `targetHaloVersion` as an explicit build option.
- Explicit ESM with metadata that still permits Halo older than 2.26 emits a strong warning instead of rewriting or rejecting the provider manifest.
- 2.26 prerelease testing requires explicit ESM selection and an explicit prerelease target.

Once ESM and a snapshot have been selected, concrete dependency resolution, import, or output validation failures fail the build. They do not silently change an intended ESM artifact back to IIFE.

### Treat ESM providers as deployable browser entries

An ESM provider is loaded directly by Halo rather than published for another bundler to consume. Vite therefore uses a normal Rollup browser entry with a preserved entry signature instead of library mode. This preserves the default `PluginModule` export without requiring a facade that only re-exports an exact library surface, while allowing Vite's production optimizer to fully minify JavaScript and CSS, honor the configured asset inline threshold, and emit independently cacheable assets and dynamic chunks. It also avoids adding a second minifier dependency merely to override Vite's intentional whitespace-preserving behavior for ES library output.

Rsbuild remains a production Web build with Rspack's native module output, module chunk loading, SWC JavaScript minimization, and Lightning CSS minimization. ESM output follows Rspack's mode-appropriate module ID defaults instead of forcing readable `named` IDs intended for debugging. Both helpers keep the provider entry as the only startup JavaScript entry and split code requested through dynamic imports; neither helper introduces automatic initial vendor entries that the provider manifest would need to describe.

ESM output is relocatable below any provider static resource mapping selected by Halo. Vite uses a relative build base so its module-preload helper resolves dependencies from `import.meta.url`. Rsbuild uses its automatic runtime public path so asynchronous CSS and other runtime-loaded resources follow the actual entry location. This is required because Halo may discover a plugin artifact through the legacy `console` fallback even when its metadata prefers `ui`; the build artifact cannot know which complete resource directory the backend will select. Main CSS asset references remain relative to the stylesheet that contains them.

These optimizations are ESM-only defaults. Caller configuration continues to merge after the provider defaults when it does not violate the output contract, and the existing IIFE library/window output, globals, startup CSS, filenames, and debug-oriented Rsbuild module IDs remain unchanged.

Plugin providers read `plugin.yaml`; theme providers read `theme.yaml`, including `spec.requires`. This extends the theme manifest reader, which currently only reads the metadata name.

The host never infers artifact format from `spec.requires`. New bundlers emit an ESM provider manifest; absence of that manifest means legacy IIFE. This preserves artifacts built by older bundlers even when their manifest already requires Halo 2.26 or newer.

The provider manifest is placed at the provider UI output root (`ui/` for plugins and `ui-plugin/dist/` for themes) and contains only fields consumed by the runtime:

```json
{
  "format": "esm",
  "entry": "./main.js",
  "style": "./style.css"
}
```

The optional `style` is the one startup stylesheet emitted for the provider entry. CSS belonging to asynchronous chunks is omitted and remains linked by the bundler runtime when the corresponding JavaScript chunk loads. The build target and actually resolved dependency versions stay in bundler validation and diagnostics. They are not serialized because the target snapshot is derived from `spec.requires`, and the runtime cannot independently prove which packages compiled the bundle. Provider identity, activation state, installed version, and compatibility requirements come from Halo, not the provider-controlled manifest. All manifest paths are normalized relative paths and must remain inside the provider resource root.

An existing but malformed manifest is an invalid ESM provider and never falls back to executing a possibly stale IIFE entry.

### Generate a host-owned shared module graph and Import Map

Halo generates the Import Map and installs it before Console/User Center host modules or provider modules resolve shared imports. Providers cannot extend or override it. Only package-root specifiers in the release snapshot are public.

During the first migration phase:

- Existing Vue, Vue Router, Pinia, Axios, and Halo UI package globals remain canonical for the host and legacy IIFE providers; generated static ESM bridges export the same functions and objects for ESM providers.
- Halo builds one FormKit runtime graph that exposes both `@formkit/vue` and `@formkit/core` bridges while retaining the compatible `window.FormKitVue` global expected by legacy providers.

Static facades are generated from the snapshot export surface and browser-tested against the actual global builds. The snapshot records exactly the exports supplied by those browser artifacts and does not retain synthetic package metadata exports such as `__esModule` unless the browser artifact actually exposes them. Facades cannot dynamically export arbitrary properties from `window`, because ESM named exports must be statically known. A missing global or export fails the Halo UI build.

FormKit requires special treatment. Its core registry is module-instance state, so a bridge over a second FormKit copy would not provide actual sharing. Both `@formkit/vue` and `@formkit/core` are public roots. Other `@formkit/*` packages remain provider-private, but any runtime import they retain to `@formkit/core` is externalized to the host graph. Identity fixtures cover operations such as `getNode`, `submitForm`, and `reset` across host/provider boundaries.

Axios sharing exposes the standard package module, not Halo's configured API client instance. `@halo-dev/api-client` continues exporting its separately created `axiosInstance` for authenticated Halo API calls. Providers use `axios.create()` for isolated custom clients. Mutating the shared Axios default object's defaults/interceptors or the shared API client instance is unsupported because it can affect the host or other providers; identity-safe checks such as `isAxiosError` remain supported.

Alternative considered: point Import Map entries at current npm ESM outputs. Current Halo package outputs retain additional bare imports such as VueUse and Axios, while raw Vue Router, Pinia, and FormKit graphs add internal dependency and identity concerns. Dedicated browser runtime artifacts keep the public dependency graph limited to the snapshot.

### Discover providers through one ordered descriptor

The backend classifies the current started plugin set and activated theme when the authenticated provider descriptor is requested. The response projects that classification into one ordered provider list in which every discovered provider appears exactly once. Each record contains Halo-owned identity, type, installed version, a `legacy`, `esm`, or `invalid` kind, and only the fields needed by that kind: ESM entry, optional startup style, or invalid reason. A shared legacy script URL is returned only when the list contains a legacy provider. The descriptor is revalidated rather than treated as an immutable resource snapshot.

The provider list is the single source of truth for discovery, registration-store seeding, startup-style precedence, and final registration order. It does not split the same provider identity across parallel registration, style, ESM, and invalid arrays that the browser would have to join again. The catalog version remains an internal cache key encoded in the legacy script URL instead of a separately exposed response field.

ESM entries and startup styles reuse the static mappings Halo already exposes for plugin `ui` or legacy `console` resources and activated-theme `ui-plugin` resources. Each provider manifest may identify one main stylesheet, and a legacy provider may expose its existing main stylesheet. The descriptor places that direct style URL on its owning provider record so each stylesheet keeps its own URL base and failure attribution. Asynchronous chunk CSS remains under the provider mapping and is loaded on demand by its bundler runtime.

Each direct entry and startup-style URL receives a provider-specific cache key. In packaged operation the key is derived from Halo-managed provider type, identity, and installed version. In development it additionally fingerprints the provider manifest and directly loaded entry/style resources through stable file metadata, so repeated descriptor requests keep the same URL until the build output changes. The catalog version hashes the ordered provider classification and provider cache keys and remains the cache key for legacy aggregate resources. This prevents one development provider or a repeated descriptor request from invalidating every provider resource. For example:

```json
{
  "legacyScript": "/apis/.../bundle.js?v=abc123",
  "providers": [
    {
      "name": "plugin-search",
      "type": "plugin",
      "version": "1.0.0",
      "kind": "esm",
      "entry": "/plugins/plugin-search/assets/ui/main.js?v=provider123",
      "style": "/plugins/plugin-search/assets/ui/style.css?v=provider123"
    },
    {
      "name": "legacy-plugin",
      "type": "plugin",
      "version": "1.0.0",
      "kind": "legacy"
    },
    {
      "name": "broken-plugin",
      "type": "plugin",
      "version": "1.0.0",
      "kind": "invalid",
      "reason": "Provider resource does not exist"
    }
  ]
}
```

`kind` is an explicit discriminator rather than being inferred from optional-field presence. An ESM record always has `entry` and may have `style`; a legacy record has no individual entry and may have `style`; an invalid record always has `reason` and no loadable resources. A legacy provider without a UI module remains represented and retains its successful no-op compatibility behavior. The interface intentionally does not add generic resource arrays, nested diagnostics, or OpenAPI polymorphism while the runtime supports exactly one entry, at most one startup style, and one discovery reason.

Plugin bundles continue preferring `ui` and falling back to `console`; the selected directory is reflected in generated entry and style URLs. Theme bundles use `/themes/{theme}/ui-plugin/assets/{resource}`. Query parameters do not change path resolution, and emitted asynchronous chunks continue using their provider-relative, content-hashed paths. Main CSS asset references therefore resolve relative to the direct stylesheet URL and need no backend rewriting.

The catalog and provider version queries are cache keys, not immutable server-side snapshots. If provider files change after a descriptor is returned, a partially loaded page can observe the newer files. Provider lifecycle changes already use a full page reload as the supported replacement boundary, so the runtime reports a load failure and reload remains the recovery path. The design intentionally avoids resource copying, retained generations, generation-specific proxy endpoints, and hashing complete resource contents on every descriptor request.

The legacy JavaScript generated for the current descriptor includes only currently classified IIFE providers. The existing aggregate CSS endpoint and compatibility aliases remain available for Halo 2.x, but emit ordered `@import url("<direct provider style URL>")` rules instead of concatenating provider CSS under the API URL base. This compatibility bridge carries an adjacent `TODO(Halo 3)` removal comment. Existing globals, `enabledPlugins`, `enabledUiPlugins`, plugin-name ordering, theme module naming, and `ui`-before-`console` resource selection remain compatible for legacy artifacts.

Public `Plugin.status` and `Theme.status` remain unchanged. Descriptor validation and errors remain internal until the diagnostic model is stable.

The authenticated descriptor endpoint declares its complete response schema in
the generated OpenAPI document. Required descriptor and provider fields remain
required in the generated TypeScript models, while kind-specific fields remain
optional and are constrained by the provider kind. Console and User Center consume the generated API
method and models through `@halo-dev/api-client`; they do not maintain a
parallel hand-written descriptor contract.

Alternative considered: retain immutable server-side generations to make descriptor and aggregate requests atomic across a concurrent provider change. That guarantee requires copying resources, proxy routes, and eviction behavior; the added machinery is not justified for an optional ESM path whose supported replacement boundary is already a full page reload.

### Expose provider availability through a shared Pinia store

`@halo-dev/ui-shared` adds `stores.uiPlugins()`, backed by the same host Pinia instance shared through the Import Map. It exposes Halo-owned metadata rather than another provider's `PluginModule`:

```ts
interface UiPluginRegistration {
  name: string;
  type: "plugin" | "theme";
  version: string;
  status: "pending" | "registered" | "failed";
}
```

The public surface provides a reactive registration collection plus `get(name)`, `isEnabled(name)`, and `isRegistered(name)`. Record presence means that the UI provider is enabled/discovered in the current descriptor; `registered` normally means its current-page module commit succeeded. A legacy provider with no UI module is a compatible successful no-op and also becomes registered, because backend-only providers historically contribute no global module. The store is seeded from Halo-owned descriptor metadata before legacy scripts or ESM entries are evaluated, then the loader changes `pending` to `registered` or `failed` as loading and registration settle. It applies equally to plugin and activated-theme providers in Console and User Center.

This distinguishes two existing use cases:

- code that only needs to feature-detect an enabled provider uses `isEnabled`;
- code that needs the provider's UI contribution to be ready observes `isRegistered` reactively instead of relying on registration order.

The host owns mutations. Provider code treats the collection as read-only and must not use Pinia mutation escape hatches to spoof state. This is a public API contract, not a browser security boundary. Failure details stay in internal diagnostics, and the store does not expose another provider's module object, routes, components, or callable implementation. Direct JavaScript dependencies and an ordering contract between providers therefore remain out of scope.

The existing internal `usePluginModuleStore` may continue holding successfully loaded module objects for Halo's own extension-point aggregation, but provider availability checks migrate to the shared metadata store. This provides a format-neutral replacement for checks such as `window.PluginXXX` and `window.enabledUiPlugins` before those globals are eventually removed.

### Load legacy and ESM providers through two coordinated lanes

The UI startup flow becomes:

```text
fetch the current provider descriptor
  -> seed the shared provider-registration store from the ordered provider list
  -> start every provider record's startup stylesheet directly
  -> start the versioned legacy aggregate and all valid ESM entries
  -> await all startup resources with all-settled semantics
  -> prepare and validate PluginModule objects
  -> register accepted providers sequentially in descriptor order
```

An ESM entry default-exports the existing `PluginModule` object. It does not register routes or components through top-level side effects. Providers may import host shared packages and their own chunks but may not directly import another provider.

All provider style loads, the legacy lane, and all ESM imports are started together. Style links are inserted in provider-list order so CSS precedence remains deterministic, while network completion order does not control registration. Halo waits for all startup resources to settle before validating results and registering accepted providers sequentially in provider-list order. It does not reload the page after an individual resource finishes and does not add an arbitrary request batch size, because the UI mounts only after provider setup and batching would extend the startup wait.

Fetch, MIME, link, evaluation, export-shape, and startup-style failures are associated with one provider and do not stop the core UI or other valid providers. A failed provider startup style prevents only its owning provider from registering. Delayed CSS chunk failures remain attributable through the owning asynchronous chunk. The UI presents one summary notification and retains structured provider-specific diagnostics for logs and management screens.

Registration uses a prepare/validate/commit boundary per provider. Existing successful route conflicts retain Vue Router's last-registration-wins behavior. Before replacing a named route, Halo retains enough information to restore the previous route if a later synchronous mutation fails; when reliable restoration is impossible, registration is rejected before mutating the router. The commit records removal or restoration handles for routes, store entries, components, extensions, and other registries where supported. A synchronous commit failure invokes those handles in reverse order before the next provider is registered. Existing framework registries do not make every mutation reversible, so an incomplete rollback is diagnosed and a full page reload remains the final recovery boundary.

Entry import success does not prove that every lazy route chunk can load later. Router errors and asynchronous component boundaries therefore carry provider identity, render a provider-specific failure state where possible, and leave unrelated routes/providers usable. Top-level module side effects, arbitrary timers/event listeners, and later unhandled asynchronous work cannot be transactionally undone; Halo attributes and reports failures it can observe but does not claim complete isolation.

Legacy aggregate behavior is kept intact rather than rewritten into per-provider classic scripts, because changing execution boundaries could itself break existing plugins.

### Use full page reload as the replacement boundary

The browser caches each module URL once per document, and current routes, Pinia stores, components, and FormKit registrations lack a safe general unload protocol. Installing, upgrading, enabling, disabling, or activating a provider therefore requires or prompts a full Console/User Center reload.

Production entry and direct startup-style URLs receive their provider-specific cache key, while legacy aggregate URLs receive the catalog version and emitted chunks and assets use provider-relative content-hashed URLs where supported. Development provider keys remain stable until directly loaded build output changes. Provider descriptor responses are not stored without revalidation. ESM execution never falls back to IIFE after import begins because top-level effects may already have run.

HMR across the Halo/provider boundary is not part of the runtime contract. Provider watch builds may trigger or prompt a full page reload.

### Generate environment-specific Import Maps through the UI build

The same Vite HTML transform injects the Import Map into both Console and User Center before their module entry scripts.

In development:

- the document origin remains Halo on port 8090;
- only Console/User Center HTML is proxied from the UI server on port 3000;
- the Import Map uses absolute port-3000 URLs for Halo UI runtime modules;
- APIs and plugin/theme entries and chunks remain on port 8090;
- browser tests verify CORS, JavaScript MIME types, deep routes, Console, and User Center.

The existing development URL transform only rewrites `src` and `href` attributes, so Import Map values are generated correctly for serve mode rather than relying on string rewriting.

In production, the UI build writes root-relative content-hashed runtime URLs into both HTML entries. Gradle copies those HTML and runtime assets into the application artifact. Production verification starts the packaged application without a UI development server and ensures no development origins remain.

Import Maps remain inline JSON and must follow any page CSP nonce or hash policy. The implementation must not add `unsafe-inline`, `unsafe-eval`, or fetch-and-execute source transformations.

### Preserve the legacy protocol and pursue best-effort dependency compatibility throughout Halo 2.x

Compatibility covers more than locating an IIFE file. Existing providers whose `spec.requires` accepts the running Halo release keep the aggregate endpoints, global module protocol, shared global names, enabled-provider metadata, resource fallback, and known-used export behavior.

Halo uses frozen legacy fixtures and a maintained ecosystem usage sample before upgrading shared libraries. It should retain known-used exports or add a focused bridge where practical, but neither legacy globals nor snapshot version warnings guarantee complete upstream behavioral compatibility. A provider can remain or return to IIFE output when ESM host replacement is unsuitable. `window.VueUse` remains a legacy-only compatibility global and is not offered to ESM providers.

The management UI does not emit a runtime warning merely because a provider is IIFE. Documentation recommends ESM for new 2.26+ targets, while the IIFE build and load paths remain supported for Halo 2.x.

Compatibility code that exists only until legacy IIFE support ends carries an adjacent, actionable removal marker instead of a vague future TODO:

```text
TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
```

This applies to provider module globals (`window[providerName]`), `window.enabledUiPlugins`, the older `window.enabledPlugins` alias, legacy shared-library globals including VueUse, aggregate/alias endpoints, `ui`-to-`console` resource fallback, bundler IIFE/global mappings, and ESM bridges whose only purpose is adapting legacy globals. Exported declarations use `@deprecated` in addition to the source removal comment where applicable. The new shared registration store, provider descriptor, ESM manifest, and Import Map are not legacy removal targets.

## Risks / Trade-offs

- [Version drift can hide incompatible types or behavior] → Validate actual roots and named exports, warn for newer/different-major/namespace use, run representative fixtures, and retain explicit IIFE output.
- [Router 4→5 or FormKit 1→2 works for some providers but not others] → Test real registration and identity operations, report the resolved/host pair, and fail only concrete build/runtime checks rather than claiming universal compatibility.
- [Generated facades drift from actual global builds] → Generate static exports from the host runtime snapshot and load the real browser global builds in build-time and browser identity tests.
- [FormKit remains duplicated through a transitive import] → Build one host-owned FormKit graph, externalize transitive core imports, and test node registry operations across host/provider boundaries.
- [Vite and Rsbuild resolve or emit different graphs] → Share format, snapshot, manifest, and import validation logic and run equivalent dynamic-chunk fixtures through both helpers.
- [Vite treats ESM output as a reusable library and preserves whitespace] → Build the ESM provider as a deployable browser entry with a preserved export signature, verify production minification, and leave IIFE library mode unchanged.
- [Rsbuild debugging IDs inflate production ESM output] → Retain `named` IDs for legacy IIFE behavior but let ESM use Rspack's mode-appropriate production defaults.
- [A provider is built for `ui` but served through the `console` fallback] → Make ESM preload, chunk, CSS, and asset URLs relative to the loaded artifact instead of hard-coding the preferred resource directory.
- [Many providers add startup requests] → Start all direct provider styles and entries together, preserve stylesheet insertion order, isolate failures by owner, and keep asynchronous CSS bundler-managed so startup is not serialized or inflated with chunk styles.
- [User build overrides produce a manifest that lies] → Validate the final resolved build configuration and output graph; fail instead of emitting an inconsistent manifest.
- [Provider state changes between descriptor and resource requests] → Treat full page reload as the replacement boundary, use a version query to invalidate caches, and report individual resource failures; do not claim immutable content across a concurrent upgrade.
- [A provider treats another provider's presence as a direct module dependency] → Expose only reactive identity/version/status metadata, distinguish enabled from registered, and keep module objects and registration ordering outside the public store contract.
- [A provider fails after replacing a named route and partially mutating host registries] → Preserve last-registration-wins on success, restore the replaced route on synchronous failure, reject un-restorable conflicts before mutation, and use reload only for genuinely non-reversible mutations.
- [Development works while the packaged build fails, or the inverse] → Make both live 3000/8090 browser checks and unpacked/started BootJar checks release-blocking.
- [Cross-origin development module fetches fail] → Generate serve-specific absolute URLs and test actual CORS and MIME responses from deep Console and User Center routes.
- [Legacy bundle execution remains all-or-nothing] → Preserve it as an explicit compatibility trade-off; apply provider-level isolation only to the new ESM lane.
- [Historical snapshot data grows or a newer target is unknown] → Publish sparse immutable entries, reuse the latest compatible older baseline with a warning, and require an update only when no eligible snapshot exists or new exports are needed.

## Migration Plan

1. Generate the initial sparse Halo host runtime snapshot, browser runtime bridges, Import Map, and shared-identity tests without producing ESM provider artifacts.
2. Add provider manifest parsing, the versioned provider descriptor, the shared provider-registration store, legacy filtering, and the mixed loader while every existing provider still classifies as legacy.
3. Add matching Vite and Rsbuild ESM output for plugins and themes, root/export validation, version-drift warnings, actionable diagnostics, fixtures, and documentation.
4. Migrate Halo's own provider-availability checks to the shared store, annotate retained IIFE/global compatibility boundaries for Halo 3 removal, and validate frozen legacy artifacts plus one representative ESM plugin and theme across both bundlers.
5. Run the same browser acceptance through the live 3000/8090 development topology and the packaged application before publishing the Halo runtime and bundler-kit snapshot.

Rollback before ESM providers are published is additive: remove the new descriptor and runtime path and keep the legacy aggregate. After adoption, provider authors can explicitly select IIFE as a migration escape hatch, but an ESM-only provider still requires a supporting Halo release as declared by `spec.requires`.

## Open Questions

None. Concrete endpoint names, generated file names, and internal diagnostic field names may be selected during implementation without changing the specified behavior or public compatibility model.
