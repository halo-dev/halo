## 1. Menu Item Edit Behavior

- [x] 1.1 Show a tree-aware parent MenuItem selector in `MenuItemEditingModal.vue` for update mode as well as create mode.
- [x] 1.2 Filter the parent tree to include the root option and same-menu MenuItems, excluding the current MenuItem and all descendants.
- [x] 1.3 Track the original parent value so unchanged edits do not trigger a hierarchy move.
- [x] 1.4 After ordinary MenuItem update succeeds, call `updateMenuItemPosition` only when the selected parent changed, using `beforeName` unset or null.
- [x] 1.5 Refresh or replace the selected MenuItem tree after save success or parent-move failure so the backend canonical tree remains the source of truth.

## 2. Tests and Validation

- [x] 2.1 Add focused frontend tests for parent option filtering, including exclusion of the current MenuItem and descendants.
- [x] 2.2 Add focused frontend tests for save behavior when the parent is unchanged, changed to another parent, and changed to root.
- [x] 2.3 Run `pnpm -C ui format`.
- [x] 2.4 Run the relevant menu frontend unit tests.
- [x] 2.5 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 2.6 Run `openspec validate support-menu-item-parent-editing --strict`.
