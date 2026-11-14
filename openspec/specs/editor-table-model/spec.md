# editor-table-model Specification

## Purpose

Define the upstream-compatible table schema, layout modes, structured formatting, legacy compatibility, and stable editor package contract.

## Requirements

### Requirement: Upstream-compatible table schema
The editor SHALL base table, row, header, and cell nodes on the installed Tiptap table extensions and SHALL preserve upstream table repair, selection, command, and HTML parsing behavior unless a documented Halo requirement overrides it. The editor MUST NOT replace upstream nodes solely to add Halo attributes or interaction controls.

#### Scenario: Upstream table content is loaded
- **WHEN** the editor loads valid HTML produced by the supported Tiptap table extension
- **THEN** it preserves rows, cells, headers, spans, column widths, alignment, and cell content without data loss

#### Scenario: Structurally invalid table is edited
- **WHEN** an edit produces a table structure that Tiptap can repair
- **THEN** the upstream table repair behavior restores a valid document without a Halo-specific divergent repair path

### Requirement: Explicit table layout modes
Every table SHALL have a `layoutMode` of `auto` or `fixed`. New tables SHALL default to `auto`, use the available content width, and allow the browser to distribute columns from content without assigning default pixel widths.

#### Scenario: New table is inserted
- **WHEN** a user inserts a table without selecting a layout mode
- **THEN** the table uses `auto` layout, fills the available content width, and has no synthetic default `colwidth` values

#### Scenario: Automatic table content changes
- **WHEN** content is added to or removed from an `auto` table
- **THEN** column distribution is recalculated by normal browser table layout without rewriting the ProseMirror document

### Requirement: Fixed layout preserves deliberate column widths
The editor SHALL use Tiptap `colwidth` values as the canonical pixel-width representation for a `fixed` table. A successful manual column resize SHALL transition an `auto` table to `fixed` before storing the resulting widths.

#### Scenario: User resizes an automatic table column
- **WHEN** the user drags a column resize handle in an `auto` table
- **THEN** the table becomes `fixed` and records valid pixel widths for the affected columns

#### Scenario: Fixed table exceeds its container
- **WHEN** the total fixed column width exceeds the available content width
- **THEN** the table preserves the deliberate column widths instead of compressing them or losing them

### Requirement: Fit to width resets fixed sizing
The editor SHALL provide a fit-to-width operation that changes a table to `auto` and removes fixed column widths that would continue to constrain browser layout.

#### Scenario: User fits a fixed table to the content area
- **WHEN** the user invokes fit to width on a fixed table
- **THEN** the table becomes `auto`, stale `colwidth` constraints are removed, and the table fills the available content width

### Requirement: Structured table formatting attributes
Row height, cell background, horizontal alignment, vertical alignment, and layout mode SHALL be represented by validated structured attributes. Generic style strings SHALL NOT be the authoritative editor state for these values.

#### Scenario: User changes row height
- **WHEN** the user sets or drags a row to a supported height
- **THEN** the editor stores a normalized numeric row height and preserves unrelated row attributes

#### Scenario: User formats selected cells
- **WHEN** the user applies background, horizontal alignment, or vertical alignment
- **THEN** the editor updates only the corresponding validated attributes on the target cells or table

#### Scenario: Unsupported formatting value is parsed
- **WHEN** imported HTML contains an invalid layout, height, alignment, or color value
- **THEN** the editor rejects or normalizes that value without executing arbitrary style content

### Requirement: Legacy Halo table compatibility
The editor SHALL parse table HTML produced by supported historical Halo releases, including existing wrapper elements, inline row-height styles, `colwidth` values, and merged cells. Loading legacy content SHALL NOT require a database migration.

#### Scenario: Legacy table is opened without editing
- **WHEN** stored content containing a legacy Halo table is loaded
- **THEN** its visible structure, content, spans, widths, row heights, and header cells remain available in the editor

#### Scenario: Legacy table is edited and saved
- **WHEN** a legacy table is changed and the document is serialized
- **THEN** the table is emitted in the canonical format with equivalent supported semantics

### Requirement: Stable editor package compatibility
The rich-text editor package SHALL continue to export `ExtensionTable`. Existing supported extension options SHALL retain their behavior or be handled by an explicit compatibility adapter.

#### Scenario: Existing consumer configures ExtensionTable
- **WHEN** a consumer imports `ExtensionTable` from the current package entry point and supplies an existing supported option
- **THEN** the editor initializes without an import break or silently ignored option
