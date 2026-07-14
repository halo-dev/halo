// @vitest-environment jsdom

import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { PMNode } from "@/tiptap";
import type { EditorView } from "@/tiptap/pm";
import { ExtensionTable } from "./index";

describe("ExtensionTable horizontal wheel scrolling", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.runOnlyPendingTimers();
    vi.useRealTimers();
  });

  it("uses vertical wheel input while the table can scroll horizontally", () => {
    const { container, scrollBy } = createScrollableTable(200);
    const event = createWheelEvent(100);

    container.dispatchEvent(event);

    expect(event.defaultPrevented).toBe(true);
    expect(scrollBy).toHaveBeenCalledWith({ left: 100 });
  });

  it.each([
    ["left", 0, -100],
    ["right", 600, 100],
  ])(
    "lets the page consume wheel input at the %s boundary",
    (_boundary, scrollLeft, deltaY) => {
      const { container, scrollBy } = createScrollableTable(scrollLeft);
      const event = createWheelEvent(deltaY);

      container.dispatchEvent(event);

      expect(event.defaultPrevented).toBe(false);
      expect(scrollBy).not.toHaveBeenCalled();
    }
  );
});

function createScrollableTable(scrollLeft: number) {
  const TableView = ExtensionTable.options.View;
  if (!TableView) {
    throw new Error("Table view is not configured");
  }

  const tableView = new TableView(
    { firstChild: null } as unknown as PMNode,
    25,
    {} as EditorView
  );
  const container = (tableView.dom as HTMLElement).querySelector<HTMLElement>(
    ".tableWrapper"
  );
  if (!container) {
    throw new Error("Table scroll container was not rendered");
  }

  const scrollBy = vi.fn();
  Object.defineProperties(container, {
    scrollWidth: { configurable: true, value: 1000 },
    clientWidth: { configurable: true, value: 400 },
    scrollLeft: { configurable: true, value: scrollLeft },
    scrollBy: { configurable: true, value: scrollBy },
  });

  return { container, scrollBy };
}

function createWheelEvent(deltaY: number) {
  return new WheelEvent("wheel", {
    bubbles: true,
    cancelable: true,
    deltaY,
  });
}
