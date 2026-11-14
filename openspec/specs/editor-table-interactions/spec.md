# editor-table-interactions Specification

## Purpose

Define accessible and lifecycle-safe table insertion, editing, formatting, navigation, and clipboard interactions in the rich-text editor.

## Requirements

### Requirement: Visual table insertion
The editor SHALL provide a keyboard-accessible visual grid for choosing an initial table size, with an 8-by-8 range available without manual dimension entry. The resulting table SHALL follow the default automatic-layout model.

#### Scenario: User inserts from the grid
- **WHEN** the user highlights a row and column count in the insertion grid and confirms
- **THEN** the editor inserts a table with those dimensions, a header row when configured by the existing option, and `auto` layout

#### Scenario: User operates the grid by keyboard
- **WHEN** the insertion grid has focus and the user navigates and confirms with the keyboard
- **THEN** the highlighted dimensions are announced and the same table is inserted as with pointer input

### Requirement: Contextual row and column controls
The editor SHALL expose contextual controls to select a row or column; insert before or after; move; duplicate; clear content; and delete. Commands that are invalid for the current selection SHALL be visibly disabled and SHALL NOT mutate the document.

#### Scenario: User opens a column menu
- **WHEN** the user activates a column handle
- **THEN** the column is selected and the menu exposes the valid column operations with invalid operations disabled

#### Scenario: User deletes the last remaining row or column
- **WHEN** deleting the selected row or column would leave no table cells
- **THEN** the editor deletes the table through a defined command instead of leaving an invalid table

### Requirement: Cell and table formatting controls
The editor SHALL provide contextual commands for merging and splitting cells, clearing cell formatting, toggling header rows and header columns, fitting to width, selecting fixed layout, setting cell background and horizontal and vertical alignment, copying a table, and deleting a table.

#### Scenario: User selects a rectangular cell range
- **WHEN** the user selects multiple cells
- **THEN** only operations valid for that rectangular selection are enabled and formatting applies to every selected cell

#### Scenario: User splits a merged cell
- **WHEN** the selected cell has a row span or column span greater than one and the user invokes split
- **THEN** the editor restores the corresponding cells without losing the merged cell's content

#### Scenario: User toggles a header column
- **WHEN** the user toggles the header-column command
- **THEN** the cell types change while their position, content, formatting, and spans remain intact

### Requirement: Predictable keyboard table navigation
The editor SHALL preserve Tiptap table keyboard behavior for Tab, Shift-Tab, Backspace, Delete, and supported modifier variants, while retaining Halo's layered Select All behavior and safe deletion adjacent to a table.

#### Scenario: User presses Tab in the final cell
- **WHEN** focus is in the final cell and Tab is pressed
- **THEN** the configured Tiptap behavior adds a row or moves focus according to the public table option without a duplicate Halo keymap

#### Scenario: User invokes Select All within a table
- **WHEN** the user repeatedly invokes Select All from inside a cell
- **THEN** selection expands predictably from cell content to the table and then to the document

#### Scenario: User deletes near a table boundary
- **WHEN** the caret is immediately before or after a table and Backspace or Delete is pressed
- **THEN** the command does not trap the caret, corrupt the table, or remove unrelated content

### Requirement: Pointer, drag, and touch interaction
Row and column selection and reordering SHALL work with pointer input, and all essential operations SHALL have a tap-activated alternative that does not depend on hover. Dragging SHALL provide a visible target and SHALL result in one undoable document change.

#### Scenario: User reorders a row
- **WHEN** the user drags a row handle to a valid target
- **THEN** the row moves with its content, cell types, spans, widths, and formatting preserved in a single undo step

#### Scenario: User edits a table on a touch device
- **WHEN** hover is unavailable and the user taps a table control
- **THEN** the corresponding selection or menu can be opened and dismissed without horizontal page scrolling

### Requirement: HTML and spreadsheet paste
The editor SHALL preserve compatible tabular structure when pasting HTML tables or spreadsheet cells and SHALL fall back to plain text rather than converting unrelated clipboard image data into a table image.

#### Scenario: User pastes a spreadsheet range
- **WHEN** the clipboard includes a rectangular HTML or tab-separated cell range
- **THEN** the editor inserts or fills table cells while preserving text and supported cell formatting

#### Scenario: Clipboard contains unsupported table markup
- **WHEN** pasted table markup contains unsupported scripts, styles, or spreadsheet features
- **THEN** unsafe content is discarded and supported cell content is retained

### Requirement: Accessible and localized controls
Every table control SHALL have a localized visible label or accessible name, expose its selected and disabled state, support logical focus order, and maintain sufficient contrast in supported light and dark appearances.

#### Scenario: Screen-reader user navigates table controls
- **WHEN** focus moves through handles, menus, and grid controls
- **THEN** each control announces its purpose, target, state, and available action in the active locale

#### Scenario: Focused control becomes unavailable
- **WHEN** a document transaction makes the focused operation invalid
- **THEN** focus moves to a stable relevant control and no hidden or detached element retains focus

### Requirement: Per-editor state and lifecycle safety
Table interaction state SHALL be scoped to the owning editor instance. Every event listener, observer, timer, decoration, and mounted Vue view created for a table SHALL be released when its view or editor is destroyed.

#### Scenario: Two editors contain tables
- **WHEN** two editor instances are mounted and a table is interacted with in one editor
- **THEN** selection, scrolling, menus, and commands in the other editor remain unchanged

#### Scenario: Editor is repeatedly mounted and destroyed
- **WHEN** an editor containing tables is mounted and destroyed multiple times
- **THEN** no table listener, observer, timer, Vue mount, or stale editor reference remains active

### Requirement: View-only state does not mutate the document
Scroll shadows, hover indicators, resize previews, and menu positioning SHALL update through view or DOM state and SHALL NOT dispatch content-equivalent transactions.

#### Scenario: User scrolls a wide table
- **WHEN** the horizontal scroll position changes without a document edit
- **THEN** edge indicators update without adding history entries, triggering serialization, or changing the editor document
