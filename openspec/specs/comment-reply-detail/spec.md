## Purpose

Allow public comment consumers to retrieve an exact reply under a known root comment without scanning reply pages, while preserving the discussion's access rules and private owner data.

## Requirements

### Requirement: Retrieve one reply under a comment

The system SHALL provide `GET /apis/api.halo.run/v1alpha1/comments/{name}/reply/{replyName}`. A successful response SHALL contain the requested reply in the existing public reply display shape, including an optional `permalink`. Retrieval SHALL be independent of reply pagination and ordering, and SHALL verify the reply belongs to the named root comment.

#### Scenario: Retrieve a reply on a later page

- **WHEN** a visitor requests a visible reply under its actual root comment
- **THEN** the endpoint returns that reply with status 200 regardless of its position in the reply list
- **AND** the visitor is not required to provide a page number

#### Scenario: Reply belongs to another comment

- **WHEN** the requested reply exists but belongs to a different root comment
- **THEN** the endpoint returns status 404 without disclosing the reply content

### Requirement: Enforce visibility of both discussion levels

The endpoint SHALL require both a visible, non-deleted root comment and a visible, non-deleted reply. It SHALL use the existing public comment access rules for the root and reply-list access rules for the reply, including authenticated owners, authorized comment viewers, and owners of hidden threads. Missing, deleted, or inaccessible targets SHALL return status 404. Providing names or a permalink SHALL NOT grant additional permissions.

#### Scenario: Anonymous reader

- **WHEN** an anonymous reader requests a reply
- **THEN** a successful response requires both the root comment and reply to be approved, non-hidden, and non-deleted

#### Scenario: Parent is not visible

- **WHEN** the reply would otherwise be visible but its root comment is hidden, unapproved, or deleted and inaccessible to the visitor
- **THEN** the endpoint returns status 404

#### Scenario: Existing privileged visibility

- **WHEN** a signed-in owner or authorized comment viewer requests a non-public reply under a root they can view
- **THEN** the result follows the existing reply-list permissions, including access for the owner of a hidden thread
- **AND** an unrelated user receives no additional access

#### Scenario: Target disappears

- **WHEN** either the named root comment or reply is missing or marked for deletion
- **THEN** the endpoint returns status 404

### Requirement: Return sanitized public display data

Successful responses SHALL follow the existing public reply display contract for owner information and counters. They SHALL NOT expose the raw owner's email address, private owner identifier, IP address, or private owner annotations. Computing display fields and the permalink SHALL NOT mutate the stored reply.

#### Scenario: Email-owned reply

- **WHEN** a visible reply authored by an email owner is requested
- **THEN** the response contains the permitted public display information
- **AND** it omits or clears private identity fields using the existing public reply conventions

#### Scenario: Read does not change the extension

- **WHEN** reply detail is retrieved
- **THEN** the stored reply's body, owner data, moderation flags, and metadata version remain unchanged

### Requirement: Isolate identity-dependent responses from caches

Successful and unavailable reply-detail responses SHALL use `Cache-Control: no-store, private` and `Vary: Cookie, Authorization` so a result evaluated for one visitor is not reused for another visitor.

#### Scenario: Authenticated and anonymous visitors

- **WHEN** an authenticated visitor receives a private reply and an anonymous visitor requests the same URL
- **THEN** the anonymous request is evaluated with its own permissions
- **AND** both the successful and unavailable responses include the specified cache headers
