## Why

The console global search currently loads every searchable user, plugin, post, category, tag, page, and attachment before searching them in the browser. Because the modal is recreated whenever it opens, large sites repeatedly transfer and index unbounded collections, while asynchronous index updates can leave visible results stale.

## What Changes

- Replace the single all-in-memory search index with an internal hybrid search model:
  - build console routes locally;
  - cache bounded plugin, category, system-setting, and theme-setting collections;
  - query posts, single pages, attachments, users, and tags remotely by keyword.
- Debounce non-empty remote searches while continuing to show local results immediately.
- Merge local and remote results into a stable, permission-aware list with per-source quotas and a global result limit.
- Preserve the selected result while responses for the same keyword arrive, and prevent stale or failed providers from replacing valid results.
- Distinguish the initial, loading, partial-failure, and final empty states.
- Show concise match context for remotely matched resources.
- Align result visibility with the permissions required by the destination route.
- Add focused unit and component tests for query orchestration, result merging, permissions, state rendering, and keyboard behavior.

## Capabilities

### New Capabilities

- `console-global-search`: Defines the console's hybrid local, cached, and remote search behavior, including result composition, permissions, asynchronous state, navigation, and caching.

### Modified Capabilities

None.

## Impact

- Affects the console global-search modal and introduces internal search orchestration, provider, and result-item modules under `ui/src/components/global-search/`.
- Reuses existing Console API keyword endpoints and the existing Vue Query integration.
- Does not change backend routes, OpenAPI contracts, generated API clients, database schemas, plugin APIs, or theme APIs.
- Adds and updates global-search messages in every supported UI locale.
- Authorization enforcement remains on the backend and router guards; the UI additionally hides results that the current user cannot open.
