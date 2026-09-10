## 1. Source recognition and conversion

- [x] 1.1 Replace supported-source edit locks with all eight existing choices and recognize resources by group, version, and kind; verify known sources are editable while unknown kinds, foreign-group kind collisions, and unsupported versions remain locked and survive unrelated saves unchanged.
- [x] 1.2 Implement session-local editable name and custom-URL preservation, one-time resolved/default seeding, and resource-name reset on actual source changes; verify create/edit round trips, deliberately cleared fields, missing resolved status, unchanged resource saves, and returning to a previously visited resource type.

## 2. Submission and annotation consistency

- [x] 2.1 Normalize source fields into a separate MenuItem view and clone the submitted object, retaining existing annotation validation and merge behavior; verify custom/route/resource conversion payloads in both directions, mutual exclusion of source fields, and preserved drafts after rejected create/update requests.
- [x] 2.2 Supply normalized source context to annotation schemas without replacing their value or remounting the form during source changes; verify unsaved annotations survive and schemas observe the new source without an old resource name or inactive text fields.
- [x] 2.3 Preserve the existing update, parent-move, and saved-event flow; verify source-only edits do not move items, simultaneous parent edits invoke the existing position endpoint, and parent-move failure still reports partial success without a full-success toast.

## 3. Integration and validation

- [x] 3.1 Replace the existing lock expectations and add a focused real-FormKit/Halo-select interaction test with mocked resource responses; verify actual source selection updates the visible fields, resource input state resets, empty required selections and whitespace-only names block writes, and hidden fields do not block valid submissions.
- [x] 3.2 Exercise a local Console item with children through resource-to-custom, custom-to-resource, resource-to-route, and cross-resource conversion; save and reopen after reconciliation to verify resolved rendering, identity, hierarchy, annotations, and opening target. Also verify keyboard selection, focus, narrow layout, and validation feedback; record any unavailable runtime checks explicitly.
- [x] 3.3 Run focused menu tests plus `pnpm -C ui lint` and `pnpm -C ui typecheck` using the repository-declared package manager; verify changed-file formatting, `git diff --check`, locale parity for any added text, and absence of generated-file or dependency changes.
- [x] 3.4 Run `openspec validate allow-menu-item-source-editing --strict` and review the final implementation against every delta requirement; record verification results and only mark completed implementation tasks as done.

## Verification record

- 2026-09-10: `NODE_ENV=test pnpm -C ui exec vp test run console-src/modules/interface/menus --reporter=default` passed: 3 files, 47 tests, including 3 real-FormKit/Halo-select interaction tests. These use mocked API responses and do not establish backend persistence or visual correctness.
- `pnpm -C ui lint`, `pnpm -C ui typecheck`, changed-file `vp fmt --check`, `git diff --check`, and strict OpenSpec validation passed. The repository-selected pnpm version is 11.17.0.
- Existing workspace package build outputs were stale (missing route-reference and comment-editing declarations); `pnpm -C ui build:packages` refreshed ignored build output before typechecking. No generated source, lockfile, dependency, API, or locale changes were made.
- Real-selector tests expose the existing `SelectOption.vue` invalid-watch-source warning when resolving resource options. That shared selector code was not modified; no test failures or unhandled errors remain.
- 2026-09-10: Task 3.2 completed based on the user's confirmation that Console acceptance testing passed. The earlier automated browser check was blocked by the unavailable authenticated session; acceptance was performed by the user, not the agent.
