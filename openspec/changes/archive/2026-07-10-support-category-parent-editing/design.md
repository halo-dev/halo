## Context

Console category creation already lets administrators choose a parent category, but the edit modal hides that field in update mode. Category hierarchy is now represented by `Category.spec.parent`, and the Console backend already exposes canonical tree and position APIs that validate moves, prevent cycles, normalize priorities, and return the canonical tree.

The change should stay UI-scoped. It should not introduce new backend routes, mutate hierarchy through frontend JSON patches, or change existing category selector behavior for callers that do not opt in to exclusion.

## Goals / Non-Goals

**Goals:**

- Let administrators change an existing category's parent from the edit modal.
- Prevent selecting the current category or any of its descendants as the new parent.
- Use the existing Console category position API for parent changes.
- Keep parent-only edits simple by appending the moved category to the target sibling list.

**Non-Goals:**

- Add a sibling-order selector to the edit modal.
- Add new category creation inside the parent selector.
- Change backend category contracts, migration behavior, or public theme/plugin APIs.
- Replace the existing drag-and-drop ordering workflow.

## Decisions

1. Reuse the shared tree-aware category selector with opt-in exclusion.

   The parent control should continue to show the canonical category tree so administrators can see hierarchy while choosing a parent. The shared `categorySelect` input should accept `excludedNames` and filter those nodes from its tree, search results, keyboard navigation, and FormKit option labels. The category edit modal should pass the current category as the excluded subtree root and disable inline creation in update mode.

   Alternative considered: use a plain FormKit `select`. That is simple but loses hierarchy context in the editing form. Adding opt-in props to `categorySelect` keeps existing callers unchanged while preserving the tree UI for this workflow.

2. Move categories through `updateCategoryPosition` only when the parent changes.

   Normal category fields should continue to save through the existing category update path. If the selected parent differs from the original parent, the modal should call the Console category position API with `parentName` set to the selected parent or unset for root, and `beforeName` unset.

   Alternative considered: set `formState.spec.parent` before `updateCategory`. That would bypass backend move validation and priority normalization, and would not keep the original and target sibling lists canonical.

3. Let backend append moved categories to the target sibling list.

   The edit modal is a parent reassignment workflow, not a precise ordering workflow. Passing `beforeName` unset uses the existing backend contract to append the moved category to the target parent or root list. Administrators can still use drag-and-drop for exact ordering.

4. Refresh the canonical category query after save.

   The current category management page is driven by the `post-categories` query and canonical tree API. After a successful edit and any optional parent move, the UI should refresh or replace that query data so the tree reflects backend-confirmed hierarchy. On position update failure, it should surface the failure and reload the canonical tree instead of keeping a stale local hierarchy.

## Risks / Trade-offs

- Field update succeeds but parent move fails -> keep the successfully saved non-hierarchy fields, show a failure toast for the parent move, and refresh the canonical category tree.
- Category tree data is stale while the modal is open -> backend validation remains authoritative; refreshing the query after save corrects the UI.
- The edited category has invalid existing hierarchy data -> canonical tree rendering already treats invalid parent references as root; the parent candidate filter should still exclude the current node when present.
- Shared selector changes could affect other forms -> keep exclusion and creation control opt-in, with existing defaults preserving current behavior.
