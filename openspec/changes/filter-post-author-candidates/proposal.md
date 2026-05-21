## Why

Post settings currently use the generic user selector for changing a post owner, so ordinary registered users can appear as author candidates even when they cannot create or edit posts. On sites with many registered users, this makes administrator workflows noisy and can assign content ownership to users who do not have the minimum post authoring capability.

## What Changes

- Restrict post author selection to users whose effective roles include the post contributor role template.
- Treat `role-template-post-contributor` as the minimum authoring capability because it grants creating and updating posts/drafts, while `role-template-post-publisher` only grants publish/unpublish operations.
- Add backend support for filtering users by effective role membership, including dependency-expanded roles and super users, so custom roles work without frontend hard-coding.
- Update the post settings and batch settings owner selectors to use the filtered author-candidate query while preserving display of already-selected owners.

## Capabilities

### New Capabilities
- `post-author-selection`: Defines which users may be listed as post author candidates and how role-based eligibility is resolved.

### Modified Capabilities

None.

## Impact

- Console user listing API: add a non-breaking query parameter for effective role filtering.
- RBAC role resolution: reuse role dependency expansion to determine whether a directly assigned role effectively contains `role-template-post-contributor`.
- Console UI: keep the shared `userSelect` unchanged and use post-local FormKit `select` options sourced from the filtered author-candidate query.
- OpenAPI/UI API client: regenerate after the Console API parameter is added.
- Tests: add backend coverage for effective role filtering and frontend/composable coverage for selector request parameters where practical.
