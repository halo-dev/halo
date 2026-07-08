## 1. Backend Permalink Matching

- [x] 1.1 Add request and response DTOs for `POST /attachments/-/match-permalinks`, using string URL inputs and ordered boolean results.
- [x] 1.2 Implement shared matching service logic that deduplicates inputs, rejects blank and non-HTTP(S) absolute URL values, builds exact and narrow canonical permalink candidates, and queries `Attachment.status.permalink`.
- [x] 1.3 Add UC attachment route for `POST /attachments/-/match-permalinks` guarded by the UC attachment upload/management permission path.
- [x] 1.4 Add Console attachment route for `POST /attachments/-/match-permalinks` guarded by the Console/system attachment upload/management permission path.
- [x] 1.5 Add backend tests for ordered results, no metadata leakage, relative URL matching, absolute URL matching, relative/absolute canonical variants, blank input rejection, unsupported absolute protocol rejection, and unauthorized access.

## 2. API Client

- [x] 2.1 Regenerate OpenAPI docs with `./gradlew generateOpenApiDocs`.
- [x] 2.2 Regenerate the UI API client with `pnpm -C ui api-client:gen`.
- [x] 2.3 Confirm the generated UC and Console attachment clients expose the match-permalinks operation under the expected storage attachment APIs.
- [x] 2.4 Run the package build needed for generated client consumers, such as `pnpm -C ui build:packages`.

## 3. Editor Matching and Transfer UX

- [x] 3.1 Add editor extension options or callbacks for permalink matching and URL transfer so Console and UC hosts use their own attachment API clients.
- [x] 3.2 Update Console post and single-page editor hosts to provide Console matching and URL-transfer behavior when `system:attachments:manage` is available.
- [x] 3.3 Update UC post editor host to provide UC matching and URL-transfer behavior when `uc:attachments:manage` is available.
- [x] 3.4 Replace synchronous external media detection with an async candidate matching flow that caches editor-session URL match results.
- [x] 3.5 Keep the paste-time `Dialog.info` transfer prompt, but show it only after permalink matching leaves unmatched external resources.
- [x] 3.6 Keep Dialog confirmation transfer scoped to the unmatched external resources from the pasted rich-text slice.
- [x] 3.7 Update per-resource transfer button visibility to hide for URLs matched by Attachment permalink and remain available for unmatched external media when upload permission exists.
- [x] 3.8 Add focused frontend tests for matched Attachment URLs, unmatched external URLs, users without upload permission, and Dialog gating behavior.

## 4. Validation

- [x] 4.1 Run focused backend tests for the new matching service and endpoints.
- [x] 4.2 Run focused frontend unit tests for editor external resource matching and Dialog gating behavior.
- [x] 4.3 Run `./gradlew spotlessCheck`.
- [x] 4.4 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 4.5 Run `openspec validate improve-editor-external-asset-transfer --strict`.
- [x] 4.6 Run `git diff --check`.
