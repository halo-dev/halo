## ADDED Requirements

### Requirement: Admin can configure required agreement pages for signup

The system SHALL allow administrators to select one or more SinglePage extensions that users must agree to during registration.

#### Scenario: Admin opens user settings

- **WHEN** the administrator navigates to System Settings > User Settings
- **THEN** a multi-select field labeled "Required Agreement Pages for Signup" is visible when "Allow Registration" is enabled
- **AND** the field allows selecting published SinglePages

#### Scenario: Admin saves agreement page configuration

- **WHEN** the administrator selects one or more SinglePages and saves
- **THEN** the selected page names (metadata.name) are stored in system config under `user.requiredAgreementPages`

### Requirement: Signup page displays agreement checkbox when pages are configured

The system SHALL display a required checkbox on the signup page when `requiredAgreementPages` is non-empty.

#### Scenario: User visits signup page with agreement pages configured

- **WHEN** a user navigates to `/signup`
- **AND** `requiredAgreementPages` contains one or more valid pages
- **THEN** the signup form displays a checkbox with text referencing the selected pages
- **AND** each page name is rendered as a hyperlink to its permalink

#### Scenario: User visits signup page without agreement pages configured

- **WHEN** a user navigates to `/signup`
- **AND** `requiredAgreementPages` is empty or not set
- **THEN** no agreement checkbox is displayed
- **AND** registration proceeds normally

### Requirement: Backend validates agreement acceptance on signup

The system SHALL reject signup requests when agreement pages are configured but the user has not checked the agreement checkbox.

#### Scenario: User submits signup without agreeing

- **WHEN** a user submits the signup form
- **AND** `requiredAgreementPages` is non-empty
- **AND** `agreedToTerms` is not `true`
- **THEN** the signup is rejected
- **AND** the signup page is re-rendered with a field error on the agreement checkbox

#### Scenario: User submits signup with agreement

- **WHEN** a user submits the signup form
- **AND** `requiredAgreementPages` is non-empty
- **AND** `agreedToTerms` is `true`
- **THEN** the signup proceeds normally

### Requirement: Agreement pages are resolved at render time

The system SHALL resolve SinglePage permalinks at the time the signup page is rendered, not through GlobalInfo.

#### Scenario: Signup page renders with agreement links

- **WHEN** the signup page is rendered
- **THEN** the backend queries each configured SinglePage by metadata.name
- **AND** extracts `status.permalink` and `spec.title` for each
- **AND** passes the resolved list as a Thymeleaf model variable

