## Context

The current menu model stores menu membership in `Menu.spec.menuItems` and child references in
`MenuItem.spec.children`. Theme rendering currently reconstructs parent links by scanning menu item
children, then attaches root items back to menus through `Menu.spec.menuItems`. Console menu
management also writes both legacy fields directly.

This change moves the structural source of truth to each `MenuItem`: `spec.menuName` identifies the
owning menu, and `spec.parent` identifies the parent item in that same menu. The existing theme output
shape must remain compatible, especially for themes that consume `MenuVo.menuItems` and nested
`MenuItemVo.children`.

## Goals / Non-Goals

**Goals:**

- Migrate existing menu data from legacy fields to `MenuItem.spec.menuName` and `MenuItem.spec.parent`.
- Keep legacy fields present and preserve their stored values as historical compatibility data.
- Make runtime menu queries, Console writes, menu deletion, menu cloning, and drag-and-drop use the new
  fields.
- Keep public theme menu output compatible by returning the same tree-shaped `menuItems` and
  `children` value objects.
- Make the migration idempotent, resilient to partial failure, and safe to retry.
- Provide backend, frontend, and focused e2e coverage for the new model.

**Non-Goals:**

- Do not remove `Menu.spec.menuItems` or `MenuItem.spec.children`.
- Do not maintain dual-write synchronization from new fields back to legacy fields.
- Do not support legacy clients continuing to mutate menu structure through deprecated fields.
- Do not add strict raw extension API validation that rejects MenuItems without `spec.menuName`.
- Do not introduce a SQL database migration or a new dependency.

## Decisions

1. Treat `menuName` and `parent` as the only runtime hierarchy source.

   Runtime code will not fallback to `Menu.spec.menuItems` or `MenuItem.spec.children`. Legacy fields
   are only migration inputs and historical data. This keeps post-migration behavior predictable and
   prevents stale legacy values from changing theme output.

   Alternative considered: keep Finder fallback to legacy fields. That would hide migration failures
   and make preserved legacy values continue to affect rendering.

2. Preserve legacy field values exactly after migration.

   `Menu.spec.menuItems` and `MenuItem.spec.children` will not be cleared, projected, or rewritten by
   migration or later Console operations. This minimizes raw API churn for old data while making it
   clear that those fields no longer represent current structure.

   Alternative considered: write legacy fields as a projection of the new tree. That would create a
   dual-state maintenance problem and conflict with the goal of making new fields authoritative.

3. Use an extension data migration, not a SQL migration.

   The data being migrated is core extension JSON, so the migration belongs in the application layer
   after core extension schemes and initial resources are available. The migration component should
   live under a narrow core extension migration package and run during startup after extension
   initialization.

   Alternative considered: database-level migration. That would need to know extension JSON layout
   across database engines and would bypass extension client behavior and indexes.

4. Make migration blocking on the success path but non-fatal to startup.

   Since runtime Finder code will only read new fields, the normal path should complete migration
   before Halo begins serving menu queries. Data-quality issues such as missing references, cycles, or
   conflicts should be handled and logged without failing startup. Persistence errors should be retried
   and summarized; remaining failures should be logged but should not prevent Halo from starting.

   Alternative considered: asynchronous background migration. That would create a startup window where
   menu output can be empty or incomplete.

5. Mark migrated MenuItems with labels but verify fields as the source of truth.

   Successfully migrated or confirmed MenuItems get `halo.run/menu-item-hierarchy-migrated=true`.
   The label accelerates repeat scans, but the migration must still treat an item with the label and no
   `spec.menuName` as incomplete. Items with `spec.menuName` but no label can be labeled without
   changing ownership.

   Alternative considered: a single global completed marker. That would be unsafe after backup restore,
   partial migration failure, or later import of legacy menu data.

6. Clone deterministic conflict paths.

   If a MenuItem is referenced by multiple menus, the original belongs to the deterministic first menu
   owner. Other menus receive clones. If a MenuItem has multiple parents in the same menu, the first
   parent path keeps the original and other parent paths receive clones. Once a node is cloned for a
   conflict path, its descendants on that path are cloned too so the resulting tree has single-menu,
   single-parent ownership throughout.

   The first owner is selected by `Menu.metadata.creationTimestamp`, then `Menu.metadata.name`, then the
   legacy appearance order. Missing references are skipped with warnings. Cycles are detected; edges
   that would create a cycle are skipped with warnings instead of cloned indefinitely.

   Alternative considered: share child nodes after cloning only the conflict parent. That would preserve
   cross-menu or multi-parent sharing, defeating the purpose of the new model.

7. Use clone annotations for retry-safe idempotence.

   Clones use `metadata.generateName = "menu-item-"` and keep user/plugin labels and annotations except
   for migration state labels. The migration adds annotations:

   - `halo.run/original-menu-item-name`
   - `halo.run/menu-item-migration-menu-name`
   - `halo.run/menu-item-migration-parent-name`
   - `halo.run/menu-item-migration-path`

   The path annotation stores the legacy original-name path as a JSON array string. On retry, the
   migration finds an existing clone by this annotation set. If multiple clones match, it reuses the
   earliest created/name-smallest clone and logs a warning without deleting extras.

8. Keep `parent` nullable and `menuName` optional at schema level.

   Root items use `null` or an absent `parent`. `menuName` is required by runtime semantics and Console
   writes, but not marked schema-required yet so old backups and raw extension API payloads remain
   deserializable during the compatibility period.

9. Update default initial data to include new ownership fields.

   New installations should not depend on migration for default menus. Built-in default MenuItems should
   include `spec.menuName: primary`; root items omit `spec.parent`.

10. Keep Console in the same change.

   Console must query, create, drag, delete, and clone menu items through `menuName` and `parent`.
   Leaving Console on legacy writes would make saved changes invisible to the new Finder.

## Risks / Trade-offs

- Preserved legacy fields can disagree with the rendered tree -> Document and test that new fields are
  authoritative and `MenuVo.menuItems` is the theme tree output.
- Partial migration can leave some menus incomplete -> Retry persistence failures, make migration
  idempotent, label only completed items, and log a startup summary with failures.
- Clone conflicts can surprise users inspecting raw data -> Record original/menu/parent/path annotations
  and keep clone behavior deterministic.
- Old raw API clients that only write legacy fields will no longer affect runtime menus -> Fields remain
  readable and deprecated, but structural writes must move to `menuName` and `parent`.
- Bad new-field data can reference a missing or cross-menu parent -> Finder and Console treat those items
  as root within their menu rather than hiding them or mounting across menus.

## Migration Plan

1. Add new optional fields and indexes.
2. Add the startup migration after core extension initialization.
3. For each menu, recursively walk legacy roots from `Menu.spec.menuItems` through
   `MenuItem.spec.children`.
4. Fill missing `menuName` and `parent` fields without overwriting existing new fields.
5. Resolve cross-menu sharing and multi-parent conflicts by deterministic subtree cloning.
6. Add migration labels and clone annotations.
7. Leave all legacy fields unchanged.
8. Switch Finder and Console behavior to the new fields.
9. Regenerate OpenAPI docs and the UI API client.

Rollback should be handled by reverting runtime code and schema additions if needed. Because legacy
fields are preserved, old code can still read the pre-migration structures. Cloned MenuItems created
during migration may remain as extension data; this change does not include automatic rollback cleanup.

## Open Questions

- None for the initial proposal.
