# UI (Frontend) — AGENTS.md

pnpm monorepo at `ui/`. Admin console + user center. Vue 3 + TypeScript + TailwindCSS 3.4.

## Quick Commands

```bash
cd ui
pnpm install                # Required before any other command
pnpm dev                    # Dev server with HMR (port varies by route)
pnpm build                  # Full: typecheck + bundle (console + UC)
pnpm build:packages         # Build workspace packages only (faster)
pnpm test:unit              # Vitest unit tests
pnpm lint                   # ESLint
pnpm format                 # Format code (vp fmt)
pnpm format:check           # Check formatting
pnpm api-client:gen         # Generate API client from OpenAPI spec
pnpm typecheck              # vue-tsc type checking
```

Or use Makefile wrappers: `make -C ui dev`, `make -C ui build`, `make -C ui test`, `make -C ui api-client-gen`.

## Directory Layout

```
ui/
├── console-src/            # Admin console (main application)
│   ├── modules/            # Feature modules — auto-discovered via import.meta.glob
│   ├── stores/             # Pinia stores (console-specific)
│   ├── router/             # Vue Router config
│   ├── layouts/            # Layout components
│   └── styles/             # Console-specific styles
├── src/                    # Shared code
│   ├── components/         # Shared Vue components
│   ├── composables/        # Shared composables (use-auto-save-content, use-role, etc.)
│   ├── stores/             # Shared Pinia stores (plugin.ts, role.ts)
│   ├── formkit/            # FormKit config + custom inputs/plugins
│   ├── locales/            # i18n JSON files (en, zh-CN, zh-TW, es)
│   └── styles/             # Shared TailwindCSS + theme config
├── uc-src/                 # User center (public-facing)
│   ├── modules/            # Feature modules — auto-discovered
│   └── router/             # Router with auth guards
├── packages/               # Workspace packages (see below)
├── public/                 # Static assets
└── scripts/                # Build scripts
```

## Workspace Packages (`ui/packages/`)

| Package | Purpose |
|---|---|
| `api-client` | Generated Axios-based API client from OpenAPI spec |
| `components` | Shared UI component library + icons (Iconify) |
| `editor` | Rich-text editor (Tiptap-based) |
| `console-shared` | Console-specific shared code (stores, events, utils) |
| `shared` | Cross-platform shared code (plugin system, types, stores) |
| `ui-plugin-bundler-kit` | Build tooling for Halo UI plugins |

## Conventions

### Module Pattern
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

### Startup Order (main.ts)
components → i18n → vue-query → api-client → pinia → core modules → user → permissions → plugin modules → router → mount

### Path Aliases
- `@/*` → `src/*`
- `@console/*` → `console-src/*`
- `@uc/*` → `uc-src/*`
Defined in `tsconfig.app.json`.

### Tech Stack
- **State:** Pinia stores
- **i18n:** vue-i18n with JSON locale files in `src/locales/`
- **Forms:** FormKit (`src/formkit/theme.ts`)
- **Icons:** Iconify via `@iconify/vue`. Icon sets: lucide, mdi, ri, fluent (installed as `@iconify-json/*` devDeps)
- **CSS:** TailwindCSS 3.4 with `tailwindcss-themer` plugin
- **Build tool:** `vite-plus` (`vp`)
- **Testing:** Vitest
- **API:** vue-query (TanStack Query)

### Theme
Default theme via `tailwindcss-themer`:
- Primary: `#4CCBA0`
- Secondary: `#0E1731`
- Danger: `#D71D1D`
FormKit theme integrated with TailwindCSS theme tokens.

### Formatting
Uses `vp fmt` (vite-plus format wrapper) and ESLint.
Pre-commit hooks run lint-staged on staged UI files.

## Pitfalls

1. **Pre-commit hooks run lint-staged on UI files.** If `node_modules/@halo-dev/components` is missing
   (no `pnpm install` after fresh clone), `git commit` fails even on Java-only changes.
   Fix: `git commit --no-verify` or `cd ui && pnpm install`.

2. **API client regeneration requires two commands in order:**
   ```bash
   ./gradlew generateOpenApiDocs    # from project root (~28s, boots Spring)
   cd ui && pnpm api-client:gen     # runs openapi-generator
   ```
   The Gradle task must complete before the pnpm script can run.

3. **Build order matters.** Run `pnpm build:packages` before `pnpm build` when packages changed.
   The full `pnpm build` includes typecheck, which can be slow. For package-only changes,
   `pnpm build:packages` then `pnpm dev` is the fast path.

4. **`pnpm install` required after `git pull`.** The `node_modules` may include workspace
   protocol links that break after upstream dependency changes.
