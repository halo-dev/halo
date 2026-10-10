// @vitest-environment jsdom

import { afterEach, describe, expect, it, vi } from "vite-plus/test";
import type { Editor } from "@/tiptap";
import { closeHistory, columnResizingPluginKey, TableMap } from "@/tiptap/pm";
import {
  createTableEditor,
  getCellPositions,
  getTableNode,
  insertTable,
} from "./test-editor";

describe("automatic table column resizing", () => {
  let editor: Editor | undefined;

  afterEach(() => {
    editor?.destroy();
    document.body.replaceChildren();
    vi.restoreAllMocks();
  });

  function startDrag(widths = [200, 200, 200], cellIndex = 0) {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: widths.length });
    editor.view.dispatch(closeHistory(editor.state.tr));
    const table = editor.view.dom.querySelector("table")!;
    table.querySelectorAll("col").forEach((column, index) => {
      vi.spyOn(column, "getBoundingClientRect").mockReturnValue({
        width: widths[index],
      } as DOMRect);
    });
    editor.view.dispatch(
      editor.state.tr.setMeta(columnResizingPluginKey, {
        setHandle: getCellPositions(editor)[cellIndex],
      })
    );
    table.querySelector("td")!.dispatchEvent(
      new MouseEvent("mousedown", {
        bubbles: true,
        cancelable: true,
        clientX: 200,
        button: 0,
      })
    );
    return table;
  }

  function move(clientX: number) {
    window.dispatchEvent(new MouseEvent("mousemove", { clientX, buttons: 1 }));
  }

  function finish(clientX: number) {
    window.dispatchEvent(new MouseEvent("mouseup", { clientX }));
  }

  it("keeps the table full width during dragging and compensates the adjacent column", () => {
    const table = startDrag();
    move(250);
    expect(table.style.width).toBe("100%");
    expect(table.style.tableLayout).toBe("fixed");
    const columns = table.querySelectorAll("col");
    expect(Number.parseFloat(columns[0].style.width)).toBeCloseTo(250 / 6);
    expect(Number.parseFloat(columns[1].style.width)).toBe(25);
    expect(
      getTableNode(editor!).node.firstChild!.firstChild!.attrs.colwidth
    ).toBeNull();

    finish(250);
    const { node } = getTableNode(editor!);
    expect(node.attrs.layoutMode).toBe("auto");
    node.forEach((row) => {
      expect(
        Array.from(
          { length: row.childCount },
          (_, i) => row.child(i).attrs.colwidth
        )
      ).toEqual([[250], [150], [200]]);
    });
    expect(table.style.width).toBe("100%");
    expect(editor!.commands.undo()).toBe(true);
    expect(
      getTableNode(editor!).node.firstChild!.firstChild!.attrs.colwidth
    ).toBeNull();
    expect(editor!.commands.redo()).toBe(true);
    expect(
      getTableNode(editor!).node.firstChild!.firstChild!.attrs.colwidth
    ).toEqual([250]);
  });

  it("constrains both columns to the minimum width", () => {
    startDrag();
    move(1000);
    finish(1000);
    const row = getTableNode(editor!).node.firstChild!;
    expect(row.child(0).attrs.colwidth).toEqual([375]);
    expect(row.child(1).attrs.colwidth).toEqual([25]);
  });

  it("does not resize the outer edge of an automatic table", () => {
    const table = startDrag([200, 200], 1);
    move(250);
    finish(250);
    expect(table.style.width).toBe("100%");
    expect(getTableNode(editor!).node.attrs.layoutMode).toBe("auto");
    expect(
      getTableNode(editor!).node.firstChild!.lastChild!.attrs.colwidth
    ).toBeNull();
  });

  it("preserves the drag preview across decoration updates", () => {
    const table = startDrag();
    move(250);
    editor!.view.dispatch(editor!.state.tr.setMeta("probe", true));
    expect(
      Number.parseFloat(table.querySelector("col")!.style.width)
    ).toBeCloseTo(250 / 6);
    finish(250);
  });

  it("cancels the preview on window blur without changing the document", () => {
    const table = startDrag();
    move(250);
    window.dispatchEvent(new Event("blur"));
    expect(table.style.tableLayout).toBe("auto");
    expect(table.querySelector("col")!.style.width).toBe("");
    expect(columnResizingPluginKey.getState(editor!.state)!.dragging).toBe(
      false
    );
    finish(250);
    expect(
      getTableNode(editor!).node.firstChild!.firstChild!.attrs.colwidth
    ).toBeNull();
  });

  it("uses rendered widths when dragging again after the container shrinks", () => {
    const table = startDrag();
    finish(250);
    const scaledWidths = [125, 75, 100];
    table.querySelectorAll("col").forEach((col, index) => {
      vi.mocked(col.getBoundingClientRect).mockReturnValue({
        width: scaledWidths[index],
      } as DOMRect);
    });
    table.querySelector("td")!.dispatchEvent(
      new MouseEvent("mousedown", {
        bubbles: true,
        cancelable: true,
        clientX: 200,
      })
    );
    finish(225);
    const row = getTableNode(editor!).node.firstChild!;
    expect(row.child(0).attrs.colwidth).toEqual([150]);
    expect(row.child(1).attrs.colwidth).toEqual([50]);
    expect(row.child(2).attrs.colwidth).toEqual([100]);
  });

  it("measures current pixels when switching a proportional table to fixed layout", () => {
    const table = startDrag();
    finish(250);
    table.querySelectorAll("col").forEach((col, index) => {
      vi.mocked(col.getBoundingClientRect).mockReturnValue({
        width: [125, 75, 100][index],
      } as DOMRect);
    });
    editor!.commands.setTableLayout("fixed");
    const { node } = getTableNode(editor!);
    expect(node.attrs.layoutMode).toBe("fixed");
    expect(node.firstChild!.child(0).attrs.colwidth).toEqual([125]);
    expect(table.style.width).toBe("300px");
  });

  it.each(["columns", "cells"])(
    "measures only the outer table using %s when switching nested tables to fixed layout",
    (measurement) => {
      editor = createTableEditor(
        `<table data-table-layout="auto"><tr><td colspan="2" colwidth="200,200"><p>Outer</p><table data-table-layout="fixed"><tr><td colwidth="60"><p>Nested</p></td></tr></table></td><td colwidth="200"><p>End</p></td></tr><tr><td colwidth="200"><p>A</p></td><td colwidth="200"><p>B</p></td><td colwidth="200"><p>C</p></td></tr></table>`
      );
      editor.commands.setTextSelection(getCellPositions(editor)[0] + 2);
      const table = editor.view.dom.querySelector("table")!;
      const nestedBefore = editor.state.doc
        .firstChild!.firstChild!.firstChild!.child(1)
        .toJSON();
      table.querySelectorAll("col").forEach((col) => {
        vi.spyOn(col, "getBoundingClientRect").mockReturnValue({
          width: 60,
        } as DOMRect);
      });
      table.querySelectorAll(":scope > colgroup > col").forEach((col) => {
        vi.mocked(col.getBoundingClientRect).mockReturnValue({
          width: measurement === "columns" ? 100 : 0,
        } as DOMRect);
      });
      Array.from(table.rows[0].cells).forEach((cell, index) => {
        vi.spyOn(cell, "getBoundingClientRect").mockReturnValue({
          width: index === 0 ? 200 : 100,
        } as DOMRect);
      });
      vi.spyOn(
        table.querySelector("table")!.querySelector("td")!,
        "getBoundingClientRect"
      ).mockReturnValue({ width: 60 } as DOMRect);

      expect(editor.commands.setTableLayout("fixed")).toBe(true);
      const outer = editor.state.doc.firstChild!;
      expect(outer.attrs.layoutMode).toBe("fixed");
      expect(outer.firstChild!.firstChild!.attrs.colwidth).toEqual([100, 100]);
      expect(outer.firstChild!.lastChild!.attrs.colwidth).toEqual([100]);
      outer.lastChild!.forEach((cell) =>
        expect(cell.attrs.colwidth).toEqual([100])
      );
      expect(outer.firstChild!.firstChild!.child(1).toJSON()).toEqual(
        nestedBefore
      );
      expect(table.style.width).toBe("300px");
      expect(editor.commands.undo()).toBe(true);
      expect(editor.state.doc.firstChild!.attrs.layoutMode).toBe("auto");
      expect(editor.commands.redo()).toBe(true);
      expect(
        editor.state.doc.firstChild!.firstChild!.firstChild!.child(1).toJSON()
      ).toEqual(nestedBefore);
    }
  );

  it("keeps fixed layout column resizing independent of adjacent columns", () => {
    startDrag();
    window.dispatchEvent(new Event("blur"));
    editor!.commands.setTableLayout("fixed");
    const table = editor!.view.dom.querySelector("table")!;
    table.querySelector("td")!.dispatchEvent(
      new MouseEvent("mousedown", {
        bubbles: true,
        cancelable: true,
        clientX: 200,
      })
    );
    finish(250);
    const row = getTableNode(editor!).node.firstChild!;
    expect(row.child(0).attrs.colwidth).toEqual([250]);
    expect(row.child(1).attrs.colwidth).toEqual([200]);
    expect(table.style.width).toBe("650px");
  });

  it("resizes merged outer cells without modifying a nested table", () => {
    editor = createTableEditor(
      `<table><tr><td colspan="2"><p>Merged</p><table data-table-layout="fixed"><tr><td colwidth="60"><p>Nested</p></td></tr></table></td><td><p>End</p></td></tr><tr><td><p>A</p></td><td><p>B</p></td><td><p>C</p></td></tr></table>`
    );
    const table = editor.view.dom.querySelector("table")!;
    table.querySelectorAll(":scope > colgroup > col").forEach((col) => {
      vi.spyOn(col, "getBoundingClientRect").mockReturnValue({
        width: 200,
      } as DOMRect);
    });
    editor.view.dispatch(
      editor.state.tr.setMeta(columnResizingPluginKey, {
        setHandle: getCellPositions(editor)[0],
      })
    );
    table.querySelector("td")!.dispatchEvent(
      new MouseEvent("mousedown", {
        bubbles: true,
        cancelable: true,
        clientX: 200,
      })
    );
    finish(250);
    const outer = editor.state.doc.firstChild!;
    expect(outer.firstChild!.firstChild!.attrs.colwidth).toEqual([200, 250]);
    expect(outer.firstChild!.lastChild!.attrs.colwidth).toEqual([150]);
    expect(
      outer.firstChild!.firstChild!.child(1).firstChild!.firstChild!.attrs
        .colwidth
    ).toEqual([60]);
    expect(columnResizingPluginKey.getState(editor.state)!.dragging).toBe(
      false
    );
  });

  it("gives inserted columns a proportional share and preserves it after reload", () => {
    startDrag();
    finish(250);
    editor!.commands.addColumnAfter();
    expect(getTableNode(editor!).node.attrs.layoutMode).toBe("auto");
    const percentages = Array.from(
      editor!.view.dom.querySelectorAll("col"),
      (col) => Number.parseFloat(col.style.width)
    );
    expect(percentages).toHaveLength(4);
    expect(percentages.reduce((sum, width) => sum + width, 0)).toBeCloseTo(100);
    editor!.commands.setContent(editor!.getHTML());
    expect(getTableNode(editor!).node.attrs.layoutMode).toBe("auto");
    expect(
      Array.from(editor!.view.dom.querySelectorAll("col"), (col) =>
        Number.parseFloat(col.style.width)
      )
    ).toEqual(percentages);
  });

  it("removes drag listeners when the editor is destroyed", () => {
    startDrag();
    move(250);
    const dispatch = vi.spyOn(editor!.view, "dispatch");
    editor!.destroy();
    move(275);
    finish(275);
    expect(dispatch).not.toHaveBeenCalled();
  });

  it("does not overwrite document changes made during a drag", () => {
    const table = startDrag();
    move(250);
    editor!.commands.setTableLayout("fixed");
    finish(250);
    expect(getTableNode(editor!).node.attrs.layoutMode).toBe("fixed");
    expect(table.style.width).toBe("600px");
    expect(columnResizingPluginKey.getState(editor!.state)!.dragging).toBe(
      false
    );
  });

  it("preserves proportions when saved and reopened", () => {
    startDrag();
    finish(250);
    const html = editor!.getHTML();
    expect(html).toContain("<colgroup>");
    expect(html).toContain("width: 25%");
    editor!.commands.setContent(html);
    const { node } = getTableNode(editor!);
    expect(node.attrs.layoutMode).toBe("auto");
    expect(node.firstChild!.child(0).attrs.colwidth).toEqual([250]);
    expect(node.firstChild!.child(1).attrs.colwidth).toEqual([150]);
    expect(editor!.view.dom.querySelector("table")!.style.width).toBe("100%");
    expect(TableMap.get(node).width).toBe(3);
  });
});
