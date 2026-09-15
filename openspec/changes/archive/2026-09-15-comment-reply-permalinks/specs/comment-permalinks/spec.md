## Purpose

Provide one subject-based permalink contract for comments and replies so Console, notifications, and compatible frontend components can address an exact discussion target independently of pagination.

## ADDED Requirements

### Requirement: Subject-based location format

The system SHALL generate comment links from the subject's frontend URL with the fragment `halo-comment=<commentName>`. Reply links SHALL use `halo-comment=<commentName>&reply=<replyName>`, where `commentName` identifies the root comment and `replyName` identifies the target reply. Names SHALL be encoded as fragment parameter values. Generation SHALL preserve the subject URL's path and query, replace its existing fragment, and SHALL NOT include pagination or sorting information. Display permalinks SHALL preserve the subject URL's relative or absolute form without automatically adding the site origin; Post and SinglePage subjects SHALL use their stored `status.permalink`.

#### Scenario: Link to a root comment

- **WHEN** a comment named `comment-a` belongs to a subject whose frontend URL is `/archives/hello-halo`
- **THEN** its permalink is `/archives/hello-halo#halo-comment=comment-a`

#### Scenario: Relative subject URL with a configured site origin

- **WHEN** the site's external URL is configured but the subject's `status.permalink` is `/archives/hello-halo`
- **THEN** the display permalink remains `/archives/hello-halo#halo-comment=comment-a`
- **AND** it does not gain the site's scheme or domain

#### Scenario: Absolute permalink configuration

- **WHEN** the existing absolute-permalink configuration produces an absolute subject `status.permalink`
- **THEN** the display permalink retains that subject URL's origin, path, and query

#### Scenario: Link to a reply that quotes another reply

- **WHEN** `reply-b` belongs to `comment-a` and quotes `reply-c`
- **THEN** its permalink ends in `#halo-comment=comment-a&reply=reply-b`
- **AND** neither the quoted reply name nor a page number replaces the target identity

#### Scenario: Preserve URL components and encode names

- **WHEN** the subject URL contains an encoded path, a query, or an existing fragment
- **THEN** the path and query retain their meaning and the existing fragment is replaced by the comment location
- **AND** decoding each generated fragment parameter once yields its original resource name

#### Scenario: Pagination and sorting change

- **WHEN** new comments arrive or comment/reply ordering, pinning, or page size changes
- **THEN** the permalink for an existing target under the same subject URL remains unchanged

### Requirement: Optional current permalink in display responses

Public comment and reply display payloads and Console comment and reply list items SHALL expose an optional top-level `permalink` derived from the subject's current frontend URL. This SHALL include replies embedded in public comment lists and comments returned by public detail queries. The same target and resolved subject URL SHALL produce the same permalink across these responses. Generating the link SHALL NOT update the stored comment or reply.

#### Scenario: Obtain a link through existing reads

- **WHEN** an authorized caller reads a comment or reply through a supported display response and its subject URL is available
- **THEN** the response includes the target's permalink without requiring a separate link request
- **AND** raw extension resources do not gain a persisted permalink field

#### Scenario: Subject URL changes

- **WHEN** the subject's canonical frontend URL changes and the target is read again
- **THEN** the returned permalink uses the new subject URL
- **AND** redirecting previously issued links remains the responsibility of the subject's existing URL handling

#### Scenario: Subject URL cannot be resolved

- **WHEN** the subject is missing, its provider is unavailable, or the provider supplies no usable frontend URL
- **THEN** the existing comment or reply display data remains available to callers who can view it
- **AND** `permalink` is absent or null rather than a fabricated link to the site root or the Console page

### Requirement: Console frontend links identify the target

Existing frontend navigation actions in Console comment lists, comment/reply detail dialogs, and the comments dashboard widget SHALL prefer the target's permalink. Subject titles and Console subject-management routes SHALL retain their existing meanings. When no permalink is available, an existing subject frontend URL SHALL remain usable as a fallback.

#### Scenario: View a reply from its detail dialog

- **WHEN** a Console user opens the frontend link for a reply with a permalink
- **THEN** the destination identifies that reply and its root comment
- **AND** the subject management link still navigates to the subject's Console route

#### Scenario: Older subject provider only supplies a Console frontend URL

- **WHEN** a comment has no backend permalink but its Console subject integration supplies a frontend URL
- **THEN** the existing frontend action continues to open that subject URL
- **AND** when neither URL is available, no unusable frontend action is presented

### Requirement: New notifications link to the comment or reply

New-comment notifications SHALL make an optional `commentUrl` available to templates. New-reply notifications SHALL make an optional `replyUrl` available for the newly created reply. Default HTML and plain-text notifications SHALL provide a target link when available. Existing `postUrl`, `pageUrl`, and `commentSubjectUrl` attributes and notification subscription identities SHALL retain their meanings. Notification timing SHALL remain unchanged.

#### Scenario: Receive a reply notification

- **WHEN** a notification is generated for a new reply with a resolvable subject URL
- **THEN** its target action uses the new reply's permalink
- **AND** the subject title continues to link to the subject page rather than changing the meaning of the existing subject URL attribute

#### Scenario: Notification link is usable outside the site

- **WHEN** a subject provides a relative URL and a notification is prepared with a configured site origin
- **THEN** the notification publisher resolves it using the existing external-link handling
- **AND** the resulting absolute notification link identifies the same target as the relative display permalink

#### Scenario: No precise link is available

- **WHEN** a notification is generated without a permalink
- **THEN** its content still renders with any available existing subject link
- **AND** it does not render an empty precise-target action

#### Scenario: Notification arrives before approval

- **WHEN** a notification is generated for a comment or reply that has not yet passed moderation
- **THEN** this feature does not delay or suppress the notification
- **AND** possession of its link does not grant permission to view the target

#### Scenario: Notification predates this feature

- **WHEN** an existing stored notification is displayed after the upgrade
- **THEN** its previously rendered content is preserved
- **AND** delivered emails are not rewritten

### Requirement: Document direct-detail consumer behavior

The published location contract SHALL require compatible frontend consumers to read the fragment, load the target directly, and verify the root comment belongs to the mounted subject by group, kind, and name. Consumers SHALL display the root comment and, when requested, the target reply without searching through earlier list pages. The contract SHALL include navigation back to the list, browser history restoration, and an unavailable state that does not bypass visibility rules. Official component implementation is delivered separately after Core.

#### Scenario: Open a target beyond the first page

- **WHEN** a compatible consumer opens a permalink for a comment or reply outside the first list page
- **THEN** the documented behavior resolves it by name without scanning earlier pages
- **AND** the consumer can locate it within its own rendered comment content

#### Scenario: Subject mismatch or unavailable content

- **WHEN** the target belongs to a different mounted subject or cannot be viewed by the visitor
- **THEN** the consumer does not display that target
- **AND** it provides an unavailable state with a way back to the normal comment list

#### Scenario: Return to the list and restore history

- **WHEN** a visitor exits detail mode and then navigates back through browser history
- **THEN** the documented behavior restores the target represented by the URL
- **AND** unrelated page fragments do not activate comment detail mode

#### Scenario: Independent frontend integration

- **WHEN** an integrator uses a separate frontend URL mapping or hash router
- **THEN** the contract identifies that integration as the integrator's responsibility
- **AND** this feature does not add a separate frontend URL override or an alternative location protocol
