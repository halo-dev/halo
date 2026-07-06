## ADDED Requirements

### Requirement: Console category tree APIs provide canonical hierarchy

The system SHALL provide Console APIs for reading and updating the editable Category hierarchy.

#### Scenario: Console reads a canonical category tree

- **WHEN** an administrator requests the Console Category tree
- **THEN** the system SHALL list Categories and return them as a tree of nodes containing `category` and `children`
- **AND** the returned `children` values SHALL be view data and SHALL NOT write to `Category.spec.children`

#### Scenario: Console category tree handles invalid parent references

- **WHEN** a Category has a missing parent, self parent, or cyclic parent chain
- **THEN** the system SHALL render the affected Category as a root Category in the Console tree
- **AND** the system SHALL continue returning other valid descendants where their parent chain remains valid

#### Scenario: Console category tree is ordered canonically

- **WHEN** multiple Categories share the same parent
- **THEN** the system SHALL order them by priority, creation timestamp, and metadata name

#### Scenario: Console moves a category by relative position

- **WHEN** an administrator requests a position update for a Category with `parentName` and `beforeName`
- **THEN** the system SHALL validate the move
- **AND** the system SHALL update `spec.parent` and `spec.priority` for affected Categories
- **AND** the system SHALL return the canonical Console Category tree

#### Scenario: Category position update appends to a sibling list

- **WHEN** an administrator requests a position update with `beforeName` unset or null
- **THEN** the system SHALL place the moved Category at the end of the target sibling list

#### Scenario: Category position update moves category to root

- **WHEN** an administrator requests a position update with `parentName` unset or null
- **THEN** the system SHALL remove `spec.parent` from the moved Category
- **AND** the moved Category SHALL become a root Category

#### Scenario: Category position update rejects invalid relatives

- **WHEN** `parentName` or `beforeName` references a Category that does not exist
- **THEN** the system SHALL reject the position update

#### Scenario: Category position update rejects inconsistent sibling target

- **WHEN** `beforeName` does not identify a sibling in the target parent after the move is applied
- **THEN** the system SHALL reject the position update

#### Scenario: Category position update rejects cycles

- **WHEN** a position update would move a Category under itself or one of its descendants
- **THEN** the system SHALL reject the position update

#### Scenario: Category position update recalculates sibling priorities

- **WHEN** a position update succeeds
- **THEN** the system SHALL assign continuous integer priorities starting at zero to the target sibling list
- **AND** if the parent changed, the system SHALL assign continuous integer priorities starting at zero to the original sibling list
- **AND** the system SHALL persist only Categories whose `spec.parent` or `spec.priority` changed

## MODIFIED Requirements

### Requirement: Console category management writes parent references

The Console SHALL manage category hierarchy through backend Console hierarchy APIs backed by `Category.spec.parent` and `Category.spec.priority`.

#### Scenario: Console loads category tree

- **WHEN** Console loads Categories for category management or category selection
- **THEN** Console SHALL load the editable Category tree from the Console tree API
- **AND** Console SHALL NOT build the editable tree from a flat Category list

#### Scenario: Console creates a root category

- **WHEN** an administrator creates a root Category
- **THEN** Console SHALL create the Category without `spec.parent`
- **AND** Console SHALL NOT add the Category name to another Category's `spec.children`

#### Scenario: Console creates a child category

- **WHEN** an administrator creates a Category under a parent Category
- **THEN** Console SHALL create the Category with `spec.parent` equal to the selected parent Category name
- **AND** Console SHALL NOT append the child Category name to the parent's `spec.children`

#### Scenario: Console saves drag-and-drop hierarchy

- **WHEN** an administrator drops a Category into a new position
- **THEN** Console SHALL send a single Category position update containing target parent and target relative sibling
- **AND** Console SHALL NOT calculate `spec.priority` values for persistence
- **AND** Console SHALL NOT batch patch Categories with hierarchy JSON patches
- **AND** Console SHALL replace its local tree with the canonical tree returned by the backend

#### Scenario: Console handles drag-and-drop save failure

- **WHEN** a drag-and-drop position update fails
- **THEN** Console SHALL reload the canonical Category tree
- **AND** Console SHALL NOT keep an unconfirmed local drag state as persisted structure

#### Scenario: Console moves category to root

- **WHEN** an administrator moves a Category to the root level
- **THEN** Console SHALL send a position update with `parentName` unset or null
- **AND** Console SHALL NOT remove `/spec/parent` with a frontend JSON Patch

#### Scenario: Category select uses canonical tree

- **WHEN** the shared category select input renders category options, keyboard navigation, or search result paths
- **THEN** it SHALL use the tree returned by the Console tree API
- **AND** it MAY flatten the tree locally for search and selected-value resolution
