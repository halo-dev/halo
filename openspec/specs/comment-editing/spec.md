## Purpose

Allow authorized Console users to correct existing comment and reply bodies without losing their authorship, moderation state, or discussion relationships.

## Requirements

### Requirement: Authorized body editing

The system SHALL allow users with comment management permission to edit existing comments and replies. Saving SHALL modify only body fields and SHALL NOT create a new comment, change moderation state, or issue a new-comment notification.

#### Scenario: Edit a pending comment or a published reply
- **WHEN** a comment manager saves a corrected body
- **THEN** the same resource contains the new body and retains its owner, timestamps, approval flags, and relationships
- **AND** the affected Console content is refreshed

#### Scenario: Read-only user
- **WHEN** a user with only comment viewing permission accesses a comment or attempts to save an edit
- **THEN** the edit action is hidden and the server rejects the write

### Requirement: Preserve editor content

The editor SHALL initialize from the saved body and preserve its markup. Providers unable to initialize existing content SHALL NOT be used to edit existing comments. Opening, canceling, or refreshing a list SHALL NOT save or erase the draft.

#### Scenario: Existing rich-text provider without editing support
- **WHEN** an existing comment is opened with a provider that cannot initialize content
- **THEN** the default source input contains the existing body including HTML markup and explains source editing

#### Scenario: Draft survives query refresh
- **WHEN** the list refreshes while the user is editing
- **THEN** the draft and its original concurrency token remain unchanged

### Requirement: Validate and protect writes

The server SHALL reject empty or unsafe HTML bodies, missing concurrency tokens, stale writes, and edits to deleted resources. The UI SHALL prevent duplicate or empty submissions and retain unsaved content when a request fails.

#### Scenario: Invalid body
- **WHEN** a request contains whitespace-only, markup-only empty content, or unsafe HTML
- **THEN** the body is not persisted

#### Scenario: Concurrent edit
- **WHEN** the saved resource version differs from the edit's original version
- **THEN** the save fails without overwriting the newer resource and the user's draft remains available

#### Scenario: Save failure
- **WHEN** an edit request fails
- **THEN** the editor remains open with the draft and displays the failure
