## 1. Simplify Vite preset enforcement

- [x] 1.1 Keep the supported ESM format, relative base, stable startup names, shared-root externals, content-hashed secondary resource patterns, and production optimization in the default Vite preset rather than enforcing them after caller configuration.
- [x] 1.2 Remove Vite checks that reject caller externals, aliases, output formats, public paths, filename overrides, or final emitted assets, including the content-hash probe and bundler-resolution cross-check.
- [x] 1.3 Retain Vite manifest representability checks for one default-exporting entry and at most one startup stylesheet without attempting to validate later caller output mutations.

## 2. Simplify Rsbuild preset enforcement

- [x] 2.1 Keep module output, automatic public path, stable startup names, shared-root externals, content-hashed secondary resource patterns, and production optimization in the default Rsbuild configuration.
- [x] 2.2 Remove Rsbuild preflight and final compiler checks that reject caller aliases, externals, module/output options, public paths, filename overrides, or later asset mutations.
- [x] 2.3 Retain Rsbuild manifest representability checks for the required default-exporting entry and at most one startup stylesheet, including watch-mode manifest emission.

## 3. Reduce shared dependency diagnostics

- [x] 3.1 Simplify syntax-aware import inspection to discover supported shared roots, reject shared-root subpaths, and collect installed package versions without validating imported names or namespace properties.
- [x] 3.2 Remove alias, fork, nested-package, and final bundler-resolution identity enforcement while preserving caller configuration and default root externalization.
- [x] 3.3 Preserve diagnostic-only version behavior: no note for a same-major provider version not newer than the host, one best-effort note for a newer provider version, and one stronger note for a different major without blocking the build.
- [x] 3.4 Keep the host runtime snapshot schema and generator unchanged, because the host ESM bridges still consume its export and runtime metadata.

## 4. Align tests and documentation

- [x] 4.1 Replace Vite and Rsbuild override-rejection and final-hash-proof tests with positive assertions for default plugin and theme ESM output, including minification, manifest structure, relative resources, shared bare imports, and content-hashed secondary resources.
- [x] 4.2 Add focused coverage that native Vite and Rsbuild overrides are merged without bundler-kit policy rejection and that manifest representability and shared-root subpath errors still fail.
- [x] 4.3 Cover all three shared version diagnostic cases, including a same-major older provider that succeeds without a compatibility note.
- [x] 4.4 Update the bundler-kit README to define the default-preset support boundary and explain caller responsibility for bare imports, shared-runtime identity, manifest consistency, resource relocation, and production cache invalidation after raw overrides.

## 5. Verify supported provider builds

- [x] 5.1 Run the bundler-kit unit tests, typecheck, package build, runtime snapshot check, UI format check, and OpenSpec strict validation.
- [x] 5.2 Build real plugin and theme fixtures through the default Vite and Rsbuild helpers and confirm their entries, startup styles, asynchronous chunks, assets, and manifests load from provider-relative paths.
- [x] 5.3 Run an Rsbuild plugin or theme development watch build through at least one rebuild and confirm the default automatic public path and manifest remain usable without caller changes.
