## Purpose

Surface whether an installed theme appears to be a local development workspace so Console and integration clients can
warn before destructive operations such as uninstall or upgrade.

## Requirements

### Requirement: Theme development workspace status is reconciled

The system SHALL expose whether an installed Theme appears to be a local development workspace in `status.inDevelopment`.

#### Scenario: Theme is marked as in development when local development indicators exist

- **WHEN** the Theme reconciler reconciles an installed Theme
- **AND** the corresponding theme directory contains at least one development indicator such as `.git`, `package.json`,
  `pnpm-lock.yaml`, `yarn.lock`, `package-lock.json`, or `node_modules`
- **THEN** the reconciled Theme status SHALL set `inDevelopment` to `true`

#### Scenario: Theme is marked as not in development when local development indicators do not exist

- **WHEN** the Theme reconciler reconciles an installed Theme
- **AND** the corresponding theme directory does not contain development indicators
- **THEN** the reconciled Theme status SHALL set `inDevelopment` to `false`

### Requirement: Console asks for a second confirmation before uninstalling a development workspace theme

The Console SHALL use `status.inDevelopment` to decide whether to show a second, stronger warning before uninstalling a
Theme.

#### Scenario: Console asks for development confirmation after ordinary uninstall confirmation

- **WHEN** a user starts a Theme uninstall in the Console
- **AND** the Theme status has `inDevelopment` set to `true`
- **AND** the user confirms the ordinary irreversible-operation warning
- **THEN** the Console SHALL show a second warning that the Theme may be under local development
- **AND** the second warning SHALL explain that uninstalling will delete the theme directory and may lose local changes
- **AND** the Console SHALL only call the existing Theme delete API after the user confirms the second warning

#### Scenario: Console keeps the ordinary uninstall warning for packaged themes

- **WHEN** a user starts a Theme uninstall in the Console
- **AND** the Theme status does not have `inDevelopment` set to `true`
- **THEN** the Console SHALL keep the ordinary irreversible-operation confirmation

#### Scenario: Console deletes configuration only after successful Theme deletion

- **WHEN** a user starts "uninstall and delete config" for any Theme
- **AND** the Theme delete request succeeds
- **THEN** the Console SHALL delete the corresponding theme Setting and ConfigMap when they exist
