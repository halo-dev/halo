## Why

Currently, the "previous post" and "next post" navigation on article pages is calculated globally across all posts sorted by publish time. In scenarios where a site organizes content by categories (e.g., knowledge bases, documentation), users expect navigation to stay within the same category rather than jumping to unrelated topics. This change addresses halo-dev/halo#5634 by providing a category-scoped navigation option.

## What Changes

- Add a new theme Finder API `cursorByCategory(String currentName)` in `PostFinder` that returns previous/next posts **limited to the post's primary category** (defined as the first category in `spec.categories`).
- Extend the existing REST endpoint `GET /posts/{name}/navigation` with a `?scope=category` query parameter to expose category-scoped navigation for headless/API consumers.
- If the post has no categories, the new API returns an empty `NavigationPostVo`.
- **Existing `cursor()` behavior is unchanged** — this is purely additive.

## Capabilities

### New Capabilities

- `category-post-navigation`: Theme Finder and REST API for navigating previous/next posts within a post's primary category.

### Modified Capabilities

- (none — existing `cursor()` behavior and `PostFinder` contract remain unchanged)

## Impact

- `application/src/main/java/run/halo/app/theme/finders/PostFinder.java` — new interface method
- `application/src/main/java/run/halo/app/theme/finders/impl/PostFinderImpl.java` — implementation
- `application/src/main/java/run/halo/app/core/endpoint/theme/PostQueryEndpoint.java` — REST endpoint extension
- `application/src/test/...` — unit tests for new behavior

