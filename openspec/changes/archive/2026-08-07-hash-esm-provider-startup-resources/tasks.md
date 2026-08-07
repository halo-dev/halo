## 1. Lock down startup resource behavior

- [x] 1.1 Add real Vite and Rsbuild build assertions that default ESM manifests reference content-hashed entry and startup-style filenames.
- [x] 1.2 Add a Vite regression assertion that hashed chunks referencing entry exports use the same content-hashed entry path rather than a stable `main.js` alias.
- [x] 1.3 Add backend descriptor assertions that ESM entry and style URLs omit query cache keys while legacy URLs retain them.

## 2. Emit content-addressed ESM startup resources

- [x] 2.1 Configure default Vite ESM entry filenames with content hashes while preserving stable IIFE output.
- [x] 2.2 Configure default Rsbuild ESM entry and startup-style filenames with content hashes while preserving stable IIFE output.
- [x] 2.3 Derive the Rsbuild ESM manifest entry and optional startup style from the actual `main` compilation entrypoint files.

## 3. Canonicalize runtime URLs

- [x] 3.1 Return manifest-selected ESM entry and style paths without query cache keys while preserving cache keys for legacy resources and aggregates.
- [x] 3.2 Update cache-boundary documentation for content-addressed ESM startup resources and caller overrides.

## 4. Verification

- [x] 4.1 Run focused bundler-kit tests, typecheck, package build, backend service tests, formatting, and OpenSpec strict validation.
- [x] 4.2 Build real Vite and Rsbuild plugin/theme projects and verify manifests, reverse entry imports, and development watch rebuilds.
