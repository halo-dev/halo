## ADDED Requirements

### Requirement: Theme Finder API provides category-scoped post navigation

The `PostFinder` interface SHALL expose a `cursorByCategory(String currentName)` method that returns the previous and next posts relative to the given post, scoped to the post's primary category.

#### Scenario: Post has categories

- **WHEN** a published post with `spec.categories = ["java", "tutorial"]` is queried via `cursorByCategory("my-post")`
- **THEN** the previous post SHALL be the most recently published post (before current) whose `spec.categories` contains `"java"`
- **AND** the next post SHALL be the earliest published post (after current) whose `spec.categories` contains `"java"`

#### Scenario: Post has no categories

- **WHEN** a published post with empty or null `spec.categories` is queried via `cursorByCategory("my-post")`
- **THEN** the result SHALL be an empty `NavigationPostVo`

#### Scenario: Unpublished or non-existent post

- **WHEN** `cursorByCategory("unknown-post")` is called for a post that does not exist or is not published
- **THEN** the result SHALL be an empty `NavigationPostVo`

### Requirement: Category scope uses exact match (no subcategory cascade)

The category-scoped navigation SHALL only match posts whose `spec.categories` directly contains the primary category name. Posts associated with subcategories of the primary category SHALL NOT be included.

#### Scenario: Post in parent category

- **WHEN** a post's primary category is `"java"` and another post's `spec.categories` is `["spring-boot"]` (a child of "java")
- **THEN** the post in `"spring-boot"` SHALL NOT appear in the navigation for the post whose primary category is `"java"`

### Requirement: REST API supports category-scoped navigation

The endpoint `GET /apis/api.content.halo.run/v1alpha1/posts/{name}/navigation` SHALL accept an optional `scope` query parameter. When `scope=category`, the endpoint SHALL return category-scoped navigation. When absent or any other value, it SHALL retain the existing global behavior.

#### Scenario: API client requests category scope

- **WHEN** `GET /posts/my-post/navigation?scope=category` is invoked
- **THEN** the response SHALL contain the previous/next posts scoped to the primary category of "my-post"

#### Scenario: API client omits scope parameter

- **WHEN** `GET /posts/my-post/navigation` is invoked without the `scope` parameter
- **THEN** the response SHALL contain the global previous/next posts (existing behavior)

### Requirement: Hidden posts are excluded from category-scoped navigation

The category-scoped navigation SHALL respect the existing `hideFromList` filter, excluding posts where `status.hideFromList` is `true`.

#### Scenario: Adjacent post is hidden

- **WHEN** the post immediately before the current post in the primary category has `status.hideFromList = true`
- **THEN** that hidden post SHALL NOT appear as the "previous" post
- **AND** the next eligible non-hidden post SHALL be returned instead

