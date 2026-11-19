# E2E Playbook (T3 reference for the verify skill)

Prerequisite: SKILL.md has already decided that this change needs T3. This file only contains configuration sources, isolation rules, and copy-paste commands; do not treat it as a full test plan.

## Configuration sources and isolation

- Ports, workdir, database, Basic Auth, and UI proxy come from `application/src/main/resources/application.yaml` and the relevant `application-*.yaml` profile. Never assume port occupancy, paths, or database names on any machine.
- Prefer the `dev` profile: H2 file database, Basic Auth enabled, UI proxy pointing at 3000, plugin runtime `development`. **Do not use the `local` profile**: it is gitignored (`application-local.yaml`) and typically points at personal databases and local plugin paths.
- Do not use the `mysql`/`postgresql`/`mariadb` profiles against a user's existing database; spin up isolated containers as described under "Other database dialects".
- For E2E, always override `--server.port` and `--halo.work-dir` with temporary values; never use the profile's default workdir (`dev` uses `${user.home}/halo2-dev`).
- **Port 8091 is reserved by `generateOpenApiDocs`**; start E2E from 8092. Run `lsof -nP -iTCP -sTCP:LISTEN` before starting, and never kill processes you did not start.
- Backend tests use the in-memory H2 configuration in `application/src/test/resources/application.yaml` (MySQL compatibility mode); Docker/Testcontainers are not required.

## Check ports, then start (API-only, fastest)

```bash
lsof -nP -iTCP -sTCP:LISTEN | head -50   # inspect all listeners; do not check only the starting port
PORT=8092
while lsof -nP -iTCP:$PORT -sTCP:LISTEN >/dev/null 2>&1; do
  PORT=$((PORT+1))
  [ "$PORT" -gt 8100 ] && { echo "no free port"; exit 1; }
done
echo "using $PORT"
WORK=$(mktemp -d /tmp/verify-halo-XXXXXX)
cd <repo>
./gradlew :application:bootRun --args="--spring.profiles.active=dev --server.port=$PORT --halo.work-dir=$WORK/workdir --halo.ui.proxy.enabled=false --halo.external-url=http://127.0.0.1:$PORT/" --console=plain
```

- `--halo.work-dir` overrides the profile workdir so nothing is written to existing data; `--halo.ui.proxy.enabled=false` prevents proxying UI requests to another process/port (`dev` defaults to `http://localhost:3000/`).
- Wait for health:

```bash
curl -fsS --retry 60 --retry-delay 2 --retry-connrefused http://127.0.0.1:$PORT/actuator/health
```

- `/actuator/health` is anonymous-accessible (see `role-template-anonymous.yaml`). First startup runs migrations; 1–2 minutes is normal.

## Initialize a temporary admin

```bash
curl -sS -o "$WORK/setup.out" -w 'setup:%{http_code}\n' \
  -X POST "http://127.0.0.1:$PORT/system/setup" \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode username=admin \
  --data-urlencode password='<disposable-password>' \
  --data-urlencode email=admin@example.com \
  --data-urlencode siteTitle='Halo Verify' \
  --data-urlencode language=en \
  --data-urlencode externalUrl="http://127.0.0.1:$PORT/"
```

- Expect 204. A 302 means this workdir was already initialized (not fresh); use another temporary workdir.
- Constraints from `SystemSetupEndpoint.SetupRequest`: username 4–63 and matching `NAME_REGEX`; password 5–257; language is one of `zh-CN|zh-TW|en|es`; externalUrl must start with http/https.
- Use a one-time strong password; keep it in `/tmp` only and never write it to the repo or report.

## Auth and probes

Auth depends on `halo.security.basic-auth.disabled` in the active profile; the `dev` profile sets it to `false`, so `curl -u` works. `/api/**`, `/apis/**`, `/actuator/**`, `/system/setup`, and `/sudo/**` are CSRF-exempt.

```bash
# Admin: user list
curl -sS -u admin:'<disposable-password>' -o "$WORK/admin-users.json" -w '%{http_code}\n' \
  'http://127.0.0.1:$PORT/apis/api.console.halo.run/v1alpha1/users?page=0&size=1'

# Current user (admin)
curl -sS -u admin:'<disposable-password>' -o "$WORK/uc-me.json" -w '%{http_code}\n' \
  'http://127.0.0.1:$PORT/apis/uc.api.halo.run/v1alpha1/users/-'

# Public content
curl -sS -o "$WORK/public-posts.json" -w '%{http_code}\n' \
  'http://127.0.0.1:$PORT/apis/api.content.halo.run/v1alpha1/posts?page=0&size=1'

# Plugin list (console)
curl -sS -u admin:'<disposable-password>' -o "$WORK/plugins.json" -w '%{http_code}\n' \
  'http://127.0.0.1:$PORT/apis/api.console.halo.run/v1alpha1/plugins?page=0&size=20'
```

- Expect 200. `/apis/api.console.halo.run/...` is the management API; `/apis/uc.api...` is the current-user view; public content is under `/apis/api.content...`; native extension APIs look like `/apis/<group>/<version>/<plural>` (consult OpenAPI for exact paths).
- Create a disposable normal user (for UC / permission boundaries):

```bash
curl -sS -u admin:'<disposable-password>' -H 'Content-Type: application/json' \
  -o "$WORK/create-user.json" -w '%{http_code}\n' \
  -X POST 'http://127.0.0.1:$PORT/apis/api.console.halo.run/v1alpha1/users' \
  -d '{"name":"verify-user","email":"verify@example.com","password":"<disposable-password>","displayName":"Verify User"}'
```

- The body is flat `{name,email,password,displayName}` (`UserEndpoint.CreateUserRequest`), not nested `{user:...}`.
- Normal-user UC: `curl -u verify-user:'<password>' .../apis/uc.api.halo.run/v1alpha1/users/-` → 200; the same user hitting management APIs should usually get 403 (depending on roles). This checks the RBAC boundary.
- Never guess `admin:admin`; use the one-time password from setup.

## Choose probes

Do not invent paths or request bodies. Pick paths relevant to the change from OpenAPI:

```bash
jq -r '.paths | keys[]' api-docs/openapi/v3_0/aggregated.json | head -100
jq -r '.paths | keys[]' api-docs/openapi/v3_0/apis_console.api_v1alpha1.json
jq -r '.paths | keys[]' api-docs/openapi/v3_0/apis_uc.api_v1alpha1.json
jq -r '.paths | keys[]' api-docs/openapi/v3_0/apis_public.api_v1alpha1.json
jq -r '.paths | keys[]' api-docs/openapi/v3_0/apis_extension.api_v1alpha1.json
# Request/response schemas
jq '.paths["<path>"].post.requestBody' api-docs/openapi/v3_0/apis_console.api_v1alpha1.json
```

- Native extension APIs `/apis/<group>/<version>/<plural>` (admin auth) are useful for diagnostics and data setup.
- Persist every raw response; save failure responses in full.

## UI verification

### Option A: Build into the backend (recommended, self-contained)

```bash
./gradlew :ui:doBuild :application:copyUiDist
./gradlew :application:bootRun --args="--spring.profiles.active=dev --server.port=$PORT --halo.work-dir=$WORK/workdir --halo.ui.proxy.enabled=false --halo.external-url=http://127.0.0.1:$PORT/" --console=plain
```

- `copyUiDist` only `mustRunAfter :ui:doBuild` and does not trigger the UI build; run both tasks explicitly.
- After the build, `application/build/resources/main/ui/` should contain `console.html`, `uc.html`, and `ui-assets/`.
- Open `http://127.0.0.1:$PORT/console`; unauthenticated requests redirect with 302 to `/login`, which is expected. Log in as admin in a browser to enter the Console.
- `halo.ui.proxy.enabled=false` is required; otherwise the `dev` profile proxies UI requests to port 3000.

### Option B: Vite dev server

```bash
pnpm -C ui dev
# Keep the backend's dev profile default halo.ui.proxy.enabled=true
```

- The Vite port is hard-coded as `DEV_SERVER_PORT = 3000` in `ui/vite.config.ts`; do not change repo configuration. If 3000 is occupied, prefer Option A.
- Vite has no API proxy; the Halo backend proxies UI requests to Vite. The entry point is still the backend port's `/console`.
- Vite hot-reloads UI changes; Java changes require restarting the backend.

### Browser

- Use `agent-browser` when available: navigate, log in, screenshot, inspect Network; save screenshots in `$WORK/`.
- If automation is not possible, write "component tests/static inference only; no real browser". Never imply pages were clicked.
- Only the Console/UC after login show real rendering; `curl` proves HTTP behavior only.

## Cleanup

```bash
PIDS=$(lsof -tiTCP:$PORT -sTCP:LISTEN 2>/dev/null)
[ -n "$PIDS" ] && kill $PIDS
lsof -nP -iTCP:$PORT -sTCP:LISTEN || echo "$PORT free"
rm -rf "$WORK"
cd <repo> && git status --short
```

- Kill only processes on the port you started; never `pkill java`/`pkill node`, which would kill other checkouts.
- The `bootRun` Gradle process may exit after the child Java process; rely on port release and do not kill unrelated Gradle daemons.
- Test data lives in the temporary workdir and is removed with it. Never write verification data to the profile's default workdir or the user's existing data directories.

## Other database dialects (optional)

Repo tests cover H2 only, and CI has no database startup matrix. To verify MySQL/MariaDB/PostgreSQL, create isolated containers (unique names, ports, temporary workdir) and never touch existing containers:

```bash
docker run -d --name verify-halo-mysql-<rand> -e MYSQL_ROOT_PASSWORD='<pw>' \
  -e MYSQL_DATABASE=halo -p 13306:3306 mysql:8
./gradlew :application:bootRun --args="--spring.profiles.active=mysql --server.port=$PORT --halo.work-dir=$WORK/workdir --spring.r2dbc.url=r2dbc:pool:mysql://127.0.0.1:13306/halo --spring.r2dbc.username=root --spring.r2dbc.password=<pw>"
```

- Delete the container after testing and confirm no volumes remain.
- If a dialect cannot be fully verified, state "H2 verified only; MySQL/MariaDB/PostgreSQL not verified" in the report.

## Cannot verify locally (state honestly in the report)

- MySQL/MariaDB/PostgreSQL dialect startup and migration matrix: tests run on H2 only; this repo's CI has no database matrix.
- Real email/SMS delivery: notifications and email verification codes need SMTP/external services; at most verify that sending was triggered.
- Plugin-market install/upgrade and legacy plugin/theme compatibility: needs internet and a real market environment.
- Third-party login providers and real OAuth2 callbacks: depend on external services.
- Reverse proxies, multi-node deployment, session sharing, public rate limiting.
- Performance/load/memory: not covered by functional verification; use a profiling approach.
- GitHub Actions, container image builds, Maven Central publishing: CI only.
