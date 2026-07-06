## Why

Console menu management still rebuilds menu item trees, derives drag-and-drop hierarchy patches, and
batch-updates MenuItems in the frontend after the menu hierarchy source of truth moved to
`MenuItem.spec.menuName` and `MenuItem.spec.parent`. This leaves important hierarchy validation and
ordering behavior split across the UI instead of being owned by the backend workflow that persists it.

## What Changes

- Add a Console API to return the canonical tree of MenuItems for a selected Menu.
- Add a Console API to move one MenuItem within its existing Menu by describing the target parent and
  relative sibling position.
- Move drag-and-drop hierarchy validation, cycle checks, sibling priority recalculation, and changed-item
  persistence to the backend.
- Move Menu deletion cascade for owned MenuItems into the backend Console workflow.
- Update Console menu management to consume the backend tree response and stop building trees or
  creating batch JSON patches for drag-save. Menu deletion stops batching MenuItem deletes in the frontend.
- Keep menu item ownership fixed after creation; cross-menu moves are not supported.
- Keep existing Menu/MenuItem extension fields and theme-facing menu output behavior unchanged.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `menu-hierarchy`: Console menu management will load canonical menu item trees from backend Console
  APIs and save drag-and-drop moves through a backend position workflow instead of building trees and
  batch patching hierarchy fields in the frontend.

## Impact

- Backend: adds Console endpoints and supporting service logic for menu item tree retrieval,
  same-menu position updates, and Menu deletion cascading to owned MenuItems.
- OpenAPI/UI client: requires regenerating OpenAPI docs and the generated UI API client.
- Console UI: replaces flat MenuItem listing, frontend tree construction, priority reset, and batch patch
  save logic with canonical tree loading and single-item move calls. Menu deletion uses the backend
  Console deletion workflow instead of frontend batch MenuItem deletion.
- Security: adds Console API subresource permissions for menu item tree reads and position updates under
  the existing menu view/manage role templates.
- Tests: adds backend endpoint/service tests and updates focused frontend menu management unit tests.
- Dependencies/database: no new dependencies and no database migration.
