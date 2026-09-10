## Why

Console users cannot correct an existing comment or reply without deleting and recreating it. Issue #5393 requests editing, including the administrator's own replies.

## What Changes

- Add a shared body-editing modal to comment and reply operation menus, gated by comment management permission.
- Populate the existing body, preserve rich-text markup, and refresh affected queries after saving.
- Reject empty or unsafe content on the server and prevent stale edits from overwriting newer changes.
- Preserve authorship, moderation, timestamps, and reply relationships. Do not add edit history, public self-service editing, or edit notifications.

## Capabilities

### New Capabilities

- `comment-editing`: Authorized Console users can safely edit the body of comments and replies.

### Modified Capabilities

None.

## Impact

- Console comment components, editor extension-point documentation, and maintained translations.
- Comment/reply services and the approved Console body-update endpoints, using existing comment management permissions.
- No new dependencies, database migrations, or manually edited generated files.
