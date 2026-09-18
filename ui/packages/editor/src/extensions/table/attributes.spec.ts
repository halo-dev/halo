// @vitest-environment jsdom

import { describe, expect, it } from "vite-plus/test";
import {
  MAX_ROW_HEIGHT,
  MIN_ROW_HEIGHT,
  joinStyles,
  normalizeCellVerticalAlign,
  normalizeCssColor,
  normalizeRowHeight,
  normalizeTableLayoutMode,
  parseCellBackgroundColor,
  parseCellVerticalAlign,
  parseColumnWidths,
  parseRowHeight,
  parseTableLayoutMode,
} from "./attributes";

describe("table attributes", () => {
  it("normalizes the supported enum and dimension values", () => {
    expect(normalizeTableLayoutMode("auto")).toBe("auto");
    expect(normalizeTableLayoutMode("fluid")).toBeNull();
    expect(normalizeCellVerticalAlign("middle")).toBe("middle");
    expect(normalizeCellVerticalAlign("baseline")).toBeNull();
    expect(normalizeRowHeight("63.6px")).toBe(64);
    expect(normalizeRowHeight(1)).toBe(MIN_ROW_HEIGHT);
    expect(normalizeRowHeight(99999)).toBe(MAX_ROW_HEIGHT);
  });

  it("accepts portable colors and rejects executable or compound styles", () => {
    expect(normalizeCssColor(" #fee2e2 ")).toBe("#fee2e2");
    expect(normalizeCssColor("var(--table-cell-background)")).toBe(
      "var(--table-cell-background)"
    );
    expect(normalizeCssColor("red; position: fixed")).toBeNull();
    expect(normalizeCssColor("url(javascript:alert(1))")).toBeNull();
  });

  it("parses canonical and legacy table formatting", () => {
    const container = document.createElement("div");
    container.innerHTML = `
      <div data-table-layout="fixed">
        <table>
          <colgroup><col width="120"><col style="width: 80px"></colgroup>
          <tbody><tr style="height: 72px">
            <td colspan="2" style="vertical-align: bottom; background-color: rgb(1, 2, 3)"></td>
          </tr></tbody>
        </table>
      </div>`;
    const table = container.querySelector("table")!;
    const row = container.querySelector("tr")!;
    const cell = container.querySelector("td")!;

    expect(parseTableLayoutMode(table)).toBe("fixed");
    expect(parseRowHeight(row)).toBe(72);
    expect(parseCellVerticalAlign(cell)).toBe("bottom");
    expect(parseCellBackgroundColor(cell)).toBe("rgb(1, 2, 3)");
    expect(parseColumnWidths(cell)).toEqual([120, 80]);
  });

  it("joins style fragments into valid declarations", () => {
    expect(joinStyles("width: 100%", undefined, " table-layout: auto; ")).toBe(
      "width: 100%; table-layout: auto;"
    );
  });
});
