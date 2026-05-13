## ADDED Requirements

### Requirement: Auto-create Terms of Service page during system setup
The system SHALL automatically create a SinglePage named "Terms of Service" during initialization.

#### Scenario: Terms of Service page exists after setup
- **WHEN** the administrator completes system setup
- **THEN** a SinglePage with metadata name `user-agreement` and slug `user-agreement` exists in the system
- **AND** the page state is published (publish: true)
- **AND** the page visibility is PUBLIC
- **AND** the page has comments disabled (allowComment: false)
- **AND** the page contains placeholder content prompting the administrator to replace it with actual content

### Requirement: Auto-create Privacy Policy page during system setup
The system SHALL automatically create a SinglePage named "Privacy Policy" during initialization.

#### Scenario: Privacy Policy page exists after setup
- **WHEN** the administrator completes system setup
- **THEN** a SinglePage with metadata name `privacy-policy` and slug `privacy-policy` exists in the system
- **AND** the page state is published (publish: true)
- **AND** the page visibility is PUBLIC
- **AND** the page has comments disabled (allowComment: false)
- **AND** the page contains placeholder content prompting the administrator to replace it with actual content

### Requirement: Auto-configure registration agreement requirement during system setup
The system SHALL automatically configure the Terms of Service and Privacy Policy pages as required agreement pages for registration.

#### Scenario: Registration page displays agreement links after setup
- **WHEN** the administrator completes system setup
- **THEN** `SystemSetting.User.requiredAgreementPages` contains `["user-agreement", "privacy-policy"]`
- **AND** users visiting the registration page can see the agreement links
- **AND** users must check the agreement checkbox to complete registration

### Requirement: Default agreement pages can be modified or deleted by administrator
The two default agreement pages created by the system SHALL have the same permissions as manually created SinglePages, and the administrator can freely edit, unpublish, or delete them.

#### Scenario: Administrator deletes default agreement page
- **WHEN** the administrator deletes the default Terms of Service page in the admin console
- **THEN** the page is deleted without affecting other system functions
- **AND** if `requiredAgreementPages` still references the page, that agreement is skipped during registration (relying on existing `fetchAgreementPages()` error-ignoring behavior)
