## ADDED Requirements

### Requirement: Console menu item tree APIs provide canonical hierarchy

The system SHALL provide Console APIs for reading and updating the editable MenuItem hierarchy of a
selected Menu.

#### Scenario: Console reads a canonical menu item tree

- **WHEN** an administrator requests the Console MenuItem tree for a Menu by `menuName`
- **THEN** the system SHALL list MenuItems whose `spec.menuName` equals the requested Menu name
- **AND** the system SHALL return those MenuItems as a tree of nodes containing `menuItem` and `children`
- **AND** the returned `children` values SHALL be view data and SHALL NOT write to `MenuItem.spec.children`

#### Scenario: Console tree handles invalid parent references

- **WHEN** a MenuItem in the selected Menu has a missing parent, self parent, parent outside the selected Menu,
  or cyclic parent chain
- **THEN** the system SHALL render the affected MenuItem as a root item in the Console tree
- **AND** the system SHALL continue returning other valid descendants where their parent chain remains valid

#### Scenario: Console tree is ordered canonically

- **WHEN** multiple MenuItems share the same selected Menu and parent
- **THEN** the system SHALL order them by priority, creation timestamp, and metadata name

#### Scenario: Console moves a menu item by relative position

- **WHEN** an administrator requests a position update for a MenuItem with `menuName`, `parentName`, and
  `beforeName`
- **THEN** the system SHALL validate the move within the requested Menu
- **AND** the system SHALL update `spec.parent` and `spec.priority` for affected MenuItems
- **AND** the system SHALL return the selected Menu's canonical Console MenuItem tree

#### Scenario: Position update appends to a sibling list

- **WHEN** an administrator requests a position update with `beforeName` unset or null
- **THEN** the system SHALL place the moved MenuItem at the end of the target sibling list

#### Scenario: Position update moves an item to root

- **WHEN** an administrator requests a position update with `parentName` unset or null
- **THEN** the system SHALL remove `spec.parent` from the moved MenuItem
- **AND** the moved MenuItem SHALL become a root item in the selected Menu

#### Scenario: Position update keeps ownership fixed

- **WHEN** an administrator requests a position update for a MenuItem
- **THEN** the system SHALL require the moved MenuItem's `spec.menuName` to equal the requested `menuName`
- **AND** the system SHALL NOT change the moved MenuItem's `spec.menuName`

#### Scenario: Position update rejects invalid relatives

- **WHEN** `parentName` or `beforeName` references a MenuItem that does not exist in the requested Menu
- **THEN** the system SHALL reject the position update

#### Scenario: Position update rejects inconsistent sibling target

- **WHEN** `beforeName` does not identify a sibling in the target parent after the move is applied
- **THEN** the system SHALL reject the position update

#### Scenario: Position update rejects cycles

- **WHEN** a position update would move a MenuItem under itself or one of its descendants
- **THEN** the system SHALL reject the position update

#### Scenario: Position update recalculates sibling priorities

- **WHEN** a position update succeeds
- **THEN** the system SHALL assign continuous integer priorities starting at zero to the target sibling list
- **AND** if the parent changed, the system SHALL assign continuous integer priorities starting at zero to the
  original sibling list
- **AND** the system SHALL persist only MenuItems whose `spec.parent` or `spec.priority` changed

### Requirement: Console menu deletion cascades owned menu items

The system SHALL provide a Console API for deleting a Menu and the MenuItems owned by that Menu.

#### Scenario: Console deletes a menu with owned menu items

- **WHEN** an administrator deletes a Menu through the Console deletion API
- **THEN** the system SHALL delete MenuItems whose `spec.menuName` equals the deleted Menu name
- **AND** the system SHALL NOT use `Menu.spec.menuItems` as the deletion scope
- **AND** the system SHALL delete the Menu only after owned MenuItem deletions complete

#### Scenario: Console menu deletion fails before deleting the menu

- **WHEN** deleting one of the owned MenuItems fails
- **THEN** the system SHALL fail the Console Menu deletion request
- **AND** the system SHALL NOT delete the Menu in that request

## MODIFIED Requirements

### Requirement: Console menu management writes the new hierarchy fields

The Console SHALL manage menu structure through backend Console hierarchy APIs backed by
`MenuItem.spec.menuName`, `MenuItem.spec.parent`, and `MenuItem.spec.priority`.

#### Scenario: Console lists items for a selected menu

- **WHEN** an administrator selects a Menu in Console
- **THEN** Console SHALL load the selected Menu's editable MenuItem tree from the Console tree API
- **AND** Console SHALL NOT build the editable tree from a flat MenuItem list

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

- **WHEN** Console shows parent choices for creating a MenuItem
- **THEN** Console SHALL derive the choices from the selected Menu's canonical Console MenuItem tree
- **AND** Console SHALL show only MenuItems from the selected Menu

#### Scenario: Console saves drag-and-drop changes

- **WHEN** an administrator drops a MenuItem into a new position
- **THEN** Console SHALL send a single-item position update containing the selected Menu name, target parent, and
  target relative sibling
- **AND** Console SHALL NOT calculate `spec.priority` values for persistence
- **AND** Console SHALL NOT batch patch MenuItems with hierarchy JSON patches
- **AND** Console SHALL replace its local tree with the canonical tree returned by the backend

#### Scenario: Console handles drag-and-drop save failure

- **WHEN** a drag-and-drop position update fails
- **THEN** Console SHALL reload the selected Menu's canonical tree
- **AND** Console SHALL NOT keep an unconfirmed local drag state as persisted structure

#### Scenario: Console deletes a menu

- **WHEN** an administrator deletes a Menu
- **THEN** Console SHALL call the backend Console Menu deletion API
- **AND** Console SHALL NOT batch delete MenuItems in the frontend

#### Scenario: Console deletes the currently selected menu

- **WHEN** an administrator deletes the currently selected Menu
- **THEN** Console SHALL select another available Menu after the deletion succeeds
- **AND** Console SHALL skip Menus that are already being deleted
- **AND** if no other Menu is available, Console SHALL clear the current Menu selection and `menu` query parameter

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
