## ADDED Requirements

### Requirement: Page layout contract resolution

Halo SHALL resolve the `layout` template reference from plugin-owned frontend templates as a theme/core layout contract
instead of as a plugin-local relative template.

#### Scenario: Active theme provides a compatible layout contract

- **WHEN** a plugin-owned frontend template renders `layout :: html(head = ..., content = ...)`
- **AND** the active theme provides a compatible `templates/layout.html`
- **THEN** Halo SHALL render the plugin page through the active theme layout

#### Scenario: Active theme does not provide the layout contract

- **WHEN** a plugin-owned frontend template renders `layout :: html(head = ..., content = ...)`
- **AND** the active theme does not provide `templates/layout.html`
- **THEN** Halo SHALL render the plugin page through the system fallback layout

#### Scenario: Plugin provides a local layout template

- **WHEN** a plugin-owned frontend template renders `layout :: html(head = ..., content = ...)`
- **AND** the plugin also contains a local `templates/layout.html`
- **THEN** Halo SHALL NOT use the plugin-local template to satisfy the theme layout contract

### Requirement: Layout fragment contract

Halo SHALL define the v1 layout contract as a `templates/layout.html` template containing an `html` fragment that accepts
`head` and `content` fragment parameters.

#### Scenario: Layout renders plugin fragments

- **WHEN** a contract-aware plugin page passes `head` and `content` fragments to `layout :: html(...)`
- **THEN** the selected layout SHALL insert the `head` fragment inside the document `<head>`
- **AND** the selected layout SHALL insert the `content` fragment inside the document `<body>`

#### Scenario: System fallback layout renders a minimal page

- **WHEN** Halo renders a contract-aware plugin page through the system fallback layout
- **THEN** the rendered page SHALL include a valid HTML document structure with `<html>`, `<head>`, and `<body>`
- **AND** the rendered page SHALL include the plugin `head` and `content` fragments
- **AND** the rendered page SHALL include Halo footer injection through `<halo:footer />`

#### Scenario: Head fragment is not provided

- **WHEN** Halo renders a plugin page through the system fallback layout without a `head` fragment
- **THEN** the system fallback layout SHALL still render the page without failing

### Requirement: Theme layout compatibility status

Halo SHALL detect whether installed themes provide a compatible page layout contract and expose the result in
Theme status.

#### Scenario: Theme provides a compatible layout

- **WHEN** the Theme reconciler reconciles an installed Theme
- **AND** the theme contains a `templates/layout.html` that declares the v1 `html(head, content)` fragment signature
- **THEN** Theme status SHALL report the page layout state as supported

#### Scenario: Theme does not provide layout

- **WHEN** the Theme reconciler reconciles an installed Theme
- **AND** the theme does not contain `templates/layout.html`
- **THEN** Theme status SHALL report the page layout state as missing
- **AND** Theme status phase SHALL NOT become failed because of the missing layout contract

#### Scenario: Theme layout exists but fails validation

- **WHEN** the Theme reconciler reconciles an installed Theme
- **AND** the theme contains `templates/layout.html`
- **AND** the template does not declare the v1 `html(head, content)` fragment signature
- **THEN** Theme status SHALL report the page layout state as invalid
- **AND** Theme status SHALL include a short diagnostic reason
- **AND** Theme status phase SHALL NOT become failed because of the invalid layout contract

#### Scenario: Theme is upgraded or reloaded

- **WHEN** an installed Theme is upgraded or reloaded
- **THEN** Halo SHALL re-evaluate the theme's page layout compatibility

### Requirement: Console reports layout compatibility

The Console SHALL show the page layout compatibility state for installed themes.

#### Scenario: Theme supports layout contract

- **WHEN** the Console displays a Theme whose status reports layout support
- **THEN** the Console SHALL indicate that page layout integration is supported

#### Scenario: Theme is missing layout contract

- **WHEN** the Console displays a Theme whose status reports the layout contract as missing
- **THEN** the Console SHALL warn that pages using the layout contract will use Halo's fallback layout and may not
  visually match the theme

#### Scenario: Theme has invalid layout contract

- **WHEN** the Console displays a Theme whose status reports the layout contract as invalid
- **THEN** the Console SHALL warn that the theme layout contract failed validation
- **AND** the Console SHALL show the diagnostic reason when available

### Requirement: Backward compatibility

Halo SHALL keep existing themes and pages working unless they opt into the new layout contract.

#### Scenario: Existing plugin page does not reference contract layout

- **WHEN** a plugin-owned frontend template does not render `layout :: html(head = ..., content = ...)`
- **THEN** Halo SHALL preserve the existing plugin template resolution behavior

#### Scenario: Existing theme lacks compatible layout

- **WHEN** an installed Theme lacks a compatible page layout contract
- **THEN** Halo SHALL still allow the Theme to be installed, upgraded, activated, and rendered
