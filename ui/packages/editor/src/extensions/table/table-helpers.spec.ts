// @vitest-environment jsdom

import { afterEach, describe, expect, it } from "vite-plus/test";
import type { Editor } from "@/tiptap";
import {
  findTable,
  getCellsInColumn,
  getCellsInRow,
  getTableHeaderState,
  isColumnSelected,
  isRowSelected,
  isTableSelected,
  selectColumn,
  selectRow,
  selectTable,
} from "@/utils/table";
import {
  createTableEditor,
  getCellPositions,
  insertTable,
} from "./test-editor";

describe("table selection helpers", () => {
  let editor: Editor | undefined;

  afterEach(() => {
    editor?.destroy();
    document.body.replaceChildren();
  });

  it("finds the owning table from a text selection", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: 2 });
    editor.commands.setTextSelection(getCellPositions(editor)[0] + 1);

    const table = findTable(editor.state.selection);

    expect(table?.node.type.spec.tableRole).toBe("table");
    expect(table?.start).toBeGreaterThan(table?.pos ?? 0);
  });

  it("returns unique cells for merged rows and columns", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: 2 });
    const cells = getCellPositions(editor);
    editor.commands.setCellSelection({
      anchorCell: cells[0],
      headCell: cells[1],
    });
    editor.commands.mergeCells();

    expect(getCellsInRow(0)(editor.state.selection)).toHaveLength(1);
    expect(getCellsInColumn([0, 1])(editor.state.selection)).toHaveLength(3);
    expect(getCellsInColumn(-1)(editor.state.selection)).toEqual([]);
  });

  it("constructs row, column, and whole-table selections", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 3, cols: 3 });

    editor.view.dispatch(selectRow(1)(editor.state.tr));
    expect(isRowSelected(1)(editor.state.selection)).toBe(true);
    expect(isRowSelected(0)(editor.state.selection)).toBe(false);

    editor.view.dispatch(selectColumn(2)(editor.state.tr));
    expect(isColumnSelected(2)(editor.state.selection)).toBe(true);
    expect(isColumnSelected(1)(editor.state.selection)).toBe(false);

    editor.view.dispatch(selectTable(editor.state.tr));
    expect(isTableSelected(editor.state.selection)).toBe(true);
  });

  it("derives header row and column state from the table model", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: 2, withHeaderRow: true });

    expect(getTableHeaderState(editor.state.selection)).toEqual({
      row: true,
      column: false,
    });

    editor.commands.toggleHeaderColumn();
    expect(getTableHeaderState(editor.state.selection)).toEqual({
      row: true,
      column: true,
    });
  });

  it("leaves transactions unchanged when no table or invalid index exists", () => {
    editor = createTableEditor("<p>outside</p>");
    const transaction = editor.state.tr;

    expect(selectTable(transaction)).toBe(transaction);
    expect(selectRow(99)(transaction)).toBe(transaction);
    expect(selectColumn(99)(transaction)).toBe(transaction);
  });
});
