# editor-external-asset-transfer Specification

## Purpose
Define how Halo matches media URLs to existing Attachment permalinks so the editor prompts users to transfer only truly external resources.

## Requirements
### Requirement: Attachment permalink matching APIs

The system SHALL provide upload-authorized APIs that match input URL strings against existing Attachment permalinks.

#### Scenario: Match API preserves input order

- **WHEN** an upload-authorized caller submits multiple URL strings to `POST /attachments/-/match-permalinks`
- **THEN** the system SHALL return one result for each input string in the same order
- **AND** each result SHALL include the original `url` and a boolean `matched` value

#### Scenario: Match API returns only match state

- **WHEN** a URL matches an Attachment permalink
- **THEN** the system SHALL return `matched: true`
- **AND** the system SHALL NOT return Attachment metadata such as name, owner, group, policy, media type, or size

#### Scenario: Match API accepts relative and absolute inputs

- **WHEN** a caller submits relative URL strings and absolute URL strings
- **THEN** the system SHALL attempt to match each input against `Attachment.status.permalink`
- **AND** the system SHALL NOT require inputs to be valid `java.net.URL` absolute URLs

#### Scenario: Match API rejects unsupported absolute protocols

- **WHEN** any input URL uses a non-HTTP(S) absolute protocol such as `data:`, `blob:`, `file:`, `ftp:`, or `mailto:`
- **THEN** the system SHALL reject the request with a client error
- **AND** the system SHALL NOT return partial match results

#### Scenario: Match API rejects empty inputs

- **WHEN** any input URL is blank
- **THEN** the system SHALL reject the request with a client error
- **AND** the system SHALL NOT return partial match results

#### Scenario: Match API matches canonical permalink variants

- **WHEN** an input URL is a same-site absolute URL and an Attachment stores the corresponding relative path and query as `status.permalink`
- **THEN** the system SHALL return `matched: true`
- **AND** when an input URL is relative and an Attachment stores the corresponding external URL as `status.permalink`, the system SHALL return `matched: true`

### Requirement: Editor detects external media using Attachment permalink matching

The default editor SHALL use Attachment permalink matching before prompting users to transfer pasted media resources.

#### Scenario: Existing object storage Attachment does not prompt

- **WHEN** a user with attachment upload permission pastes an image, audio, or video URL served from a third-party storage domain
- **AND** the URL matches an existing `Attachment.status.permalink`
- **THEN** the editor SHALL treat the resource as an existing Halo attachment
- **AND** the editor SHALL NOT show an external resource transfer prompt for that URL

#### Scenario: True external media receives paste confirmation dialog

- **WHEN** a user with attachment upload permission pastes an image, audio, or video URL that does not match any Attachment permalink
- **THEN** the editor SHALL show the existing paste-time external resource transfer confirmation dialog
- **AND** the dialog SHALL transfer only the unmatched external media resources when confirmed

#### Scenario: User without upload permission receives no transfer prompt

- **WHEN** a user without attachment upload permission pastes an unmatched external media URL
- **THEN** the editor SHALL NOT call the permalink matching API
- **AND** the editor SHALL NOT show an upload or transfer prompt

#### Scenario: Explicit per-resource transfer only appears for unmatched external media

- **WHEN** an existing media node URL matches an Attachment permalink
- **THEN** the editor SHALL NOT show the per-resource transfer action for that node
- **AND** when the media node URL is unmatched and the user has upload permission, the editor SHALL allow explicit transfer for that node
