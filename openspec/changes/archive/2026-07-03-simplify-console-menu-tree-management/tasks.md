## 1. Backend Console API

- [x] 1.1 Add Console DTOs for `MenuItemTreeNode` and the MenuItem position request.
- [x] 1.2 Implement canonical same-menu MenuItem tree building for Console using `spec.menuName`, `spec.parent`, and backend ordering.
- [x] 1.3 Handle missing, self, cross-menu, and cyclic parent references as root items in the Console tree.
- [x] 1.4 Implement same-menu position validation for moved item, `parentName`, `beforeName`, sibling consistency, and descendant-cycle prevention.
- [x] 1.5 Implement position updates that recalculate continuous integer priorities for affected sibling lists and persist only changed MenuItems.
- [x] 1.6 Return the selected Menu's canonical tree after successful position updates.
- [x] 1.7 Add Console endpoint routes for `GET menuitems/-/tree` and `PUT menuitems/{name}/position`.

## 2. Backend Tests And Permissions

- [x] 2.1 Add service/unit tests for canonical tree building, invalid parent handling, cycle handling, and deterministic ordering.
- [x] 2.2 Add service/unit tests for root moves, child moves, append moves, before-sibling moves, unchanged moves, and changed-item persistence.
- [x] 2.3 Add service/unit tests rejecting missing relatives, cross-menu relatives, inconsistent `beforeName`, self moves, descendant moves, and mismatched `menuName`.
- [x] 2.4 Add endpoint tests for tree read and position update response shape.
- [x] 2.5 Add role-template rules for `menuitems/tree` read access and `menuitems/position` update access under existing menu view/manage roles.

## 3. OpenAPI And Generated Client

- [x] 3.1 Run `./gradlew generateOpenApiDocs`.
- [x] 3.2 Run `pnpm -C ui api-client:gen`.
- [x] 3.3 Confirm generated client changes are limited to the new Console menu item tree and position API surface.

## 4. Console Refactor

- [x] 4.1 Replace selected-menu MenuItem flat listing with the generated Console tree API.
- [x] 4.2 Update Menu item rendering to use `MenuItemTreeNode.menuItem` and view-only `children`.
- [x] 4.3 Replace drag-save flattening, priority reset, and batch JSON patch logic with a single position update call.
- [x] 4.4 Compute only the moved item name, target `parentName`, and target `beforeName` from the local draggable tree.
- [x] 4.5 Replace local tree state with the canonical tree returned by position updates and reload the canonical tree on failures.
- [x] 4.6 Update parent select options for MenuItem creation to flatten the canonical tree without adding edit-time parent changes.
- [x] 4.7 Remove or narrow frontend menu hierarchy utilities and tests that are no longer used by the new workflow.

## 5. Frontend Tests

- [x] 5.1 Add or update focused tests for deriving a position request from drag results.
- [x] 5.2 Add or update focused tests for parent option generation from `MenuItemTreeNode`.
- [x] 5.3 Add or update focused tests proving drag-save no longer computes priority patches or batch patches MenuItems.

## 6. Verification

- [x] 6.1 Run `./gradlew :application:test --tests "*Menu*"` or a narrower backend test set covering the new Console workflow.
- [x] 6.2 Run `./gradlew spotlessCheck`.
- [x] 6.3 Run `pnpm -C ui test:unit -- console-src/modules/interface/menus`.
- [x] 6.4 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 6.5 Run `openspec validate simplify-console-menu-tree-management --strict`.
- [x] 6.6 Run `git diff --check`.

## 7. Backend Console Menu Deletion

- [x] 7.1 Add a Console `DELETE menus/{name}` endpoint for Menu deletion.
- [x] 7.2 Implement backend deletion that lists MenuItems by `spec.menuName`, deletes those MenuItems, then deletes the Menu.
- [x] 7.3 Ensure a MenuItem deletion failure prevents the Menu deletion in that request.
- [x] 7.4 Add endpoint/service tests for successful cascade deletion and failure-before-menu-delete behavior.

## 8. Console Delete Refactor

- [x] 8.1 Regenerate OpenAPI docs and the UI API client for the new Console Menu deletion API.
- [x] 8.2 Add the generated Console Menu API to `consoleApiClient`.
- [x] 8.3 Replace frontend Menu deletion's generic Menu delete plus batch MenuItem deletes with the new Console deletion API.

## 9. Reverification

- [x] 9.1 Run `./gradlew :application:test --tests "*Menu*"`.
- [x] 9.2 Run `./gradlew generateOpenApiDocs`.
- [x] 9.3 Run `pnpm -C ui api-client:gen`.
- [x] 9.4 Run `pnpm -C ui build:packages`.
- [x] 9.5 Run `pnpm -C ui test:unit -- console-src/modules/interface/menus`.
- [x] 9.6 Run `./gradlew spotlessCheck`.
- [x] 9.7 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 9.8 Run `openspec validate simplify-console-menu-tree-management --strict`.
- [x] 9.9 Run `git diff --check`.

## 10. Selected Menu Fallback After Deletion

- [x] 10.1 Update the spec for selected Menu fallback after deleting the currently selected Menu.
- [x] 10.2 Select another available Menu after current Menu deletion succeeds and clear selection when none exists.
- [x] 10.3 Add focused tests for fallback Menu selection.
- [x] 10.4 Run focused frontend tests, type/lint, OpenSpec validation, and diff checks.
