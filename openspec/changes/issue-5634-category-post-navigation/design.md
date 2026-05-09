## Context

Halo's theme system exposes a `postFinder` bean to Thymeleaf templates. The `cursor(String currentName)` method returns the globally adjacent previous/next posts by publish time. Issue halo-dev/halo#5634 requests a category-scoped variant.

The `Post` extension stores categories as `List<String>` in `spec.categories`. There is no explicit "primary category" field. The team has reached an internal consensus: the first element in `spec.categories` is treated as the primary category.

The existing `PostFinderImpl` already has infrastructure for category-based queries (e.g., `listByCategory`), but `cursor()` does not use it. The new feature must be additive — existing themes using `cursor()` must not break.

## Goals / Non-Goals

**Goals:**
- Provide a new `cursorByCategory(String currentName)` Finder API that returns previous/next posts scoped to the primary category.
- Extend `GET /posts/{name}/navigation` with `?scope=category` for API consumers.
- Match the exact category (no cascade to subcategories) — if a post's primary category is "Java", navigation only includes posts directly associated with "Java", not "Spring Boot" (a child of "Java").
- Return an empty `NavigationPostVo` when the post has no categories.

**Non-Goals:**
- No console UI changes or category-level toggle switches.
- No modification to `Category` spec or `Post` spec data models.
- No changes to existing `cursor()` behavior.
- No subcategory cascade for category-scoped navigation.

## Decisions

**Decision 1: Exact-match primary category, no cascade**
- **Rationale**: The user explicitly chose "option 2" (exact match). This is simpler than `listByCategory`'s cascade behavior and gives theme authors predictable semantics. A post in "Java" navigates only within "Java", not its children.

**Decision 2: New Finder method instead of modifying `cursor()`**
- **Rationale**: `cursor()` is a stable, widely-used API. Changing its semantics would be a breaking change for existing themes. A new method `cursorByCategory()` lets theme authors opt-in.

**Decision 3: Query parameter `?scope=category` on existing endpoint instead of a new path**
- **Rationale**: Keeps the REST surface area small. The endpoint represents the same resource (post navigation), just with a different scope. Default behavior (no param) remains unchanged.

**Decision 4: No console configuration**
- **Rationale**: The feature is purely a theme/API concern. Adding a per-category toggle would require model changes, console UI work, and migration logic — out of scope for this change.

## Risks / Trade-offs

|                                             Risk                                             |                                 Mitigation                                 |
|----------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| Category list order is unstable (first element changes)                                      | This is accepted per team consensus. Documented in Finder API docs.        |
| Posts with multiple categories may navigate differently depending on which category is first | Same as above — team consensus. Theme authors can guide users.             |
| Adding `cursorByCategory` to `PostFinder` interface is a minor API change                    | All implementations must be updated. Only `PostFinderImpl` exists in core. |

