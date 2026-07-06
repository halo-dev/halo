## Context

MenuItem hierarchy now uses `MenuItem.spec.menuName` for ownership and `MenuItem.spec.parent` for nesting. Console menu management already reads a canonical tree from the backend and uses the position update API for drag-and-drop moves.

The edit modal currently has parent-selection state and option building for creation, but it hides the field in update mode and only writes `spec.parent` when creating a MenuItem. Editing ordinary fields still uses the generic MenuItem update API.

## Goals / Non-Goals

**Goals:**

- Let administrators change an existing MenuItem's parent from the edit modal.
- Keep hierarchy persistence on the existing Console position update API.
- Prevent invalid parent selections in the UI by excluding the current MenuItem and descendants.
- Keep precise sibling ordering in the drag-and-drop flow.

**Non-Goals:**

- Do not add backend APIs or storage fields.
- Do not update legacy `Menu.spec.menuItems` or `MenuItem.spec.children`.
- Do not change category parent editing.
- Do not make the edit modal a full ordering UI.

## Decisions

1. Reuse `UpdateMenuItemPosition` for parent changes.

   The edit modal should save ordinary MenuItem fields through the existing update API, then call the position update API only when the selected parent differs from the original parent. This keeps validation and priority recalculation in the backend path already used by drag-and-drop.

   Alternative considered: directly mutate `formState.spec.parent` during `updateMenuItem`. That would bypass backend move validation and would not normalize sibling priorities.

2. Append moved items to the target sibling list.

   The edit modal should send `beforeName: null` for parent changes. This makes "change parent" a simple placement operation, while drag-and-drop remains the precise ordering tool.

   Alternative considered: let the modal choose a sibling position. That adds UI and testing surface that duplicates drag-and-drop.

3. Filter the parent tree in the frontend.

   Parent choices should come from the selected Menu's canonical tree and exclude the current MenuItem plus all descendants. The edit modal should use a menu-domain FormKit input so the choices keep their hierarchy in the UI without adding another global input under `ui/src/formkit`. The backend still rejects invalid moves, but hiding invalid choices avoids a save-time error for predictable cases.

   Alternative considered: show all items and rely on backend validation. That is simpler but exposes choices users can never save.

4. Keep ordinary-field save and parent move as separate operations.

   If the ordinary-field update succeeds and the later position update fails, the modal should refresh the canonical tree and surface the move failure without trying to roll back ordinary fields. A backend compound update would be required for all-or-nothing semantics, and that is outside this small UI change.

## Risks / Trade-offs

- Partial success between ordinary-field update and parent move -> Refresh the canonical tree after move failure and keep the backend as the source of truth.
- Stale tree data while editing -> Backend position validation remains authoritative and rejects invalid parent or descendant moves.
- Parent option filtering misses an edge case -> Backend validation still prevents invalid hierarchy writes.

## Migration Plan

No data migration is required. Rollback is limited to hiding the parent selector in update mode and removing the edit-save position update call.

## Open Questions

None.
