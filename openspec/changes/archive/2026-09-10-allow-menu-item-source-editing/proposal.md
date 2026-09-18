## Why

Editing a menu item currently locks resource-backed source types and prevents custom links or built-in routes from becoming resource references, even though the existing MenuItem model and reconciler support changing the source. Opening these transitions also requires fixing destructive draft handling so switching types or retrying a failed save does not discard user-entered values.

## What Changes

- Allow existing menu items to switch among all eight supported sources: custom link, archives, categories, tags, Post, SinglePage, Category, and Tag.
- Preserve the current menu item identity and hierarchy while replacing only its link source; continue using the existing update and parent-position APIs.
- Preserve editable name and custom URL drafts during temporary source changes, and normalize mutually exclusive source fields on a separate submission object.
- Seed editable fields from the existing item's resolved values when appropriate; require user input when no usable value is available.
- Recognize resource sources by their complete supported reference descriptor rather than kind alone. Keep unsupported references locked and unchanged when saving unrelated fields.
- Keep annotation form context consistent with the selected source and retain annotation edits during switching.
- Replace the existing resource-lock specification and tests with conversion, draft-preservation, validation, and compatibility coverage.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `menu-route-bindings`: Expand Console source editing to supported resource references, define conversion and draft-preservation behavior, and preserve unsupported references.

## Impact

- Primary implementation: `ui/console-src/modules/interface/menus/components/MenuItemEditingModal.vue` and its adjacent tests; a small local helper is acceptable if needed for direct testing.
- Existing resource selectors, annotations, save notifications, and tree refresh behavior remain in use. No shared selector redesign or parent-move transaction redesign is included.
- No public API, generated API client, database schema, stored identity, or dependency changes are planned. Backend reconciliation continues to derive rendered values from the saved source.
- Plugin/theme compatibility: preserve unsupported references and existing annotation data; annotation schema context must reflect the selected source without exposing hidden draft fields as active fields. Theme rendering remains based on the existing resolved status.
- Authorization remains the existing menu-edit authorization; resource selection retains existing selector access behavior.
- Reuse existing localized type labels and validation messages. Any necessary new explanatory text must be added to every maintained locale.
