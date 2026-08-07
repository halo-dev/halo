## Why

Vite may emit asynchronous chunks that statically import bindings from the provider entry. The current stable `main.js` filename is loaded by Halo with a cache-key query but referenced by chunks without that query, producing distinct browser module URLs and allowing a long-lived cached entry to be fetched or evaluated again.

## What Changes

- Give default Vite and Rsbuild ESM startup JavaScript content-hashed filenames and record the actual emitted entry in `ui-plugin.json`.
- Give the default Rsbuild ESM startup stylesheet a content-hashed filename for parity with Vite and record its actual path in the manifest.
- Serve ESM entry and startup-style URLs without Halo's query cache key so every import in the provider module graph resolves to one canonical URL.
- Retain stable `main.js`, legacy cache keys, and existing globals for IIFE providers.
- Keep raw caller overrides as an advanced escape hatch whose cache correctness remains the provider developer's responsibility.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `ui-plugin-bundler-provider`: Default ESM startup resources become content hashed and manifests use their actual emitted filenames.
- `ui-plugin-esm-runtime`: Default ESM startup resources are served by canonical content-addressed URLs without query cache keys.

## Impact

- Vite and Rsbuild defaults and ESM manifest generation in `ui-plugin-bundler-kit`.
- Provider descriptor URL generation and focused backend tests.
- Existing IIFE artifacts, provider source APIs, manifest schema, and shared dependencies are unchanged.
