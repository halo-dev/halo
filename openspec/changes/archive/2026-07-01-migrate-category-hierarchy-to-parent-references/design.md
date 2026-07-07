## Context

Post categories currently express hierarchy through parent-owned `Category.spec.children` lists. A category does not store its parent directly, so parent lookup, breadcrumbs, tree rendering, hidden-category inheritance, and `categoryWithChildren` queries must reconstruct hierarchy by scanning other categories.

This change is similar to the menu hierarchy migration, but category data has stricter compatibility constraints. Posts store category names in `Post.spec.categories`, so resolving legacy multi-parent conflicts by cloning categories would change taxonomy semantics and post counts. Category migration must therefore choose one parent relationship deterministically instead of creating new categories.

## Goals / Non-Goals

**Goals:**

- Add `Category.spec.parent` as the explicit hierarchy source for non-root categories.
- Preserve `Category.spec.children` as a deprecated compatibility field and migration input.
- Migrate existing legacy `children` data to `parent` safely and idempotently.
- Make runtime category hierarchy reads use `parent` without falling back to `children`.
- Keep theme-facing `CategoryTreeVo.children` and `CategoryTreeVo.parentName` output compatible.
- Update Console category management and shared category select behavior to write and render `parent`.
- Preserve existing hidden-category and category post-query semantics.

**Non-Goals:**

- Do not remove `Category.spec.children` in this change.
- Do not dual-write `children` as a projection of `parent`.
- Do not clone categories to resolve legacy multi-parent references.
- Do not introduce a SQL migration, database-specific JSON rewrite, or new dependency.
- Do not make `Category.spec.parent` schema-required during the compatibility period.
- Do not redesign category deletion semantics or hidden-category storage semantics.

## Decisions

1. Treat `Category.spec.parent` as the only runtime hierarchy source after migration.

   `CategoryServiceImpl`, `CategoryFinderImpl`, post query paths, and Console utilities will build hierarchy from `parent`. They will not fall back to `children`, even when legacy fields still contain data. This avoids two hierarchy sources disagreeing after Console writes stop updating `children`.

   Alternative considered: keep runtime fallback to `children`. That would hide migration problems and allow stale legacy fields to affect rendering after new writes.

2. Keep `Category.spec.children` and its index temporarily.

   The field remains readable, marked deprecated in schema/OpenAPI, and preserved exactly by migration and Console writes. The existing `spec.children` index also remains while a new `spec.parent` index is added. Removal can happen later with a separate compatibility decision.

   Alternative considered: remove the field or clear it during migration. That would make rollback harder and create unnecessary raw extension API churn.

3. Use an extension startup migration.

   The migration runs after core extensions are initialized, reads all categories, and fills missing `spec.parent` values inferred from legacy `spec.children`. It marks processed or confirmed categories with a label such as `content.halo.run/category-hierarchy-migrated=true`.

   The label is not the source of truth. The migration must still inspect actual fields so it can recover from partial failures, imported legacy data, or inconsistent labels.

   Alternative considered: a SQL migration. Category data is extension JSON across multiple database engines, so application-level migration is safer and closer to the extension model.

4. Existing `spec.parent` values take precedence.

   If a category already has `spec.parent`, migration will not overwrite it. If legacy data agrees, the category can be marked migrated. If legacy data disagrees, the migration logs a warning and skips that legacy edge.

   Alternative considered: legacy children always overwrite existing parent. That would be unsafe for partial migrations and manual repairs.

5. Resolve invalid legacy data conservatively.

   Missing child references are skipped with warnings. If one category is referenced by multiple parents, migration chooses one parent deterministically and skips the others. The selection order is based on parent category creation timestamp, parent name, and legacy child order. Cyclic legacy edges are skipped with warnings.

   Categories are not cloned for conflicts because post category references point to category names. Cloning would split taxonomy semantics without updating posts.

6. Runtime tree builders must tolerate bad parent data.

   When `parent` is missing, points to a missing category, points to itself, or participates in a cycle, tree rendering treats the affected category as a root instead of failing or hiding it. This keeps category pages and Console usable after imperfect data imports.

7. Preserve `listChildren` semantics.

   `CategoryService.listChildren(name)` continues returning the requested category plus descendants. Descendant traversal still stops at categories whose `spec.preventParentPostCascadeQuery` is true for post cascade queries. Separate traversal is needed for hidden-state propagation because `preventParentPostCascadeQuery` must not block hide inheritance.

8. Preserve hidden-category behavior.

   `isCategoryHidden(name)` walks the `parent` chain and returns true if any ancestor is hidden. `CategoryReconciler` should propagate hidden state to descendants through `parent` relationships without being stopped by `preventParentPostCascadeQuery`. `CategoryFinderImpl.listAsTree(name)` keeps the current behavior of returning a hidden target subtree when the requested target category is hidden.

9. Console writes only new hierarchy fields.

   Console category creation, drag-and-drop save, parent changes, and shared category select tree utilities will use `spec.parent` and `spec.priority`. Moving a category to the root removes `/spec/parent` instead of setting it to `null`. Console does not patch `spec.children`.

10. Migration is non-fatal to startup.

   The normal path should complete during startup after extension initialization. Data quality problems are logged and skipped. Optimistic locking or persistence failures are retried where practical, summarized, and then allowed to continue startup if unresolved.

## Risks / Trade-offs

- Preserved `children` can disagree with rendered hierarchy -> Runtime and Console use `parent` only, and schema marks `children` deprecated.
- Partial migration could leave incomplete trees -> Make migration idempotent, inspect real fields rather than trusting labels, and log a final summary.
- Legacy multi-parent data loses extra edges -> Choose one deterministic edge, warn, and avoid category cloning that would corrupt post taxonomy semantics.
- Bad `parent` values can create cycles -> Detect invalid parent chains and render affected nodes as roots.
- Hidden-state propagation can regress if it reuses post-cascade traversal -> Keep separate unfiltered descendant traversal for hide inheritance.
- Old raw API clients that only mutate `children` will no longer affect runtime hierarchy -> Preserve read compatibility but require structural writes to move to `parent`.

## Migration Plan

1. Add optional `Category.spec.parent`, deprecate `Category.spec.children`, and add the `spec.parent` index.
2. Add the startup migration after extension initialization.
3. For each category, read legacy `spec.children` and build candidate child-to-parent edges.
4. Assign missing `spec.parent` values for safe edges and mark migrated or confirmed categories.
5. Skip missing references, conflicting parent references, and cyclic edges with warnings.
6. Leave all legacy `children` values unchanged.
7. Switch backend runtime hierarchy reads and Console writes to `parent`.
8. Regenerate OpenAPI docs and the UI API client.

Rollback is possible by reverting runtime code to legacy `children` reads because the migration preserves legacy fields. New Console writes after the change will not update `children`, so rollback after active use may require a separate projection repair if exact hierarchy continuity is required.

## Open Questions

None for the initial proposal.
