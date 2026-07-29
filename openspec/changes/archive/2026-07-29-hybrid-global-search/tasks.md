## 1. Establish Search Contracts and Tests

- [x] 1.1 Add shared internal result and provider types with stable source-and-resource identifiers, source modes, permission predicates, limits, match context, icons, groups, and routes.
- [x] 1.2 Add failing unit tests for provider model conversion, destination-permission gating, hidden-user filtering, source-specific ordering, and match-context selection.
- [x] 1.3 Add failing unit tests for trimmed keyword handling, 300 ms debounce, empty-keyword suppression, stale-response isolation, partial failures, cache states, stable selection, and the 8/4/20 merge limits.
- [x] 1.4 Add failing component tests for initial, loading, partial-failure, and final-empty states plus safe Enter and keyboard selection behavior.

## 2. Implement Search Providers

- [x] 2.1 Implement the accessible console-route provider and reactive Fuse search over local and cached result data.
- [x] 2.2 Implement 60-second cached providers for plugins, categories, system settings, and active-theme settings without adding cross-module invalidation.
- [x] 2.3 Implement keyword providers for posts, single pages, and attachments using page 1, size 4, deterministic newer-first ordering, and existing Console API response wrappers.
- [x] 2.4 Implement keyword providers for users and tags using page 1, size 4, deterministic display-name ordering, and existing hidden-user filters.
- [x] 2.5 Map provider output to localized groups, stable IDs, existing routes, icons, and concise slug, excerpt, username, tag-slug, or media-type context.

## 3. Implement Search Orchestration

- [x] 3.1 Implement `use-global-search.ts` with normalized keyword state, 300 ms debounce, provider-specific Vue Query entries, empty-keyword disabling, and permission-aware provider activation.
- [x] 3.2 Configure bounded-source caches with 60-second freshness and remote keyword caches with 30-second freshness, 5-minute garbage collection, and no automatic retry for the current keyword.
- [x] 3.3 Pass cancellation signals where supported and add a keyword-generation guard so late responses cannot replace current results.
- [x] 3.4 Implement deterministic result composition with at most 8 local or cached results, at most 4 per remote provider, stable provider rotation, and a global limit of 20.
- [x] 3.5 Preserve selection by stable result ID while results for the same keyword arrive, and reset or clamp selection safely when the keyword or available results change.
- [x] 3.6 Expose explicit initial, pending, partial-failure, and completed-empty state needed by the modal.

## 4. Refactor the Modal Presentation

- [x] 4.1 Add `GlobalSearchResultItem.vue` for the existing icon, title, source label, selection styling, and optional match context.
- [x] 4.2 Refactor `GlobalSearchModal.vue` to consume the composable, render explicit search states, and remove all full-collection request and imperative Fuse-index mutation logic.
- [x] 4.3 Preserve arrow, Ctrl+J, Ctrl+K, Enter, Escape, scrolling, destination routing, and same-route refresh behavior while making confirmation with no result a no-op.
- [x] 4.4 Add localized initial, loading, and partial-failure messages to every supported UI locale.

## 5. Verify the Change

- [x] 5.1 Run the focused global-search unit and component tests and confirm every provider, cache, race, merge, permission, state, and keyboard scenario passes.
- [x] 5.2 Run `pnpm -C ui format` and verify the change does not reformat unrelated files.
- [x] 5.3 Run `pnpm -C ui typecheck`, `pnpm -C ui lint`, and the relevant `pnpm -C ui test:unit` scope.
- [x] 5.4 Inspect the final diff to confirm no backend, OpenAPI-generated client, dependency, or unrelated UI changes were introduced.
