## Why

Console menu item creation already lets administrators choose a parent item, but editing an existing menu item cannot change that parent. Now that menu hierarchy is stored through `MenuItem.spec.parent`, the editor can safely expose parent changes without touching legacy child arrays.

## What Changes

- Show the parent menu item selector when editing an existing MenuItem.
- Allow moving an existing MenuItem to another parent or to the menu root from the edit modal.
- Exclude the current MenuItem and its descendants from parent choices.
- Reuse the existing Console position update API so parent changes update `spec.parent` and sibling priorities consistently.
- Keep drag-and-drop as the only UI for precise sibling ordering; edit-modal parent changes append the item to the target sibling list.
- Do not change category editing behavior in this change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `menu-hierarchy`: Console menu item editing can update a MenuItem's parent through the existing parent-reference hierarchy model.

## Impact

- UI: `ui/console-src/modules/interface/menus` edit modal and menu tree refresh flow.
- Specs/tests: `menu-hierarchy` spec delta plus focused frontend tests for parent option filtering and save behavior.
- API/backend: no new API, storage field, database migration, or authorization rule expected; implementation should reuse existing Console MenuItem tree and position APIs.
- i18n: no new copy expected if the existing parent selector label and placeholder are reused.
