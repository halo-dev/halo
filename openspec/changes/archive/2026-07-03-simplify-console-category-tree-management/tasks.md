## 1. Backend Console APIs

- [x] 1.1 Add Console DTOs for `CategoryTreeNode` and `CategoryPositionRequest`.
- [x] 1.2 Add a Console category service that lists all Categories, builds the canonical tree from `spec.parent`, handles invalid parent chains as roots, and orders siblings by priority, creation timestamp, and metadata name.
- [x] 1.3 Implement single Category position updates that validate parent/before references, reject self or descendant moves, recalculate affected sibling priorities, update only changed Categories, and return the canonical tree.
- [x] 1.4 Add Console endpoint routes for `GET categories/-/tree` and `PUT categories/{name}/position` with OpenAPI metadata.
- [x] 1.5 Add RBAC rules for `categories/tree` read access and `categories/position` update access in category role templates.
- [x] 1.6 Add backend tests for tree ordering, invalid parent handling, root moves, append moves, sibling insertion, cycle rejection, invalid relative rejection, priority recalculation, changed-only persistence, and endpoint request/response behavior.

## 2. API Client

- [x] 2.1 Regenerate OpenAPI docs with `./gradlew generateOpenApiDocs`.
- [x] 2.2 Regenerate the UI API client with `pnpm -C ui api-client:gen`.
- [x] 2.3 Expose the generated Console Category API under `consoleApiClient.content.category`.
- [x] 2.4 Run the package build needed for generated client consumers, such as `pnpm -C ui build:packages`.

## 3. Frontend Category State

- [x] 3.1 Update `usePostCategory()` to fetch the Console category tree and expose `categoriesTree`, flattened `categories`, loading state, and refetch.
- [x] 3.2 Replace frontend flat-list tree construction with backend `CategoryTreeNode` usage in category management, category select, category tags, search result paths, and category filter consumers.
- [x] 3.3 Add frontend helpers to flatten backend tree nodes, find category paths, and derive one position request from previous and current tree states.
- [x] 3.4 Remove or stop using hierarchy persistence helpers that build trees from flat Categories, reset priorities for persistence, flatten trees for batch saves, or create hierarchy JSON patches.

## 4. Frontend Drag-and-Drop Save

- [x] 4.1 Store the previous canonical tree before drag changes and derive `{ name, parentName, beforeName }` after drop.
- [x] 4.2 Call the Console position API for drag-and-drop saves and replace local tree state with the returned canonical tree.
- [x] 4.3 Refetch the canonical tree when position detection fails or the position update request fails.
- [x] 4.4 Keep existing Category create, update, delete, and UI permission behavior unchanged.
- [x] 4.5 Update focused frontend unit tests for flattening, path lookup, position request derivation, and no-op or ambiguous drag handling.

## 5. Validation

- [x] 5.1 Run focused backend tests for the new Console category service and endpoint.
- [x] 5.2 Run focused frontend unit tests for category utilities and affected category-select behavior.
- [x] 5.3 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 5.4 Run `./gradlew spotlessCheck`.
- [x] 5.5 Run `openspec validate simplify-console-category-tree-management --strict`.
- [x] 5.6 Run `git diff --check`.
