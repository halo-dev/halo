## ADDED Requirements

### Requirement: Console edits menu item parents

The Console SHALL allow administrators to change an existing MenuItem's parent from the MenuItem edit modal while preserving backend-owned hierarchy validation and ordering.

#### Scenario: Edit modal shows parent choices

- **WHEN** an administrator edits an existing MenuItem in a selected Menu
- **THEN** Console SHALL show the parent MenuItem selector
- **AND** Console SHALL initialize the selector from the MenuItem's current `spec.parent`, or the root option when `spec.parent` is unset or null
- **AND** Console SHALL derive parent choices from the selected Menu's canonical Console MenuItem tree
- **AND** Console SHALL exclude the current MenuItem and all of its descendants from parent choices

#### Scenario: Edit modal moves item to selected parent

- **WHEN** an administrator saves an existing MenuItem after selecting a different parent
- **THEN** Console SHALL save the ordinary MenuItem fields through the MenuItem update API
- **AND** Console SHALL send a single MenuItem position update with the selected Menu name, the selected parent as `parentName`, and `beforeName` unset or null
- **AND** the moved MenuItem SHALL be appended to the target sibling list
- **AND** Console SHALL refresh or replace the local tree with the canonical tree returned by the backend

#### Scenario: Edit modal moves item to root

- **WHEN** an administrator saves an existing MenuItem after selecting the root option as its parent
- **THEN** Console SHALL send the MenuItem position update with `parentName` unset or null
- **AND** the moved MenuItem SHALL become a root item in the selected Menu

#### Scenario: Edit modal keeps unchanged parent

- **WHEN** an administrator saves an existing MenuItem without changing the selected parent
- **THEN** Console SHALL NOT send a MenuItem position update
- **AND** Console SHALL preserve the existing hierarchy position

#### Scenario: Parent move fails after ordinary fields save

- **WHEN** saving ordinary MenuItem fields succeeds but the parent position update fails
- **THEN** Console SHALL reload the selected Menu's canonical tree
- **AND** Console SHALL NOT keep an unconfirmed parent selection as persisted hierarchy
- **AND** Console SHALL NOT attempt to roll back the already-saved ordinary MenuItem fields
