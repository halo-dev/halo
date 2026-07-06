## Why

Console category management still builds editable category trees in the frontend and saves drag-and-drop changes by batch patching Categories. The menu hierarchy work moved this kind of canonical tree building, validation, and priority recalculation behind backend Console APIs; category management should follow the same boundary to reduce frontend hierarchy logic and avoid partial batch-save states.

## What Changes

- Add Console APIs for reading a canonical Category tree and moving one Category by relative position.
- Return Console category tree nodes as `{ category, children }`, keeping tree `children` as view data instead of part of the Category extension object.
- Move drag-and-drop hierarchy validation and sibling priority recalculation to the backend.
- Refactor Console category management and shared category selection to consume the backend tree and flatten it only for view/search needs.
- Remove frontend hierarchy save logic that resets priorities, flattens trees into Categories, and batch patches parent/priority JSON patches.
- Keep existing Category create, update, delete, and UI permission behavior unchanged.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `category-hierarchy`: Console category management changes from frontend-built trees and batch hierarchy patches to backend-provided canonical trees and single-item position updates.

## Impact

- Backend application: new Console category endpoint/service, request/response DTOs, OpenAPI annotations, RBAC resources, and focused tests.
- Frontend: generated API client, category management view, shared `usePostCategory()` composable, category select, category filter, and category tree utility tests.
- OpenAPI: regenerate Console API docs and generated TypeScript client after adding the Console category tree APIs.
- Authorization: add Console category tree and position resources to category role templates; do not change existing UI permission strings in this change.
