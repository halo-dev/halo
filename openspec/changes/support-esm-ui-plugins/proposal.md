## Why

Halo currently loads plugin and active-theme UI modules as concatenated IIFE bundles whose modules and shared dependencies are exposed through global variables. This prevents standard ESM chunk loading and independent caching, while leaving providers unable to check whether their resolved build dependencies fall within the compatibility window offered by their target Halo release.

This change turns the ESM direction raised in [halo-dev/halo#10205](https://github.com/halo-dev/halo/issues/10205) into one compatibility-preserving contract for both plugins and themes, which already use the same UI module contract and bundler toolchain.

## What Changes

- Add native ESM build and loading support for both plugin-provided and theme-provided Console/User Center UI modules, including asynchronous JavaScript and CSS chunks.
- Keep existing IIFE bundles, global variables, compatibility endpoints, and legacy resource-directory fallback operational throughout Halo 2.x so existing artifacts run without rebuilding.
- Generate a minimal provider manifest that identifies ESM output, one entry module, and at most one startup stylesheet; asynchronous chunk styles remain bundler-managed and providers without a manifest remain legacy IIFE providers.
- Publish sparse immutable host runtime snapshots that record a Halo baseline's resolved shared-package versions and available root exports; use version differences as best-effort warnings while still rejecting concrete resolution, identity, deep-import, and missing-export incompatibilities.
- Provide a host-owned Import Map that resolves the supported shared package roots to one Halo-supplied runtime instance while bundling non-shared dependencies, including VueUse, into each ESM provider.
- Add an authenticated provider descriptor that represents each plugin or theme exactly once in one ordered provider list, references existing static resource mappings with stable provider-specific cache keys, and drives a mixed loader that starts styles and ESM entries in parallel before preserving stable registration order.
- Expose host-owned UI provider availability and registration state through a shared Pinia store in `@halo-dev/ui-shared`, replacing new dependencies on provider globals without exposing another provider's module implementation.
- Make ESM the modern bundler default when the provider's minimum required stable Halo version is 2.26.0 or newer, with explicit `auto`, `iife`, and `esm` selection modes.
- Mark every retained compatibility boundary intended to disappear with legacy IIFE support using an explicit Halo 3 removal comment.
- Verify the feature in both the proxied UI development topology and the packaged BootJar production topology.

## Capabilities

### New Capabilities

- `ui-plugin-esm-runtime`: Defines ESM provider manifests, host runtime snapshots and Import Map behavior, mixed legacy/ESM loading, best-effort compatibility policy, failure isolation, and development/production runtime behavior for plugin and theme UI modules.

### Modified Capabilities

- `ui-plugin-bundler-provider`: Extends the Vite and Rsbuild provider presets with Halo-version-based format selection, ESM output, generated manifests, shared dependency validation, and equivalent plugin/theme behavior.

## Impact

- Frontend runtime and build integration under `ui/src/setup`, `ui/src/vite`, and the Console/User Center HTML entry pipeline.
- Public shared Pinia provider-registration metadata under `@halo-dev/ui-shared`; direct JavaScript dependencies between providers remain unsupported.
- `@halo-dev/ui-plugin-bundler-kit` Vite and Rsbuild presets, provider manifest parsing, dependency resolution, output validation, tests, and documentation.
- Backend UI plugin bundle services and endpoints in `application/`, plus existing plugin and theme static resource discovery.
- Generated Halo UI runtime bridges and sparse host runtime snapshots for Vue, Vue Router, Pinia, Axios, FormKit Vue/Core, and public Halo UI packages.
- Authenticated UI provider metadata responses; existing same-origin static asset authorization remains unchanged.
- No database migration and no change to the public `Plugin.status` or `Theme.status` models are required.
