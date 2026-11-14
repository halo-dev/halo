## 1. Subject-Based Permalink Generation

- [x] 1.1 Add a concrete application-level permalink service using the existing subject display lookup and one fragment formatter; verify focused tests cover default relative links, configured absolute links, root comments, quoted replies targeting the new reply, encoded path/query/name values, fragment replacement, and existing external URL handling.
- [x] 1.2 Add optional `permalink` fields to `ListedComment`, `ListedReply`, `CommentVo`, and `ReplyVo`, retaining the field through `CommentWithReplyVo` conversion; verify serialization and read-service tests cover Console lists, public detail/lists, and embedded replies with equal URLs for the same target.
- [x] 1.3 Reuse resolved subject URLs when enriching one subject's comment/reply results; verify tests preserve results for missing subjects/providers/URLs, use a changed subject URL on the next read, do not fetch the subject once per reply, and leave stored extensions unchanged.

## 2. Public Reply Detail

- [x] 2.1 Add a parent-aware single-reply operation to `CommentPublicQueryService` and its implementation using existing visibility helpers; verify focused tests cover anonymous, owner, unrelated user, authorized viewer, hidden-thread owner, inaccessible parent, wrong parent, and deleted/missing targets.
- [x] 2.2 Add `GET comments/{name}/reply/{replyName}` to `CommentFinderEndpoint` with operation ID `GetCommentReply`; verify endpoint tests return the requested display record independently of page position, use 404 for unavailable targets, propagate service failures, and set the specified cache headers on 200 and 404 responses.
- [x] 2.3 Reuse sanitized reply display conversion on detached data and enrich the result with its permalink; verify email/private owner fields and IP data are filtered, public counters remain present, and fetched/stored extension data is not mutated.

## 3. Notification Links

- [x] 3.1 Add optional `commentUrl` and `replyUrl` reason attributes and populate them with the common formatter; verify publisher tests for posts, single pages, and quoted replies preserve the existing subject URL attributes, subscription identities, and emission timing.
- [x] 3.2 Add conditional precise-target links to the default HTML and plain-text notification templates while retaining subject-title links; verify rendering with and without the new attributes and confirm existing stored notification bodies are not rewritten.

## 4. Generated API and Console Consumers

- [x] 4.1 Run `./gradlew generateOpenApiDocs` and `pnpm -C ui api-client:gen` using the repository-declared package manager; verify generated schemas/client types expose optional permalink fields and `GetCommentReply`, with no hand-edited generated output.
- [x] 4.2 Update existing frontend links in the Console comment list, comment/reply detail dialogs, and comments dashboard widget to prefer their target's permalink; verify normal targets, source-URL fallback, absent URLs, and unchanged subject-management routes with focused UI checks, maintaining existing i18n keys or all affected locales.

## 5. Integration and Release Handoff

- [x] 5.1 Exercise the new endpoint through Halo's real HTTP and authorization path; record anonymous/authenticated results for public/private targets, inaccessible or deleted parents, wrong-parent replies, response sanitization, and cache headers without adding broader role permissions.
- [x] 5.2 Verify Console and newly rendered notification links against the public detail responses in a running Halo instance; record that the fragment names identify the intended comment/new reply, and distinguish Core verification from the later component's browser acceptance.
- [x] 5.3 Add public API documentation for the permalink field and location contract, and finalize the companion handoff in `design.md`; verify it covers missing subject URLs, subject matching, direct detail/history/unavailable behavior, first publishing Halo 2.27, then requiring Halo 2.27.0 in the adapted plugin/npm component, and independent frontends adapting themselves.
- [x] 5.4 Run the focused backend and frontend tests for the changed behavior, `./gradlew spotlessCheck`, and `pnpm -C ui typecheck && pnpm -C ui lint`; verify all required checks pass and record any separate live-integration evidence.
- [x] 5.5 Run `openspec validate comment-reply-permalinks --strict` and `git diff --check`; verify the final diff is scoped to the Core implementation, generated API artifacts, and its documentation, with companion implementation/release tracked outside this checkout.
