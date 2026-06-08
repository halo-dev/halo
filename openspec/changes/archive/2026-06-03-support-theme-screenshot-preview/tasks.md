## 1. API Contract

- [x] 1.1 Add optional `screenshot` to `Theme.ThemeStatus` with a concise schema/documentation comment.
- [x] 1.2 Add or update API-level tests that assert `Theme.status.screenshot` serializes as part of Theme status.

## 2. Backend Implementation

- [x] 2.1 Add deterministic screenshot detection for `screenshot.png`, `screenshot.jpeg`, `screenshot.jpg`, and
  `screenshot.webp` in `ThemeReconciler.reconcileStatus`.
- [x] 2.2 Set `Theme.status.screenshot` to a public screenshot URL when a supported root-level file is readable, and clear
  it when no supported file exists.
- [x] 2.3 Add focused `ThemeReconcilerTest` coverage for detected screenshots, deterministic file priority, and missing
  screenshots.
- [x] 2.4 Add a narrow static route or resource handler for `/themes/{themeName}/screenshot.{extension}` that serves only
  supported root-level screenshot files.
- [x] 2.5 Preserve directory traversal protection and add route/resource tests for successful serving, unsupported files,
  missing files, and traversal attempts.
- [x] 2.6 Add the screenshot route to the public static resource matcher in WebFlux security configuration.

## 3. OpenAPI And Console

- [x] 3.1 Regenerate OpenAPI docs and the generated UI api-client after the Theme status model change.
- [x] 3.2 Update Console theme list cards to derive the preview image from `theme.status?.screenshot || theme.spec.logo`.
- [x] 3.3 Update Console theme preview selector rows to use the same screenshot-first fallback.
- [x] 3.4 Keep existing empty/error/loading image states and behavior for themes without screenshots.

## 4. Verification

- [x] 4.1 Run `./gradlew :api:test --tests "*ThemeTest*"`.
- [x] 4.2 Run `./gradlew :application:test --tests "*ThemeReconcilerTest*"`.
- [x] 4.3 Run focused route/security tests covering theme screenshot static resources.
- [x] 4.4 Run `./gradlew spotlessCheck`.
- [x] 4.5 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 4.6 Run `openspec validate support-theme-screenshot-preview --strict`.
