## Context

Menu hierarchy is now represented by `MenuItem.spec.menuName`, `MenuItem.spec.parent`, and
`MenuItem.spec.priority`. Theme runtime code already builds tree output on the backend from those
fields, but Console menu management still loads a flat `MenuItem[]`, builds an editable tree in the
frontend, recalculates priority, creates JSON patches, and sends one patch request per affected item
after drag-and-drop.

That split makes the frontend responsible for hierarchy correctness even though the backend owns the
persistent model. It also makes Console behavior drift from the backend's canonical tree handling for
missing parents, self parents, cross-menu parents, cycles, and tie-break sorting.

The change should keep the existing core extension model and theme-facing output intact. It changes the
Console management workflow for reading an editable tree, saving drag-and-drop position changes, and
deleting a Menu with its owned MenuItems.

## Goals / Non-Goals

**Goals:**

- Provide a Console API that returns the canonical MenuItem tree for one selected Menu.
- Provide a Console API that moves one MenuItem within its existing Menu by target parent and relative
  sibling position.
- Keep the backend responsible for hierarchy validation, cycle prevention, priority recalculation, and
  persistence of affected MenuItems.
- Have successful position updates return the canonical tree so Console can replace local drag state.
- Remove frontend responsibility for flat-to-tree construction, priority reset, and batch hierarchy
  JSON patch generation.
- Move Menu deletion cascade for `spec.menuName`-owned MenuItems into a backend Console workflow.
- Keep parent selection for new MenuItems based on the same canonical tree data.
- Add RBAC rules for the new Console subresources under existing menu view/manage role templates.

**Non-Goals:**

- Do not support cross-menu movement of MenuItems. A MenuItem's owning `spec.menuName` is fixed after
  creation for this workflow.
- Do not change Menu/MenuItem extension schemas beyond generated OpenAPI DTOs for the new Console APIs.
- Do not remove or rewrite deprecated `Menu.spec.menuItems` or `MenuItem.spec.children`.
- Do not change theme-facing `MenuVo` / `MenuItemVo` behavior.
- Do not move menu cloning, menu item deletion, or menu item count workflows in this change.
- Do not introduce tree-level revision resources, ETags, new dependencies, or a database migration.

## Decisions

1. Use Console-specific APIs instead of overloading generic extension CRUD.

   The generic MenuItem extension API should remain resource CRUD. Tree retrieval and drag position
   updates are Console workflows that normalize multiple MenuItems and return a management view. Keeping
   them under Console APIs avoids exposing hierarchy write orchestration as generic raw extension
   behavior.

   Alternative considered: continue using frontend-generated JSON Patch operations. That keeps the API
   surface small but preserves the current frontend-heavy validation and multi-request save flow.

2. Use `menuitems` as the API resource and model tree/position as subresources.

   Proposed routes:

   - `GET /apis/api.console.halo.run/v1alpha1/menuitems/-/tree?menuName={menuName}`
   - `PUT /apis/api.console.halo.run/v1alpha1/menuitems/{name}/position`

   The managed resource is MenuItem because the persisted changes are `spec.parent` and `spec.priority`
   updates on MenuItems. In Halo's RBAC parser, these routes map naturally to `menuitems/tree` and
   `menuitems/position` subresources.

   Alternative considered: `menus/{menuName}/items/tree` and `menus/{menuName}/items/{name}/position`.
   That reads well as REST nesting, but RBAC maps it as a `menus/items` subresource even though the
   workflow mutates MenuItems.

3. Return a wrapper tree node DTO for Console.

   The Console tree response should use a management wrapper:

   ```java
   class MenuItemTreeNode {
       MenuItem menuItem;
       List<MenuItemTreeNode> children;
   }
   ```

   This keeps `MenuItem` as the managed resource and makes `children` a view-only field. It also avoids
   copying `metadata`, `spec`, and `status` into a second Console DTO that must track future MenuItem
   shape changes.

   Alternative considered: reuse theme `MenuItemVo` or flatten `metadata/spec/status` onto a Console
   node. Reusing the theme VO couples management workflows to theme compatibility, while flattening adds
   DTO drift without meaningful benefit.

4. Read tree data from backend canonical state.

   The tree API lists only MenuItems whose `spec.menuName` equals the selected Menu. It treats missing
   parents, self parents, parents outside the selected Menu item set, and cyclic parent chains as root
   items. Siblings are ordered by `spec.priority`, creation timestamp, and metadata name, matching the
   canonical backend ordering used for menu output.

   Alternative considered: return invalid data as errors. That would make Console unusable for repairing
   dirty menu data.

5. Save drag-and-drop as a single-item position command.

   The frontend sends the moved item name in the path and the target context in the body:

   ```json
   {
     "menuName": "primary",
     "parentName": "new-parent-name",
     "beforeName": "next-sibling-name"
   }
   ```

   `parentName: null` means root. `beforeName: null` means append to the end of the target sibling list.
   The frontend does not send `priority` or an index.

   Alternative considered: submit the full tree after every drop. That makes stale clients more likely
   to overwrite unrelated changes and still treats the frontend tree as a write source of truth.

6. Validate move semantics strictly.

   The backend validates:

   - the moved item exists;
   - the moved item has `spec.menuName` equal to request `menuName`;
   - `parentName`, when present, exists in the same Menu;
   - `beforeName`, when present, exists in the same Menu;
   - `beforeName` belongs to the target sibling list after the move;
   - the item is not moved under itself or any descendant;
   - the workflow never changes `spec.menuName`.

   Invalid requests should fail instead of being guessed or normalized into a different move.

7. Recalculate continuous sibling priority on the backend.

   The backend inserts the moved item into the target sibling list using `beforeName`, then reassigns
   continuous priorities `0..n-1` for the target sibling list. If the parent changed, it also reassigns
   continuous priorities for the original sibling list. Only MenuItems whose `spec.parent` or
   `spec.priority` actually changed are updated.

   Alternative considered: gap ranking or fractional ranking. Menu trees are small and the current model
   already uses integer priorities, so continuous integers are simpler and match existing behavior.

8. Return the canonical tree after a successful position update.

   `PUT .../position` returns the selected Menu's canonical tree after persistence. Console replaces its
   local draggable tree with the response. On failure, Console reloads the canonical tree and shows an
   error instead of trying to keep the optimistic local tree.

   Alternative considered: return `204 No Content` or only the moved MenuItem. That forces the frontend
   to infer final backend ordering, conflict handling, and concurrent changes.

9. Use current resource versions with retry, not a tree-level revision.

   The position workflow reads current MenuItems, applies the requested move, and updates changed
   MenuItems. If an optimistic locking failure occurs, the backend can retry the same move against fresh
   data. If the move no longer validates or updates still conflict, return a conflict response and let
   Console refresh from the tree API.

   Alternative considered: introduce a tree revision or ETag. That adds a new consistency concept for a
   low-frequency admin workflow and does not map cleanly onto Halo's per-extension versioning.

10. Keep parent select on canonical tree data without adding new editing behavior.

    The create modal should consume the canonical tree and flatten it into parent select options. This
    avoids keeping a second flat list query only for parent choices. Editing an existing MenuItem still
    does not gain parent-changing behavior in this change; hierarchy changes continue through drag and
    drop.

11. Delete a Menu through a Console workflow that cascades owned MenuItems.

    Proposed route:

    - `DELETE /apis/api.console.halo.run/v1alpha1/menus/{name}`

    The deletion scope for MenuItems is `MenuItem.spec.menuName == {name}`. The workflow should delete
    matching MenuItems before deleting the Menu itself so a MenuItem deletion failure does not first
    remove the Menu and leave orphaned MenuItems. The endpoint returns the deleted Menu resource from
    the final Menu deletion.

    Alternative considered: keep frontend deletion as `delete Menu` plus a batch of generic MenuItem
    deletes. That is exactly the current failure mode: a client or network failure between requests can
    leave MenuItems whose owner Menu was already deleted.

## Risks / Trade-offs

- New Console API surface expands RBAC and OpenAPI maintenance -> Add explicit role-template rules,
  endpoint tests, generated client updates, and focused frontend refactor tests.
- Sequential updates can partially succeed if a later MenuItem update fails -> Update through a narrow
  service with retry and changed-item calculation; on failure Console reloads canonical tree. Existing
  extension storage does not provide a multi-item transaction, so tests must cover retry/conflict
  behavior.
- Invalid legacy or raw API data can contain cycles -> Tree building must track visited ancestors and
  treat cyclic items as roots instead of looping.
- The frontend still needs some tree traversal for drag interaction -> Keep traversal limited to deriving
  `name`, `parentName`, and `beforeName`; do not reintroduce priority or patch construction.
- The wrapper DTO changes UI access shape from direct node fields to `node.menuItem` -> Regenerated API
  types and focused component updates should make this explicit.
- Menu and MenuItem deletion is still not database-transactional across extension resources -> Do the
  cascade server-side and delete MenuItems before the Menu so the most common partial-failure mode does
  not orphan MenuItems.

## Migration Plan

1. Add backend Console DTOs and endpoint/service tests for tree read and position update.
2. Implement Console endpoints and service logic.
3. Add RBAC role-template rules for `menuitems/tree` and `menuitems/position`.
4. Add a Console Menu deletion endpoint that deletes owned MenuItems before deleting the Menu.
5. Regenerate OpenAPI docs and the generated UI API client.
6. Refactor Console menu management to use the generated Console APIs.
7. Remove frontend hierarchy patch utilities that become unused by the new flow.
8. Run focused backend, OpenAPI/client, frontend type/lint, and menu unit validations.

Rollback is code-only. The change does not alter stored schema or data shape beyond normal
`spec.parent` and `spec.priority` updates that the current Console already performs.

## Open Questions

- None.
