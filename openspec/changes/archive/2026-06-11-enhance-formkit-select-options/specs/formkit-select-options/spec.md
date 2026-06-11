## ADDED Requirements

### Requirement: Select option metadata contract

Halo's custom FormKit `select` input SHALL accept option objects containing required `label` and `value` fields plus optional `icon`, `description`, and `attrs` fields.

#### Scenario: Static option includes metadata

- **WHEN** a custom FormKit `select` receives a static option with `label`, `value`, `icon`, and `description`
- **THEN** Halo SHALL keep the metadata available for dropdown rendering and selection callbacks

#### Scenario: Static option omits metadata

- **WHEN** a custom FormKit `select` receives a static option with only `label` and `value`
- **THEN** Halo SHALL render and select the option as it did before this change

#### Scenario: Disabled option metadata remains supported

- **WHEN** a custom FormKit `select` receives an option with `attrs.disabled`
- **THEN** Halo SHALL continue treating the option as disabled regardless of whether `icon` or `description` is present

### Requirement: Dropdown option metadata rendering

Halo SHALL render optional select option metadata in the dropdown without changing the closed selected-state display.

#### Scenario: Option has icon and description

- **WHEN** a dropdown option has both `icon` and `description`
- **THEN** Halo SHALL render the icon as an image source next to the option text
- **THEN** Halo SHALL render the description as secondary text below the label

#### Scenario: Option has description only

- **WHEN** a dropdown option has `description` but no `icon`
- **THEN** Halo SHALL render the label and secondary description text without reserving visible icon content

#### Scenario: Option has icon only

- **WHEN** a dropdown option has `icon` but no `description`
- **THEN** Halo SHALL render the icon image and the label without adding an empty secondary text row

#### Scenario: Icon image fails to load

- **WHEN** a dropdown option icon image fails to load
- **THEN** Halo SHALL keep the option selectable
- **THEN** Halo SHALL avoid showing a broken-image artifact as primary option content

#### Scenario: Selected state remains compact

- **WHEN** an option with `icon` or `description` is selected
- **THEN** Halo SHALL keep the closed single-select display and multiple-select chips label-only

### Requirement: Select value behavior

Halo SHALL preserve existing custom `select` form value behavior while exposing full selected option objects to the change callback.

#### Scenario: Single select stores value only

- **WHEN** a user selects an option with `label`, `value`, `icon`, and `description` in single-select mode
- **THEN** Halo SHALL store the option `value` as the FormKit node value

#### Scenario: Multiple select stores values only

- **WHEN** a user selects multiple options with metadata in multiple-select mode
- **THEN** Halo SHALL store an array of selected option `value` strings as the FormKit node value

#### Scenario: Change callback receives metadata

- **WHEN** a selected option includes `icon` or `description`
- **THEN** Halo SHALL pass selected option objects including that metadata to the select change callback

### Requirement: Action request option metadata mapping

Halo SHALL support optional metadata field mappings for custom `select` options loaded through `action + requestOption`.

#### Scenario: Action request maps metadata fields

- **WHEN** a custom FormKit `select` uses `action + requestOption` with `iconField` and `descriptionField`
- **THEN** Halo SHALL map those response fields into the option `icon` and `description` fields

#### Scenario: Action request omits metadata fields

- **WHEN** a custom FormKit `select` uses `action + requestOption` without `iconField` or `descriptionField`
- **THEN** Halo SHALL continue parsing options with `labelField` and `valueField` only

#### Scenario: Custom parser returns metadata

- **WHEN** `requestOption.parseData` returns options containing `icon` or `description`
- **THEN** Halo SHALL preserve those fields in the parsed options

### Requirement: Remote option metadata preservation

Halo SHALL preserve custom `select` option metadata returned by remote option providers.

#### Scenario: Remote search returns metadata

- **WHEN** `remoteOption.search` returns options containing `icon` or `description`
- **THEN** Halo SHALL keep those fields available for dropdown rendering and selection callbacks

#### Scenario: Remote value lookup returns metadata

- **WHEN** `remoteOption.findOptionsByValues` returns options containing `icon` or `description`
- **THEN** Halo SHALL keep those fields available after mapping unresolved selected values

#### Scenario: Action value lookup maps metadata

- **WHEN** an `action + requestOption` select looks up selected values by `fieldSelector`
- **THEN** Halo SHALL apply the configured `iconField` and `descriptionField` mappings to the lookup response

### Requirement: Local select option search

Halo SHALL include option descriptions in local custom `select` search matching.

#### Scenario: Local search matches label

- **WHEN** a user searches a local custom `select` with a keyword contained in an option label
- **THEN** Halo SHALL include that option in the filtered dropdown results

#### Scenario: Local search matches description

- **WHEN** a user searches a local custom `select` with a keyword contained in an option description
- **THEN** Halo SHALL include that option in the filtered dropdown results

#### Scenario: Local search does not match icon source

- **WHEN** a user searches a local custom `select` with a keyword contained only in an option icon source
- **THEN** Halo SHALL NOT include that option solely because of the icon source

#### Scenario: Remote search remains provider-driven

- **WHEN** a user searches a remote custom `select`
- **THEN** Halo SHALL continue passing the keyword to the remote provider without applying local description filtering to the returned remote results

### Requirement: Select option documentation

Halo SHALL document the custom `select` option metadata contract for Vue SFC and FormKit Schema usage.

#### Scenario: Documentation shows static metadata

- **WHEN** a developer reads the custom FormKit input documentation
- **THEN** Halo SHALL show how to provide `icon` and `description` in static `select` options

#### Scenario: Documentation shows action metadata mapping

- **WHEN** a developer reads the custom FormKit input documentation
- **THEN** Halo SHALL show how to configure `iconField` and `descriptionField` for `action + requestOption` selects
