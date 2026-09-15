## Why

Comment notifications and Console links currently open the subject page without identifying the comment or reply. Halo needs one subject-based permalink contract so every entry point can reach the same discussion target regardless of pagination or sorting, and comment components can display that target directly.

## What Changes

- Define `#halo-comment=<commentName>` and `#halo-comment=<commentName>&reply=<replyName>` as the shared comment location format.
- Generate permalinks in Core from the existing `CommentSubject` display URL and resource names. Return an optional `permalink` in Console and public comment/reply display responses without persisting it on extensions. Preserve relative subject permalinks by default and configured absolute subject permalinks; add an external origin only when preparing notification links.
- Add a public endpoint for one reply under a named comment, reusing Core visibility rules and sanitized display data.
- Use generated permalinks for existing Console frontend links and new notification actions. Preserve subject links and notification attributes with their existing meanings, including fallback when no permalink is available.
- Document the component consumption contract: load a target directly, verify its subject and parent relationship, and handle unavailable content without changing permissions or notification timing.
- Ship the Core capability in Halo 2.27 first. Adapt and release `plugin-comment-widget` separately afterward, raising its minimum Halo version to 2.27.0 and replacing its plugin-owned reply-detail query with the Core endpoint.

## Capabilities

### New Capabilities

- `comment-permalinks`: Subject-derived link generation, display response fields, Console and notification consumption, and the frontend location contract.
- `comment-reply-detail`: Public retrieval of one reply with parent validation, existing visibility rules, sanitized output, and identity-safe caching.

### Modified Capabilities

None. The existing comment-editing requirements are unchanged.

## Impact

- **Backend:** `application` comment services, public query service, display DTOs, `CommentFinderEndpoint`, and notification publishers/templates. Reuse the existing `api` module's `CommentSubject` extension point without requiring plugin implementations to add methods.
- **Frontend:** Existing comment/reply frontend links in Console lists, detail modals, and the comments dashboard widget. Keep subject management routes intact and follow existing i18n conventions for any changed labels.
- **API compatibility:** Additive optional response fields and one public read endpoint. Regenerate OpenAPI documents and the UI API client with the repository tools; do not hand-edit generated files. Older subject plugins may continue to return no display URL.
- **Authorization:** A permalink grants no access. Reply detail must verify both parent and reply visibility and must not expose private owner fields. Existing moderation, notification timing, and role configuration remain unchanged.
- **Storage and dependencies:** No database schema changes, data migration, or new dependencies. Existing rendered notifications and delivered emails are not rewritten.
- **Companion release:** Component implementation and release are outside this repository's implementation scope. Raising that component's minimum Halo version is a compatibility change for the companion release, including standalone npm consumers.
- **Limits:** Link stability covers comment pagination and sorting; subject URL changes rely on the subject's existing redirects. Independent frontend URL mapping and hash-router adaptation are out of scope and remain the integrator's responsibility.
