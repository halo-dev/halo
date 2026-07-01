# menu-hierarchy Specification

## Purpose
Define the migrated menu hierarchy model where MenuItems declare their owning menu and parent item,
while preserving theme-facing menu output compatibility.

## Requirements
### Requirement: Menu items express ownership and parent relationships

The system SHALL represent menu structure through `MenuItem.spec.menuName` and `MenuItem.spec.parent`
after migration.

#### Scenario: Menu item belongs to a menu

- **WHEN** a MenuItem is part of a Menu after migration or new Console creation
- **THEN** the MenuItem SHALL set `spec.menuName` to the owning `Menu.metadata.name`

#### Scenario: Menu item is a root item

- **WHEN** a MenuItem is a top-level item in its owning menu
- **THEN** the MenuItem SHALL have `spec.parent` unset or null

#### Scenario: Menu item is a child item

- **WHEN** a MenuItem is nested under another MenuItem in the same menu
- **THEN** the MenuItem SHALL set `spec.parent` to the parent MenuItem `metadata.name`

#### Scenario: Legacy hierarchy fields remain available

- **WHEN** the API schema describes Menu and MenuItem specs
- **THEN** `Menu.spec.menuItems` and `MenuItem.spec.children` SHALL remain present and SHALL be marked
  deprecated

### Requirement: Legacy menu hierarchy data is migrated safely

The system SHALL migrate legacy menu hierarchy data from `Menu.spec.menuItems` and
`MenuItem.spec.children` to `MenuItem.spec.menuName` and `MenuItem.spec.parent` without rewriting
legacy fields.

#### Scenario: Existing legacy menu tree is migrated

- **WHEN** Halo starts with a Menu whose `spec.menuItems` references legacy root MenuItems
- **AND** those MenuItems reference descendants through `spec.children`
- **THEN** the migration SHALL recursively assign `spec.menuName` and `spec.parent` to the reachable
  MenuItems
- **AND** the migration SHALL leave `Menu.spec.menuItems` and `MenuItem.spec.children` unchanged

#### Scenario: Existing new fields take precedence

- **WHEN** a legacy Menu references a MenuItem that already has `spec.menuName` or `spec.parent`
- **THEN** the migration SHALL NOT overwrite the existing new field value

#### Scenario: Shared MenuItem is referenced by multiple menus

- **WHEN** the same legacy MenuItem is reachable from more than one Menu
- **THEN** the migration SHALL keep the original MenuItem for the deterministic first owning menu
- **AND** the migration SHALL create cloned MenuItems for additional owning menus

#### Scenario: MenuItem has multiple parents in one menu

- **WHEN** a legacy MenuItem is reachable through more than one parent path in the same Menu
- **THEN** the migration SHALL keep the original MenuItem on the deterministic first parent path
- **AND** the migration SHALL create cloned MenuItems for additional parent paths

#### Scenario: Conflict path is cloned

- **WHEN** the migration clones a MenuItem because of cross-menu or multi-parent conflict
- **THEN** the migration SHALL clone the descendants on that conflict path
- **AND** every cloned descendant SHALL use the corresponding cloned parent in `spec.parent`

#### Scenario: Missing legacy reference is encountered

- **WHEN** `Menu.spec.menuItems` or `MenuItem.spec.children` references a missing MenuItem
- **THEN** the migration SHALL skip that missing reference
- **AND** the migration SHALL continue migrating other reachable MenuItems

#### Scenario: Legacy cycle is encountered

- **WHEN** following `MenuItem.spec.children` would create a cycle
- **THEN** the migration SHALL skip the cyclic edge
- **AND** the migration SHALL continue migrating the acyclic reachable paths

#### Scenario: Migration is retried

- **WHEN** the migration runs more than once
- **THEN** the migration SHALL NOT duplicate already-created conflict clones for the same original item,
  menu, parent, and legacy path

#### Scenario: Migration labels are inconsistent

- **WHEN** a MenuItem has the migration label but does not have `spec.menuName`
- **THEN** the migration SHALL treat the MenuItem as incomplete and attempt to migrate it again

#### Scenario: MenuItem is not reachable from any legacy menu

- **WHEN** a MenuItem is not reachable from any Menu through legacy `spec.menuItems` roots
- **THEN** the migration SHALL leave that MenuItem unassigned unless it already has `spec.menuName`

#### Scenario: Default initial menu data is installed

- **WHEN** Halo installs the built-in initial Menu and MenuItems
- **THEN** the built-in MenuItems SHALL include `spec.menuName` for their owning Menu

### Requirement: Runtime menu queries use ownership and parent fields

The system SHALL build menu trees from `MenuItem.spec.menuName` and `MenuItem.spec.parent` without
falling back to legacy hierarchy fields.

#### Scenario: Theme queries menu by name

- **WHEN** a client queries a Menu by metadata name through the theme menu API
- **THEN** the system SHALL fetch that Menu
- **AND** the system SHALL list MenuItems whose `spec.menuName` equals the Menu name
- **AND** the system SHALL build the returned tree by grouping those MenuItems by `spec.parent`

#### Scenario: Theme queries primary menu

- **WHEN** a client queries the primary menu
- **THEN** the system SHALL resolve the primary Menu name
- **AND** the system SHALL build that Menu tree from `spec.menuName` and `spec.parent`

#### Scenario: Legacy fields disagree with new fields

- **WHEN** `Menu.spec.menuItems` or `MenuItem.spec.children` disagrees with `MenuItem.spec.menuName` or
  `MenuItem.spec.parent`
- **THEN** runtime menu queries SHALL use `spec.menuName` and `spec.parent`
- **AND** runtime menu queries SHALL NOT fallback to the legacy fields

#### Scenario: Parent reference is invalid

- **WHEN** a MenuItem has `spec.menuName` for a Menu
- **AND** its `spec.parent` is missing, points to itself, or points outside the same Menu item set
- **THEN** runtime menu queries SHALL render that MenuItem as a root item in its Menu

#### Scenario: Sibling items are rendered

- **WHEN** multiple MenuItems share the same `spec.menuName` and `spec.parent`
- **THEN** runtime menu queries SHALL order them by priority, creation timestamp, and metadata name

### Requirement: Theme menu output remains compatible

The system SHALL preserve the existing theme menu value object shape while sourcing hierarchy from the
new fields.

#### Scenario: Menu tree is returned to themes

- **WHEN** the theme menu API returns a Menu
- **THEN** the response SHALL include the tree under `MenuVo.menuItems`
- **AND** child MenuItems SHALL be nested under `MenuItemVo.children`

#### Scenario: Stored Menu spec is included

- **WHEN** the theme menu API returns `MenuVo.spec`
- **THEN** `MenuVo.spec.menuItems` SHALL remain the stored legacy Menu spec value
- **AND** it SHALL NOT be recomputed from the new MenuItem ownership fields

### Requirement: Console menu management writes the new hierarchy fields

The Console SHALL manage menu structure through `MenuItem.spec.menuName`, `MenuItem.spec.parent`, and
`MenuItem.spec.priority`.

#### Scenario: Console lists items for a selected menu

- **WHEN** an administrator selects a Menu in Console
- **THEN** Console SHALL load MenuItems by `spec.menuName` equal to the selected Menu name
- **AND** Console SHALL build the editable tree from `spec.parent`

#### Scenario: Console creates a root item

- **WHEN** an administrator creates a root MenuItem in a selected Menu
- **THEN** Console SHALL create the MenuItem with `spec.menuName` equal to the selected Menu name
- **AND** Console SHALL leave `spec.parent` unset or null
- **AND** Console SHALL NOT add the item name to `Menu.spec.menuItems`

#### Scenario: Console creates a child item

- **WHEN** an administrator creates a MenuItem under a parent item
- **THEN** Console SHALL create the MenuItem with the selected Menu name in `spec.menuName`
- **AND** Console SHALL set `spec.parent` to the selected parent MenuItem name
- **AND** Console SHALL NOT append the item name to the parent's `spec.children`

#### Scenario: Console chooses a parent

- **WHEN** Console shows parent choices for a MenuItem
- **THEN** Console SHALL show items from the same `spec.menuName`
- **AND** Console SHALL exclude the current MenuItem and its descendants

#### Scenario: Console saves drag-and-drop changes

- **WHEN** an administrator saves a drag-and-drop hierarchy change
- **THEN** Console SHALL patch affected MenuItems with updated `spec.parent` and `spec.priority`
- **AND** Console SHALL NOT patch `MenuItem.spec.children`

#### Scenario: Console deletes a menu

- **WHEN** an administrator deletes a Menu
- **THEN** Console SHALL delete MenuItems whose `spec.menuName` equals the deleted Menu name
- **AND** Console SHALL NOT use `Menu.spec.menuItems` as the deletion scope

#### Scenario: Console deletes a menu item

- **WHEN** an administrator deletes a MenuItem from a Menu tree
- **THEN** Console SHALL delete that MenuItem and descendants derived from `spec.parent`
- **AND** Console SHALL NOT patch `Menu.spec.menuItems`

#### Scenario: Console clones a menu

- **WHEN** an administrator clones a Menu
- **THEN** Console SHALL clone MenuItems whose `spec.menuName` equals the source Menu name
- **AND** cloned MenuItems SHALL set `spec.menuName` to the new Menu name
- **AND** cloned child MenuItems SHALL set `spec.parent` to the corresponding cloned parent name
- **AND** the cloned Menu SHALL NOT copy the source Menu's legacy `spec.menuItems`
