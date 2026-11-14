// @vitest-environment jsdom

import { afterEach, describe, expect, it } from "vite-plus/test";
import type { Editor } from "@/tiptap";
import { TableMap } from "@/tiptap/pm";
import {
  createTableEditor,
  getCellPositions,
  getTableNode,
  insertTable,
} from "./test-editor";

function tableShape(editor: Editor) {
  const table = getTableNode(editor).node;
  const map = TableMap.get(table);
  return { rows: map.height, columns: map.width };
}

describe("Halo table commands", () => {
  let editor: Editor | undefined;

  afterEach(() => {
    editor?.destroy();
    document.body.replaceChildren();
  });

  it("supports dry-run checks and rejects commands outside tables", () => {
    editor = createTableEditor("<p>outside</p>");

    expect(editor.can().setTableLayout("fixed")).toBe(false);
    expect(editor.can().setTableRowHeight(60)).toBe(false);
    expect(editor.can().selectTableRow(0)).toBe(false);
  });

  it("duplicates and deletes rows and columns without breaking the table map", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: 2 });

    expect(editor.commands.duplicateTableRow()).toBe(true);
    expect(tableShape(editor)).toEqual({ rows: 3, columns: 2 });
    expect(editor.commands.duplicateTableColumn()).toBe(true);
    expect(tableShape(editor)).toEqual({ rows: 3, columns: 3 });
    editor.commands.setTextSelection(getCellPositions(editor)[0] + 1);
    expect(editor.commands.selectTableRow(0)).toBe(true);
    expect(editor.commands.deleteRow()).toBe(true);
    editor.commands.setTextSelection(getCellPositions(editor)[0] + 1);
    expect(editor.commands.selectTableColumn(0)).toBe(true);
    expect(editor.commands.deleteColumn()).toBe(true);
    expect(tableShape(editor)).toEqual({ rows: 2, columns: 2 });
  });

  it("moves a row in one undoable document change", () => {
    editor = createTableEditor(`
      <table><tbody>
        <tr><td><p>first</p></td></tr>
        <tr><td><p>second</p></td></tr>
      </tbody></table>`);
    editor.commands.selectTableRow(0);

    expect(editor.commands.moveTableRow(1)).toBe(true);
    expect(getTableNode(editor).node.firstChild?.textContent).toBe("second");
    expect(editor.commands.undo()).toBe(true);
    expect(getTableNode(editor).node.firstChild?.textContent).toBe("first");
  });

  it("merges and splits cells while preserving a valid rectangular map", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: 2 });
    const cells = getCellPositions(editor);
    editor.commands.setCellSelection({
      anchorCell: cells[0],
      headCell: cells[1],
    });

    expect(editor.commands.mergeCells()).toBe(true);
    let table = getTableNode(editor).node;
    expect(table.firstChild?.firstChild?.attrs.colspan).toBe(2);
    expect(TableMap.get(table).width).toBe(2);

    expect(editor.commands.splitCell()).toBe(true);
    table = getTableNode(editor).node;
    expect(table.firstChild?.childCount).toBe(2);
    expect(TableMap.get(table).width).toBe(2);
  });

  it("applies and clears structured formatting on the selected cells", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: 2 });
    const cells = getCellPositions(editor);
    editor.commands.setCellSelection({
      anchorCell: cells[0],
      headCell: cells[3],
    });

    expect(editor.commands.setTableCellBackground("#fee2e2")).toBe(true);
    expect(editor.commands.setTableCellVerticalAlign("bottom")).toBe(true);
    expect(editor.commands.setCellAttribute("align", "center")).toBe(true);
    expect(editor.commands.setTableRowHeight(80)).toBe(true);

    let table = getTableNode(editor).node;
    table.descendants((node) => {
      if (node.type.spec.tableRole?.includes("cell")) {
        expect(node.attrs.backgroundColor).toBe("#fee2e2");
        expect(node.attrs.verticalAlign).toBe("bottom");
        expect(node.attrs.align).toBe("center");
      }
    });
    expect(table.firstChild?.attrs.rowHeight).toBe(80);
    expect(table.lastChild?.attrs.rowHeight).toBe(80);

    expect(editor.commands.clearTableCellFormatting()).toBe(true);
    table = getTableNode(editor).node;
    table.descendants((node) => {
      if (node.type.spec.tableRole?.includes("cell")) {
        expect(node.attrs.backgroundColor).toBeNull();
        expect(node.attrs.verticalAlign).toBeNull();
        expect(node.attrs.align).toBeNull();
      }
    });
  });

  it("deletes the table when the selected axis covers its last row", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 1, cols: 2 });
    editor.commands.selectTableRow(0);

    expect(editor.commands.deleteRow()).toBe(true);
    expect(editor.view.dom.querySelector("table")).toBeNull();
  });
});
