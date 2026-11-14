## Why

The ESM UI provider presets currently try to prove that arbitrary caller Vite and Rsbuild overrides still produce a Halo-compatible bundle. Raw bundler hooks can always mutate configuration or assets after those checks, so the enforcement has become complex, brittle across bundler versions, and unable to provide the guarantee it claims.

The bundler kit should instead guarantee its supported default presets while preserving native caller configuration as an explicit escape hatch whose output, runtime, and cache correctness remain the provider developer's responsibility.

## What Changes

- Limit the supported Vite and Rsbuild ESM contract to builds that use the Halo preset defaults for format, shared externals, resource paths, entry and startup style names, and content-hashed secondary resources.
- Continue merging caller Vite and Rsbuild configuration after the preset without rejecting, rewriting, or heuristically warning about conflicting raw overrides.
- Remove final configuration, bundler-resolution, alias, external, and secondary-resource hash enforcement that attempts to prove overridden output remains compatible.
- Reduce shared dependency inspection to root-import discovery and best-effort installed-version diagnostics; stop validating statically imported exports, namespace usage, or the bundler's final resolved package identity.
- Continue rejecting shared-package deep imports because Halo's Import Map exposes only supported root specifiers.
- Retain manifest structural validation when the emitted output cannot be represented as one ESM provider entry and at most one startup stylesheet.
- Preserve the existing host runtime snapshot schema, backend cache policy, provider APIs, IIFE behavior, and ESM runtime loading behavior.
- Document that raw overrides can produce unresolvable bare imports, duplicate shared runtimes, invalid manifests, or stale secondary resources under Halo's production cache policy.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ui-plugin-bundler-provider`: Restrict the bundler-kit guarantee to its default Vite and Rsbuild presets, allow caller overrides without policy enforcement, and simplify shared dependency diagnostics.
- `ui-plugin-esm-runtime`: Scope the content-hashed secondary-resource guarantee to output produced by the default bundler-kit production presets.

## Impact

- `@halo-dev/ui-plugin-bundler-kit` Vite and Rsbuild preset composition, shared dependency diagnostics, output validation, tests, and README documentation.
- OpenSpec requirements for caller overrides, shared dependency validation, Vite/Rsbuild equivalence, and production cache behavior.
- No public configuration API, provider manifest schema, backend endpoint, authorization rule, database schema, host runtime snapshot schema, or runtime loader behavior changes.
- Existing callers that do not override Halo-owned ESM defaults retain the supported output contract; callers using raw overrides retain their configuration freedom but no longer receive bundler-kit enforcement of the resulting artifact.
