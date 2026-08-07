## Context

Halo currently loads an ESM provider entry through the descriptor as `main.js?v=<provider-cache-key>`. Vite can generate hashed asynchronous chunks that statically import bindings from `../main.js`; because URL queries participate in browser module identity, the descriptor entry and the chunk reference identify different modules. Production static resources are cached for one year, so the queryless reference can also reuse an older stable entry.

The existing `ui-plugin.json` already records arbitrary provider-relative entry and style paths. Vite's ESM manifest plugin already reads the emitted entry filename, while Rsbuild still assumes `main.js`.

## Goals / Non-Goals

**Goals:**

- Give default Vite and Rsbuild ESM startup JavaScript content-hashed names.
- Make every reference to the ESM entry resolve to one canonical queryless URL.
- Record actual Vite and Rsbuild entry and startup-style filenames in `ui-plugin.json`.
- Preserve IIFE output and caller APIs.

**Non-Goals:**

- Enforcing hashed filenames after raw caller overrides.
- Changing the provider manifest schema or legacy aggregate endpoints.
- Hot-replacing provider modules after a build.

## Decisions

### Use content-hashed ESM startup filenames

The default Vite entry pattern becomes `main.[hash].js`. The default Rsbuild entry and startup-style patterns become `main.[contenthash:8].js` and `style.[contenthash:8].css`. This follows the bundlers' native cache-busting model and lets `ui-plugin.json` select the current artifact without a stable alias.

Stable IIFE startup names remain unchanged because legacy Halo versions and aggregate loading depend on them.

### Derive manifests from emitted entrypoint files

Vite continues using its output chunk metadata. Rsbuild obtains JavaScript and CSS startup files from the `main` compilation entrypoint, requires exactly one JavaScript entry and at most one CSS startup file, validates the actual JavaScript asset, and writes those paths to the provider manifest.

This avoids an additional Vite or Rsbuild manifest and keeps `ui-plugin.json` as the only provider contract.

### Omit query cache keys from ESM startup URLs

The backend emits ESM manifest resource paths without a query parameter. A chunk reverse-importing the entry therefore uses the exact same URL that Halo initially imports. Content hashes provide production invalidation; development profiles already serve static resources with `no-cache`, and watched builds change the manifest filename when entry content changes.

Legacy JavaScript, legacy styles, and aggregate bundle URLs retain their existing query cache keys.

## Risks / Trade-offs

- **Caller overrides restore stable ESM names** → Preserve the previously documented escape hatch; production cache correctness remains the provider developer's responsibility.
- **A provider package contains stale hashed files** → The descriptor references only the manifest-selected entry, and default builds clean their output directory.
- **Hashed filenames change existing artifact assertions** → Update tests and consumers to read `ui-plugin.json` rather than assume `main.js` for ESM.
