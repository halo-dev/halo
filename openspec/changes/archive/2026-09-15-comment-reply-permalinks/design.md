## Context

See `proposal.md` for motivation and scope. Relevant existing seams are:

| Area | Current implementation |
| --- | --- |
| Subject URL | `api/.../content/comment/CommentSubject.getSubjectDisplay()` returns an optional `SubjectDisplay`; Post and SinglePage implementations return their stored `status.permalink`; notification publishers use `ExternalLinkProcessor` when preparing externally usable links. |
| Console data | `CommentServiceImpl.toListedComment()` and `ReplyServiceImpl.toListedReply()` assemble `ListedComment` and `ListedReply`. |
| Public data | `CommentPublicQueryServiceImpl` assembles `CommentVo` and `ReplyVo`; `CommentWithReplyVo` inherits comment display fields. |
| Public detail | `CommentFinderEndpoint` exposes `comments/{name}` and paginated `comments/{name}/reply`, but no individual reply read. |
| Notifications | `CommentNotificationReasonPublisher` already resolves subject URLs; notification templates currently link to the subject. Rendered notification bodies are stored. |
| Console subject integration | `use-subject-ref.ts` resolves source titles, management routes, and frontend URLs, including a separate plugin UI extension point. |
| Component companion | The inspected `plugin-comment-widget` branch already consumes the proposed hash format and has a plugin-owned reply-detail endpoint. Its current minimum Halo version is 2.26.0. |

The backend subject extension point defaults to an empty display result for compatibility. A plugin can therefore have a working Console source link without providing a backend subject URL. The existing public query service also applies identity-dependent visibility: authenticated owners and authorized comment viewers can see content that anonymous visitors cannot.

## Goals / Non-Goals

**Goals:**

- Centralize link generation while retaining the current subject extension contract.
- Make existing display reads sufficient to obtain a permalink, with no persisted derived URL and no extra request per copy action.
- Provide an exact reply query using the same access decisions as public discussion reads.
- Make the Core contract ready for a separately released Halo 2.27-compatible component.

**Non-Goals:**

- A redirect registry or a guarantee that old links survive subject URL changes.
- New moderation rules, approval-time notifications, or administrator-only notification destinations.
- Independent frontend URL overrides, hash-router integration, or multiple location protocols.
- Rewriting historical notification bodies, adding Console copy-link UI, or redesigning comment management.
- Editing or publishing the companion component from this repository's change.

## Decisions

### 1. Use one dedicated fragment protocol

The wire format is defined in `specs/comment-permalinks/spec.md`. It retains the component branch's existing `halo-comment` and optional `reply` parameters. A reply always carries its root comment name; `quoteReply` does not change the target link.

Use URI-aware composition and fragment parameter encoding. Preserve the provider's path and query, including already encoded values, and replace any old fragment with the dedicated comment location. Post and SinglePage subject displays preserve `status.permalink` as-is: relative by default, absolute when the existing `halo.use-absolute-permalink` setting produces absolute subject links. Merely configuring the site external URL must not turn display permalinks into absolute URLs. Missing or unusable URLs yield no permalink. Do not derive the URL from a Console request path, a browser's current URL, or a client-supplied subject override.

**Alternatives:** Page numbers would depend on ordering; a new server page or redirect route adds routing and lifecycle work without being needed for the agreed stability guarantee. A second query-string protocol or preserved hash-router route is outside the integration scope.

### 2. Resolve dynamically and enrich display DTOs

Add one concrete application-level permalink service in `run.halo.app.content.comment`. It reuses `CommentSubject.supports(ref)` and `getSubjectDisplay(name)` for subject resolution, plus one shared formatter for comment/reply fragments. A separate public interface or a new plugin extension point is unnecessary.

The service can format a URL already resolved by a caller, so existing notification subject lookups do not need to be repeated. For replies, obtain the subject through `reply.spec.commentName` and the root comment. When enriching a reply page or a public comment page for one subject, reuse that subject URL across the items instead of fetching it separately for every reply. Keep this reuse scoped to the request, with no persistent URL cache.

Add optional top-level `permalink` fields to:

- `ListedComment` and `ListedReply` for Console;
- `CommentVo` and `ReplyVo` for public REST and theme display reads;
- `CommentWithReplyVo` through its existing inheritance/conversion.

Enrichment must not drop an otherwise valid result when a provider, subject, or URL is absent. Preserve the existing data and leave the field absent/null. Avoid broad error suppression that would turn unrelated storage or service failures into successful responses. Keep URL resolution reactive in request handling and reuse already resolved URLs in the existing notification event path.

**Alternatives:** Storing `status.permalink` would require refreshing all associated comments and replies whenever a subject URL changes. Separate permalink endpoints would add network calls to consumers that already possess display records. Neither is needed for this change.

### 3. Add the reply detail route next to the reply list route

Add:

```text
GET /apis/api.halo.run/v1alpha1/comments/{name}/reply/{replyName}
operationId: GetCommentReply
response: ReplyVo
```

The singular `reply` segment follows Core's existing list/create subresource. Add the corresponding operation to `CommentPublicQueryService`; keep the root-comment detail and existing list routes intact.

Validate the root against the same visibility selection as `getByName`, then select the named reply using the existing parent-aware reply visibility selection and an exact-name constraint. The parent check is essential: fetching a root extension or checking only the reply is insufficient. Reuse Core's query helpers rather than copying the plugin endpoint's permission predicates into another implementation. Preserve hidden-thread-owner access only where the existing reply-list rules grant it.

Return 404 for a missing, deleted, inaccessible, or incorrectly parented target. Propagate genuine service failures as errors. Return the normal sanitized public reply shape, and enrich it with the optional permalink. Apply sanitization to a detached display copy so the fetched extension is not modified; do not return the raw extension or introduce a second public reply DTO.

Set `Cache-Control: no-store, private` and `Vary: Cookie, Authorization` on both 200 and 404 responses. The existing public API read permissions cover this route; validate the route through the normal request/authorization path without broadening role rules.

**Alternatives:** Scanning reply pages has unbounded work and changing positions. Keeping the implementation only in the widget plugin prevents other consumers from using Core's public contract and duplicates visibility policy.

### 4. Update consumers without changing subject identity

**Console:** Use `ListedComment.permalink` in existing frontend actions in `CommentListItem`, `CommentDetailModal`, and the dashboard comments item. Use `ListedReply.permalink` in `ReplyDetailModal`. Fall back to `subjectRefResult.externalUrl` when the precise link is missing. Keep subject titles, Console management routes, and the plugin `comment:subject-ref:create` contract unchanged. Match existing component and i18n conventions if an action label needs clarification.

**Notifications:** Add optional `commentUrl` to new-comment reason data and `replyUrl` to new-reply reason data, with matching reason-type attributes. Generate them with the same formatter used for display responses and the already resolved subject URL. At the notification boundary, resolve the subject URL with the existing `ExternalLinkProcessor` before formatting target links so emails remain usable outside the site. This may make the notification URL absolute while the API field remains relative; both identify the same path, query, and fragment. The reply action targets the newly created reply, including when it quotes another reply.

Keep `postUrl`, `pageUrl`, and `commentSubjectUrl` unchanged and continue to use them for subject-title links. Add a conditional "view comment" or "view reply" action to the default HTML body and a target URL to its plain-text counterpart. When no permalink is available, omit only that new action. Keep subscription subject identity and emission timing unchanged; custom templates using the old attributes continue to work. No migration of stored HTML or old emails is needed.

**Alternatives:** Replacing the meaning of existing subject URL attributes would silently affect custom templates and plugins. Duplicating fragment concatenation in every UI and template would defeat the shared generation contract.

### 5. Hand off a small component adaptation after Halo 2.27

The public integration contract is documented in `docs/developer-guide/comment-permalinks.md`, including the display field, Core routes, consumer navigation behavior, and release requirements below.

The companion change will consume Core's `permalink`, switch reply detail reads to the new Core route, and remove its redundant plugin reply-detail implementation. It will raise the plugin minimum Halo version to `>=2.27.0` and document the same backend requirement for standalone npm users; no runtime fallback between old and new reply endpoints is planned.

The existing component detail flow remains the basis of adaptation: parse the fragment, fetch the root, compare subject group/kind/name, then fetch the requested reply. The component owns scrolling, target highlighting, and focus inside its Shadow DOM. It must continue to handle hash/history changes, stale requests, return-to-list navigation, and unavailable targets. It must not scan earlier list pages or override server visibility decisions.

The normal sharing action uses the Core permalink. If an older subject provider supplies no permalink, it must not invent a canonical URL from the API base or current browser page; the component can omit the copy action while retaining the timestamp. Console retains the source-link fallback described above. Independent frontend mapping remains the integrator's responsibility.

These are companion handoff requirements, not additional implementation tasks in the Halo checkout. Core's location documentation must make them explicit so another component can implement the same contract.

## Risks / Trade-offs

- **Older subject providers return no URL** → Keep permalink optional, preserve existing display data and source links, and document that providers must supply a usable subject display URL for precise links.
- **Subject URLs change** → Newly generated links use the current URL; existing subject redirects remain responsible for older URLs.
- **A subject uses its fragment as a router** → The comment contract owns the fragment. Integrators must adapt; this release does not introduce another protocol.
- **Readers see different moderation states** → Validate both discussion levels and prevent caching across identities; an unavailable page is an expected outcome for a link the visitor cannot currently view.
- **Halo 2.27 precedes the adapted widget** → Existing widgets still open the subject page even if they do not consume the fragment. Release notes identify that exact positioning requires a compatible component.
- **Repeated subject resolution adds reads** → Reuse a resolved URL for a response's shared subject and in notification construction; avoid persistent invalidation machinery.
- **Generated schema/client drift** → Regenerate OpenAPI and the UI client after the DTO and endpoint changes, then validate their consumers.

## Migration Plan

1. Implement and validate the Core service, display fields, public reply endpoint, Console links, and notification templates in this change. Update the public location/endpoint documentation and the companion release handoff.
2. Publish the Core capability with Halo 2.27. Existing extensions, source links, raw resources, and previously rendered notifications remain usable; there is no data migration.
3. In the component repository, adapt against released Halo 2.27, raise the minimum requirement to 2.27.0, remove the plugin-owned reply detail path, and verify direct links from Console, notifications, and copied component links before publishing the component.
4. If the component needs rollback, restore its previous release. A Core rollback below 2.27 requires rolling back the adapted component as well. No persisted comment/reply data needs reversal; issued fragment links still identify their subject page even when an older component cannot show detail mode.
