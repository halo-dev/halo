## Why

Menu authors currently have to create custom links for the archives, categories, and tags index pages. Saving the route path as a literal URL is fragile because changing the corresponding theme route rule leaves those menu items pointing at the old path.

## What Changes

- Add a first-class menu-item route binding for the archives, categories, and tags index pages.
- Resolve a bound menu item's rendered URL from the current theme route rules and reconcile it after those rules change.
- Let Console users create, convert, switch, and remove system-route bindings while keeping menu labels user-controlled.
- Keep existing custom links unchanged; conversion to a system-route binding is explicit.
- Bind the default archives menu item on new installations to the archives route.
- Extend the public `MenuItem` model and regenerate the OpenAPI documentation and UI API client with the official generators.

## Capabilities

### New Capabilities

- `menu-route-bindings`: Defines durable menu-item bindings to built-in theme routes, their reconciliation behavior, and Console authoring behavior.

### Modified Capabilities

None.

## Impact

- Public API model: `MenuItemSpec` gains an optional system-route reference.
- Backend: menu-item reconciliation reacts to theme route rule changes.
- Console: menu-item source selection, conversion, unlinking, read-only resolved-link display, and i18n messages change.
- Initialization: the default archives menu item uses the new binding on new installations.
- Generated artifacts: OpenAPI documentation and the UI API client change through their existing generators; generated files are not edited manually.
- Compatibility: existing menu items are not migrated automatically, and themes continue rendering `MenuItem.status.href`.
- Authorization and dependencies: no new permission, endpoint, database migration, or dependency is required.
