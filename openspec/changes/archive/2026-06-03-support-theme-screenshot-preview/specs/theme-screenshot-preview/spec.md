## ADDED Requirements

### Requirement: Installed theme status exposes screenshot preview

The system SHALL expose a preview screenshot URL in `Theme.status.screenshot` when an installed theme provides a
supported screenshot file in the theme root directory.

#### Scenario: Theme root contains a supported screenshot file

- **WHEN** the Theme reconciler reconciles an installed Theme named `earth`
- **AND** the directory for `earth` contains a readable root-level `screenshot.png`
- **THEN** the reconciled Theme status SHALL set `screenshot` to a public URL for that screenshot file

#### Scenario: Theme root contains multiple supported screenshot files

- **WHEN** the Theme reconciler reconciles an installed Theme
- **AND** the theme root contains more than one supported screenshot file
- **THEN** the reconciled Theme status SHALL choose the first readable file in the order `screenshot.png`,
  `screenshot.jpeg`, `screenshot.jpg`, `screenshot.webp`

#### Scenario: Theme root does not contain a supported screenshot file

- **WHEN** the Theme reconciler reconciles an installed Theme
- **AND** the theme root does not contain `screenshot.png`, `screenshot.jpeg`, `screenshot.jpg`, or `screenshot.webp`
- **THEN** the reconciled Theme status SHALL NOT expose a screenshot URL

### Requirement: Theme screenshot files are served as public static resources

The system SHALL serve root-level theme screenshot files through a public static route that only exposes supported
screenshot filenames.

#### Scenario: Client requests a detected screenshot

- **WHEN** a client sends a GET request to the URL exposed by `Theme.status.screenshot`
- **AND** the corresponding root-level screenshot file exists and is readable
- **THEN** the system SHALL respond with the screenshot file content

#### Scenario: Client requests an unsupported root-level file

- **WHEN** a client requests a root-level theme file that is not one of `screenshot.png`, `screenshot.jpeg`,
  `screenshot.jpg`, or `screenshot.webp`
- **THEN** the system SHALL NOT serve that file through the screenshot route

#### Scenario: Client requests a path traversal screenshot URL

- **WHEN** a client requests a screenshot URL that would resolve outside the configured theme root
- **THEN** the system SHALL reject the request instead of serving filesystem content outside the theme root

### Requirement: Console displays theme screenshot preview when available

The Console SHALL prefer `Theme.status.screenshot` over `Theme.spec.logo` for theme preview cover images.

#### Scenario: Installed theme has a screenshot status URL

- **WHEN** the Console renders an installed Theme with `status.screenshot` set
- **THEN** the theme list and theme preview selector SHALL display the screenshot URL as the preview image

#### Scenario: Theme has no screenshot status URL

- **WHEN** the Console renders a Theme without `status.screenshot`
- **THEN** the Console SHALL continue to use `spec.logo` as the preview image when a logo is present
