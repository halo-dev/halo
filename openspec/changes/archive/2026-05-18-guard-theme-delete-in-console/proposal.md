## Why

Theme uninstall from the Console currently calls the generic Theme delete API. The delete flow eventually removes
`$workDir/themes/<themeName>` recursively, so an administrator can accidentally delete a theme directory that is also
being used for local theme development.

Halo should guard this Console workflow before the Theme resource is deleted, while still allowing an explicit forced
delete when the administrator confirms the risk.

## What Changes

- Add a Console-specific theme delete API that accepts a `force` query parameter.
- Before deleting a Theme through this Console API, inspect the theme directory for local development indicators such as
  `.git`, `package.json`, lockfiles, or `node_modules`.
- When development indicators are found and `force` is not true, reject the delete request instead of deleting the
  Theme resource.
- Let the Console theme uninstall UI call the new Console API and, when the guard rejects the first request, show a
  stronger confirmation dialog before retrying with `force=true`.
- Keep the existing Theme reconciler cleanup behavior unchanged; it remains responsible for deleting theme files after a
  Theme resource is actually deleted.

## Capabilities

### New Capabilities

- `theme-delete-guard`: Console theme deletion guard for possible local development theme directories and forced retry.

### Modified Capabilities

- None.

## Impact

- `application/src/main/java/run/halo/app/theme/endpoint/ThemeEndpoint.java` — add Console delete route and development
  theme detection before deletion.
- `application/src/test/java/run/halo/app/theme/endpoint/ThemeEndpointTest.java` — cover guarded delete, forced delete,
  and ordinary delete behavior.
- `ui/console-src/modules/interface/themes/components/operation/UninstallOperationItem.vue` — switch theme uninstall to
  the new Console API and add the forced confirmation flow.
- `ui/src/locales/*.json` — add i18n strings for the possible development theme warning.
- OpenAPI-generated UI API client — regenerate after adding the Console API.
- No database migration or new dependency is required.
