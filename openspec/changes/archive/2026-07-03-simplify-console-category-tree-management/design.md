## Context

Category hierarchy now uses `Category.spec.parent` as the runtime source of truth, but Console category management still owns too much hierarchy behavior in Vue utilities. The current flow lists flat Categories, builds a local editable tree, recalculates every sibling priority after drag-and-drop, flattens the tree back to Categories, and sends concurrent JSON Patch requests.

The Console menu hierarchy work established a better boundary: backend Console APIs return canonical tree data and accept a single relative move request, while the frontend keeps only interaction state. Category management should follow that model, adjusted for categories not having an owning menu field.

## Goals / Non-Goals

**Goals:**

- Provide a backend Console API that returns a canonical editable Category tree.
- Provide a backend Console API that moves one Category by target parent and next sibling.
- Keep `children` in the Console response as view data, not as `Category.spec.children`.
- Refactor category management and shared category select code to consume backend tree data.
- Remove frontend hierarchy persistence code that calculates priorities and batch patches Categories.
- Preserve existing category create, update, delete, theme, public API, and parent-reference storage behavior.

**Non-Goals:**

- Do not add a Console-specific Category create API in this change.
- Do not move category creation priority calculation to the backend in this change.
- Do not remove or rewrite deprecated `Category.spec.children`.
- Do not reuse theme-facing `CategoryTreeVo` for Console editing payloads.
- Do not change Console UI permission strings from `system:posts:*` to `system:categories:*`.
- Do not change category deletion semantics.

## Decisions

1. Add Console-specific tree and position endpoints.

   Use `GET /apis/api.console.halo.run/v1alpha1/categories/-/tree` to list the editable tree and `PUT /apis/api.console.halo.run/v1alpha1/categories/{name}/position` to move one Category. A dedicated position endpoint is clearer than accepting a whole tree because the operation is a single user drag-and-drop move.

   Alternative considered: return a tree but keep frontend batch patches. That removes only part of the current complexity and keeps partial save failures possible.

2. Use a Console DTO instead of `CategoryTreeVo`.

   The response node should be shaped as `CategoryTreeNode { Category category; List<CategoryTreeNode> children; }`. `CategoryTreeVo` is theme-facing and includes theme output concerns such as `parentName` and post-count projection. Console editing needs the original Category extension plus view-only child nodes.

   Alternative considered: extend Category objects with `children` in the API response. That keeps the existing frontend shape but blurs the line between extension state and editable tree view data.

3. Backend owns move validation and priority recalculation.

   The position request contains `parentName` and `beforeName`. Missing `parentName` means root level; missing `beforeName` means append to the end of the target sibling list. The service validates that the parent exists, the target is not itself or a descendant, and `beforeName` is a sibling in the target parent after the move. It then recalculates continuous integer priorities for affected sibling lists and updates only Categories whose parent or priority changed.

   Alternative considered: frontend computes all new priorities and sends a patch list. That preserves the current failure mode and duplicates canonical ordering rules in the UI.

4. Frontend derives only view helpers from backend tree data.

   `usePostCategory()` should call the Console tree API and expose both `categoriesTree` and flattened `categories`. Search, selected value resolution, category filters, keyboard navigation, and path labels can still use frontend flatten/path helpers. Drag-and-drop should keep a previous tree snapshot, derive one move request from previous/current tree, and replace local state with the response tree.

   Alternative considered: have each consumer call a different endpoint. That would fragment category state and keep tree-building behavior in secondary consumers such as `categorySelect`.

5. Leave category creation as-is.

   Category creation currently uses the core Category API and computes a new sibling priority in the frontend. Fully moving that responsibility to the backend would require a Console create API and broader edit-modal changes. This change targets tree read and position update only.

6. Add RBAC resources without changing UI permission semantics.

   Category role templates should include Console resources for `categories/tree` and `categories/position`. Existing Console route/button permissions use `system:posts:*`; changing those UI permission strings is a separate authorization cleanup.

## Risks / Trade-offs

- Concurrent hierarchy edits can conflict -> retry optimistic locking once in the backend and return a conflict if the move cannot be applied; the frontend refetches the canonical tree on failure.
- Invalid imported parent data could hide nodes -> tree construction treats invalid, self, missing, or cyclic parent chains as roots so Console remains usable.
- Frontend move detection may fail for unexpected multi-node changes -> if no single move explains the drag result, refetch instead of persisting an ambiguous local state.
- Category creation still calculates initial priority in the frontend -> acceptable for this scoped change; a later Console create API can remove that remaining calculation.
- New Console API requires generated client changes -> regenerate OpenAPI docs and the UI API client in the implementation.

## Migration Plan

1. Add backend Console DTOs, service, endpoint, RBAC rules, and tests.
2. Regenerate OpenAPI docs and the generated UI API client.
3. Update `usePostCategory()` and category consumers to use the Console tree API.
4. Replace batch hierarchy save with single position updates and canonical response replacement.
5. Remove now-unused frontend hierarchy persistence utilities and update focused unit tests.

Rollback is code-level only. No stored data migration is introduced, and existing `Category.spec.parent` storage remains unchanged.

## Open Questions

None.
