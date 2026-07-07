## Why

Post category creation already supports choosing a parent category, but editing an existing category does not expose the same hierarchy control. Now that Console category management is backed by `Category.spec.parent` and backend position APIs, administrators should be able to change a category's parent from the edit modal without relying on drag-and-drop.

## What Changes

- Show a parent category field when editing an existing post category.
- Let administrators move an edited category to root or under an existing category.
- Exclude the current category and its descendants from selectable parent candidates.
- Use the existing Console category position API for parent changes so backend validation and priority normalization stay authoritative.
- Keep precise sibling ordering as a drag-and-drop workflow; edit-form parent changes append the category to the target sibling list.

## Capabilities

### New Capabilities

- None.

### Modified Capabilities

- `category-hierarchy`: Console category management will support parent changes from the category edit modal.

## Impact

- Affected UI: `ui/console-src/modules/contents/posts/categories/`
- Affected APIs: existing Console `UpdateCategoryPosition` usage only; no new backend route or OpenAPI contract is expected.
- Compatibility: no database, theme API, plugin API, or public REST contract changes.
- i18n: reuse the existing parent category field label and existing common option wording where possible.
