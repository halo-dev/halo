// @vitest-environment jsdom

import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Editor } from "@/tiptap";
import { HaloTableView } from "./table-view";
import { createTableEditor, getTableNode, insertTable } from "./test-editor";

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
    expect(view.dom.dataset.tableLayout).toBe("auto");
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
