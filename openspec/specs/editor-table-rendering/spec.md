# editor-table-rendering Specification

## Purpose

Define canonical, portable, responsive, and regression-tested table rendering across the editor, previews, and published themes.

## Requirements

### Requirement: Canonical table HTML contract
Serialized table content SHALL use one documented canonical structure with stable Halo wrapper classes and table data attributes. It SHALL preserve layout mode, widths, row height, cell type, spans, alignment, background, and cell content using valid HTML and CSS property names.

#### Scenario: Automatic table is serialized
- **WHEN** the editor serializes an `auto` table
- **THEN** the HTML identifies automatic layout, fills the available content width, and does not emit stale fixed column widths

#### Scenario: Fixed table is serialized
- **WHEN** the editor serializes a `fixed` table
- **THEN** the HTML identifies fixed layout and emits a colgroup whose valid pixel widths match the document `colwidth` values

#### Scenario: Mixed header cells are serialized
- **WHEN** a table contains header cells outside the first row or includes a header column
- **THEN** the canonical HTML preserves each `th` or `td` cell in its row without forcing a lossy `thead` conversion

### Requirement: Consistent editor and published rendering
The editor, console preview, and theme-facing post content SHALL consume the same canonical table HTML semantics. NodeView-only wrapper or class differences MUST NOT be required for layout, overflow, or stored formatting to work.

#### Scenario: Content is previewed and published
- **WHEN** the same saved post is shown in the editor, console preview, and a compliant theme
- **THEN** table layout mode, column sizing, overflow, spans, row height, alignment, and background have equivalent meaning in every context

#### Scenario: Editor UI decorations are present
- **WHEN** table handles, resize guides, selections, or menus are displayed in the editor
- **THEN** those decorations are excluded from serialized post content

### Requirement: Responsive overflow behavior
Automatic tables SHALL adapt to their content container without causing page-level horizontal overflow. Fixed tables that are wider than their content container SHALL scroll within the canonical table wrapper and expose correct leading and trailing edge states in the editor.

#### Scenario: Automatic table is rendered in a narrow container
- **WHEN** an `auto` table is rendered in a content area narrower than the editor
- **THEN** it uses the available content width and the page itself does not horizontally scroll because of the table wrapper

#### Scenario: Fixed table is rendered in a narrow container
- **WHEN** a fixed table's declared width exceeds the content area
- **THEN** horizontal overflow is contained by the table wrapper and all cells remain reachable

### Requirement: Portable layout and theme-owned appearance
The canonical HTML SHALL carry the minimum valid inline layout and stored-format semantics needed for layout mode, width, overflow, row height, alignment, and selected cell background to survive without editor CSS. Stable classes and data attributes SHALL allow themes to customize typography, spacing, colors, and border presentation without changing table structure.

#### Scenario: Theme provides no table layout override
- **WHEN** canonical table HTML is rendered with only browser defaults and Halo's portable attributes
- **THEN** its automatic or fixed layout, overflow containment, row heights, alignments, and stored cell backgrounds remain usable

#### Scenario: Theme customizes table appearance
- **WHEN** a theme styles the documented table classes and data attributes
- **THEN** it can change cosmetic presentation without depending on private editor DOM or invalidating the stored layout mode

### Requirement: Legacy and foreign HTML normalization
Legacy Halo and compatible foreign table HTML SHALL be accepted through parse rules and SHALL normalize to the canonical contract after a document edit and save. Unsupported presentation details SHALL be discarded without losing supported table structure or text.

#### Scenario: Legacy wrapper is normalized
- **WHEN** a legacy table with nested Halo scroll wrappers is edited and saved
- **THEN** one canonical wrapper is emitted with equivalent supported table semantics

#### Scenario: Colgroup-only widths are imported
- **WHEN** compatible HTML declares column widths in a colgroup but omits cell `colwidth` metadata
- **THEN** the editor reconstructs supported column widths and serializes them consistently

### Requirement: Rendering regression coverage
The table contract SHALL be verified in wide and narrow containers with automatic, fixed, merged, header-row, header-column, formatted, and legacy fixtures. Coverage SHALL compare editor output with the DOM consumed by representative theme styling.

#### Scenario: Canonical fixture suite runs
- **WHEN** automated table rendering tests execute
- **THEN** semantic and visual regressions in the supported fixture matrix fail the test suite
