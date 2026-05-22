## 1. Backend Status

- [x] 1.1 Add `status.inDevelopment` to Theme status.
- [x] 1.2 Let `ThemeReconciler` inspect `$workDir/themes/<themeName>` for `.git`, `package.json`, `pnpm-lock.yaml`,
  `yarn.lock`, `package-lock.json`, and `node_modules`.
- [x] 1.3 Set `status.inDevelopment=true` when indicators are found.
- [x] 1.4 Set `status.inDevelopment=false` when indicators are not found.

## 2. Backend Tests and Generated Client

- [x] 2.1 Add `ThemeReconcilerTest` coverage for themes with development indicators.
- [x] 2.2 Add `ThemeReconcilerTest` coverage for themes without development indicators.
- [x] 2.3 Run `./gradlew generateOpenApiDocs` and `pnpm -C ui api-client:gen` to refresh generated OpenAPI and UI API
  artifacts.

## 3. Console UI

- [x] 3.1 Keep theme uninstall on the existing Theme delete API.
- [x] 3.2 Use `theme.status.inDevelopment` to show a second local-development warning after the ordinary uninstall
  confirmation.
- [x] 3.3 Keep Setting and ConfigMap deletion behind successful Theme deletion.
- [x] 3.4 Add i18n strings for the possible development theme warning in supported Console locale files.

## 4. Validation

- [x] 4.1 Run `./gradlew spotlessApply` or `./gradlew spotlessCheck` for backend formatting.
- [x] 4.2 Run focused backend tests for `ThemeReconciler`.
- [x] 4.3 Run `pnpm -C ui typecheck` and `pnpm -C ui lint`.
- [ ] 4.4 Optionally smoke-test the Console theme uninstall flow in browser with a Theme whose status has
  `inDevelopment=true`.
