## Why

Halo currently models post category hierarchy through parent-owned `Category.spec.children` lists. This makes parent lookup indirect, allows invalid states such as multi-parent references or cycles, and forces backend and Console code to reconstruct hierarchy by scanning other categories.

Moving hierarchy ownership to each category through `Category.spec.parent` makes category lookup, tree rendering, breadcrumbs, hidden-category inheritance, and `categoryWithChildren` queries simpler while preserving theme-facing tree output compatibility.

## What Changes

- Add optional `Category.spec.parent`, referencing the parent `Category.metadata.name`; root categories leave it unset.
- Keep `Category.spec.children` available as a deprecated compatibility field and migration input.
- Add a `spec.parent` index while temporarily retaining the existing `spec.children` index.
- Add an idempotent startup migration that fills missing `spec.parent` values from legacy `spec.children` references, marks processed categories, and preserves existing category data.
- Update runtime category hierarchy behavior to use `spec.parent` as the source of truth without falling back to `spec.children`.
- Update Console category management and shared category select utilities to build, create, move, and sort categories through `spec.parent` and `spec.priority`.
- Regenerate OpenAPI docs and the UI API client for the new category model field and deprecated legacy field metadata.

## Capabilities

### New Capabilities

- `category-hierarchy`: Defines category parent-reference hierarchy storage, compatibility migration, runtime category tree behavior, and Console category hierarchy writes.

### Modified Capabilities

None.

## Impact

- API model: `api/src/main/java/run/halo/app/core/extension/content/Category.java`.
- Backend runtime: category scheme indexes, `CategoryServiceImpl`, `CategoryFinderImpl`, post/category query paths, category hidden-state reconciliation, and startup extension migration.
- Frontend: Console post category management, category tree utilities, shared `categorySelect` FormKit input behavior, and focused unit tests.
- Generated artifacts: OpenAPI JSON and `ui/packages/api-client`.
- Compatibility: theme-facing `CategoryTreeVo.children` and `CategoryTreeVo.parentName` output shapes remain compatible; raw extension clients can still read legacy `spec.children`, but hierarchy writes move to `spec.parent`.
- Security: no auth or authorization model changes are expected.
