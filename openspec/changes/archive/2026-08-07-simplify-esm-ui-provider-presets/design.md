## Context

The ESM UI provider work added Vite and Rsbuild presets that produce a stable entry, an optional startup stylesheet, provider-relative asynchronous resources, shared bare imports, and `ui-plugin.json`. It also added increasingly deep validation of caller configuration, final bundler configuration, resolved package identity, imported exports, and emitted asset filenames.

That enforcement boundary cannot be closed while `viteConfig` and `rsbuildConfig` intentionally expose native configuration and plugin hooks. A caller plugin can mutate output after an earlier plugin's final-looking hook, and bundler APIs do not provide a stable cross-version proof that a filename came from content rather than merely resembling a hash. Continuing to emulate such a proof makes the preset more coupled to Vite, Rollup, Rsbuild, and Rspack internals without making arbitrary overrides safe.

Halo still needs a reliable supported path: an unmodified preset must produce relocatable and cache-safe ESM output for both plugins and themes. Advanced callers also need to retain native bundler configuration without the helper silently discarding it.

## Goals / Non-Goals

**Goals:**

- Make the supported contract the output produced by the default Halo Vite and Rsbuild presets.
- Preserve existing caller configuration APIs and native merge order.
- Remove enforcement that attempts to prove arbitrary raw overrides remain compatible.
- Keep shared dependency version reporting useful and best-effort without statically policing package exports or aliases.
- Keep default plugin and theme ESM output equivalent, optimized, relocatable, and cache-safe.
- Reduce negative test combinations in favor of positive default-output and real-provider integration tests.

**Non-Goals:**

- Making arbitrary Vite, Rollup, Rsbuild, or Rspack overrides safe.
- Detecting or warning about every override that can invalidate the provider artifact.
- Changing the provider manifest, descriptor API, runtime loader, backend cache headers, or Import Map.
- Changing automatic ESM/IIFE selection or legacy IIFE compatibility.
- Removing host runtime snapshot exports or bridge metadata.
- Introducing accepted dependency ranges, a runtime contract version, or semantic compatibility guarantees.

## Decisions

### Guarantee preset defaults, not arbitrary final output

The helpers will continue to merge caller Vite and Rsbuild configuration after Halo defaults using the bundlers' native configuration semantics. The helper will not reject, rewrite, or heuristically warn about caller overrides to entry, format, public path, externals, aliases, output filenames, or raw bundler hooks.

The supported ESM contract applies when callers leave the relevant preset defaults intact. Raw overrides are an escape hatch: the provider developer owns the resulting manifest consistency, browser module resolution, shared-runtime identity, resource relocation, and cache invalidation.

The preset itself will continue to configure:

- one provider entry and at most one startup stylesheet;
- native ESM/module output;
- a relative Vite base or automatic Rsbuild public path;
- Halo shared roots as bare external imports;
- stable `main.js` and `style.css` startup names;
- content-hashed asynchronous JavaScript, CSS, and assets;
- production minification and provider manifest emission.

Alternative considered: apply Halo-owned values after caller configuration. This would make defaults harder to break but would silently ignore legitimate advanced configuration and contradict the existing merge contract.

Alternative considered: retain final-output validation and add more late hooks. Raw hooks remain order-dependent, and each bundler version can introduce another mutation boundary. This adds complexity without establishing a durable guarantee.

### Keep only lightweight shared dependency diagnostics

Syntax-aware import discovery will continue using `es-module-lexer` so comments and string literals are not mistaken for dependencies. For an imported shared root, maintained package-resolution and semver libraries will read the provider's installed version and compare it with the selected Halo snapshot when available.

Version behavior remains diagnostic-only:

- the same major with a provider version not newer than the host needs no compatibility note;
- a newer provider version in the same major emits one best-effort note;
- a different major emits one stronger best-effort note;
- version differences never decide externalization or fail the build.

The validator will stop checking statically imported export names, namespace properties, aliases, forks, nested final resolution, and bundler-resolved package identity. Explicit aliases and external mappings belong to the caller escape hatch.

Shared-root subpaths remain unsupported and fail the build. Halo's Import Map exposes exact root specifiers only; silently bundling a deep Vue-family entry would make the default preset violate its shared-instance contract. Non-shared packages, including FormKit packages outside the shared roots and editor internals, remain provider-private without additional policy diagnostics.

The runtime snapshot schema stays unchanged. Its export list and bridge/global fields are still required to generate the host's static ESM bridge modules even though provider source exports are no longer checked against that list.

### Retain only manifest representability checks

The ESM integration will still fail when it cannot emit a truthful provider manifest at the point its manifest hook runs. This includes a missing or ambiguous entry, an entry without the required default `PluginModule` export, multiple startup styles where the manifest supports one, or invalid manifest paths or fields.

These checks protect the manifest schema rather than policing arbitrary caller configuration. A later caller hook that mutates files after manifest generation remains the caller's responsibility.

### Keep the production cache policy and default hashed resources

Halo production plugin and theme resources remain long-lived cacheable resources. Provider descriptor cache keys continue to version the stable entry and startup stylesheet, while the default presets continue to put content hashes in asynchronous resource filenames.

The bundler kit will not inspect emitted assets to prove a hash is genuine. A caller that emits or configures stable secondary filenames accepts the risk that the entry query does not propagate to relative chunks, CSS, or assets. This risk will be documented rather than shifted to every correctly hashed provider by weakening backend caching.

Development profiles already disable caching, so no development cache change is required.

### Test the supported path instead of adversarial overrides

Tests will assert the observable output of default plugin and theme presets for both bundlers: entry/default export, startup style, manifest, shared bare imports, relocatable asynchronous resources, content-hashed secondary resources, and production minification. Watch-mode coverage remains for Rsbuild development builds.

One focused test per helper will establish that caller raw configuration is merged and not rejected by policy enforcement. Tests that enumerate conflicting aliases, externals, filenames, output formats, and late hooks will be removed because those outcomes are intentionally outside the supported preset contract.

Real plugin and theme builds will verify that the default presets work through both Vite and Rsbuild without changing existing helper invocations.

## Risks / Trade-offs

- [Caller overrides can produce runtime failures or stale cached resources] → Document the escape-hatch boundary and keep safe defaults for the common path.
- [Removing export checks moves some incompatibilities from build time to browser evaluation] → Retain host bridge generation checks and version diagnostics; providers that require a newer surface must continue raising `spec.requires` and using an updated bundler snapshot.
- [Missing or aliased packages may make version reporting incomplete] → Treat the report as best-effort and let native bundler behavior plus the caller's explicit configuration determine the artifact.
- [Fewer negative tests can miss regressions in default output] → Replace them with direct assertions over complete default output and real provider builds rather than configuration-shape branches.
- [Deep imports remain one intentional restriction] → Keep the rule narrow to shared-root subpaths that cannot be represented by Halo's root-only Import Map.

## Migration Plan

1. Update the two affected specifications to define the preset-only guarantee and caller-owned override boundary.
2. Remove Vite and Rsbuild final configuration, resolution, alias, external, and emitted-resource enforcement.
3. Simplify shared dependency inspection and diagnostics while retaining deep-import rejection and snapshot selection.
4. Replace rejection-oriented tests with default-output and override-passthrough coverage.
5. Update the bundler-kit README with the supported preset contract and production cache warning.
6. Validate focused package tests and real Vite/Rsbuild plugin and theme builds.

Rollback restores the previous validation code and specification requirements; no stored data, public API, or runtime migration is involved.

## Open Questions

None.
