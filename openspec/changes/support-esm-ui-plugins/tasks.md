## 1. Shared Dependency Contract

- [x] 1.1 Define and test structural validation for sparse Halo inventories and the minimal ESM provider manifest, including normalized provider-root-relative resource paths.
- [x] 1.2 Generate inventory host facts from the UI build's actually resolved package roots, exact versions, runtime exports, bridge assets, and singleton requirements while keeping accepted build ranges manually reviewed.
- [x] 1.3 Add the initial best-effort ranges for Vue, Vue Router, Pinia, Axios, FormKit Vue/Core, and the four Halo-owned shared packages.
- [x] 1.4 Validate that inventories expose exactly the ten supported package roots and exclude VueUse, Tiptap, ProseMirror, and other FormKit subpackages.
- [x] 1.5 Package sparse immutable historical inventories with the bundler kit and implement latest-eligible-older selection for unknown newer Halo targets.
- [x] 1.6 Add inventory generation/check tests for actual resolution, export extraction, prerelease exclusion, sparse reuse, and no eligible inventory diagnostics.

## 2. Host Shared Runtime

- [x] 2.1 Generate content-hashed ESM bridges from existing Vue, Vue Router, Pinia, Axios, and Halo package globals using the inventory's static export list.
- [x] 2.2 Build one FormKit runtime graph that supplies both `@formkit/vue` and `@formkit/core` bridges and retains the legacy `window.FormKitVue` contract.
- [x] 2.3 Keep `@halo-dev/api-client` on its separately created authenticated `axiosInstance` and add tests that custom `axios.create()` clients do not mutate that instance.
- [x] 2.4 Generate the production Import Map before Console/User Center entries with root-relative content-hashed bridge URLs.
- [x] 2.5 Generate the development Import Map through the Vite HTML transform with absolute port-3000 bridge URLs while APIs and provider resources remain on Halo.
- [x] 2.6 Test Import Map ordering, all ten root mappings, Vue/Router/Pinia identity, FormKit Core registry identity, Axios behavior, deep routes, CORS/MIME responses, and CSP-safe loading.

## 3. Provider Discovery and Backend Delivery

- [x] 3.1 Discover and validate plugin and activated-theme ESM manifests from their UI resource roots, including containment checks for entry and style paths.
- [x] 3.2 Classify a provider with no ESM manifest as legacy and classify an existing malformed or incompatible manifest as invalid without IIFE fallback.
- [x] 3.3 Build an immutable provider snapshot from one plugin/theme classification and expose an authenticated descriptor containing generation-bound legacy URLs, ESM descriptors, and invalid-provider diagnostics.
- [x] 3.4 Serve legacy JavaScript/CSS and ESM provider resources through generation-bound URLs that never substitute another generation's content.
- [x] 3.5 Invalidate snapshots on plugin and theme lifecycle changes and retain at least the current and immediately previous generation in a bounded cache.
- [x] 3.6 Keep the existing aggregate endpoints and aliases while sourcing legacy-only content, globals, metadata, ordering, and `ui` to `console` fallback from the same snapshot.
- [x] 3.7 Add backend tests for manifest containment, inactive-theme exclusion, mixed classification, concurrent state changes, previous-generation completion, eviction failure, and aggregate consistency.

## 4. Console and User Center Loading

- [x] 4.1 Add `stores.uiPlugins()` and public registration metadata types to `@halo-dev/ui-shared`, with reactive `get`, `isEnabled`, and `isRegistered` queries and read-only provider-facing types.
- [x] 4.2 Seed valid providers as pending and invalid providers as failed from the snapshot before module evaluation, then update registration status through host-only lifecycle actions.
- [x] 4.3 Replace direct all-provider aggregate startup with a mixed loader that consumes one provider snapshot, retains the generation-bound legacy lane, and independently imports ESM entries.
- [x] 4.4 Validate each ESM entry's default export as the existing `PluginModule` contract and initialize accepted modules in stable provider order after imports settle.
- [x] 4.5 Insert legacy and ESM styles in descriptor order and isolate individual ESM style failures without letting network settlement reorder CSS precedence.
- [x] 4.6 Add per-provider prepare/validate/commit registration with reversible handles for routes, stores, components, extensions, and other registries where the current APIs support undo.
- [x] 4.7 Roll back supported mutations in reverse order after a synchronous registration failure, mark the provider failed, diagnose incomplete rollback, and continue with later providers.
- [x] 4.8 Attribute entry and delayed route/component chunk failures to their provider through router and asynchronous-component error boundaries.
- [x] 4.9 Isolate observable discovery, fetch, link, evaluation, export, style, registration, and chunk failures while documenting top-level and arbitrary asynchronous side effects as non-transactional.
- [x] 4.10 Emit one user-facing failure summary per startup and retain provider-specific structured diagnostics for logs and management UI inspection.
- [x] 4.11 Apply the same mixed loader and registration store to Console and User Center and keep full-page reload as the lifecycle and final recovery boundary.
- [x] 4.12 Migrate Halo's provider-availability checks from `window.enabledUiPlugins` or module-global presence to the shared metadata store where module access is not required.
- [x] 4.13 Add frontend unit tests for shared-store initialization and reactivity, mixed loading, CSS/registration ordering, rollback, invalid exports, delayed chunks, diagnostics aggregation, generation eviction, and reload behavior.

## 5. Bundler Target and Dependency Validation

- [x] 5.1 Extend plugin and theme readers to obtain `spec.requires` and recognize simple stable or `>=MAJOR.MINOR.PATCH` targets without trying to reproduce Halo's full Java range grammar.
- [x] 5.2 Add `auto`, `iife`, and `esm` selection to Vite and Rsbuild, including warning-and-IIFE fallback for unparseable auto targets and `targetHaloVersion` for explicit ESM without a derived target.
- [x] 5.3 Resolve each shared dependency from the provider project root, normalize workspace/symlink paths, locate its owning package root, and compare its actual version with the inventory range.
- [x] 5.4 Cross-check final Vite and Rsbuild resolution so aliases, forks, shared deep imports, conditional entries, and externalization overrides cannot bypass validation.
- [x] 5.5 Validate statically named root exports, allow namespace/dynamic-property use with a warning, and warn when an admitted provider version is newer than the host baseline.
- [x] 5.6 Externalize both FormKit Vue and Core, keep other FormKit packages private while externalizing their Core imports, and keep VueUse private.
- [x] 5.7 Keep direct Tiptap/ProseMirror imports private and warn when they cross the shared rich-text editor boundary.
- [x] 5.8 Emit actionable build summaries and failures containing format reason, provider root, target/inventory versions, actual and host dependency versions, accepted ranges, and remediation.
- [x] 5.9 Add shared validation vectors for simple/unsupported `spec.requires`, sparse inventory selection, all admitted ranges, forward warnings, namespace warnings, and range/export failures.

## 6. Vite and Rsbuild ESM Output

- [x] 6.1 Implement Vite ESM output for plugins and themes with a default-exported `PluginModule`, external shared root imports, provider-root-safe entries, styles, and dynamic chunks.
- [x] 6.2 Implement the equivalent Rsbuild ESM output and enforce the same externalization and final-output checks as Vite.
- [x] 6.3 Emit the minimal ESM provider manifest with only format, entry, and styles after output validation succeeds, leaving the selected target and resolved dependency versions in build diagnostics.
- [x] 6.4 Preserve existing IIFE output and global mappings for explicit or automatic legacy builds and ensure IIFE builds do not emit an ESM manifest.
- [x] 6.5 Ensure a selected ESM build fails rather than falling back to IIFE after dependency, import, or output validation starts.
- [x] 6.6 Add Vite and Rsbuild fixture tests for plugin/theme builds, automatic fallbacks, explicit targets, range/export diagnostics, conflicting configuration, CSS, and dynamic imports.

## 7. Compatibility and End-to-End Verification

- [x] 7.1 Freeze representative previously published plugin and theme IIFE artifacts and verify their bundle endpoints, globals, metadata aliases, resource fallback, and VueUse compatibility on the new runtime.
- [x] 7.2 Maintain one representative ESM plugin, one ESM theme, one legacy plugin, and one legacy theme fixture covering all shared roots, registration-store queries, FormKit Core identity, Axios instance isolation, asynchronous JavaScript, and CSS.
- [x] 7.3 Exercise enabled/registered/failed store transitions, in-range older versions, admitted forward versions and warnings, range/export failures, namespace warnings, invalid manifests, registration rollback, delayed chunks, and snapshot generation races.
- [x] 7.4 Run real-browser development-topology acceptance through Halo on port 8090 and the UI server on port 3000, including Console/User Center deep routes, Import Map ordering, resource origins, API calls, and provider chunks.
- [x] 7.5 Build the packaged BootJar and run the same fixture acceptance without a UI development server, verifying generation/hash-bound packaged assets and the absence of development URLs.
- [x] 7.6 Verify one failed provider does not block other providers, Console, or User Center and that lifecycle changes recover through a full page reload.

## 8. Documentation and Final Checks

- [x] 8.1 Document plugin/theme migration, `stores.uiPlugins()` availability/registration queries, migration from provider globals, format selection and fallback, explicit ESM targeting, sparse Inventory selection, best-effort ranges, and the no-manifest legacy rule.
- [x] 8.2 Document all ten shared roots, FormKit Vue/Core identity, private FormKit subpackages, Axios versus `api-client.axiosInstance`, unsupported shared mutations, private VueUse, and editor-internal warnings.
- [x] 8.3 Document Inventory host-fact generation, manual range review, sparse historical publication/reuse, newer-target fallback, and compatibility fixture maintenance.
- [x] 8.4 Add adjacent `TODO(Halo 3)` removal comments to provider/metadata globals, legacy library globals, aggregate aliases, resource fallback, IIFE bundler mappings, and global-backed ESM bridges; add language deprecation markers where applicable.
- [x] 8.5 Review that permanent ESM/runtime APIs are not marked for removal, then run backend formatting/tests, frontend lint/typecheck/unit tests, bundler fixture tests, strict OpenSpec validation, and both browser acceptance suites.

## 9. Halo 2.26 Release Baseline

- [x] 9.1 Rebase automatic ESM selection, the initial shared dependency inventory, and host bridge lookup from Halo 2.27.0 to Halo 2.26.0.
- [x] 9.2 Update plugin and theme fixtures, prerelease and older-target warnings, inventory selection diagnostics, and documentation so Halo 2.25 remains automatic IIFE while Halo 2.26 selects ESM.
- [x] 9.3 Regenerate and check the Halo 2.26 inventory, then rerun backend, frontend, bundler, and strict OpenSpec checks.
- [x] 9.4 Repeat real-browser acceptance in both the proxied UI development topology and the packaged BootJar topology for the Halo 2.26 baseline.
