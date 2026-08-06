# Shared dependency package resolution research

Date: 2026-08-06

## Question

`ui-plugin-bundler-kit` currently finds an installed shared package, reads its
`package.json`, then guesses its entry from `module`, `main`, or `index.js`.
That fails for exports-only packages such as Pinia 4: the package is installed
and resolvable by Rspack, but the guessed `index.js` does not exist.

This note checks how `node-modules-inspector` and Vite DevTools avoid this
problem, then compares reusable resolvers for Halo. All repository links below
are pinned to the inspected commit.

## Findings

### `node-modules-inspector`: trust the package manager's package root

Inspected `node-modules-inspector` 2.3.3 at commit
[`4a87a17`](https://github.com/antfu/node-modules-inspector/tree/4a87a17bc9938f0a08728f103e562dc18441637e).

Its production dependency inventory does not resolve a package import and does
not infer an entry file. For pnpm it executes `pnpm ls --json`, whose result
contains the installed package `path`; that path becomes the package node's
`filepath` ([pnpm adapter](https://github.com/antfu/node-modules-inspector/blob/4a87a17bc9938f0a08728f103e562dc18441637e/packages/node-modules-tools/src/agents/pnpm/list.ts#L76-L107),
[path normalization](https://github.com/antfu/node-modules-inspector/blob/4a87a17bc9938f0a08728f103e562dc18441637e/packages/node-modules-tools/src/agents/pnpm/list.ts#L157-L181)).
It then reads `${filepath}/package.json` directly
([source](https://github.com/antfu/node-modules-inspector/blob/4a87a17bc9938f0a08728f103e562dc18441637e/packages/node-modules-tools/src/resolve.ts#L48-L80)).
Therefore pnpm's virtual-store and symlink layout are interpreted by pnpm, not
reimplemented by the inspector.

There is also a useful exports-safe pattern in its tests. It first asks `mlly`
to resolve the package entry. If `pkg/package.json` is hidden by the package's
exports map, it calls `pkg-types.resolvePackageJSON(resolvedEntry)` to walk from
the real entry back to the nearest package metadata
([test helper](https://github.com/antfu/node-modules-inspector/blob/4a87a17bc9938f0a08728f103e562dc18441637e/packages/node-modules-tools/test/module-type.test.ts#L1-L16)).
This is test support, not the inspector's production inventory algorithm, but
it demonstrates the correct fallback for exports-only packages.

### Vite DevTools: preserve the bundler's resolved IDs

Inspected Vite DevTools 0.4.12 at commit
[`4fdf7b7`](https://github.com/vitejs/devtools/tree/4fdf7b70f3456264dfa25013461a676cc44de56d).

Vite DevTools does not run a second package resolver for its Vite module graph.
It wraps each Vite plugin's `resolveId` hook and records the returned `id`
([source](https://github.com/vitejs/devtools/blob/4fdf7b70f3456264dfa25013461a676cc44de56d/packages/vite/src/node/inspect/hijack.ts#L150-L191)).
In development it reads dependencies from Vite's `moduleGraph`; during build it
uses Rollup's `getModuleInfo().importedIds`
([source](https://github.com/vitejs/devtools/blob/4fdf7b70f3456264dfa25013461a676cc44de56d/packages/vite/src/node/inspect/module.ts#L9-L47)).
Its own `resolveIdRecursive` only follows the already-recorded resolution chain
([source](https://github.com/vitejs/devtools/blob/4fdf7b70f3456264dfa25013461a676cc44de56d/packages/vite/src/node/inspect/context.ts#L388-L401)).

This means exports conditions, aliases, extensions, and pnpm symlinks remain the
responsibility of the active bundler resolver. Its regression tests explicitly
retain a resolved `.pnpm/.../node_modules/.../dist/index.mjs` ID
([test](https://github.com/vitejs/devtools/blob/4fdf7b70f3456264dfa25013461a676cc44de56d/packages/vite/src/node/__tests__/inspect-context.test.ts#L954-L977)).
The Rolldown integration goes further: `@rolldown/debug` supplies authoritative
`package_root`, version, and module paths, and DevTools consumes those values
([source](https://github.com/vitejs/devtools/blob/4fdf7b70f3456264dfa25013461a676cc44de56d/packages/rolldown/src/node/rpc/functions/rolldown-get-packages.ts#L122-L165)).

Neither inspected project directly imports `oxc-resolver` for this task. Vite
DevTools uses `mlly` for import analysis/data URLs and `exsolve` in unrelated
build scripts; those dependencies are not the source of its package graph.

### Why `module || main || index.js` is invalid

Node.js documents that `exports` is a modern, conditional entry mechanism and
takes precedence over `main`; it may also hide `pkg/package.json`
([Node.js 26.5.1 package entry points](https://nodejs.org/api/packages.html#package-entry-points)).
Consequently, reading `module`/`main` and inventing `index.js` is not a valid
approximation of package resolution. It can reject a package that both Node and
the bundler resolve correctly, which is exactly the Pinia 4 failure.

## Library comparison

### `pkg-types` 2.3.1

At commit
[`6dc514b`](https://github.com/unjs/pkg-types/tree/6dc514b530123f2e4147727019dba6d128a0754f),
`resolvePackageJSON(path)` finds the nearest `package.json` from a resolved file,
and `readPackageJSON` reads it
([implementation](https://github.com/unjs/pkg-types/blob/6dc514b530123f2e4147727019dba6d128a0754f/src/packagejson/utils.ts#L118-L168)).
This avoids requiring `pkg/package.json` to be publicly exported.

It is the best small library for the metadata half of the problem, but its API
is asynchronous. It should be fed a bundler-resolved file; it is not itself a
replacement for Vite/Rspack resolution.

### `exsolve` 1.1.1 and `mlly` 1.8.2

`exsolve` is the current small UNJS resolver. It is pure JavaScript, based on
Node's upstream ESM algorithm, supports configurable exports conditions, caches
results, checks that targets exist, and canonicalizes resolved symlinks
([API and defaults](https://github.com/unjs/exsolve/blob/4d138a899b37e341e12c6a43040ebd21d6ffff9c/src/resolve.ts#L1-L58),
[resolution](https://github.com/unjs/exsolve/blob/4d138a899b37e341e12c6a43040ebd21d6ffff9c/src/resolve.ts#L67-L155),
[realpath](https://github.com/unjs/exsolve/blob/4d138a899b37e341e12c6a43040ebd21d6ffff9c/src/internal/resolve.ts#L200-L231)).

`mlly` provides the same useful `resolvePath`/`resolvePathSync` shape and is the
library used by the `node-modules-inspector` test helper. Its resolver uses
`import-meta-resolve`, defaults to `node` + `import`, and accepts explicit
conditions
([source](https://github.com/unjs/mlly/blob/beece9be0a98bd170bfef6a4dca4cc84eb84cf74/src/resolve.ts#L1-L47),
[API](https://github.com/unjs/mlly/blob/beece9be0a98bd170bfef6a4dca4cc84eb84cf74/src/resolve.ts#L87-L207)).
For new code, prefer `exsolve`: `pkg-types` 2.3.1 itself now depends on it, while
`mlly` 1.8.2 still depends on `pkg-types` 1.x and carries additional module
analysis APIs that Halo does not need.

Both are acceptable fallbacks when the bundler cannot expose its resolved file.
They are still a second resolver, so Halo would have to choose conditions that
match each bundler. They cannot automatically reproduce arbitrary Vite/Rspack
aliases or plugin-specific resolution.

### `oxc-resolver` 11.24.2

`oxc-resolver` is the most complete standalone option. It implements exports,
configurable condition/main/alias fields, symlink canonicalization, caching,
and returns both the resolved path and `packageJsonPath`
([options](https://github.com/oxc-project/oxc-resolver/blob/c61574ff055fe615c329529138c1d851f8e6c487/README.md#L125-L178),
[resolver matrix](https://github.com/oxc-project/oxc-resolver/blob/c61574ff055fe615c329529138c1d851f8e6c487/README.md#L210-L235),
[result type](https://github.com/oxc-project/oxc-resolver/blob/c61574ff055fe615c329529138c1d851f8e6c487/napi/index.d.ts#L263-L278)).

It is a native N-API package and exposes many knobs. More importantly, the
caller still has to mirror Vite/Rspack conditions, aliases, browser fields,
extensions, and symlink policy. Neither reference project uses it for package
identity. It is therefore technically capable but unjustified for this narrow
validation problem.

### `@rspack/resolver`

Rspack also maintains its own Rust port of `enhanced-resolve`, forked from
`oxc-resolver` specifically for Rspack's filesystem and long-term resolver
requirements. It offers sync/async APIs and configurable exports conditions,
aliases, main fields, browser fields, symlinks, and Plug'n'Play
([source snapshot `ab14d9d`](https://github.com/web-infra-dev/rspack-resolver/tree/ab14d9d09079b1faadf0307c6887ac823d6da982),
[API and rationale](https://github.com/web-infra-dev/rspack-resolver/blob/ab14d9d09079b1faadf0307c6887ac823d6da982/README.md#L1-L60),
[options](https://github.com/web-infra-dev/rspack-resolver/blob/ab14d9d09079b1faadf0307c6887ac823d6da982/README.md#L62-L175)).

If Halo ever needs a standalone resolver solely for Rsbuild, this is a better
fit than upstream `oxc-resolver`. It still requires Halo to copy the active
Rspack resolve configuration, however, and therefore ranks behind consuming
the compiler's configured resolver or using the narrower `pkg-types` helper.

## Recommendation for Halo

Use the same precedence as Vite DevTools:

1. **Bundler result is authoritative.** For Vite, keep using
   `ResolvedConfig.createResolver()` and consume its resolved ID. For Rsbuild,
   first prototype use of Rspack's configured normal resolver/result instead of
   treating Rsbuild's `api.resolve` request as a resolved resource. Rsbuild
   documents `api.resolve` as a pre-resolution hook
   ([official API](https://rsbuild.dev/plugins/dev/core#api-resolve)); Rspack
   exposes `NormalModuleFactory.afterResolve` after normal resolution
   ([official API](https://www.rspack.dev/api/plugin-api/normal-module-factory-hooks#afterresolve)).
   Because Halo marks these requests external, verify whether the normal
   factory reaches `afterResolve`; externals may short-circuit that lifecycle.
2. **Resolve metadata from the resolved file.** Remove the inferred `entry`
   field from Halo's installed-package result. Canonicalize the bundler ID,
   locate/read its nearest package metadata with `pkg-types`, and validate the
   package name and version. Cache by canonical package root.
3. **Use `pkg-types`/`exsolve` when externalization hides the result.** If
   Rsbuild cannot expose a resolved resource before externalization,
   `resolvePackageJSON(root, { from: importer })` provides the narrowest tested
   library path: `pkg-types` resolves the exports-only entry through `exsolve`,
   then finds its nearest metadata. Keep this fallback isolated and test its
   exact conditions. There is no reason for Halo to infer an entry itself.
4. **Do not add `oxc-resolver` now.** It adds native packaging and a second
   configurable resolver without solving the fundamental drift from the real
   bundler. Reconsider it only if Halo later needs a bundler-independent module
   resolver as a product feature.

The immediate Pinia failure can be removed simply by deleting the unused entry
guess. The durable implementation should additionally bind the validated
package name/version to the actual bundler-resolved resource, with fixtures for:

- exports-only packages with no `main` or root `index.js`;
- conditional `browser`/`import` exports;
- pnpm peer-variant paths and symlinks;
- an alias that resolves outside the validated package root;
- duplicate package versions resolved from different importers.

This keeps Halo's contract check focused on package identity and version, while
leaving module resolution to the system that will actually build the plugin.
