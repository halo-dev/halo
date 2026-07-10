## Why

The default editor currently treats any pasted media URL outside the current site origin as an external resource, so attachments served from user-configured object storage domains can be misclassified and trigger the transfer confirmation dialog. Halo needs to distinguish existing Attachment permalinks from true external resources before showing the existing paste-time transfer confirmation.

## What Changes

- Add an attachment permalink matching API that accepts URL-like strings and reports whether each input matches an existing `Attachment.status.permalink`.
- Validate that non-HTTP(S) absolute URL protocols such as `data:`, `blob:`, `file:`, and `ftp:` are rejected by the matching API instead of being treated as matchable resources.
- Use the permalink matching API in the default editor before surfacing transfer prompts for pasted image, audio, or video resources.
- Keep the existing paste-time transfer confirmation dialog, but show it only for unmatched external resources.
- Keep explicit per-resource transfer actions available for true external resources.

## Capabilities

### New Capabilities

- `editor-external-asset-transfer`: Default editor external media detection and attachment permalink matching before transfer prompts.

### Modified Capabilities

- None.

## Impact

- Backend application: new UC attachment endpoint, request/response DTOs, permalink matching service logic, validation, authorization, and tests.
- API/OpenAPI: regenerate OpenAPI docs and the generated UI API client after adding the endpoint.
- Frontend editor package: update external media detection, paste handling, and focused tests.
- Authorization: keep the matching capability aligned with attachment upload/transfer permission so users without attachment upload permission do not get transfer prompts or probing access.
- Compatibility: no database schema change, no new dependency, and no change to plugin/theme public APIs.
