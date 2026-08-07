## 1. Shared Dependency Contract

- [x] 1.1 Define and test structural validation for sparse Halo host runtime snapshots and the minimal ESM provider manifest, including normalized provider-root-relative resource paths.
- [x] 1.2 Generate snapshot host facts from the UI build's actually resolved package roots, exact versions, runtime exports, bridge assets, and singleton requirements.
- [x] 1.3 Record exact host versions as diagnostic facts without claiming accepted provider build ranges.
- [x] 1.4 Validate that snapshots expose exactly the ten supported package roots and exclude VueUse, Tiptap, ProseMirror, and other FormKit subpackages.
- [x] 1.5 Package sparse immutable historical snapshots with the bundler kit and implement latest-eligible-older selection for unknown newer Halo targets.
- [x] 1.6 Add snapshot generation/check tests for actual resolution, export extraction, sparse reuse, and no eligible snapshot diagnostics.

## 2. Host Shared Runtime

- [x] 2.1 Generate content-hashed ESM bridges from existing Vue, Vue Router, Pinia, Axios, and Halo package globals using the snapshot's static export list.
- [x] 2.2 Build one FormKit runtime graph that supplies both `@formkit/vue` and `@formkit/core` bridges and retains the legacy `window.FormKitVue` contract.
- [x] 2.3 Keep `@halo-dev/api-client` on its separately created authenticated `axiosInstance` and add tests that custom `axios.create()` clients do not mutate that instance.
- [x] 2.4 Generate the production Import Map before Console/User Center entries with root-relative content-hashed bridge URLs.
- [x] 2.5 Generate the development Import Map through the Vite HTML transform with absolute port-3000 bridge URLs while APIs and provider resources remain on Halo.
- [x] 2.6 Test Import Map ordering, all ten root mappings, Vue/Router/Pinia identity, FormKit Core registry identity, Axios behavior, deep routes, CORS/MIME responses, and CSP-safe loading.

## 3. Provider Discovery and Backend Delivery

- [x] 3.1 Discover and validate plugin and activated-theme ESM manifests from their UI resource roots, including containment checks for entry and style paths.
- [x] 3.2 Classify a provider with no ESM manifest as legacy and classify an existing malformed or incompatible manifest as invalid without IIFE fallback.
- [x] 3.3 Build a current provider descriptor containing one version, versioned legacy URLs, ESM descriptors, and invalid-provider diagnostics.
- [x] 3.4 Reuse existing plugin `ui`/`console` and theme `ui-plugin` static mappings for ESM resources and append a cache key.
- [x] 3.5 Avoid copied provider resources, retained generations, and generation-specific resource proxy endpoints; use full page reload as the replacement boundary.
- [x] 3.6 Keep the existing aggregate endpoints and aliases while sourcing legacy-only content, globals, metadata, ordering, and `ui` to `console` fallback from current provider classification.
- [x] 3.7 Add backend tests for manifest containment, inactive-theme exclusion, mixed classification, versioned direct resource URLs, and legacy aggregate filtering.

## 4. Console and User Center Loading

- [x] 4.1 Add `stores.uiPlugins()` and public registration metadata types to `@halo-dev/ui-shared`, with reactive `get`, `isEnabled`, and `isRegistered` queries and read-only provider-facing types.
- [x] 4.2 Seed valid providers as pending and invalid providers as failed from the descriptor before module evaluation, then update registration status through host-only lifecycle actions.
- [x] 4.3 Replace direct all-provider aggregate startup with a mixed loader that consumes one provider descriptor, retains the versioned legacy lane, and independently imports ESM entries.
- [x] 4.4 Validate each ESM entry's default export as the existing `PluginModule` contract and initialize accepted modules in stable provider order after imports settle.
- [x] 4.5 Load the descriptor's versioned startup stylesheet and preserve provider CSS order in the backend aggregate.
- [x] 4.6 Add per-provider prepare/validate/commit registration with reversible handles for routes, stores, components, extensions, and other registries where the current APIs support undo.
- [x] 4.7 Roll back supported mutations in reverse order after a synchronous registration failure, mark the provider failed, diagnose incomplete rollback, and continue with later providers.
- [x] 4.8 Attribute entry and delayed route/component chunk failures to their provider through router and asynchronous-component error boundaries.
- [x] 4.9 Isolate observable discovery, fetch, link, evaluation, export, style, registration, and chunk failures while documenting top-level and arbitrary asynchronous side effects as non-transactional.
- [x] 4.10 Emit one user-facing failure summary per startup and retain provider-specific structured diagnostics for logs and management UI inspection.
- [x] 4.11 Apply the same mixed loader and registration store to Console and User Center and keep full-page reload as the lifecycle and final recovery boundary.
- [x] 4.12 Migrate Halo's provider-availability checks from `window.enabledUiPlugins` or module-global presence to the shared metadata store where module access is not required.
- [x] 4.13 Add frontend unit tests for shared-store initialization and reactivity, mixed loading, CSS/registration ordering, rollback, invalid exports, delayed chunks, diagnostics aggregation, and versioned direct resource loading.

## 5. Bundler Target and Dependency Validation

- [x] 5.1 Extend plugin and theme readers to obtain `spec.requires` and recognize simple stable or `>=MAJOR.MINOR.PATCH` targets without trying to reproduce Halo's full Java range grammar.
- [x] 5.2 Add `auto`, `iife`, and `esm` selection to Vite and Rsbuild, including warning-and-IIFE fallback for unparseable auto targets and `targetHaloVersion` for explicit ESM without a derived target.
- [x] 5.3 Resolve each shared dependency from the provider project root, normalize workspace/symlink paths, locate its owning package root, and compare its actual version with the host snapshot for diagnostics.
- [x] 5.4 Cross-check final Vite and Rsbuild resolution so aliases, forks, shared deep imports, conditional entries, and externalization overrides cannot bypass validation.
- [x] 5.5 Validate statically named root exports, allow namespace/dynamic-property use with a warning, and warn when a provider version is newer than or on a different major from the host baseline.
- [x] 5.6 Externalize both FormKit Vue and Core, keep other FormKit packages private while externalizing their Core imports, and keep VueUse private.
- [x] 5.7 Keep direct Tiptap/ProseMirror imports private and warn when they cross the shared rich-text editor boundary.
- [x] 5.8 Emit actionable build summaries and failures containing format reason, provider root, target/snapshot versions, actual and host dependency versions, and remediation.
- [x] 5.9 Add shared validation vectors for simple/unsupported `spec.requires`, sparse snapshot selection, version-drift warnings, namespace warnings, and concrete resolution/export failures.

## 6. Vite and Rsbuild ESM Output

- [x] 6.1 Implement Vite ESM output for plugins and themes with a default-exported `PluginModule`, external shared root imports, provider-root-safe entries, styles, and dynamic chunks.
- [x] 6.2 Implement the equivalent Rsbuild ESM output and enforce the same externalization and final-output checks as Vite.
- [x] 6.3 Emit the minimal ESM provider manifest with format, entry, and at most one optional main style after output validation succeeds, leaving the selected target and resolved dependency versions in build diagnostics.
- [x] 6.4 Preserve existing IIFE output and global mappings for explicit or automatic legacy builds and ensure IIFE builds do not emit an ESM manifest.
- [x] 6.5 Ensure a selected ESM build fails rather than falling back to IIFE after dependency, import, or output validation starts.
- [x] 6.6 Add Vite and Rsbuild fixture tests for plugin/theme builds, automatic fallbacks, explicit targets, resolution/export diagnostics, conflicting configuration, CSS, and dynamic imports.

## 7. Compatibility and End-to-End Verification

- [x] 7.1 Freeze representative previously published plugin and theme IIFE artifacts and verify their bundle endpoints, globals, metadata aliases, resource fallback, and VueUse compatibility on the new runtime.
- [x] 7.2 Maintain one representative ESM plugin, one ESM theme, one legacy plugin, and one legacy theme fixture covering all shared roots, registration-store queries, FormKit Core identity, Axios instance isolation, asynchronous JavaScript, and CSS.
- [x] 7.3 Exercise enabled/registered/failed store transitions, version-drift warnings, concrete resolution/export failures, namespace warnings, invalid manifests, registration rollback, delayed chunks, and provider lifecycle reload recovery.
- [x] 7.4 Run real-browser development-topology acceptance through Halo on port 8090 and the UI server on port 3000, including Console/User Center deep routes, Import Map ordering, resource origins, API calls, and provider chunks.
- [x] 7.5 Build the packaged BootJar and run the same fixture acceptance without a UI development server, verifying versioned/hash-bound packaged assets and the absence of development URLs.
- [x] 7.6 Verify one failed provider does not block other providers, Console, or User Center and that lifecycle changes recover through a full page reload.

## 8. Documentation and Final Checks

- [x] 8.1 Document plugin/theme migration, `stores.uiPlugins()` availability/registration queries, migration from provider globals, format selection and fallback, explicit ESM targeting, sparse snapshot selection, warning-only version drift, and the no-manifest legacy rule.
- [x] 8.2 Document all ten shared roots, FormKit Vue/Core identity, private FormKit subpackages, Axios versus `api-client.axiosInstance`, unsupported shared mutations, private VueUse, and editor-internal warnings.
- [x] 8.3 Document host runtime snapshot generation, package-version-derived output, sparse historical publication/reuse, newer-target fallback, and compatibility fixture maintenance.
- [x] 8.4 Add adjacent `TODO(Halo 3)` removal comments to provider/metadata globals, legacy library globals, aggregate aliases, resource fallback, IIFE bundler mappings, and global-backed ESM bridges; add language deprecation markers where applicable.
- [x] 8.5 Review that permanent ESM/runtime APIs are not marked for removal, then run backend formatting/tests, frontend lint/typecheck/unit tests, bundler fixture tests, strict OpenSpec validation, and both browser acceptance suites.

## 9. Halo 2.26 Release Baseline

- [x] 9.1 Rebase automatic ESM selection, the initial host runtime snapshot, and host bridge lookup from Halo 2.27.0 to Halo 2.26.0.
- [x] 9.2 Update plugin and theme fixtures, prerelease and older-target warnings, snapshot selection diagnostics, and documentation so Halo 2.25 remains automatic IIFE while Halo 2.26 selects ESM.
- [x] 9.3 Regenerate and check the Halo 2.26 snapshot, then rerun backend, frontend, bundler, and strict OpenSpec checks.
- [x] 9.4 Repeat real-browser acceptance in both the proxied UI development topology and the packaged BootJar topology for the Halo 2.26 baseline.

## 10. Simplify Provider Resource Delivery

- [x] 10.1 Replace the immutable snapshot contract with a current versioned provider descriptor in planning artifacts and runtime types.
- [x] 10.2 Add backend and frontend regression coverage for direct static resource URLs with `?v=` and remove generation-eviction expectations.
- [x] 10.3 Remove provider resource copying, retained generations, and generation-specific proxy routes from the backend and frontend loader.
- [x] 10.4 Run focused backend/frontend checks, strict OpenSpec validation, and update the existing pull request.

## 11. Harden Shared Package Resolution

- [x] 11.1 Replace manual shared-package entry inference with maintained exports-aware package metadata resolution while preserving bundler-resolved cross-checks.
- [x] 11.2 Add regression coverage for exports-only packages and run focused bundler-kit plus representative Rsbuild provider verification.

## 12. Simplify Startup CSS and Shared Compatibility Metadata

- [x] 12.1 Change the ESM provider manifest and descriptor to one optional main style and one aggregate startup stylesheet, and add backend coverage for mixed legacy/ESM CSS ordering and invalid-provider exclusion.
- [x] 12.2 Keep asynchronous chunk CSS bundler-managed, keep all ESM entry imports fully parallel with all-settled semantics, register in stable order without per-entry reloads, and cover the behavior in Vite, Rsbuild, and frontend tests.
- [x] 12.3 Replace accepted-range inventories with host runtime snapshots, make version drift warning-only while retaining concrete resolution/export failures, and derive the snapshot baseline/output path from the bundler-kit package version without wiring generation into build or CI.
- [x] 12.4 Update migration documentation and run strict OpenSpec validation, focused backend/frontend/bundler checks, and a representative real provider build.

## 13. Consolidate Bundler Compatibility Diagnostics

- [x] 13.1 Replace per-source best-effort warnings with one deterministic build report containing an aligned shared-dependency table and grouped compatibility notes with concise source labels.
- [x] 13.2 Add focused diagnostic-format coverage, run bundler-kit validation, and verify the resulting output with a representative real Rsbuild provider build.

## 14. Optimize Native ESM Provider Output

- [x] 14.1 Build Vite ESM providers as deployable Rollup entries with preserved entry exports, full production minification, external shared roots, dynamic chunks, and normal asset emission while leaving IIFE library mode unchanged.
- [x] 14.2 Let Rsbuild ESM use mode-appropriate optimized module IDs while retaining the existing IIFE window-library behavior and native production minifiers.
- [x] 14.3 Add Vite/Rsbuild production-output and IIFE regression coverage, run focused bundler checks, strict OpenSpec validation, and representative real provider builds.

## 15. Make ESM Provider Assets Relocatable

- [x] 15.1 Make Vite and Rsbuild ESM preload, chunk, asynchronous CSS, and emitted-asset URLs resolve from the loaded provider artifact while leaving IIFE resource roots unchanged.
- [x] 15.2 Add production fixture coverage for an ESM plugin whose metadata prefers `ui` but whose complete artifact is served through `console`, including Vite module preloads and Rsbuild asynchronous CSS.
- [x] 15.3 Run focused bundler tests, package checks, strict OpenSpec validation, representative real Vite/Rsbuild builds, and the live Halo fallback-resource browser acceptance.

## 16. Generate the Provider Descriptor Client Contract

- [x] 16.1 Declare required provider descriptor fields and provider-type values in the backend OpenAPI schema, then regenerate the committed OpenAPI JSON and `@halo-dev/api-client` models and method.
- [x] 16.2 Expose the generated UI plugin client through `consoleApiClient` and replace the runtime's hand-written descriptor types and raw URL request with the generated contract.
- [x] 16.3 Verify the generated OpenAPI/client diff, focused backend and frontend tests, API client/UI type checking, formatting, and strict OpenSpec validation.
- [x] 16.4 Grant the authenticated role access to the provider descriptor resource and verify ordinary User Center authorization.

## 17. Close Provider Startup and Build-Contract Review Gaps

- [x] 17.1 Replace aggregate startup CSS in the descriptor with ordered direct provider style descriptors, retain the Halo 2.x aggregate endpoint through ordered `@import` rules and a Halo 3 removal marker, isolate backend resource reads from reactive threads, migrate new JSON parsing to Jackson 3, and cover the behavior with backend tests.
- [x] 17.2 Regenerate the OpenAPI document and API client, load all ordered provider styles, ESM entries, and legacy JavaScript concurrently with all-settled semantics, isolate style failures by provider, treat legacy providers without UI modules as successful no-ops, and cover 50-provider startup behavior without eager asynchronous CSS.
- [x] 17.3 Preserve successful last-registration-wins route behavior while restoring a replaced named route after synchronous registration failure and rejecting un-restorable conflicts before mutation, with focused frontend tests.
- [x] 17.4 Reject arbitrary Vite and Rsbuild externals plus conflicting Rsbuild ESM output and entry filename overrides before emitting a misleading manifest, with focused bundler tests.
- [x] 17.5 Align runtime snapshots with actual browser exports, make host bridge lookup reuse the latest eligible sparse snapshot, and distinguish same-core prerelease reuse from genuinely older-snapshot warnings, with generator, host, and bundler tests.
- [x] 17.6 Document the deliberate `ui-plugin.json` reserved output name and compatibility CSS bridge, then run formatting, strict OpenSpec validation, backend/frontend/bundler checks, OpenAPI checks, and feasible development and packaged-runtime acceptance.

## 18. Stabilize Provider Resource Cache Keys

- [x] 18.1 Separate the catalog version used by legacy aggregate resources from provider-specific entry and startup-style cache keys in the runtime contract and design.
- [x] 18.2 Derive packaged provider keys from Halo-managed identity/version data and development provider keys from stable directly loaded resource metadata, with backend regression coverage for unchanged and rebuilt resources.
- [x] 18.3 Run backend formatting and focused tests plus strict OpenSpec validation.

## 19. Consolidate the Provider Descriptor

- [x] 19.1 Replace the parallel registration, style, ESM, and invalid arrays plus exposed catalog version with one authoritative ordered provider list and an optional catalog-versioned legacy script URL in the runtime contract and design.
- [x] 19.2 Refactor backend descriptor projection and tests so each discovered provider appears exactly once with an explicit `legacy`, `esm`, or `invalid` kind and its kind-specific fields.
- [x] 19.3 Regenerate OpenAPI and the UI API client, then consume the unified provider list without cross-array joins while preserving parallel startup, stable registration, failure isolation, and legacy no-op behavior.
- [x] 19.4 Run backend formatting and focused tests, frontend unit/type checks, generated API and runtime snapshot checks, and strict OpenSpec validation.
