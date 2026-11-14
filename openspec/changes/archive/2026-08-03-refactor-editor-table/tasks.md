## 1. Baseline and Compatibility Fixtures

- [x] 1.1 Inventory the current `ExtensionTable` public export, supported options, schema attributes, commands, shortcuts, NodeView DOM, and serialized HTML shapes, and encode the compatibility boundary in tests.
- [x] 1.2 Add canonical fixtures for automatic, fixed, merged, header-row, header-column, formatted, malformed, and colgroup-only tables.
- [x] 1.3 Add historical Halo table fixtures covering the supported legacy wrapper, width, row-height, and merged-cell representations found in repository history.
- [x] 1.4 Add characterization tests for IME input, adjacent-table deletion, layered Select All, Tab navigation, Excel/HTML paste, scroll cursor geometry, and wheel boundaries before replacing the current code.
- [x] 1.5 Record large-table transaction-count, mount/unmount, serialization, and interaction baselines and define regression budgets.

## 2. Upstream-Based Table Model

- [x] 2.1 Refactor Halo table, row, header, and cell extensions to extend their upstream Tiptap counterparts and merge parent attributes, parse rules, render rules, commands, and shortcuts.
- [x] 2.2 Remove copied upstream column-update and TableMap navigation algorithms, then prove equivalent supported behavior with model and command tests.
- [x] 2.3 Introduce a validated table `layoutMode` attribute with safe parsing and rendering.
- [x] 2.4 Replace generic row style state with a bounded numeric `rowHeight` attribute while retaining legacy height parsing.
- [x] 2.5 Add validated cell/header `verticalAlign` and `backgroundColor` attributes and preserve upstream horizontal `align`.
- [x] 2.6 Preserve `ExtensionTable` at the package entry point and add compatibility mappings and tests for every existing supported option.
- [x] 2.7 Add table-map invariant and undo/redo tests for sequences of insert, delete, move, duplicate, merge, split, header toggle, and formatting commands.

## 3. Layout Modes and Canonical HTML

- [x] 3.1 Make newly inserted tables default to `auto` with full available width and no synthetic default `colwidth` values.
- [x] 3.2 Implement the automatic-to-fixed transition that snapshots rendered column widths once at resize start and preserves valid widths across spans.
- [x] 3.3 Implement fit-to-width to switch a table to `auto` and remove stale fixed column constraints in one undoable command.
- [x] 3.4 Implement fixed-table colgroup serialization from canonical Tiptap `colwidth` values with normalized pixel widths.
- [x] 3.5 Implement the canonical `halo-table-wrapper` and table data-attribute serialization using valid portable layout styles and excluding editor-only UI.
- [x] 3.6 Implement compatibility parsing for current and historical Halo wrappers, inline heights, cell widths, colgroup-only widths, mixed headers, and supported foreign HTML.
- [x] 3.7 Add semantic round-trip tests proving legacy and foreign fixtures normalize without losing supported content, spans, widths, heights, headers, or formatting.

## 4. Table View Isolation and Lifecycle

- [x] 4.1 Replace the custom copied TableView logic with the upstream TableView or a thin composed subclass limited to canonical wrapper and layout-mode behavior.
- [x] 4.2 Remove module-global editor state and scope every table view and interaction plugin to its owning editor instance.
- [x] 4.3 Add an idempotent cleanup registry for DOM listeners, observers, animation frames, timers, decorations, and mounted Vue views.
- [x] 4.4 Reimplement scroll edge indicators with scroll/resize DOM state without dispatching content-equivalent ProseMirror transactions.
- [x] 4.5 Reimplement row-height resize preview and commit through the structured `rowHeight` command in one history event.
- [x] 4.6 Add two-editor isolation and repeated mount/destroy tests that fail on leaked resources or cross-editor interaction.

## 5. Table Commands and Clipboard Behavior

- [x] 5.1 Implement command-backed row and column selection, insert-before/after, clear, and delete operations with `editor.can()`-compatible dry runs.
- [x] 5.2 Implement command-backed row and column move and duplicate operations that preserve content, spans, cell types, widths, and formatting in one undo step.
- [x] 5.3 Implement merge, split, header-row/header-column toggle, table copy, table delete, and clear-formatting commands with disabled-state tests.
- [x] 5.4 Implement range-aware background and horizontal/vertical alignment commands using structured attributes.
- [x] 5.5 Compose Halo-only layered Select All and adjacent-table deletion behavior with the upstream keyboard shortcuts without redefining upstream Tab or delete keymaps.
- [x] 5.6 Normalize compatible HTML and tab-separated spreadsheet paste into table cells while sanitizing unsupported or unsafe clipboard content.

## 6. Vue Interaction UI

- [x] 6.1 Build a localized 8-by-8-or-larger visual insertion grid with pointer and keyboard selection, announced dimensions, and automatic-layout insertion.
- [x] 6.2 Build row and column handles that support target selection, leading/trailing add controls, correct disabled states, and stable focus.
- [x] 6.3 Build row and column menus for insert, move, duplicate, clear, delete, and header operations using command availability rather than DOM assumptions.
- [x] 6.4 Build a rectangular cell-selection toolbar for merge/split, clear formatting, background, and horizontal and vertical alignment.
- [x] 6.5 Build table controls for fit-to-width, fixed layout, header row/column, copy, and delete.
- [x] 6.6 Build row and column drag-reorder affordances with visible targets, a single history event, and equivalent menu/keyboard actions.
- [x] 6.7 Add tap-activated mobile controls and contain fixed-table horizontal scrolling without hover-only interactions or page-level overflow.
- [x] 6.8 Add localized visible labels, ARIA names and states, logical focus management, escape/dismiss behavior, and light/dark contrast coverage for all table controls.
- [x] 6.9 Refine the table Bubble Menu hierarchy by exposing copy and delete as final root actions and by giving segmented formatting controls unambiguous selected, hover, focus, and dark-mode states.
- [x] 6.10 Prevent localized segmented-control labels from clipping or crossing adjacent option boundaries at desktop, zoomed, and narrow viewport sizes.
- [x] 6.11 Remove the low-value border preset model, rendering contract, and Bubble Menu control while preserving the default table grid borders.
- [x] 6.12 Keep add-row and add-column controls above intersecting handles, and align delete actions with the editor-wide red-icon button treatment.

## 7. Rendering and Theme Contract

- [x] 7.1 Add portable editor/content styles for automatic and fixed layout, overflow containment, row height, alignment, background, selection, handles, and scroll edges using the documented canonical selectors.
- [x] 7.2 Verify that editor `getHTML()`, console preview, and theme-facing saved content consume the same canonical table semantics without NodeView-only dependencies.
- [x] 7.3 Add wide/narrow rendering fixtures for automatic, fixed, merged, mixed-header, formatted, and legacy tables under browser-default and representative theme CSS.
- [x] 7.4 Document the canonical wrapper, classes, data attributes, inline semantics, theme customization boundary, legacy normalization behavior, and theme-author migration guidance.

## 8. End-to-End Validation and Cleanup

- [x] 8.1 Add component tests for insertion grid navigation, menu availability, selection changes, focus recovery, touch alternatives, and all formatting controls.
- [ ] 8.2 Add browser tests for IME input before/after tables, keyboard navigation, layered Select All, adjacent deletion, merge/split, undo/redo, drag reorder, spreadsheet paste, and scrolled cursor geometry.
- [ ] 8.3 Add visual regression tests comparing editor and published rendering across the canonical wide/narrow fixture matrix.
- [x] 8.4 Run the large-table performance suite and eliminate transaction, listener, mount, or serialization regressions beyond the recorded budgets.
- [x] 8.5 Remove superseded custom algorithms, global state, legacy editor-only wrappers, and unused styles only after all compatibility and regression suites pass.
- [x] 8.6 Run editor unit tests plus `pnpm -C ui typecheck` and `pnpm -C ui lint`, and resolve every table-related failure.
- [x] 8.7 Rebuild a focused behavior-oriented table test suite from the stabilized implementation, covering public helpers, model/HTML compatibility, commands, lifecycle behavior, and Vue controls without relying on large snapshots.
- [x] 8.8 Extract stateless, developer-reusable table position and selection helpers into the editor `utils` package, preserve the existing `ExtensionTable` contract, and cover the public export boundary.
- [x] 8.9 Audit the table model, command, view, and Vue UI dependency boundaries; remove proven duplication without exposing DOM or transaction internals, then rerun the complete editor validation suite.
- [x] 8.10 Replace traditional scoped CSS in the Vue components added or modified by this change with equivalent Tailwind utilities, minimize arbitrary-value utilities, retain only token-dependent or complex-selector CSS that cannot be expressed without visual drift, and verify computed-style and Chrome visual parity.
- [x] 8.11 Move the remaining fixed scalar presentation values in change-owned Vue components to exact Tailwind utilities, use shared configuration only for genuinely reusable semantic tokens, retain CSS only for token-dependent or complex state styling, and reverify computed-style and overflow parity in Chrome.
