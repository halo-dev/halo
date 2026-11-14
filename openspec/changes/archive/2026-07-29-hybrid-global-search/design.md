## Context

`GlobalSearchModal.vue` currently rebuilds a single Fuse index whenever the modal is mounted. Console routes are added synchronously, while users, plugins, posts, categories, tags, single pages, attachments, system settings, and theme settings are fetched asynchronously and added to the same non-reactive Fuse instance. Each collection request uses `paginate`, which follows `hasNext` until the entire collection has been loaded. The parent renders the modal with `v-if`, so reopening the modal repeats that work.

The existing Console APIs already support keyword pagination for posts, single pages, attachments, users, tags, and plugins. Category search is the only relevant collection without a Console keyword endpoint. This allows the high-cardinality sources to move to remote search without changing backend or OpenAPI contracts.

The design must preserve current keyboard navigation and resource routing, respect the repository's existing Vue Query and i18n conventions, and remain internal to the console rather than creating a plugin-facing extension point.

## Goals / Non-Goals

**Goals:**

- Stop loading complete high-cardinality collections whenever global search opens.
- Show local results immediately while remote providers search concurrently.
- Make query races, partial failures, result quotas, sorting, selection, and caching deterministic.
- Filter results according to the permission required by their destination route.
- Keep the modal focused on rendering and interaction by separating orchestration and source adapters.
- Add focused automated coverage for the behavior that is currently untested.

**Non-Goals:**

- Adding a backend global-search endpoint or server-side cross-resource relevance score.
- Changing existing backend, OpenAPI, generated-client, database, plugin, or theme contracts.
- Creating a public UI-plugin search-provider extension point.
- Adding recent-search history, suggestions for an empty keyword, or a manual refresh action.
- Actively invalidating global-search caches from plugin, category, setting, or theme mutation flows.
- Making categories remotely searchable in this change.

## Decisions

### 1. Use three internal source modes

The global-search feature will model sources as local, cached, or remote providers:

- Local: accessible console routes derived from the router.
- Cached: plugins, categories, system settings, and theme settings.
- Remote: posts, single pages, attachments, users, and tags.

Provider definitions remain private to the feature. Each provider declares a stable source ID, mode, permission predicate, and search or load function with a mapper into the shared result model. Remote providers additionally declare their result limit, which is used as the request page size and as their merge quota; local and cached sources instead share the pool-level limit of 8.

The shared result model includes at least a stable `source:name` ID, source ID, title, optional context, icon, group label, and route.

**Alternative considered:** only replace the three largest resource loops inline. This would reduce some traffic but leave the modal responsible for incompatible source lifecycles and reproduce the same orchestration for future sources.

**Alternative considered:** add a public provider registry. No external extension requirement exists, so a public contract would add compatibility and lifecycle obligations without solving the immediate problem.

### 2. Keep all source data reactive

Cached source arrays and provider results will be reactive values. Local Fuse results will be derived from the current keyword and current local/cached source data; asynchronous providers will replace reactive result arrays rather than mutating a non-reactive Fuse instance with `fuse.add`.

This ensures cached data that arrives after the user types invalidates the derived local search automatically.

### 3. Reuse existing Console APIs

Remote providers use the existing keyword-aware endpoints with page 1 and a provider limit of 4:

- `content.post.listPosts`
- `content.singlePage.listSinglePages`
- `storage.attachment.searchAttachments`
- `user.listUsers`
- `content.tag.listPostTags`

The adapters unwrap `ListedPost.post`, `ListedSinglePage.page`, and `ListedUser.user` before mapping results. Existing filters that prevent hidden system users from appearing must be preserved.

Plugins remain a cached source even though a keyword endpoint exists because their installed cardinality is operationally bounded. Categories remain a cached full-list exception because no keyword Console API exists. System and theme settings remain single-resource configuration loads.

**Alternative considered:** add an aggregated backend endpoint. It could reduce request count, but it would introduce a new contract, authorization aggregation, cross-resource ranking questions, and generated-client changes that are unnecessary for the initial correction.

### 4. Orchestrate searches with Vue Query

`use-global-search.ts` owns the normalized keyword, 300 ms debounce, provider query state, and merge result. It uses provider-specific Vue Query entries so failures and loading states remain isolated.

- Bounded cached sources use a 60-second stale time and stale-while-revalidate behavior.
- Remote query keys include the provider and normalized keyword.
- Remote results use a 30-second stale time and a 5-minute garbage-collection time.
- Remote query retries are disabled for the current keyword.
- Empty keywords disable remote queries.
- Query cancellation signals are passed to requests where supported; a keyword generation check also prevents late responses from becoming current results.

The change intentionally does not update unrelated feature mutations to invalidate these query keys. Bounded sources are eventually consistent within 60 seconds.

**Alternative considered:** a manual watcher with `Promise.allSettled`. It would require reimplementing caching, lifecycle cleanup, cancellation, and per-provider status already supplied by the project's query layer.

### 5. Merge results with explicit capacity

The merged result list has a hard limit of 20:

1. Search local and cached items with Fuse and keep at most 8.
2. Keep at most 4 ordered results from each successful remote provider.
3. Fill the remaining capacity using a fixed provider rotation: posts, single pages, attachments, users, then tags.

Posts, single pages, and attachments request deterministic newer-first ordering. Users and tags request deterministic display-name ordering. The feature does not attempt to compare Fuse scores with server results because the server endpoints return predicate matches rather than relevance scores.

The selected result is stored by stable result ID. A new normalized keyword selects the first result; provider updates for the same keyword preserve the selected item when it remains present.

### 6. Derive availability from destination permissions

Local route items are filtered using the permissions declared by the route, mirroring the router guard. Function-form permission predicates are evaluated with the current user permissions; predicates that return a promise cannot be verified synchronously, so their routes are excluded conservatively. Resource providers use the permission required by their destination route, not merely the permission required to list the resource.

In particular, post and single-page results navigate to editor routes and therefore require the corresponding manage permission. The existing combined settings and ConfigMap permission check remains intact.

Backend authorization and router guards remain authoritative. This additional filtering prevents knowingly presenting actions that lead directly to a forbidden page.

### 7. Represent asynchronous states explicitly

The composable exposes enough state for the modal to distinguish:

- empty keyword;
- local results with remote providers pending;
- completed search with no results;
- successful results with one or more failed providers.

A failed provider does not clear successful results. The UI shows one localized partial-failure message without exposing internal provider names. Changing the keyword or reopening the modal naturally creates another attempt.

The searching state covers any in-flight request of a permitted provider, including a background refresh of stale cached data. The completed-empty state additionally requires every permitted provider — bounded sources included — to have completed successfully.

### 8. Keep match context compact

The shared result model carries optional secondary context:

- posts and single pages show the slug, replacing it with a truncated excerpt when only the excerpt explains the match, using the effective `status.excerpt` that the backend keyword search matches;
- users show the username;
- tags show the slug;
- attachments show the media type.

The existing title, icon, group, keyboard navigation, destination routes, and same-route refresh behavior remain unchanged.

### 9. Split orchestration from presentation

The feature is divided into:

- `GlobalSearchModal.vue`: modal shell, keyboard interaction, scrolling, state presentation, and navigation;
- `GlobalSearchResultItem.vue`: one result row and optional match context;
- `use-global-search.ts`: keyword state, query lifecycle, merge, selection, and exposed actions;
- `global-search-providers.ts`: provider definitions, permission predicates, API calls, ordering, and model adapters.

This boundary keeps the modal a composition surface while avoiding a general framework outside the feature.

## Risks / Trade-offs

- [Category collections can also become large] → Keep category full-list loading as an explicit exception and cache it for 60 seconds; a category keyword endpoint can be proposed separately if real installations demonstrate the need.
- [Five parallel remote requests can add server load] → Debounce by 300 ms, request at most 4 items per provider, disable empty-keyword requests, cancel stale work, and cache recent keywords.
- [Existing endpoints do not expose relevance scores] → Use deterministic source-specific ordering and explicit quotas instead of presenting incomparable ordering as global relevance.
- [Cached plugin, category, or setting data can be stale for up to 60 seconds] → Use stale-while-revalidate and accept bounded eventual consistency rather than coupling unrelated mutation modules to global search.
- [Late asynchronous updates can move visible rows] → Use deterministic merging and retain selection by stable result ID.
- [Permission declarations could drift from routes] → Centralize each provider's destination and permission predicate in its provider definition and test the editor permission cases explicitly.
- [Context derived from excerpt matching may be imperfect without match metadata] → Only use the excerpt when the normalized keyword is absent from the title and slug but present in the returned excerpt.

## Migration Plan

1. Add the provider, composable, and result-item modules with tests.
2. Adapt the modal to the new composable while preserving its navigation and keyboard contracts.
3. Add localized state messages for every supported locale.
4. Run focused unit tests, UI type checking, linting, and formatting.

No data or contract migration is required. Rollback consists of reverting the UI modules and locale changes.

## Open Questions

None.
