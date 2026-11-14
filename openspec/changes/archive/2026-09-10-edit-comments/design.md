## Context

Comment and Reply share raw and rendered body fields. Console currently creates replies through a replaceable editor, but its contract has no initial value. Existing core PATCH routes update extensions without the comment creation service's HTML validation.

## Goals / Non-Goals

Provide body-only editing with preserved resource identity and optimistic concurrency. Keep creation and moderation behavior unchanged. Do not introduce a history model or change generic extension persistence.

## Decisions

- Use one editing modal for comments and replies. Capture the body and version when opening; list refreshes must not replace an in-progress draft. Successful saves invalidate comments and the relevant reply list. Failed saves retain the draft.
- Extend the editor contract with optional `initialContent` and an explicit provider `supportsEditing` flag. Existing providers still handle creation. Editing falls back to the default textarea when a provider cannot initialize content; retain HTML source verbatim and explain source editing to the user. This is needed for already-shipped plugin providers, not speculative compatibility.
- The default editor initializes once. Providers must preserve markup and emit body changes; raw and rendered values that differ must not be overwritten merely by opening and closing the modal.
- Add dedicated Console body-update endpoints accepting raw, content, and resource version. Reuse the HTML whitelist, reject semantically empty bodies and deleted resources, and change only body fields. Do not retry stale writes automatically.
- Reuse the existing comment management role; any content subresource routes must be included in its rules. Viewing comments alone does not authorize editing.

## Risks / Trade-offs

- Older rich-text providers cannot preload content → preserve markup in a source textarea; upgraded providers opt in to rich-text editing.
- Resource versions also change on controller updates → reject stale submissions and retain the draft rather than silently overwrite newer data.
- Existing raw/rendered representations may differ → retain both unless the user changes the body; unchanged submissions are disabled.

## Migration Plan

No persisted schema change or migration. Generate OpenAPI and API client outputs with the repository tools after any approved endpoint additions; never hand-edit them. Reverting the feature leaves edited bodies as ordinary comment content.
