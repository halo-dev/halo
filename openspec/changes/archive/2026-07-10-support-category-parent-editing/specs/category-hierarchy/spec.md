## ADDED Requirements

### Requirement: Console edits category parents

The Console SHALL allow administrators to change a Category's parent from the category edit modal using backend Console hierarchy APIs.

#### Scenario: Category edit modal shows parent choices
- **WHEN** an administrator opens the edit modal for an existing Category
- **THEN** Console SHALL show a parent Category field
- **AND** the field SHALL include an option for no parent
- **AND** the field SHALL include existing Category candidates from the canonical Console Category tree
- **AND** the field SHALL NOT offer inline Category creation

#### Scenario: Category edit parent choices exclude invalid candidates
- **WHEN** Console builds parent choices for an existing Category
- **THEN** Console SHALL exclude the Category being edited
- **AND** Console SHALL exclude all descendants of the Category being edited

#### Scenario: Category edit changes parent
- **WHEN** an administrator saves an edited Category with a different parent Category selected
- **THEN** Console SHALL send a Category position update with `parentName` equal to the selected parent Category name
- **AND** Console SHALL send the position update with `beforeName` unset or null
- **AND** the moved Category SHALL be appended to the target parent's sibling list
- **AND** Console SHALL refresh from the canonical Category tree after the save

#### Scenario: Category edit moves category to root
- **WHEN** an administrator saves an edited Category with no parent selected
- **THEN** Console SHALL send a Category position update with `parentName` unset or null
- **AND** Console SHALL send the position update with `beforeName` unset or null
- **AND** the moved Category SHALL be appended to the root sibling list
- **AND** Console SHALL refresh from the canonical Category tree after the save

#### Scenario: Category edit keeps unchanged parent
- **WHEN** an administrator saves an edited Category without changing its parent
- **THEN** Console SHALL NOT send a Category position update
- **AND** Console SHALL preserve the Category's existing hierarchy position

#### Scenario: Category edit parent move fails
- **WHEN** the edited Category fields save but the Category position update fails
- **THEN** Console SHALL report the parent move failure
- **AND** Console SHALL refresh from the canonical Category tree
- **AND** Console SHALL NOT keep an unconfirmed local hierarchy state as persisted structure
