## Context

Themes are stored under `$workDir/themes/<themeName>`. Uninstall and upgrade are both destructive for that directory:
uninstall eventually deletes it through `ThemeReconciler`, while upgrade replaces it with the uploaded package.

This is convenient for installed theme packages, but risky for local theme development because Halo also supports
working directly inside the `themes` directory. If a developer manages that theme from Console or an app marketplace
client, a normal uninstall or upgrade can remove uncommitted source changes.

## Goals / Non-Goals

**Goals:**

- Expose a Theme status field that indicates whether the theme directory appears to be a local development workspace.
- Detect possible development themes using shallow filesystem indicators in the theme directory.
- Let Console ask for a second confirmation before uninstalling a Theme whose status says it may be under local
  development.
- Make the status usable by other clients, such as an app marketplace plugin that wants to warn before upgrade.
- Keep existing Theme delete and cleanup semantics unchanged.

**Non-Goals:**

- Do not add a new Console-specific delete API.
- Do not block Theme deletion or upgrade in the backend.
- Do not add a formal user-controlled Theme development mode.
- Do not add database migrations or new dependencies.

## Decisions

### 1. Reconcile a Theme status field instead of guarding one endpoint

- **Choice**: Add `status.inDevelopment` to Theme and set it from `ThemeReconciler`.
- **Rationale**: The local-development signal is useful for more than Console uninstall. Storing it in Theme status lets
  Console, app marketplace plugins, and other consumers share the same signal without duplicating filesystem heuristics.
- **Alternative considered**: Add a guarded Console delete endpoint with a `force` flag. Rejected because it only covers
  Console deletion and does not help clients that need to warn before theme upgrade.

### 2. Treat detection as heuristic, not identity

- **Choice**: Detect "possible development theme" by checking direct children of the theme directory for indicators:
  `.git`, `package.json`, `pnpm-lock.yaml`, `yarn.lock`, `package-lock.json`, and `node_modules`.
- **Rationale**: Halo does not currently store theme origin or development mode. These signals are common for theme
  source directories and are cheap to inspect. UI wording should say the theme "may be" under development.
- **Alternative considered**: Only check `.git`. Rejected because many local development directories may not be a Git
  working tree, especially when copied or scaffolded.

### 3. Keep destructive behavior unchanged

- **Choice**: Continue using the existing Theme delete API and existing `ThemeReconciler` cleanup path.
- **Rationale**: The current deletion lifecycle already handles settings, annotation settings, cache, and files. The new
  status supports safer UX without introducing another deletion implementation or changing low-level extension semantics.
- **Alternative considered**: Block backend deletion when `status.inDevelopment` is true. Rejected because the immediate
  problem is accidental frontend operation, and backend blocking would require a broader override and compatibility
  design.

### 4. Regenerate generated API artifacts

- **Choice**: Regenerate OpenAPI docs and the generated UI API client after adding the Theme status field.
- **Rationale**: This repo treats `ui/packages/api-client/src/` as generated output. Consumers should see the new status
  field through the normal generated types.

## Risks / Trade-offs

- **[Risk] False positives show a second stronger warning for ordinary uninstalls.**
  - **Mitigation**: The warning is recoverable; the user can still confirm and proceed.
- **[Risk] False negatives still allow deleting or upgrading a development theme.**
  - **Mitigation**: Use multiple common development indicators and keep the normal irreversible-operation warning.
- **[Risk] Status can be stale until the next reconciliation.**
  - **Mitigation**: Store the signal in status for client interoperability, and rely on existing theme reload/reconcile
    flows to refresh it when theme resources change.

## Migration Plan

No data migration is required. Existing themes continue to use the same Theme resources and filesystem layout. The new
status field is populated the next time each Theme is reconciled.

Rollback is straightforward: remove the status field, remove the reconciler assignment, and let Console use the ordinary
uninstall warning for every Theme.

## Open Questions

None.
