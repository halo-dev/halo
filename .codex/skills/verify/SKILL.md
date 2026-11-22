---
name: verify
description: Layered post-change verification for the Halo core repo (halo-dev/halo — Java 21 / Spring Boot WebFlux + R2DBC + Vue 3 monorepo) — T0 scope the change → T1 static gates (Spotless, Gradle compile and api/application tests, pnpm typecheck/lint/test/build/i18n/format, OpenAPI sync) → T2 regression and blast radius (callers, extension indexes, events, RBAC, migrations, plugins/themes, UI contracts, OpenSpec) → T3 end-to-end (temp workdir, free port, setup, API probes, and a mandatory real-browser screenshot pass for any `ui/` change). Use whenever api, application, ui, configuration, migrations, dependencies, or CI change; after fixing a bug, refactoring, or completing an OpenSpec change; or when the user says "verify this", "don't introduce regressions", "test it end-to-end", or "did you test it". Even small changes must pass the gates at the matching depth. Claiming "done, no problems" without running commands is wrong. A more specific Pro/enterprise verification skill such as `verify-halo-pro` takes precedence; this is the shared baseline.
---

# verify — layered verification after changes in Halo core

This skill guards against two failure modes: **verifying by feel** (claiming things work without running gates, tests, or a server) and **guessing the blast radius** (services, endpoints, reconcilers, extension indexes, Spring events, RBAC, generated contracts, and UI evolve together, so a change in one place often touches another). Conclusions must come from actual command and probe output; static analysis is only inference.

## Relationship to a Pro/enterprise `verify` variant

Some repositories ship a more specific verification skill alongside this one (for example, `verify-halo-pro`).

- **Single entry point**: when the repository provides such a variant, start verification from that skill; this skill is the shared baseline and is not run as a separate pass. When trigger phrases overlap, the repository-specific variant wins.
- **Baseline here**: generic Halo core work (shared `api/` and `application/`, shared `ui/`, static gates) follows the steps in this skill; commands duplicated in both skills are run once.
- **Deltas there**: repository-specific modules, configuration, and safety rules always follow the more specific variant.

## Read first

1. Root `AGENTS.md`, plus the module guides for the areas you touch: `application/AGENTS.md`, `ui/AGENTS.md`.
2. Find the expected behavior:
   - In-progress OpenSpec change: `openspec/changes/<change>/proposal.md`, `design.md`, `tasks.md`, delta specs; run `openspec validate <change> --type change --strict --no-interactive`.
   - Archived capabilities: Requirement / Scenario (WHEN/THEN) in `openspec/specs/<capability>/spec.md`.
3. For changes without a spec, use existing code, tests, and callers as the behavior baseline. Do not invent acceptance criteria.

## Three principles

1. **Honest classification**: the report must distinguish **verified** (commands/probes ran and produced output), **static inference only** (derived by reading code), and **cannot verify locally** (missing external dependency, credentials, or CI environment). Presenting inference as verified is the worst mistake. List pre-existing failures separately as **pre-existing baseline failures**; do not attribute them to this change and do not fix unrelated issues.
2. **Persist evidence**: write verification artifacts to `/tmp/verify-<slug>-<rand>/` (command logs, build/test logs, raw HTTP responses, screenshots). Report the path; anything not persisted does not count as verified.
3. **Do not expand scope**: check only this round's change, its direct callers, and the relevant OpenSpec scenarios. Do not re-report already-fixed items; do not reopen unrelated investigations; do not modify unrelated code.

## Step 0: scope the change

Look at the complete change surface, not just `git diff` (which misses committed changes, the index, and untracked files):

```bash
git status --short
git diff --name-only
git diff --cached --name-only
git ls-files --others --exclude-standard
git diff --name-only origin/main...HEAD   # if the change is already committed on a branch; use the actual PR base
```

Map paths to layers: `api/` (extension models/contracts/security), `application/` (services/routers/migrations/packaging/test resources), `platform/application|plugin/` (BOMs), `ui/console-src|uc-src|src|packages/`, `openspec/`, `.github/workflows/`, `gradle/`.

Pick the depth from this table (take the union when multiple rows match):

| Change | Depth |
|---|---|
| Docs/comments only | T0; if Markdown/JSON/properties/XML changed, run `./gradlew spotlessCheck` |
| `ui` styles/display copy/template tweaks (no type or logic changes) | T1 + T3 browser check (real page, before/after states, screenshots shown to the user) |
| `ui` component logic/composables/routing/permissions/api types | T1 + relevant Vitest + T3 browser check (drive the affected flow, screenshot every relevant state, show the user) |
| Java with no behavior change (rename/format) | T1 compile + Spotless |
| Java logic/endpoints/services/reconcilers | T1 + T2 |
| Core paths: content publishing/comments, auth/session/2FA/sudo, RBAC, plugin lifecycle, theme rendering, search, migrations, snapshots/backups | T1 + T2 + T3 |
| External contracts: API routes/response fields/permissions, extension models/indexes, Spring events, OpenAPI | T1 + T2 + T3 + OpenAPI sync |
| Database migrations/schema/initial data | T1 + T2 + T3 (at least H2 startup; other dialects have limits, see T3) |
| Dependency/version upgrades | T1 + relevant tests + `./gradlew clean check`; use the `gradle-dependency-updates` skill for upgrades; versions live only in `gradle/libs.versions.toml`, `gradle.properties`, `ui/package.json`/workspace catalog |
| CI workflows | T1 + manual review of CI semantics; GitHub Actions cannot run locally — say so in the report |

**T3 boundary**: any change under `ui/` (Console, UC, workspace packages, styles, templates, copy, or logic) always includes T3 with a real browser. Start an isolated instance, open the affected page, exercise the change, capture screenshots of every relevant state (for example empty vs. filled, success vs. error), and show those screenshots to the user. T1 + Vitest prove the code compiles and units pass; they say nothing about whether the page renders or the flow actually works, so they never substitute for the browser pass. Never downgrade a UI change to "T1 only" because it is "just CSS" or "just copy". If a browser pass is genuinely blocked (no automation, missing login, environment broken), report the change as unverified/FAIL rather than PASS, and say exactly what blocked it. Backend-only changes follow the rows above.

## T1 — static gates

Choose the minimal set for the changed layers. Fix failing gates before proceeding.

### Backend

```bash
./gradlew spotlessCheck
./gradlew :api:compileJava :application:compileJava   # pick the modules you changed
./gradlew :application:test --tests '*Post*'          # targeted during iteration; narrow by class/package
./gradlew :application:test                           # full run before a PR
./gradlew :api:test                                   # when api changed
```

- Test classes are named `XxxTest` / `XxxIntegrationTest`; quote `--tests` patterns to avoid shell expansion.
- Backend tests use an in-memory H2 database (`application/src/test/resources/application.yaml`, MySQL compatibility mode). Docker/Testcontainers are not needed.
- A full run covers hundreds of test classes and can take 10+ minutes. Target first during iteration and run the full suite before a PR. Never claim the full suite passed if you did not run it.
- `spotlessApply` modifies files. Use it only when formatting is needed, inspect `git diff` afterwards, and revert unrelated formatting.

### Frontend

```bash
pnpm -C ui typecheck
pnpm -C ui lint                 # eslint --max-warnings=0; any warning fails
pnpm -C ui test:unit
pnpm -C ui build
pnpm -C ui format:check         # when TS/Vue/JSON/Markdown changed
pnpm -C ui i18n:check           # when UI copy/locales changed
```

- **Record the baseline first**: run the same commands on a clean `main` or on the commit before your change and record the real output. Do not assume `typecheck` / `lint` / `i18n:check` are green; the baseline may already have failures. Errors only in untouched files → record as pre-existing baseline failures, not regressions; errors in changed files → must fix.
- This repo's `lint` script is `eslint . --max-warnings=0` with no `--fix`, so it does not rewrite files; `format:check` only checks.
- `i18n:check` covers four locales: `ui/src/locales/{en,es,zh-CN,zh-TW}.json`. New keys must exist in all four locales; never use enum values directly as UI copy.

### OpenAPI / generated contracts

When DTOs, routes, responses, permissions, or extension APIs change:

```bash
./gradlew generateOpenApiDocs --no-configuration-cache
pnpm -C ui api-client:gen
git diff --exit-code -- api-docs/openapi ui/packages/api-client/src
```

- Never hand-edit generated files (`api-docs/openapi`, `ui/packages/api-client/src`). If there is a diff, include the regenerated output in the change.
- `generateOpenApiDocs` starts a temporary server on the **fixed port 8091** with the `doc` profile and a workdir under `application/build/tmp/workdir-for-generating-apidocs`. Check the port with `lsof -nP -iTCP:8091 -sTCP:LISTEN` first. If another process owns it, **do not kill it**; if you cannot free the port, mark the local OpenAPI check as **blocked** and rely on CI `openapi-check`.
- The generator may add trailing whitespace in new client code; exclude the generated directories from `git diff --check`.

### Other gates

```bash
./gradlew clean check                    # CI parity
./gradlew build                          # additionally builds the UI (:ui:doBuild) and bootJar/checksums
git diff --check -- . ':(exclude)api-docs/openapi' ':(exclude)ui/packages/api-client/src'
```

- Run from the repo root, `./gradlew check` covers all subprojects (confirmed with `--dry-run`: `:ui:lint`, `:ui:typecheck`, `:ui:test`, `:api:test`, `:application:test`, and Spotless). CI's actual command is `./gradlew clean check --configuration-cache --configuration-cache-problems=warn`.
- `clean check` may fail on pre-existing baseline issues; record them as baseline and do not change unrelated files to make it green.
- Run `./gradlew clean downloadPluginPresets build -x check` only when packaging/plugin presets/UI embedding changed (it downloads preset plugins and needs network).
- When an OpenSpec change is modified, run `openspec validate <change> --type change --strict --no-interactive`.

## T2 — regression and blast radius

1. **Run relevant tests**: targeted/full backend tests and relevant frontend Vitest. Intentional behavior changes may require assertion updates, but the report must list which assertions changed and why.
2. **Semantic impact analysis** (the parts tests do not cover; go through each item):
   - Method signature/semantics → grep all callers and check each one.
   - Endpoint routes/DTOs/responses/permissions → check OpenAPI, the generated UI client, `ui/` usages, and RBAC rules in `application/src/main/resources/extensions/role-template-*.yaml`.
   - Extension fields/indexes → check index registration (`IndexSpec`/`SearchOption`), reconcilers, endpoint queries, UI types, old-data compatibility, and per-dialect migrations.
   - Event publishing/listening → grep `@EventListener`, `ApplicationEventPublisher`, and event classes; check transaction/async boundaries and retry idempotency.
   - Reactive paths → no blocking I/O or `.block()` in request paths; use `subscribeOn(boundedElastic)` only for genuinely blocking work.
   - Security → Basic Auth (`halo.security.basic-auth.disabled`, disabled by default), CSRF-exempt paths (`/api/**`, `/apis/**`, `/actuator/**`, `/system/setup`, `/sudo/**`), sessions, 2FA/sudo, password reset, role-template permissions.
   - DB migrations → four dialects `h2/mysql/mariadb/postgresql` under `application/src/main/resources/db/migration/`; tests cover only H2 (MySQL compatibility mode) and this repo's CI has no database matrix; state clearly when other dialects cannot be verified locally.
   - Plugins/themes → lifecycle, PF4J classloader, plugin presets, Finder API, theme templates/UI resources, `halo.plugin.runtime-mode` (`development`/`deployment`), `halo.plugin.fixed-plugin-path`.
   - Search/cache/snapshots → Lucene search indexes, cache configuration, snapshot/backup, actuator endpoints.
   - UI → `console-src`/`uc-src`/`src`/`packages`; routes/menus/permissions; `@halo-dev/components`, FormKit, Tailwind; i18n keys (consistent across four locales); never hand-edit the generated `ui/packages/api-client/src`.
3. **OpenSpec scenarios**: list each Requirement/Scenario covered by this change and mark it "covered by tests / needs T3 probe / cannot cover locally". Do not run tests without checking them against the spec.
4. Produce a "behavior change list" with the coverage method for each item; recommend tests for high-risk points tests cannot cover (do not write them automatically unless the user asks).

## T3 — end-to-end

**Read `references/e2e-playbook.md` in full first** (configuration sources, ports, setup, auth, probes, and cleanup live there). Key points:

1. **Safety boundaries**: do not touch the user's existing instances/workdirs/containers. Before starting, run `lsof -nP -iTCP -sTCP:LISTEN` to see what is in use; assume no port is free. **Do not kill processes you did not start.** Never use the profile's default workdir; always use a temporary workdir. **Do not use the `local` profile**: it is gitignored and typically points at personal databases and local plugin paths.
2. Pick a free backend port (start at 8092; 8091 is reserved for `generateOpenApiDocs`) and create a temporary workdir:

```bash
lsof -nP -iTCP -sTCP:LISTEN | head -50
PORT=8092
while lsof -nP -iTCP:$PORT -sTCP:LISTEN >/dev/null 2>&1; do
  PORT=$((PORT+1))
  [ "$PORT" -gt 8100 ] && { echo "no free port"; exit 1; }
done
echo "using $PORT"
WORK=$(mktemp -d /tmp/verify-halo-XXXXXX)
```

Start with the `dev` profile (commands below reuse `$PORT`):

```bash
./gradlew :application:bootRun --args="--spring.profiles.active=dev --server.port=$PORT --halo.work-dir=$WORK/workdir --halo.ui.proxy.enabled=false --halo.external-url=http://127.0.0.1:$PORT/" --console=plain
```

Wait until `http://127.0.0.1:$PORT/actuator/health` is UP; first startup/migration may take 1–2 minutes.

3. Initialize a temporary admin via `POST /system/setup` (form) and keep the one-time password. The `dev` profile has `halo.security.basic-auth.disabled: false`, so Basic Auth works and `curl -u` can perform writes; whether Basic Auth is enabled depends on the current profile's `application-*.yaml`. `/apis/**`, `/actuator/**`, `/system/setup`, and `/sudo/**` are CSRF-exempt.
4. Choose probes from the OpenAPI paths relevant to the change (`api-docs/openapi/v3_0/aggregated.json` and `apis_{console,uc,public,extension}.api_v1alpha1.json`); do not invent request bodies from memory. Use admin for management APIs, and admin or a disposable user for UC (`POST /apis/api.console.halo.run/v1alpha1/users`, flat body `{name,email,password,displayName}`). A normal user hitting management APIs should usually get 403 (depending on role configuration).
5. UI verification — **mandatory whenever the change touches `ui/`**, choose one way to serve the UI:
   - **Build into the backend**: `./gradlew :ui:doBuild :application:copyUiDist` (`copyUiDist` only `mustRunAfter :ui:doBuild`; it does not trigger the UI build, so run both explicitly). Start with `--halo.ui.proxy.enabled=false` and open `http://127.0.0.1:$PORT/console` (unauthenticated requests redirect with 302 to `/login`).
   - **Vite dev server**: `pnpm -C ui dev`. The Vite port is hard-coded to 3000 in `ui/vite.config.ts`; do not change repo config. If 3000 is occupied, prefer the build-into-backend path. Vite has no API proxy of its own; the backend proxies UI requests to Vite, so the entry point is still the backend port's `/console`.
   - Then use real browser automation (e.g. `agent-browser`) to log in, navigate to the affected page, and exercise the change. At minimum: capture the page with the change visible (and the empty/filled, success/error states the change affects), save the screenshots under `/tmp/verify-*`, and attach/show them to the user in the final reply. Check the Network panel for failed requests and console errors.
   - "No real browser" is a blocked verification, not a valid stopping point: report FAIL/unverified with the blocker instead of claiming the UI works.
6. Persist all evidence under `/tmp/verify-*`; cleanup: kill only the port processes you started, remove the temporary workdir, confirm the port is free, and check `git status` for stray files. Test data lives in the temporary workdir and is removed with it.

Items that cannot be verified locally (list them honestly in the report): MySQL/MariaDB/PostgreSQL dialect startup matrix (tests run only on H2; this repo's CI has no database matrix), real email/SMS delivery (SMTP/notifications), plugin-market install/upgrade and legacy plugin/theme compatibility, third-party login providers, reverse proxies/multi-node/session sharing, performance/load, and GitHub Actions/container image/Maven Central publishing.

## Report format (required in the final reply)

```
Verdict: PASS / PASS_WITH_NOTES / FAIL
Evidence: /tmp/verify-<slug>-<rand>/
Verified: ... (commands/probes run and their results)
UI screenshots: ... (paths for every UI change; required, not optional)
Static inference only: ...
Cannot verify locally: ... (reason)
Pre-existing baseline failures: ... (e.g. existing ui typecheck / i18n:check errors; not caused by this change)
Recommendations: ... (optional: tests to add / manual follow-ups)
```

Report only what you did. If you did not run T3, say so explicitly; if a gate was blocked, state why. Keep the report short; details belong in the evidence directory.

## Boundaries

- This skill only verifies. It does not commit, push, or open PRs. If the user later asks to submit, use the `halo-pr-submit` skill; do not push directly to the official `origin` (`halo-dev/halo`) without confirming the remote and permissions.
- Never hand-edit `api-docs`, `ui/packages/api-client/src`, or `pnpm-lock.yaml`; regenerate/install with the proper tools.
- Do not `git add -A`; stage specific files. Never commit secrets, temporary workdirs, or H2/build artifacts.
- Do not add new dependencies, change security configuration/migrations/CI, or fix unrelated pre-existing failures unless the user explicitly asks.
- Verification is not a full code review. Use the review workflow when a full review is needed; do not expand scope here.
