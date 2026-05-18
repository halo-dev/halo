## Context

Themes are stored under `$workDir/themes/<themeName>`. The current Console uninstall action calls the generic Theme
extension delete API, and `ThemeReconciler` later removes theme files by recursively deleting that theme directory.

This is convenient for installed theme packages, but risky for local theme development because Halo also supports
working directly inside the `themes` directory. If a developer manages that theme from the Console, a normal uninstall
can remove uncommitted source changes.

## Goals / Non-Goals

**Goals:**

- Add a Console-specific delete path that protects possible local development themes before the Theme resource is
  deleted.
- Detect possible development themes using filesystem indicators in the theme directory.
- Support an explicit `force=true` retry so administrators can intentionally delete a protected theme.
- Update the Console uninstall UI to use the guarded endpoint and show a stronger second confirmation when needed.
- Keep the generic Theme extension API and existing reconciler cleanup semantics unchanged.

**Non-Goals:**

- Do not add a formal Theme development mode or new Theme API fields.
- Do not change `ThemeReconciler` deletion behavior.
- Do not prevent direct callers from using the generic Theme extension delete API.
- Do not add database migrations or new dependencies.

## Decisions

### 1. Add a guarded Console delete endpoint

- **Choice**: Add `DELETE /apis/api.console.halo.run/v1alpha1/themes/{name}?force={boolean}` in the existing
  `ThemeEndpoint`.
- **Rationale**: The existing `ThemeEndpoint` already owns Console theme operations such as install, upgrade, reload,
  activation, config fetch/update, and list. Keeping this behavior in the Console API makes the protection explicit to
  Console workflows without changing the generic extension CRUD contract.
- **Alternative considered**: Add logic to the generic Theme delete API. Rejected because it would change low-level
  extension semantics and affect non-Console callers.

### 2. Return conflict when a possible development theme is detected

- **Choice**: When `force` is not true and the theme directory contains development indicators, reject with HTTP
  `409 Conflict`.
- **Rationale**: This represents a state conflict with the requested destructive operation and gives the frontend a
  stable signal for the second confirmation flow.
- **Alternative considered**: Return a successful preflight response that tells the frontend whether force is required.
  Rejected because it adds another round trip and separates the check from the operation, which creates room for drift.

### 3. Treat detection as heuristic, not identity

- **Choice**: Detect "possible development theme" by checking direct children of the theme directory for indicators:
  `.git`, `package.json`, `pnpm-lock.yaml`, `yarn.lock`, `package-lock.json`, and `node_modules`.
- **Rationale**: Halo does not currently store theme origin or development mode. These signals are common for theme
  source directories and are cheap to inspect. The UI wording should say the theme "may be" under development.
- **Alternative considered**: Only check `.git`. Rejected because many local development directories may not be a Git
  working tree, especially when copied or scaffolded.

### 4. Keep actual file deletion in the reconciler

- **Choice**: The guarded endpoint deletes the Theme resource through `ReactiveExtensionClient`; it does not delete
  files itself.
- **Rationale**: `ThemeReconciler` already performs cleanup for settings, annotation settings, cache, and theme files.
  Reusing that path avoids creating a second deletion implementation.
- **Alternative considered**: Delete files directly in the Console endpoint. Rejected because it would duplicate
  cleanup responsibilities and risk diverging from existing lifecycle behavior.

### 5. Regenerate the generated UI API client

- **Choice**: The frontend calls the generated Console API method after OpenAPI regeneration.
- **Rationale**: This repo treats `ui/packages/api-client/src/` as generated output. The endpoint should be documented
  in SpringDoc and consumed through the generated client rather than handwritten Axios calls.

## Risks / Trade-offs

- **[Risk] False positives block ordinary uninstalls.**
  - **Mitigation**: The block is recoverable through the force confirmation flow.
- **[Risk] False negatives still allow deleting a development theme.**
  - **Mitigation**: Use multiple common development indicators and keep the normal irreversible-operation warning.
- **[Risk] Direct generic API deletion bypasses the guard.**
  - **Mitigation**: Scope this change explicitly to Console accidental deletion. Stronger global protection would require
    a separate design that changes Theme deletion semantics.
- **[Risk] Filesystem checks are blocking.**
  - **Mitigation**: Keep checks shallow and run them in a bounded elastic flow if needed by the endpoint implementation.

## Migration Plan

No data migration is required. Existing themes continue to use the same Theme resources and filesystem layout. The
Console starts using the new guarded endpoint after the generated API client is refreshed.

Rollback is straightforward: revert the Console call site to the generic Theme delete API and remove the Console route.

## Open Questions

None.
