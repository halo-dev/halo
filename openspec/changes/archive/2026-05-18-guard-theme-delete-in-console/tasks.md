## 1. Backend Console API

- [x] 1.1 Add a `DELETE themes/{name}` route to `ThemeEndpoint` with an optional `force` query parameter and SpringDoc metadata.
- [x] 1.2 Implement shallow development-indicator detection for `$workDir/themes/<themeName>` using `.git`, `package.json`, `pnpm-lock.yaml`, `yarn.lock`, `package-lock.json`, and `node_modules`.
- [x] 1.3 Return HTTP `409 Conflict` without deleting the Theme when indicators are found and `force` is not true.
- [x] 1.4 Delete the Theme resource through `ReactiveExtensionClient` when no indicators are found or `force=true`, leaving file cleanup to `ThemeReconciler`.

## 2. Backend Tests and Generated Client

- [x] 2.1 Add `ThemeEndpointTest` coverage for rejecting possible development themes without force.
- [x] 2.2 Add `ThemeEndpointTest` coverage for deleting possible development themes with force.
- [x] 2.3 Add `ThemeEndpointTest` coverage for deleting ordinary themes without force.
- [x] 2.4 Run `./gradlew generateOpenApiDocs` and `pnpm -C ui api-client:gen` to refresh the generated Console API client.

## 3. Console UI

- [x] 3.1 Update `UninstallOperationItem.vue` to call the generated Console theme delete API instead of the generic Core Theme delete API.
- [x] 3.2 Detect the guarded delete conflict response and show a second danger confirmation for possible local development themes.
- [x] 3.3 Retry deletion with `force=true` only after the second confirmation.
- [x] 3.4 Keep Setting and ConfigMap deletion behind successful Theme deletion, including the forced deletion path.
- [x] 3.5 Add i18n strings for the possible development theme warning in supported Console locale files.

## 4. Validation

- [x] 4.1 Run `./gradlew spotlessApply` or `./gradlew spotlessCheck` for backend formatting.
- [x] 4.2 Run focused backend tests for `ThemeEndpoint`.
- [x] 4.3 Run `pnpm -C ui typecheck` and `pnpm -C ui lint`.
- [ ] 4.4 Optionally smoke-test the Console theme uninstall flow in browser with an ordinary theme and a theme directory containing development indicators.
