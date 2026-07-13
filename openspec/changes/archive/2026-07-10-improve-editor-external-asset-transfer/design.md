## Context

The default editor currently detects pasted image, audio, and video nodes synchronously. It treats relative URLs and current-origin URLs as local, and treats other `http(s)` URLs as external. That makes object-storage Attachment permalinks look external when the storage policy exposes files through a custom CDN or bucket domain.

There are two related editor paths today:

- Paste handling scans the pasted slice and opens a blocking confirmation dialog before transfer.
- Existing media node views show an explicit transfer action for resources considered external.

The backend already stores Attachment access URLs in `Attachment.status.permalink`, indexes that field, and uses exact permalink lookup in thumbnail resolution. This change should reuse that Attachment fact instead of introducing a storage-domain allowlist.

## Goals / Non-Goals

**Goals:**

- Add upload-authorized Attachment permalink matching APIs for URL-like strings.
- Treat a media URL as an existing Halo attachment when it matches `Attachment.status.permalink`.
- Accept both relative and absolute input strings in the matching API.
- Reject non-HTTP(S) absolute URL protocols such as `data:`, `blob:`, `file:`, and `ftp:`.
- Keep the existing paste-time transfer confirmation dialog, but suppress it for URLs matched as existing Attachments.
- Keep explicit transfer actions for true external media resources.
- Keep Console and UC editor behavior aligned with their respective upload permissions.

**Non-Goals:**

- Do not add storage-domain allowlist settings.
- Do not infer ownership from URL host names.
- Do not expose Attachment metadata from the matching API.
- Do not change Attachment storage policy models or database schemas.
- Do not change plugin/theme public APIs.
- Do not make real network reachability checks for external URLs.

## Decisions

1. Add `POST /attachments/-/match-permalinks` beside attachment upload endpoints.

   Provide the same operation shape in the UC attachment API and the Console attachment API, backed by shared service logic. The endpoint accepts `{ urls: string[] }` and returns `{ items: [{ url, matched }] }`, preserving input order. The operation belongs next to upload/transfer because callers use the result to decide whether an upload/transfer prompt is meaningful.

   Alternative considered: reuse attachment list/search with `fieldSelector=status.permalink=...`. That leaks an implementation query into editor code, does not batch cleanly, and does not express the permission boundary as clearly.

2. Guard matching with attachment upload/transfer permissions.

   UC matching uses the UC attachment management permission path. Console matching uses the Console/system attachment management path. Users without upload permission should not receive transfer prompts or use this probing endpoint. The response returns only booleans, not names, owners, groups, or policy data.

   Alternative considered: make the endpoint readable to anyone because permalinks are public. Keeping it with upload permission is more consistent with the transfer workflow and avoids offering upload-related UI to users who cannot act on it.

3. Match by Attachment permalink, with narrow normalization.

   The service first matches the original input string against `Attachment.status.permalink`. It also tries a small set of canonical equivalents:

   - For same-site absolute URLs, try the path plus query form.
   - For relative URLs, try resolving against the configured external URL when available.

   This keeps Attachment records as the source of truth while covering relative/absolute differences caused by permalink configuration. It does not match by host allowlist, filename, storage policy, or fuzzy path.

   Alternative considered: treat all configured object-storage domains as local. That would classify unrelated files on the same domain as Halo attachments.

4. Accept URL-like strings, but reject non-HTTP(S) absolute protocols.

   The request DTO should use strings, not `java.net.URL`, so relative URLs are accepted. Empty values and absolute URL strings whose protocol is not `http` or `https` are request errors. If any input is invalid, reject the whole request with `400` so frontend callers filter obvious local resources before calling the endpoint.

   Alternative considered: return per-item errors. That makes the endpoint behave like a generic URL classifier, which is broader than this matching operation.

5. Move editor detection to an async match step.

   Paste handling should first collect candidate media `src` values, filter values that are obviously local in the browser, call the matching API for the remaining candidates when the user has upload permission, and treat unmatched results as external transfer candidates. Matching results should be cached by URL during the editor session to avoid repeated calls for the same permalink.

   Alternative considered: keep the synchronous `isExternalAsset()` check and only add domain handling. That preserves the false-positive problem for object storage permalinks.

6. Preserve the paste-time transfer confirmation dialog.

   Pasting should keep the existing `Dialog.info` confirmation behavior for true external resources. The async matching step should run before showing the dialog so Attachment permalinks backed by third-party storage domains do not trigger the prompt.

   Alternative considered: replace the dialog with a non-blocking editor prompt. That broader UI change is deferred so this change can focus on permalink recognition.

7. Keep transfer execution permission-aware.

   The editor package should not hard-code only the UC upload client for every host. Console and UC hosts should provide matching and URL-transfer behavior that uses the same backend context as their normal upload flow.

   Alternative considered: keep the current direct UC transfer call inside the editor utility. That can misalign Console behavior with system attachment permissions.

## Risks / Trade-offs

- Matching many URLs could add extra backend queries -> deduplicate inputs, batch with an `in` query where possible, and keep a reasonable request limit.
- URL normalization could become too broad -> limit normalization to exact original, same-site absolute to relative, and relative to configured external URL.
- Permission behavior could diverge between Console and UC -> implement shared service logic and separate thin endpoints guarded by their existing upload permissions.
- A URL may be uploaded after a failed match -> cache only for the editor session and allow future paste actions to refresh stale results.

## Migration Plan

1. Add backend request/response DTOs, shared matching service logic, UC and Console routes, OpenAPI metadata, and authorization coverage.
2. Regenerate OpenAPI docs and the UI API client.
3. Add editor matching and transfer callbacks/options so Console and UC hosts call the correct API clients.
4. Update paste-time `Dialog.info` gating to use Attachment permalink matching.
5. Update focused backend/frontend tests.

Rollback is code-level only. No stored data migration is introduced.

## Open Questions

None.
