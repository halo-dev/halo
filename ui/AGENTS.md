# UI (Frontend) — AGENTS.md

pnpm workspace at `ui/`. It contains the admin console, user center, shared packages, and the generated API client used by the frontend.

Read this file together with the root [`AGENTS.md`](../AGENTS.md). If a change touches backend contracts, auth flows, or generated OpenAPI, also load [`application/AGENTS.md`](../application/AGENTS.md). If a shared public type or extension contract changes, also load [`api/AGENTS.md`](../api/AGENTS.md).

## Quick commands

Prefer root-scoped commands:

```bash
pnpm -C ui install
pnpm -C ui dev
pnpm -C ui build
pnpm -C ui build:packages
pnpm -C ui test:unit
pnpm -C ui lint
pnpm -C ui format
pnpm -C ui format:check
pnpm -C ui typecheck
pnpm -C ui api-client:gen
```

Local wrappers also exist:

```bash
make -C ui dev
make -C ui build
make -C ui test
make -C ui api-client-gen
```

## Cross-stack touchpoints

- `packages/api-client` is generated from backend OpenAPI docs. Do not hand-edit generated files.
- Backend route, DTO, auth, or schema changes often require `./gradlew generateOpenApiDocs && pnpm -C ui api-client:gen`.
- The frontend build output is bundled into the backend JAR during the application build.

## Directory layout

```text
ui/
├── console-src/            # Admin console
│   ├── modules/            # Feature modules, auto-discovered via import.meta.glob
│   ├── stores/             # Console-specific Pinia stores
│   ├── router/             # Console router config
│   ├── layouts/            # Console layouts
│   └── styles/             # Console styles
├── src/                    # Shared frontend code
│   ├── components/         # Shared Vue components
│   ├── composables/        # Shared composables
│   ├── stores/             # Shared Pinia stores
│   ├── formkit/            # FormKit setup and theme
│   ├── locales/            # i18n JSON files
│   └── styles/             # Shared Tailwind/theme files
├── uc-src/                 # User center
├── packages/               # Workspace packages
├── public/                 # Static assets
└── scripts/                # Build scripts
```

## Workspace packages

| Package                 | Purpose                                           |
| ----------------------- | ------------------------------------------------- |
| `api-client`            | Generated Axios-based API client from OpenAPI     |
| `components`            | Shared UI components and icons                    |
| `editor`                | Tiptap-based rich-text editor                     |
| `console-shared`        | Console-specific shared stores, events, and utils |
| `shared`                | Cross-platform shared types and utilities         |
| `ui-plugin-bundler-kit` | Build tooling for Halo UI plugins                 |

## Conventions

### Module pattern

Each feature in `console-src/modules/<feature>/module.ts` uses `definePlugin()`:

```ts
export default definePlugin({
  name: 'posts',
  routes: [...],
  menus: [...],
  permissions: [...],
})
```

Modules are auto-discovered via `import.meta.glob("./**/module.ts")`.

### Startup order

`components -> i18n -> vue-query -> api-client -> pinia -> core modules -> user -> permissions -> plugin modules -> router -> mount`

### Path aliases

- `@/*` → `src/*`
- `@console/*` → `console-src/*`
- `@uc/*` → `uc-src/*`

Defined in `tsconfig.app.json`.

### Tech stack

- **State:** Pinia
- **i18n:** `vue-i18n` with JSON locale files in `src/locales/`
- **Forms:** FormKit
- **Icons:** Iconify via `@iconify/vue`
- **CSS:** TailwindCSS 3.4 + `tailwindcss-themer`
- **Build tool:** `vite-plus` (`vp`)
- **Testing:** Vitest
- **Data fetching:** TanStack Query (`@tanstack/vue-query`)

### Theme

Default theme tokens:

- Primary: `#4CCBA0`
- Secondary: `#0E1731`
- Danger: `#D71D1D`

Prefer theme tokens over hard-coded colors in features.

### Formatting

Use `vp fmt` via `pnpm -C ui format`. Pre-commit hooks run `lint-staged` for staged UI files.

## Testing

- **Framework:** Vitest with jsdom
- **Location:** colocated `.spec.ts` files
- **Single file:** `pnpm -C ui test:unit -- src/utils/foo.spec.ts`
- **Pattern:** `pnpm -C ui test:unit -- -t "test name pattern"`
- **Watch:** `pnpm -C ui test:unit:watch`

Add or update tests for changed UI behavior, especially utilities, composables, and package exports.

## Validation flow

- Run `pnpm -C ui build:packages` before `pnpm -C ui build` when workspace packages changed.
- Use `pnpm -C ui typecheck && pnpm -C ui lint` as the normal validation path.
- Regenerate the API client after backend contract changes:

```bash
./gradlew generateOpenApiDocs
pnpm -C ui api-client:gen
```

## Boundaries

- ✅ **Always:** Work from the repo root for command orchestration; run `pnpm -C ui format`; run `pnpm -C ui build:packages` when package code changes; keep frontend behavior in sync with backend contract changes.
- ⚠️ **Ask first:** Public exports from `packages/*`; new npm dependencies; `vite.config.ts` or `tsconfig*.json` changes; global FormKit or theme token changes.
- 🚫 **Never:** Commit `node_modules/`; hand-edit `packages/api-client/src/`; hard-code theme colors in feature code when a token exists; treat `--no-verify` as a normal fix for broken hooks.

## Pitfalls

1. **`pnpm install` is required after dependency changes or fresh clones.** Missing workspace links can break lint-staged and local package resolution.
2. **API client generation is a two-step flow.** `generateOpenApiDocs` must finish before `pnpm -C ui api-client:gen`.
3. **Package build order matters.** Run `pnpm -C ui build:packages` first when `packages/*` changed.
