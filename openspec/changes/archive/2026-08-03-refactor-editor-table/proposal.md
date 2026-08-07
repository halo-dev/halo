## Why

Halo's table extension has accumulated duplicated Tiptap internals, custom schema nodes, global editor state, DOM listeners, and editor-only rendering behavior across many bug fixes. This makes small fixes risky and leaves unresolved problems such as automatic column sizing and inconsistent table output between the editor, console preview, and themes.

## What Changes

- Rebase table schema, commands, selection behavior, keyboard handling, column resizing, and repair logic on the upstream Tiptap table extensions, keeping Halo-specific behavior as narrow extensions instead of copied internals.
- Introduce explicit automatic and fixed table layout modes. New tables use responsive automatic sizing, while manual column resizing transitions a table to fixed sizing with horizontal overflow when required.
- Define a canonical, portable HTML contract for table layout, widths, row height, alignment, background, merged cells, and overflow that is shared by editor serialization, console rendering, and themes.
- Continue parsing legacy Halo table wrappers, inline styles, and `colwidth` content without a database-wide migration, then serialize edited content to the canonical format.
- Separate document semantics from a Vue-based table interaction layer with lifecycle-safe handles, menus, selection affordances, drag operations, and disabled states derived from editor commands.
- Provide Yuque-aligned document-table interactions: visual grid insertion, row and column operations, merge and split, header row and column, cell formatting, fit-to-width, keyboard access, touch-friendly controls, and HTML/spreadsheet paste.
- Add fixture, invariant, component, end-to-end, visual, lifecycle, multi-editor, and performance coverage for the failure modes found in Halo's table history.
- Keep `ExtensionTable` and its current package export compatible. This proposal does not add a backend API, database migration, security change, or spreadsheet features such as formulas, filtering, or typed columns.

## Capabilities

### New Capabilities

- `editor-table-model`: Table schema, layout modes, structured attributes, legacy-content compatibility, and canonical HTML serialization.
- `editor-table-interactions`: Insertion, selection, editing commands, menus, keyboard and touch behavior, paste handling, accessibility, and lifecycle requirements.
- `editor-table-rendering`: Responsive and theme-safe rendering contract shared by the editor, console preview, and published content.

### Modified Capabilities

None.

## Impact

- Primary changes are in `ui/packages/editor/src/extensions/table/`, editor menus and translations, editor styles, and their unit/component/end-to-end tests.
- Editor HTML produced through `getHTML()` changes to the canonical table representation; existing stored Halo table HTML remains readable and is normalized only when content is edited and saved.
- Theme authors receive stable wrapper classes and data attributes plus portable layout styles. Cosmetic theme styling remains theme-owned, and compatibility tests cover representative theme CSS.
- The public `@halo-dev/richtext-editor` table export remains available. Existing options are preserved where possible; any option that cannot be mapped safely requires a documented compatibility adapter rather than silent removal.
- The refactor uses the installed upstream Tiptap table capabilities and does not initially require a new runtime dependency.
- All new labels, tooltips, and accessible names participate in Halo's existing i18n system.
