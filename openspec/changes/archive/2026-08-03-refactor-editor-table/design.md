## Context

Halo's table extension currently combines five responsibilities: ProseMirror schema, copied Tiptap column-width and navigation algorithms, a custom NodeView and scroll wrapper, document commands, and Vue/DOM interaction controls. The implementation recreates `TableCell` and `TableHeader`, keeps an editor reference in module-global state, dispatches a content-equivalent transaction for scroll state, and owns listeners and timers across several plugins. These choices have made upstream table fixes difficult to inherit and have created regressions around IME input, cursor geometry, deletion, selection, merged cells, paste, row height, and scroll boundaries.

Saved posts use the editor's serialized HTML directly. The backend does not rebuild tables for themes, so an editor-only NodeView cannot solve publication behavior. Existing content also contains several historical Halo wrapper and style shapes that must remain readable. `ExtensionTable` is exported from the editor package and is therefore a compatibility boundary.

Tiptap 3.29 already supplies table schema nodes, colgroup parsing and rendering, repair logic, commands, resize behavior, keyboard navigation, alignment attributes, and mutation handling that overlap Halo's copies. Tiptap's commercial Table Node UI is not reusable in Halo's Vue editor, but its interaction model is a useful reference.

## Goals / Non-Goals

**Goals:**

- Make upstream Tiptap table behavior the maintained core and reduce Halo code to explicit product requirements.
- Resolve automatic-width behavior, including issue #7536, without sacrificing deliberate fixed-width tables.
- Establish one content contract for editor, preview, and themes while preserving legacy content.
- Deliver a document-table experience aligned with Yuque: fast insertion, direct row/column manipulation, cell formatting, merge/split, keyboard access, and usable touch controls.
- Remove cross-editor state and guarantee cleanup of view-only resources.
- Build a regression suite around the failure classes in Halo's table history.

**Non-Goals:**

- Spreadsheet formulas, filters, typed columns, charts, frozen panes, or collaborative cell calculations.
- A drop-in adoption of Tiptap's React-based commercial Table Node UI.
- A database-wide rewrite of existing post HTML.
- A backend table rendering service or a new public backend API.
- Converting header rows to `thead` in the first iteration; mixed header rows and columns cannot be represented there losslessly.
- Perfect preservation of arbitrary third-party inline CSS.

## Decisions

### 1. Extend the upstream table nodes instead of recreating them

`ExtensionTable` will extend Tiptap's `Table`, and Halo row, header, and cell extensions will extend the corresponding upstream nodes while merging parent attributes, parse rules, render rules, commands, and keyboard shortcuts. Halo will remove copied `updateColumns`, TableMap navigation helpers, and shortcuts already supplied upstream.

Halo-only behavior will be isolated behind named extensions or plugins:

- structured Halo attributes and compatibility parse rules;
- layered Select All and safe adjacent-table deletion where upstream behavior does not satisfy Halo;
- interaction decorations, menus, row-height controls, and drag operations;
- compatibility mapping for existing `ExtensionTable` options.

This is preferred over continuing the fork because every copied upstream algorithm increases upgrade risk. Replacing the whole editor table with Tiptap's Table Node UI was rejected because it is a commercial React UI layer and would not match Halo's Vue component system or licensing/product boundary.

### 2. Separate the document model, table view, and interaction UI

The implementation will have three explicit layers:

1. **Model extensions** own schema attributes, commands, parsing, serialization, and invariant repair.
2. **Table view** composes or subclasses the upstream `TableView` only where a canonical wrapper or layout-mode update cannot be expressed by options. It owns per-instance DOM and resize state.
3. **Halo table UI** is a Vue-based extension/plugin that renders handles, menus, overlays, drag previews, and selection affordances from editor state.

The UI will use `editor.can()` or equivalent dry-run commands for enabled states. It will not encode table structure by directly mutating DOM. Scroll shadows and hover state will use DOM state plus `ResizeObserver` and scroll events; they will not dispatch empty ProseMirror transactions.

This separation is preferred over a single large NodeView because NodeView DOM is not serialized and because UI lifecycle bugs must not affect the content model.

### 3. Use two explicit layout modes

`layoutMode` is a table attribute with two values:

- `auto`: the default for new and fit-to-width tables. The table uses `width: 100%` and `table-layout: auto`; cells do not receive synthetic `[100]` `colwidth` defaults.
- `fixed`: used after deliberate manual resizing or an explicit fixed-layout action. Valid Tiptap `colwidth` values are authoritative, a colgroup is emitted, and the table retains the sum of those pixel widths.

Starting a successful resize on an automatic table will first materialize the current rendered column widths, switch to `fixed`, and then apply the drag delta. “Fit to width” reverses that transition by clearing column-width constraints and changing to `auto`.

A proportional percentage-width mode was considered but deferred. It adds rounding, span, paste, and narrow-container complexity without being required to solve automatic layout or preserve existing fixed widths.

### 4. Define a canonical HTML boundary with compatibility parsing

Newly serialized tables will use this semantic shape:

```html
<div class="halo-table-wrapper" data-table-layout="auto">
  <table data-table-layout="auto" style="width: 100%; table-layout: auto">
    <colgroup><!-- fixed mode only when widths are meaningful --></colgroup>
    <tbody>
      <tr>
        <th or td><!-- cell content --></th or td>
      </tr>
    </tbody>
  </table>
</div>
```

The wrapper provides portable horizontal overflow containment. Fixed mode serializes valid pixel column widths through the upstream colgroup representation. Rows serialize a normalized CSS `height`; cells serialize valid standard alignment, vertical alignment, and background properties plus stable data attributes where themes need semantic selectors.

Parse rules will recognize the canonical representation, historical Halo nested wrappers, row `style` heights, cell widths, and colgroup-only widths. The ProseMirror document is the normalized form. No background migration is performed; canonical output is produced the next time a document is saved after editing.

Keeping only the upstream bare `table` was considered, but rejected because overflow containment must survive in theme output. Keeping the current editor-specific nested wrappers was rejected because they expose implementation details and have already diverged between NodeView and serialization.

### 5. Preserve mixed header cells and use structured attributes

The first version will keep rows under `tbody` and represent both header rows and header columns with `th` cells in their actual row positions. This preserves mixed header configurations and existing content. A separate future proposal can introduce `thead` only if a lossless mapping and theme migration are demonstrated.

Halo-owned persisted values will be typed:

- table: `layoutMode`;
- row: `rowHeight` as a bounded number or `null`;
- cell/header: upstream `align`, plus `verticalAlign` and `backgroundColor`.

Generic `style` strings will be accepted only by compatibility parsers and converted into validated values. Attribute rendering will use allowlisted enums, bounded dimensions, and normalized CSS colors.

### 6. Build the interaction layer from commands with accessible alternatives

The visual insertion grid will support at least 8 by 8 dimensions and full keyboard operation. Row and column handles will select their targets before opening menus. Menus will cover insert, move, duplicate, clear, delete, header toggles, merge/split, fit-to-width, layout, alignment, background, copy, and table deletion.

Drag reorder will call tested table commands and produce one history event. Every drag action will have menu/keyboard alternatives. On touch devices, controls open by tap and do not depend on hover. All strings and accessible names use Halo i18n resources.

Sorting is omitted because it changes document data order and moves the feature toward spreadsheet behavior; it can be proposed independently after document-table parity is stable.

### 7. Make isolation and cleanup enforceable

The module-global editor reference will be removed. Each NodeView/plugin view owns its editor/view reference and a cleanup registry for listeners, observers, timers, Vue mounts, and decorations. `destroy()` will be mandatory for every owner, idempotent, and covered by repeated mount/unmount and two-editor tests.

High-frequency pointer, resize, and scroll work will be animation-frame throttled where appropriate. Pure view updates will not enter the document history. Large-table benchmarks will set budgets for transaction count and interaction latency before the old implementation is removed.

### 8. Use compatibility fixtures and invariant tests as the migration gate

Before replacing behavior, tests will capture canonical, current Halo, historical Halo, Tiptap, spreadsheet, merged-cell, header-column, and malformed fixtures. Round-trip tests will compare supported semantics rather than insignificant attribute order. Command-sequence tests will assert valid table maps after add, delete, move, merge, split, resize, and undo/redo operations.

Component and end-to-end tests will cover menu states, keyboard selection, IME input before and after a table, cursor geometry while scrolled, spreadsheet paste, multiple editors, touch alternatives, and cleanup. Visual tests will compare editor and published DOM in wide and narrow containers with representative theme selectors.

## Risks / Trade-offs

- **Legacy HTML contains undocumented variants** → Build a fixture corpus from repository history and real anonymized samples; retain a legacy parser during at least one compatibility window.
- **Switching to upstream nodes changes subtle selection or paste behavior** → Land model parity and invariant tests before enabling the new UI, and compare behavior against the current extension fixture by fixture.
- **Theme CSS can override portable layout rules** → Document stable selectors, keep minimum layout semantics inline, add representative theme compatibility tests, and publish a theme migration note.
- **Manual resize from automatic layout can jump because browser widths are fractional** → Snapshot rendered widths once at drag start, normalize rounding across spanned cells, and test that total width remains stable.
- **A broad Yuque-parity scope delays the root refactor** → Deliver behind implementation phases: model and contract first, essential interaction parity second, advanced formatting and drag controls third.
- **Canonical serialization creates noisy diffs when old posts are saved** → Normalize only tables in edited documents, preserve semantic values, and avoid a bulk database migration.
- **Public extension consumers rely on accidental internals** → Preserve the package export and documented options; provide compatibility adapters for known options while treating private DOM classes as non-API.

## Migration Plan

1. Capture legacy/current fixtures and regression tests against the existing implementation.
2. Introduce upstream-derived model extensions and compatibility parsers behind the existing `ExtensionTable` export.
3. Add layout modes and canonical serialization, then verify round trips and editor/theme rendering before changing the default insertion path.
4. Replace the custom NodeView and copied algorithms with the layered table view and UI plugin.
5. Enable automatic layout for newly inserted tables and expose fit-to-width for existing fixed tables.
6. Add the remaining Yuque-aligned controls, i18n, accessibility, touch behavior, and performance gates.
7. Remove legacy implementation code only after compatibility, end-to-end, and visual suites pass.

Rollback is code-only: restore the previous extension implementation while retaining the canonical parser so content saved during rollout remains readable. Because there is no database migration and fixed widths remain compatible with Tiptap `colwidth`, rollback does not require rewriting stored posts.

## Open Questions

- Establish the exact supported historical Halo version window and gather representative anonymized HTML outside repository fixtures before removing any legacy parse rule.
- Set measurable interaction and serialization budgets for the large-table performance suite after recording the current implementation baseline.
