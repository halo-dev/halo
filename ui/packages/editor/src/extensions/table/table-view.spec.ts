// @vitest-environment jsdom

import Document from "@tiptap/extension-document";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vite-plus/test";
import { Editor } from "@/tiptap";
import { ExtensionIndent } from "../indent";
import { ExtensionTable } from "./index";
import { HaloTableView } from "./table-view";
import { createTableEditor, getTableNode, insertTable } from "./test-editor";

function createIndentedTableEditor() {
  return new Editor({
    element: document.createElement("div"),
    extensions: [Document, Paragraph, Text, ExtensionTable, ExtensionIndent],
    content: {
      type: "doc",
      content: [
        {
          type: "table",
          attrs: { indent: 24 },
          content: [
            {
              type: "tableRow",
              content: [
                { type: "tableCell", content: [{ type: "paragraph" }] },
              ],
            },
          ],
        },
      ],
    },
  });
}

describe("HaloTableView", () => {
  let editor: Editor | undefined;
  let resizeDisconnect: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    resizeDisconnect = vi.fn();
    vi.stubGlobal(
      "ResizeObserver",
      class {
        observe = vi.fn();
        disconnect = resizeDisconnect;
      }
    );
    vi.stubGlobal(
      "requestAnimationFrame",
      vi.fn(() => 1)
    );
    vi.stubGlobal("cancelAnimationFrame", vi.fn());
  });

  afterEach(() => {
    editor?.destroy();
    document.body.replaceChildren();
    vi.unstubAllGlobals();
  });

  it("applies the automatic responsive wrapper contract", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 1, cols: 2 });
    const view = new HaloTableView(getTableNode(editor).node, 25, editor.view);

    expect(view.dom.className).toBe("halo-table-wrapper");
    expect(view.dom.dataset.gapCursorClickArea).toBe("");
    expect(view.dom.dataset.tableLayout).toBe("auto");
    expect(view.table.dataset.gapCursorAnchor).toBe("");
    expect(view.dom.style.overflowX).toBe("auto");
    expect(view.table.style.width).toBe("100%");
    expect(view.table.style.tableLayout).toBe("auto");
    expect(Array.from(view.colgroup.children)).toHaveLength(2);
    expect(
      (view.colgroup.firstElementChild as HTMLTableColElement).style.minWidth
    ).toBe("25px");

    const canceledFrames = vi.mocked(cancelAnimationFrame).mock.calls.length;
    view.destroy();
    view.destroy();
    expect(resizeDisconnect).toHaveBeenCalledTimes(1);
    expect(cancelAnimationFrame).toHaveBeenCalledTimes(canceledFrames + 1);
  });

  it("leaves rendered indentation on the wrapper instead of the table", () => {
    const indentedEditor = createIndentedTableEditor();
    try {
      const node = getTableNode(indentedEditor).node;
      const view = new HaloTableView(node, 25, indentedEditor.view, {
        "data-indent": "24",
        style:
          "margin-left: 24px!important;max-width: calc(100% - 24px)!important;",
      });

      expect(view.table.style.marginLeft).toBe("");
      expect(view.table.style.maxWidth).toBe("");
      expect(view.table.style.width).toBe("100%");
      view.destroy();
    } finally {
      indentedEditor.destroy();
    }
  });

  it("contains horizontal wheel scrolling only while the table can move", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 1, cols: 2 });
    const view = new HaloTableView(getTableNode(editor).node, 25, editor.view);
    const scrollBy = vi.fn();
    Object.defineProperties(view.dom, {
      scrollWidth: { configurable: true, value: 500 },
      clientWidth: { configurable: true, value: 200 },
      scrollLeft: { configurable: true, value: 50, writable: true },
      scrollBy: { configurable: true, value: scrollBy },
    });

    const contained = new WheelEvent("wheel", {
      bubbles: true,
      cancelable: true,
      deltaY: 30,
    });
    view.dom.dispatchEvent(contained);
    expect(contained.defaultPrevented).toBe(true);
    expect(scrollBy).toHaveBeenCalledWith({ left: 30 });

    Object.defineProperty(view.dom, "scrollLeft", {
      configurable: true,
      value: 300,
    });
    const boundary = new WheelEvent("wheel", {
      cancelable: true,
      deltaY: 30,
    });
    view.dom.dispatchEvent(boundary);
    expect(boundary.defaultPrevented).toBe(false);
    expect(scrollBy).toHaveBeenCalledTimes(1);

    view.destroy();
  });
});
