## Purpose

Protect locally developed themes from accidental deletion in Console theme management while still allowing an explicit
forced uninstall when the user confirms the risk.

## Requirements

### Requirement: Console theme deletion is guarded for possible development themes

The system SHALL provide a Console theme deletion API that refuses to delete a Theme when the corresponding theme
directory appears to contain local development files, unless the request explicitly forces deletion.

#### Scenario: Delete request is rejected for a possible development theme

- **WHEN** a Console client requests to delete a Theme without `force=true`
- **AND** the theme directory contains at least one development indicator such as `.git`, `package.json`,
  `pnpm-lock.yaml`, `yarn.lock`, `package-lock.json`, or `node_modules`
- **THEN** the system MUST NOT delete the Theme resource
- **AND** the system MUST respond with HTTP `409 Conflict`

#### Scenario: Delete request proceeds for a possible development theme when forced

- **WHEN** a Console client requests to delete a Theme with `force=true`
- **AND** the theme directory contains at least one development indicator
- **THEN** the system SHALL delete the Theme resource
- **AND** existing Theme reconciler cleanup SHALL remain responsible for deleting associated files and resources

#### Scenario: Delete request proceeds for an ordinary theme

- **WHEN** a Console client requests to delete a Theme without `force=true`
- **AND** the theme directory does not contain development indicators
- **THEN** the system SHALL delete the Theme resource
- **AND** existing Theme reconciler cleanup SHALL remain responsible for deleting associated files and resources

### Requirement: Console handles guarded deletion with a forced confirmation flow

The Console SHALL call the guarded Console theme deletion API for theme uninstall operations and SHALL ask for explicit
confirmation before retrying a guarded deletion with force.

#### Scenario: Console retries with force after guarded deletion is rejected

- **WHEN** a user confirms a theme uninstall in the Console
- **AND** the guarded delete API responds with HTTP `409 Conflict`
- **THEN** the Console SHALL show a second warning that the theme may be under local development
- **AND** the warning SHALL explain that forced uninstall will delete the theme directory and may lose local changes
- **AND** if the user confirms the second warning, the Console SHALL retry the delete request with `force=true`

#### Scenario: Console does not delete configuration when guarded deletion is cancelled

- **WHEN** a user starts "uninstall and delete config" for a possible development theme
- **AND** the guarded delete API responds with HTTP `409 Conflict`
- **AND** the user cancels the forced deletion confirmation
- **THEN** the Console MUST NOT delete the Theme resource
- **AND** the Console MUST NOT delete the theme Setting or ConfigMap

#### Scenario: Console deletes configuration after successful forced deletion

- **WHEN** a user starts "uninstall and delete config" for a possible development theme
- **AND** the user confirms forced deletion
- **AND** the forced delete request succeeds
- **THEN** the Console SHALL delete the corresponding theme Setting and ConfigMap when they exist
