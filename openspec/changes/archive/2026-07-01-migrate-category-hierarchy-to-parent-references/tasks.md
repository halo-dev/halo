## 1. API Model And Schema

- [x] 1.1 Add optional `parent` to `Category.CategorySpec` and keep root categories represented by an absent or null parent.
- [x] 1.2 Mark `Category.spec.children` as deprecated in Java schema/OpenAPI metadata while preserving serialization compatibility.
- [x] 1.3 Add a migration marker label constant for category hierarchy migration.
- [x] 1.4 Add a `spec.parent` Category index in `SchemeInitializer` and retain the existing `spec.children` index.
- [x] 1.5 Add or update API model tests proving `parent` serializes and legacy `children` remains available and deprecated.

## 2. Migration

- [x] 2.1 Add a core extension migration component that runs after `ExtensionInitializedEvent`.
- [x] 2.2 Load all Categories and derive candidate parent edges from legacy `spec.children` values.
- [x] 2.3 Fill missing `spec.parent` values without overwriting existing parent values.
- [x] 2.4 Mark migrated or already-consistent Categories with the migration label while treating labels as advisory only.
- [x] 2.5 Handle missing legacy child references by warning and continuing.
- [x] 2.6 Handle multi-parent legacy references by choosing one deterministic parent and skipping conflicting edges without cloning categories.
- [x] 2.7 Detect legacy cycles and skip cyclic edges without failing startup.
- [x] 2.8 Retry optimistic locking or persistence failures where practical and log a migration summary without blocking startup.
- [x] 2.9 Add migration tests for normal trees, existing parent precedence, idempotent reruns, inconsistent labels, missing references, multi-parent references, and cycles.

## 3. Runtime Backend Behavior

- [x] 3.1 Update `CategoryServiceImpl.getParentByName` to resolve parent through the target Category's `spec.parent`.
- [x] 3.2 Update `CategoryServiceImpl.listChildren` to return the requested Category plus descendants discovered through `spec.parent`, preserving `preventParentPostCascadeQuery` cascade stops.
- [x] 3.3 Add or expose an unfiltered descendant traversal for hidden-state propagation that is not stopped by `preventParentPostCascadeQuery`.
- [x] 3.4 Update `CategoryServiceImpl.isCategoryHidden` to walk ancestor links through `spec.parent`.
- [x] 3.5 Update `CategoryFinderImpl` tree and breadcrumb construction to use `spec.parent`, preserve `CategoryTreeVo.children` and `parentName`, and treat missing, self, or cyclic parents as roots.
- [x] 3.6 Update `PostServiceImpl` and theme `PostFinderImpl` category-with-children paths to use the new `listChildren` behavior.
- [x] 3.7 Update `CategoryReconciler` hidden-state propagation to discover descendants through `spec.parent` without writing legacy `children`.
- [x] 3.8 Add or update backend tests for parent lookup, descendant queries, invalid parent handling, breadcrumbs, category tree rendering, hidden inheritance, and `categoryWithChildren` post queries.

## 4. Console And Shared Category UI

- [x] 4.1 Update category tree utilities to build trees from `spec.parent`, detect invalid parents/cycles, and flatten trees with updated parent references.
- [x] 4.2 Update category drag-and-drop save to patch `spec.parent` and `spec.priority` only, using `remove /spec/parent` when moving a category to root.
- [x] 4.3 Update category creation to set child `spec.parent`, compute priority from sibling order, and stop patching parent `spec.children`.
- [x] 4.4 Update category edit and create modal state so root categories omit parent and child categories keep parent references.
- [x] 4.5 Update shared `categorySelect` tree rendering, keyboard navigation, search result path labels, and inline category creation to use the parent-based tree.
- [x] 4.6 Add or update frontend unit tests for tree building, flattening, root moves, child creation payloads, drag patches, invalid parent handling, and category select paths.

## 5. Generated API Client

- [x] 5.1 Run `./gradlew generateOpenApiDocs`.
- [x] 5.2 Run `pnpm -C ui api-client:gen`.
- [x] 5.3 Inspect generated OpenAPI and UI API client changes to confirm they are limited to expected Category model/schema updates.

## 6. Verification

- [x] 6.1 Run `./gradlew :api:test` or focused API Category model tests.
- [x] 6.2 Run focused backend tests for Category migration, `CategoryServiceImpl`, `CategoryFinderImpl`, post category queries, and `CategoryReconciler`.
- [x] 6.3 Run `./gradlew spotlessCheck`.
- [x] 6.4 Run focused frontend unit tests for post category utilities and category select behavior.
- [x] 6.5 Run `pnpm -C ui typecheck && pnpm -C ui lint`.
- [x] 6.6 Assess whether a narrow Console e2e pass is needed for category create, child create, drag root move, drag child move, delete parent behavior, and post category select display.
- [x] 6.7 Run `openspec validate migrate-category-hierarchy-to-parent-references --strict`.
- [x] 6.8 Run `git diff --check`.
