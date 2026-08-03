# Frontend Guidelines

`ui/` is the Vue 3 + TypeScript + TailwindCSS frontend, managed with pnpm workspaces.

## Structure

- `ui/src/` — console application.
- `ui/uc-src/` — user center application.
- `ui/packages/` — shared workspace packages (`@halo-dev/api-client`, `@halo-dev/components`, `@halo-dev/console-shared`, `@halo-dev/editor`, `@halo-dev/shared`, `@halo-dev/ui-plugin-bundler-kit`).
- Unit tests: `*.spec.ts` files, usually under `__tests__/` next to the code they cover.

## Commands

Run from the repo root:

- `pnpm -C ui install` / `pnpm -C ui dev` — install dependencies / run the dev server.
- `pnpm -C ui build` — production build.
- `pnpm -C ui typecheck` — `vue-tsc` type checking.
- `pnpm -C ui lint` — ESLint (fails on any warning).
- `pnpm -C ui format` / `pnpm -C ui format:check` — Prettier formatting.
- `pnpm -C ui test:unit` — Vitest unit tests.
- `./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen` — regenerate `ui/packages/api-client` after backend contract changes.

## Conventions

- Follow existing component and composable patterns; match surrounding code rather than introducing new stylistic islands.
- Never hand-edit `ui/packages/api-client/src/` — it is generated; regenerate it instead.
- Keep dependency versions in `ui/package.json`; never hard-code versions.
- Payload or UX changes usually ship together with the backend change that defines the contract.

## Boundaries

- New npm dependencies require approval.
