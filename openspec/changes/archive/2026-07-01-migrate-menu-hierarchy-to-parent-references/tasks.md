## 1. API Model And Schema

- [x] 1.1 Add optional `menuName` and `parent` fields to `MenuItem.MenuItemSpec`.
- [x] 1.2 Mark `Menu.spec.menuItems` and `MenuItem.spec.children` as deprecated in schema/OpenAPI
  descriptions while keeping the fields available.
- [x] 1.3 Add indexes for `MenuItem.spec.menuName` and `MenuItem.spec.parent` in scheme
  registration.
- [x] 1.4 Update built-in initial menu data so default MenuItems include `spec.menuName`.
- [x] 1.5 Add or update API serialization/schema tests for the new fields and deprecated legacy fields.

## 2. Migration

- [x] 2.1 Add a core extension migration component that runs after core extensions are initialized.
- [x] 2.2 Implement recursive legacy menu tree discovery from `Menu.spec.menuItems` and
  `MenuItem.spec.children`.
- [x] 2.3 Fill missing `spec.menuName` and `spec.parent` without overwriting existing new field values.
- [x] 2.4 Add `halo.run/menu-item-hierarchy-migrated=true` to migrated or confirmed MenuItems.
- [x] 2.5 Implement deterministic cross-menu and multi-parent conflict resolution with subtree cloning.
- [x] 2.6 Add clone annotations for original item name, migration menu name, migration parent name, and
  JSON-array migration path.
- [x] 2.7 Make migration retry-safe by reusing existing annotated clones and handling duplicate clone
  candidates deterministically.
- [x] 2.8 Handle missing references, cycles, unreachable orphan items, and invalid legacy edges without
  blocking startup.
- [x] 2.9 Retry optimistic locking and persistence failures where practical and log a final migration
  summary.
- [x] 2.10 Add migration tests for normal trees, shared MenuItems, multi-parent items, subtree cloning,
  missing references, cycles, existing new fields, labels, clone idempotence, and unreachable orphan
  items.

## 3. Runtime Menu Queries

- [x] 3.1 Update `MenuFinderImpl.getByName` to fetch one Menu and list MenuItems by `spec.menuName`.
- [x] 3.2 Update primary menu lookup to build the selected menu from new fields.
- [x] 3.3 Build MenuItem trees by grouping same-menu items by nullable `spec.parent`.
- [x] 3.4 Treat missing, self, or cross-menu parent references as root items.
- [x] 3.5 Preserve `MenuVo.menuItems` and nested `MenuItemVo.children` output shape for themes.
- [x] 3.6 Keep `MenuVo.spec.menuItems` as the stored legacy value and do not recompute it.
- [x] 3.7 Add Finder and public endpoint tests proving new-field rendering, ordering, invalid-parent
  handling, and no fallback to legacy fields.

## 4. Console Management

- [x] 4.1 Update menu item loading to query by `spec.menuName` for the selected Menu.
- [x] 4.2 Update Console tree utilities and tests to build and flatten trees using `spec.parent`.
- [x] 4.3 Update menu item creation to set `spec.menuName`, nullable `spec.parent`, and sibling-based
  `spec.priority`.
- [x] 4.4 Update parent selection to load same-menu candidates and exclude the current item plus
  descendants.
- [x] 4.5 Update drag-and-drop save to patch `spec.parent`, `spec.priority`, and current `spec.menuName`
  without patching legacy `children`.
- [x] 4.6 Update menu item deletion to derive descendants from the new tree and avoid patching
  `Menu.spec.menuItems`.
- [x] 4.7 Update menu deletion to delete items by `spec.menuName`.
- [x] 4.8 Update menu cloning to clone items by `spec.menuName`, remap cloned parents, preserve priority,
  and avoid copying source `spec.menuItems` to the new Menu.
- [x] 4.9 Preserve i18n behavior and existing UI states while updating affected Console menu forms and
  lists.
- [x] 4.10 Add or update focused frontend unit tests for create/edit tree utilities, drag output, delete
  descendants, and clone mapping.

## 5. Generated API Client

- [x] 5.1 Run `./gradlew generateOpenApiDocs`.
- [x] 5.2 Run `pnpm -C ui api-client:gen`.
- [x] 5.3 Confirm generated API client changes are limited to expected model/schema updates.

## 6. Verification

- [x] 6.1 Run `./gradlew :api:test` or focused API model tests covering Menu and MenuItem.
- [x] 6.2 Run `./gradlew :application:test --tests "*Menu*"`.
- [x] 6.3 Run `./gradlew spotlessCheck`.
- [x] 6.4 Run `pnpm -C ui test:unit` with focused menu tests.
- [x] 6.5 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 6.6 Assess narrow e2e coverage for Console menu create, child create, drag reorder, item delete,
  menu clone, and theme menu endpoint output. Not run in this pass because focused backend and frontend
  tests cover the changed hierarchy logic and no local end-to-end setup was required.
- [x] 6.7 Run `openspec validate migrate-menu-hierarchy-to-parent-references --strict`.
- [x] 6.8 Run `git diff --check`.
