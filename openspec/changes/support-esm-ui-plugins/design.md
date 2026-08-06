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
- Publish sparse shared dependency inventories that record exact host facts and broad, best-effort provider build ranges.
- Resolve shared dependencies through one host-owned Import Map and preserve identity for stateful frameworks.
- Apply the same ESM contract to Vite and Rsbuild, plugin and theme providers, Console and User Center.
- Reuse the existing plugin and theme static resource mappings and add a provider version query for cache invalidation.
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
- Guaranteeing semantic compatibility for every dependency version admitted by a broad inventory range.
- Fully undoing ESM top-level side effects, arbitrary event handlers, timers, or asynchronous work started by a provider.

## Decisions

### Use `spec.requires` to select the build-time target inventory

Providers continue to declare only `spec.requires`. There is no independent UI runtime version. When the bundler can derive a minimum stable Halo version, that version selects the latest immutable inventory whose Halo baseline is not newer than the target. The selected target is build input and diagnostic context, not a field in the emitted runtime manifest.

For example, `requires: ">=2.26.0"` selects the 2.26.0 inventory. A provider that needs an API introduced by Halo 2.28 raises its minimum required version and builds against the 2.28 inventory.

Inventories are sparse: Halo publishes a new one only when a shared version, accepted range, root export surface, or runtime bridge changes. Multiple Halo releases may reuse one inventory. Each entry records:

- the exact package name and version resolved by the baseline Halo UI build;
- a manually reviewed `range` of provider build versions that Halo will replace on a best-effort basis;
- package-root runtime exports generated from the actual host runtime artifact; and
- the host runtime asset and identity requirements.

For example:

```json
{
  "vue": {
    "version": "3.5.40",
    "range": ">=3.2.0 <4",
    "exports": ["computed", "defineComponent", "ref"]
  }
}
```

The initial compatibility windows are intentionally broad to support the existing provider ecosystem:

| Specifier group | Accepted provider build range |
| --- | --- |
| `vue` | `>=3.2.0 <4` |
| `vue-router` | `>=4 <6` |
| `pinia` | `>=2 <4` |
| `axios` | `>=1 <2` |
| `@formkit/vue`, `@formkit/core` | `>=1 <3` |
| Halo-owned shared packages | `>=2 <3` |

These ranges are admission policy, not proof that every admitted version has identical types or behavior. Stable ranges do not implicitly admit prereleases. The actual host version remains visible in build diagnostics, and a provider built with a version newer than that host version receives an explicit best-effort warning.

The inventory uses these public specifiers:

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

The current inventory is emitted with the Halo UI runtime. Immutable historical inventories are embedded in published bundler-kit releases; builds never fetch a mutable online inventory. If a target is newer than the installed bundler knows, the bundler selects its latest inventory not newer than the target and warns. If no eligible ESM inventory exists, the ESM build fails and points to a bundler update or IIFE output.

Alternative considered: an independently versioned `uiRuntime` contract. It adds a second provider-facing version and implies a stronger ABI guarantee than Halo can make for broad upstream ranges. `spec.requires`, exact host diagnostics, root-export checks, and compatibility fixtures provide a simpler best-effort contract.

### Validate the provider's resolved packages, not declaration ranges

The bundler resolves each shared package from the provider project root and validates the package actually used for type checking and compilation. A declaration such as `^3.5.0` or lockfile text is diagnostic context, not the source of truth.

Resolution is anchored at the provider project or manifest path, not the installed bundler-kit package. The resolver:

1. resolves the package from the provider project using Node-compatible project resolution;
2. locates the owning package root even when `package.json` is not exported;
3. resolves symlinks/workspace links to the real package root;
4. reads and normalizes the exact package name and version;
5. cross-checks the bundler's final resolved entry so aliases or conditional-entry changes cannot bypass validation.

The resolved version must fall inside the selected inventory range. The bundler also rejects missing shared packages, deep imports, unsupported statically named exports, aliases, forks, and configuration that bypasses externalization. Only after those checks does it externalize a shared root import. A namespace import or dynamic namespace property cannot be proven completely, so it is allowed with a best-effort warning. A shared version above the inventory's exact host version is also allowed within range with a warning; a range mismatch fails the ESM build rather than silently bundling a second copy.

Other dependencies, including VueUse, remain private. Direct `@tiptap/*` or `prosemirror-*` runtime imports also remain private, but produce a compatibility warning when the provider uses `@halo-dev/richtext-editor`; authors should prefer the editor package's re-exports to avoid class-identity and version skew.

Error messages and the normal build summary identify the selected Halo inventory, provider root, resolved source and version, exact host version, accepted range, output format, and remediation. Range acceptance is deliberately best-effort: root-export validation catches many concrete incompatibilities, but cannot prove overload, type, default-export-property, or behavioral equivalence.

Alternative considered: exact matching. Local ecosystem sampling showed that exact versions would exclude nearly every existing provider, while broad ranges plus an IIFE escape hatch preserve a practical migration path without claiming complete compatibility.

### Let the bundler select format from `spec.requires`, but let the artifact declare format

Modern Vite and Rsbuild helpers accept `format: "auto" | "iife" | "esm"`, defaulting to `auto`.

- `auto` recognizes a simple stable version or `>=MAJOR.MINOR.PATCH` lower bound and emits ESM when that minimum is at least 2.26.0.
- Missing, wildcard, composite, or otherwise unparseable `spec.requires` values warn and fall back to IIFE. This avoids duplicating Halo's broader Java version-range grammar in Node merely to select an optional output format.
- `iife` ignores target parsing and always remains available as a migration escape hatch.
- `esm` uses the derived target when available; otherwise the author supplies `targetHaloVersion` as an explicit build option.
- Explicit ESM with metadata that still permits Halo older than 2.26 emits a strong warning instead of rewriting or rejecting the provider manifest.
- 2.26 prerelease testing requires explicit ESM selection and an explicit prerelease target.

Once ESM and an inventory have been selected, dependency, import, or output validation failures fail the build. They do not silently change an intended ESM artifact back to IIFE.

Plugin providers read `plugin.yaml`; theme providers read `theme.yaml`, including `spec.requires`. This extends the theme manifest reader, which currently only reads the metadata name.

The host never infers artifact format from `spec.requires`. New bundlers emit an ESM provider manifest; absence of that manifest means legacy IIFE. This preserves artifacts built by older bundlers even when their manifest already requires Halo 2.26 or newer.

The provider manifest is placed at the provider UI output root (`ui/` for plugins and `ui-plugin/dist/` for themes) and contains only fields consumed by the runtime:

```json
{
  "format": "esm",
  "entry": "./main.js",
  "styles": ["./style.css"]
}
```

The build target and actually resolved dependency versions stay in bundler validation and diagnostics. They are not serialized because the target inventory is derived from `spec.requires`, and the runtime cannot independently prove which packages compiled the bundle. Provider identity, activation state, installed version, and compatibility requirements come from Halo, not the provider-controlled manifest. All manifest paths are normalized relative paths and must remain inside the provider resource root.

An existing but malformed manifest is an invalid ESM provider and never falls back to executing a possibly stale IIFE entry.

### Generate a host-owned shared module graph and Import Map

Halo generates the Import Map and installs it before Console/User Center host modules or provider modules resolve shared imports. Providers cannot extend or override it. Only package-root specifiers in the release inventory are public.

During the first migration phase:

- Existing Vue, Vue Router, Pinia, Axios, and Halo UI package globals remain canonical for the host and legacy IIFE providers; generated static ESM bridges export the same functions and objects for ESM providers.
- Halo builds one FormKit runtime graph that exposes both `@formkit/vue` and `@formkit/core` bridges while retaining the compatible `window.FormKitVue` global expected by legacy providers.

Static facades are generated from the inventory export surface and browser-tested against the actual global builds. They cannot dynamically export arbitrary properties from `window`, because ESM named exports must be statically known. A missing global or export fails the Halo UI build.

FormKit requires special treatment. Its core registry is module-instance state, so a bridge over a second FormKit copy would not provide actual sharing. Both `@formkit/vue` and `@formkit/core` are public roots. Other `@formkit/*` packages remain provider-private, but any runtime import they retain to `@formkit/core` is externalized to the host graph. Identity fixtures cover operations such as `getNode`, `submitForm`, and `reset` across host/provider boundaries.

Axios sharing exposes the standard package module, not Halo's configured API client instance. `@halo-dev/api-client` continues exporting its separately created `axiosInstance` for authenticated Halo API calls. Providers use `axios.create()` for isolated custom clients. Mutating the shared Axios default object's defaults/interceptors or the shared API client instance is unsupported because it can affect the host or other providers; identity-safe checks such as `isAxiosError` remain supported.

Alternative considered: point Import Map entries at current npm ESM outputs. Current Halo package outputs retain additional bare imports such as VueUse and Axios, while raw Vue Router, Pinia, and FormKit graphs add internal dependency and identity concerns. Dedicated browser runtime artifacts keep the public dependency graph limited to the inventory.

### Discover providers through one versioned descriptor

The backend classifies the current started plugin set and activated theme when the authenticated provider descriptor is requested. The response contains a version derived from Halo-managed provider identity and version data, versioned legacy aggregate URLs, valid ESM descriptors, and invalid-provider diagnostics. The descriptor is revalidated rather than treated as an immutable resource snapshot.

ESM entries and styles reuse the static mappings Halo already exposes for plugin `ui` or legacy `console` resources and activated-theme `ui-plugin` resources. The version is appended as a query parameter to invalidate cached entry, style, and aggregate responses without copying resources or introducing another proxy path. For example:

```json
{
  "version": "abc123",
  "legacy": {
    "script": "/apis/.../bundle.js?v=abc123",
    "style": "/apis/.../bundle.css?v=abc123"
  },
  "providers": [
    {
      "name": "plugin-search",
      "type": "plugin",
      "entry": "/plugins/plugin-search/assets/ui/main.js?v=abc123",
      "styles": []
    }
  ]
}
```

Plugin bundles continue preferring `ui` and falling back to `console`; the selected directory is reflected in the generated URL. Theme bundles use `/themes/{theme}/ui-plugin/assets/{resource}`. Query parameters do not change path resolution, and emitted asynchronous chunks continue using their provider-relative, content-hashed paths.

The version query is a cache key, not an immutable server-side snapshot. If provider files change after a descriptor is returned, a partially loaded page can observe the newer files. Provider lifecycle changes already use a full page reload as the supported replacement boundary, so the runtime reports a load failure and reload remains the recovery path. The design intentionally avoids resource copying, retained generations, and generation-specific proxy endpoints.

The legacy JavaScript and CSS generated for the current descriptor include only currently classified IIFE providers. Existing aggregate endpoints and compatibility aliases remain available and accept the same version query. Existing globals, `enabledPlugins`, `enabledUiPlugins`, plugin-name ordering, theme module naming, and `ui`-before-`console` resource selection remain compatible for legacy artifacts.

Public `Plugin.status` and `Theme.status` remain unchanged. Descriptor validation and errors remain internal until the diagnostic model is stable.

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

The public surface provides a reactive registration collection plus `get(name)`, `isEnabled(name)`, and `isRegistered(name)`. Record presence means that the UI provider is enabled/discovered in the current descriptor; `registered` means its current-page module commit succeeded. The store is seeded from Halo-owned descriptor metadata before legacy scripts or ESM entries are evaluated, then the loader changes `pending` to `registered` or `failed` as loading and registration settle. It applies equally to plugin and activated-theme providers in Console and User Center.

This distinguishes two existing use cases:

- code that only needs to feature-detect an enabled provider uses `isEnabled`;
- code that needs the provider's UI contribution to be ready observes `isRegistered` reactively instead of relying on registration order.

The host owns mutations. Provider code treats the collection as read-only and must not use Pinia mutation escape hatches to spoof state. This is a public API contract, not a browser security boundary. Failure details stay in internal diagnostics, and the store does not expose another provider's module object, routes, components, or callable implementation. Direct JavaScript dependencies and an ordering contract between providers therefore remain out of scope.

The existing internal `usePluginModuleStore` may continue holding successfully loaded module objects for Halo's own extension-point aggregation, but provider availability checks migrate to the shared metadata store. This provides a format-neutral replacement for checks such as `window.PluginXXX` and `window.enabledUiPlugins` before those globals are eventually removed.

### Load legacy and ESM providers through two coordinated lanes

The UI startup flow becomes:

```text
fetch the current provider descriptor
  -> seed the shared provider-registration store as pending
  -> insert provider styles in descriptor order
  -> load the versioned legacy aggregate once
  -> import valid ESM entries in parallel with all-settled semantics
  -> prepare and validate PluginModule objects
  -> register accepted providers sequentially in descriptor order
```

An ESM entry default-exports the existing `PluginModule` object. It does not register routes or components through top-level side effects. Providers may import host shared packages and their own chunks but may not directly import another provider.

Fetch, MIME, link, evaluation, export-shape, and style failures are associated with one provider and do not stop the core UI or other valid providers. Styles keep descriptor order even when their network requests settle out of order. The UI presents one summary notification and retains structured provider-specific diagnostics for logs and management screens.

Registration uses a prepare/validate/commit boundary per provider. The commit records removal or restoration handles for routes, store entries, components, extensions, and other registries where supported. A synchronous commit failure invokes those handles in reverse order before the next provider is registered. Existing framework registries do not make every mutation reversible, so an incomplete rollback is diagnosed and a full page reload remains the final recovery boundary.

Entry import success does not prove that every lazy route chunk can load later. Router errors and asynchronous component boundaries therefore carry provider identity, render a provider-specific failure state where possible, and leave unrelated routes/providers usable. Top-level module side effects, arbitrary timers/event listeners, and later unhandled asynchronous work cannot be transactionally undone; Halo attributes and reports failures it can observe but does not claim complete isolation.

Legacy aggregate behavior is kept intact rather than rewritten into per-provider classic scripts, because changing execution boundaries could itself break existing plugins.

### Use full page reload as the replacement boundary

The browser caches each module URL once per document, and current routes, Pinia stores, components, and FormKit registrations lack a safe general unload protocol. Installing, upgrading, enabling, disabling, or activating a provider therefore requires or prompts a full Console/User Center reload.

Production entry and style URLs receive the descriptor version query, while emitted chunks and assets use provider-relative content-hashed URLs where supported. Provider descriptor responses are not stored without revalidation. ESM execution never falls back to IIFE after import begins because top-level effects may already have run.

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

Halo uses frozen legacy fixtures and a maintained ecosystem usage sample before upgrading shared libraries. It should retain known-used exports or add a focused bridge where practical, but neither legacy globals nor broad ESM inventory ranges guarantee complete upstream behavioral compatibility. A provider can remain or return to IIFE output when an ESM sharing window is unsuitable. `window.VueUse` remains a legacy-only compatibility global and is not offered to ESM providers.

The management UI does not emit a runtime warning merely because a provider is IIFE. Documentation recommends ESM for new 2.26+ targets, while the IIFE build and load paths remain supported for Halo 2.x.

Compatibility code that exists only until legacy IIFE support ends carries an adjacent, actionable removal marker instead of a vague future TODO:

```text
TODO(Halo 3): Remove after legacy IIFE UI provider support ends.
```

This applies to provider module globals (`window[providerName]`), `window.enabledUiPlugins`, the older `window.enabledPlugins` alias, legacy shared-library globals including VueUse, aggregate/alias endpoints, `ui`-to-`console` resource fallback, bundler IIFE/global mappings, and ESM bridges whose only purpose is adapting legacy globals. Exported declarations use `@deprecated` in addition to the source removal comment where applicable. The new shared registration store, provider descriptor, ESM manifest, and Import Map are not legacy removal targets.

## Risks / Trade-offs

- [Broad ranges admit versions with incompatible types or behavior] → Treat ranges as best-effort, validate actual roots and named exports, warn for forward/namespace use, run representative fixtures, and retain explicit IIFE output.
- [Router 4→5 or FormKit 1→2 works for some providers but not others] → Test real registration and identity operations, report the resolved/host pair, and fail only concrete build/runtime checks rather than claiming universal compatibility.
- [Generated facades drift from actual global builds] → Generate static exports from the inventory and load the real browser global builds in build-time and browser identity tests.
- [FormKit remains duplicated through a transitive import] → Build one host-owned FormKit graph, externalize transitive core imports, and test node registry operations across host/provider boundaries.
- [Vite and Rsbuild resolve or emit different graphs] → Share format, inventory, manifest, and import validation logic and run equivalent dynamic-chunk fixtures through both helpers.
- [User build overrides produce a manifest that lies] → Validate the final resolved build configuration and output graph; fail instead of emitting an inconsistent manifest.
- [Provider state changes between descriptor and resource requests] → Treat full page reload as the replacement boundary, use a version query to invalidate caches, and report individual resource failures; do not claim immutable content across a concurrent upgrade.
- [A provider treats another provider's presence as a direct module dependency] → Expose only reactive identity/version/status metadata, distinguish enabled from registered, and keep module objects and registration ordering outside the public store contract.
- [A provider fails after partially mutating host registries] → Prepare before commit, retain reversible handles, roll back best-effort, attribute the failure, and use reload as the recovery boundary.
- [Development works while the packaged build fails, or the inverse] → Make both live 3000/8090 browser checks and unpacked/started BootJar checks release-blocking.
- [Cross-origin development module fetches fail] → Generate serve-specific absolute URLs and test actual CORS and MIME responses from deep Console and User Center routes.
- [Legacy bundle execution remains all-or-nothing] → Preserve it as an explicit compatibility trade-off; apply provider-level isolation only to the new ESM lane.
- [Historical inventory data grows or a newer target is unknown] → Publish sparse immutable entries, reuse the latest compatible older baseline with a warning, and require an update only when no eligible inventory exists or new exports are needed.

## Migration Plan

1. Generate the initial sparse Halo inventory, browser runtime bridges, Import Map, and shared-identity tests without producing ESM provider artifacts.
2. Add provider manifest parsing, the versioned provider descriptor, the shared provider-registration store, legacy filtering, and the mixed loader while every existing provider still classifies as legacy.
3. Add matching Vite and Rsbuild ESM output for plugins and themes, range/export validation, actionable diagnostics, fixtures, and documentation.
4. Migrate Halo's own provider-availability checks to the shared store, annotate retained IIFE/global compatibility boundaries for Halo 3 removal, and validate frozen legacy artifacts plus one representative ESM plugin and theme across both bundlers.
5. Run the same browser acceptance through the live 3000/8090 development topology and the packaged application before publishing the Halo runtime and bundler-kit inventory.

Rollback before ESM providers are published is additive: remove the new descriptor and runtime path and keep the legacy aggregate. After adoption, provider authors can explicitly select IIFE as a migration escape hatch, but an ESM-only provider still requires a supporting Halo release as declared by `spec.requires`.

## Open Questions

None. Concrete endpoint names, generated file names, and internal diagnostic field names may be selected during implementation without changing the specified behavior or public compatibility model.
