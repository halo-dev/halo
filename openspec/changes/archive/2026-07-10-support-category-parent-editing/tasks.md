## 1. Category Parent Utilities

- [x] 1.1 Add category tree helpers to derive selectable parent candidates while excluding the current category and descendants.
- [x] 1.2 Add a helper to build a parent move request only when the selected parent differs from the original parent.
- [x] 1.3 Cover the helper behavior with focused unit tests for root moves, child moves, unchanged parents, and descendant exclusion.

## 2. Edit Modal Integration

- [x] 2.1 Show a tree-aware parent category selector in the category edit modal for both create and update modes.
- [x] 2.2 Initialize the parent select from the category being edited.
- [x] 2.3 Save normal category fields through the existing category update path.
- [x] 2.4 When the parent changes, call `updateCategoryPosition` with the selected `parentName` and no `beforeName`.
- [x] 2.5 Refresh the canonical `post-categories` query after successful saves and after parent move failures.
- [x] 2.6 Report parent move failures without keeping unconfirmed local hierarchy state.

## 3. Validation

- [x] 3.1 Run the focused category utility unit test.
- [x] 3.2 Run `pnpm -C ui format`.
- [x] 3.3 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 3.4 Run `openspec validate support-category-parent-editing --strict`.
- [x] 3.5 Run `git diff --check`.
