## Why

Frontend pages that are rendered outside a theme-native template can render their own fallback templates today, but they
do not have a stable way to reuse the active theme's page shell. This forces themes and plugins to hard-code integration
templates for each other, causing duplicated work and inconsistent user-facing pages.

## What Changes

- Define `templates/layout.html` as the standard theme layout contract for contract-aware frontend pages.
- Add a core fallback `layout.html` so plugins using the contract still render when the active theme does not provide a
  compatible layout.
- Treat `layout.html` as a special integration template for plugin-owned templates so the active theme layout is preferred
  over plugin-local templates, with core fallback when the theme is missing or incompatible.
- Require the v1 layout fragment signature to accept `head` and `content` fragment parameters.
- Detect theme layout support during theme reconciliation and expose the result in Theme status for Console display.
- Show layout compatibility state in Console theme management to guide users and theme authors.
- Preserve existing themes and plugins by falling back instead of failing installation or rendering.

## Capabilities

### New Capabilities

- `page-layout-contract`: Standard rendering contract and compatibility reporting for pages that integrate with the
  active theme layout.

### Modified Capabilities

None.

## Impact

- Backend theme rendering and template resolution in `application/src/main/java/run/halo/app/theme`.
- Theme reconciliation and `Theme.status` in `api/src/main/java/run/halo/app/core/extension/Theme.java` and
  `application/src/main/java/run/halo/app/core/reconciler/ThemeReconciler.java`.
- System templates under `application/src/main/resources/templates`.
- Console theme management UI and generated API client if Theme status changes.
- Developer documentation and starter-theme guidance for theme and plugin authors.
