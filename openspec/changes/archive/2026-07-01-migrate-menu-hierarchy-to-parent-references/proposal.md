## Why

Halo currently stores menu ownership and hierarchy through `Menu.spec.menuItems` and
`MenuItem.spec.children`. That model makes ownership ambiguous, requires reverse lookups to find a
parent, and allows legacy cross-menu or multi-parent references that are difficult to reason about.

## What Changes

- Add `MenuItem.spec.menuName` as the owning `Menu.metadata.name`.
- Add `MenuItem.spec.parent` as the parent `MenuItem.metadata.name`; root menu items use `null` or an
  absent value.
- Mark `Menu.spec.menuItems` and `MenuItem.spec.children` as deprecated compatibility fields.
- Add a startup migration that reads the legacy fields, recursively migrates existing menu trees to
  `menuName` and `parent`, and preserves legacy field values as historical data.
- Handle legacy shared items and multi-parent references by cloning deterministic conflict paths
  instead of allowing cross-menu or multi-parent ownership in the new model.
- Update theme menu queries to build trees only from `menuName` and `parent`, while preserving the
  existing `MenuVo.menuItems` and `MenuItemVo.children` response shape for themes.
- Update Console menu management to create, edit, drag, clone, and delete menu items through
  `menuName` and `parent`.
- Regenerate OpenAPI docs and the generated UI API client.
- Do not remove the deprecated legacy fields in this change.

## Capabilities

### New Capabilities

- `menu-hierarchy`: Defines menu item ownership, parent references, legacy data migration, theme menu
  output compatibility, and Console menu management behavior.

### Modified Capabilities

- None.

## Impact

- API model: adds optional `MenuItem.spec.menuName` and `MenuItem.spec.parent`; deprecates
  `Menu.spec.menuItems` and `MenuItem.spec.children` without removing them.
- Backend: adds indexes for the new fields, a core extension data migration, and updates
  `MenuFinderImpl` to query by menu ownership.
- Theme API: keeps public menu tree output compatible through `MenuVo.menuItems` and nested
  `MenuItemVo.children`; `MenuVo.spec.menuItems` remains the stored legacy value.
- Console UI: updates menu list, item creation/editing, drag-and-drop, deletion, cloning, parent
  selection, tests, and i18n-safe UI behavior to use the new fields.
- OpenAPI/UI client: requires regenerating OpenAPI docs and `ui/packages/api-client`.
- Security: no permission or authorization model changes.
- Dependencies/database: no new dependencies and no SQL database migration; migration operates on
  core extension data.
