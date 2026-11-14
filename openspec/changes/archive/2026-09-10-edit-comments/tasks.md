## 1. Editor contract

- [x] 1.1 Add initial-content support and safe fallback for existing providers; verify source preservation, draft retention, creation behavior, and provider opt-in with component tests.

## 2. Server editing

- [x] 2.1 Add body-only comment and reply update services and Console endpoints with required raw/content/version; verify safe content, invalid input, deleted resources, state preservation, and stale writes with focused tests.
- [x] 2.2 Authorize content subresources through the comment management role; verify management allows updates and view-only rules deny them.
- [x] 2.3 Generate OpenAPI documentation and API client through repository tools; inspect generated changes and build the client.

## 3. Console integration

- [x] 3.1 Add a shared edit modal and permission-gated comment/reply menu actions with translations; verify initialization, save dispatch, duplicate/empty prevention, conflict retention, cancel behavior, and query refresh with component tests.

## 4. Validation

- [x] 4.1 Run focused backend/frontend tests, Spotless, typecheck, lint, and OpenSpec validation; inspect the final diff and report any runtime verification limits.

## Verification results

- Focused backend tests passed, including content-update validation, endpoint routing, role rules, creation regression, and notification publisher tests.
- All 15 comment editor/modal component tests passed; UI typecheck and lint passed.
- `./gradlew spotlessCheck` and strict OpenSpec validation passed. OpenAPI and API client were regenerated and the API client package built successfully.
- An isolated development instance verified real browser saves for a pending comment and an approved reply. Read-back confirmed preserved approval, authorship, creation time, and parent identity. Both endpoints rejected unsafe HTML with 400 and stale versions with 409 without changing the stored body.
- Desktop and 390px mobile modal layouts were visually checked. Test servers and the isolated browser were closed afterward.
- Authored files pass `git diff --check`. The generated TypeScript client retains generator-produced trailing whitespace; it was not hand-formatted. The Console OpenAPI generator also refreshed the existing Reply schema description.
- Plugin editing opt-in and fallback are covered by component tests; an upgraded external rich-text plugin was not installed for the browser run.
