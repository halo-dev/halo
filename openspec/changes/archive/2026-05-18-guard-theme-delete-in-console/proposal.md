## Why

Theme uninstall from the Console currently calls the generic Theme delete API. The delete flow eventually removes
`$workDir/themes/<themeName>` recursively, so an administrator can accidentally delete a theme directory that is also
being used for local theme development.

The same signal is also useful outside Console uninstall. For example, an app marketplace client can warn before
upgrading an already-published theme that is currently being developed locally, because the upgrade flow overwrites the
theme directory.

Halo should reconcile this local-development heuristic into Theme status so all clients can make protective UX
decisions without introducing a new delete API.

## What Changes

- Add `status.inDevelopment` to Theme status.
- Let `ThemeReconciler` inspect the theme directory for local development indicators such as `.git`, `package.json`,
  lockfiles, or `node_modules`, and keep `status.inDevelopment` up to date.
- Let the Console theme uninstall UI keep calling the existing Theme delete API, but require a second, stronger
  confirmation when `status.inDevelopment` is true.
- Keep the existing Theme reconciler cleanup behavior unchanged; it remains responsible for deleting theme files after a
  Theme resource is actually deleted.

## Capabilities

### New Capabilities

- `theme-delete-guard`: Theme development-workspace status and Console uninstall warning for possible local development
  theme directories.

### Modified Capabilities

- None.

## Impact

- `api/src/main/java/run/halo/app/core/extension/Theme.java` — expose `status.inDevelopment`.
- `application/src/main/java/run/halo/app/core/reconciler/ThemeReconciler.java` — reconcile development-workspace
  status from the theme directory.
- `application/src/test/java/run/halo/app/core/reconciler/ThemeReconcilerTest.java` — cover development-workspace
  status updates.
- `ui/console-src/modules/interface/themes/components/operation/UninstallOperationItem.vue` — show a second stronger
  warning before uninstalling a theme whose status says it may be under local development.
- `ui/src/locales/*.json` — add i18n strings for the possible development theme warning.
- OpenAPI-generated UI API client — regenerate after adding the Theme status field.
- No database migration or new dependency is required.
