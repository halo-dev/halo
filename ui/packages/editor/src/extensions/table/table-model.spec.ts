// @vitest-environment jsdom

import { afterEach, describe, expect, it } from "vite-plus/test";
import type { Editor } from "@/tiptap";
import { TableMap } from "@/tiptap/pm";
import { sanitizePastedTableHTML, transformPastedTableHTML } from "./index";
import { createTableEditor, getTableNode, insertTable } from "./test-editor";

describe("ExtensionTable model and HTML contract", () => {
  let editor: Editor | undefined;

  afterEach(() => {
    editor?.destroy();
    document.body.replaceChildren();
  });

  it("inserts an automatic table that fills its container without fixed widths", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: 3, withHeaderRow: true });

    const { node } = getTableNode(editor);
    const html = editor.getHTML();

    expect(node.attrs.layoutMode).toBe("auto");
    expect(TableMap.get(node).width).toBe(3);
    node.descendants((child) => {
      if (child.type.spec.tableRole?.includes("cell")) {
        expect(child.attrs.colwidth).toBeNull();
      }
    });
    expect(html).toContain('class="halo-table-wrapper"');
    expect(html).toContain('data-table-layout="auto"');
    expect(html).toContain("width: 100%");
    expect(html).not.toContain("<colgroup>");
  });

  it("materializes widths for fixed layout and clears them when fitting", () => {
    editor = createTableEditor();
    insertTable(editor, { rows: 2, cols: 2 });

    expect(editor.commands.setTableLayout("fixed")).toBe(true);
    let { node } = getTableNode(editor);
    expect(node.attrs.layoutMode).toBe("fixed");
    expect(editor.getHTML()).toContain("<colgroup>");
    expect(editor.getHTML()).toContain("table-layout: fixed");

    expect(editor.commands.fitTableToWidth()).toBe(true);
    node = getTableNode(editor).node;
    expect(node.attrs.layoutMode).toBe("auto");
    node.descendants((child) => {
      if (child.type.spec.tableRole?.includes("cell")) {
        expect(child.attrs.colwidth).toBeNull();
      }
    });
    expect(editor.getHTML()).not.toContain("<colgroup>");
  });

  it("normalizes legacy wrappers, colgroup widths, row height, and cell formatting", () => {
    editor = createTableEditor(`
      <div class="table-wrapper">
        <table style="table-layout: fixed">
          <colgroup><col width="140"><col width="90"></colgroup>
          <tbody>
            <tr style="height: 66px">
              <th style="vertical-align: middle; background-color: #fee2e2"><p>A</p></th>
              <th><p>B</p></th>
            </tr>
          </tbody>
        </table>
      </div>`);

    const { node } = getTableNode(editor);
    const firstRow = node.firstChild!;
    const firstCell = firstRow.firstChild!;
    const html = editor.getHTML();

    expect(node.attrs.layoutMode).toBe("fixed");
    expect(firstRow.attrs.rowHeight).toBe(66);
    expect(firstCell.attrs.colwidth).toEqual([140]);
    expect(firstCell.attrs.verticalAlign).toBe("middle");
    expect(firstCell.attrs.backgroundColor).toBe("rgb(254, 226, 226)");
    expect(html.match(/halo-table-wrapper/g)).toHaveLength(1);
    expect(html).toContain('data-row-height="66"');
    expect(html).toContain('data-vertical-align="middle"');
  });

  it.each([
    { source: "load", attribute: "style", widths: [20, 80] },
    { source: "paste", attribute: "width", widths: [20, 80] },
    { source: "load", attribute: "width", widths: [0.5, 99.5] },
    { source: "paste", attribute: "style", widths: [0.5, 99.5] },
  ])(
    "preserves percentage-only colgroups from $source using $attribute: $widths",
    ({ source, attribute, widths }) => {
      const columns = widths
        .map((width) =>
          attribute === "style"
            ? `<col style="width: ${width}%">`
            : `<col width="${width}%">`
        )
        .join("");
      const html = `<div data-table-layout="auto"><table><colgroup>${columns}</colgroup><tr><th><p>A</p></th><th><p>B</p></th></tr><tr><td><p>C</p></td><td><p>D</p></td></tr></table></div>`;
      editor = createTableEditor(source === "load" ? html : "<p></p>");
      if (source === "paste") {
        expect(
          editor.view.pasteHTML(html, new Event("paste") as ClipboardEvent)
        ).toBe(true);
      }

      for (let roundtrip = 0; roundtrip < 2; roundtrip++) {
        const { node } = getTableNode(editor);
        expect(node.attrs.layoutMode).toBe("auto");
        node.forEach((row) => {
          row.forEach((cell) => {
            expect(cell.attrs.colwidth).toHaveLength(1);
            expect(Number.isInteger(cell.attrs.colwidth[0])).toBe(true);
            expect(cell.attrs.colwidth[0]).toBeGreaterThan(0);
          });
        });
        const table = editor.view.dom.querySelector("table")!;
        expect(table.style.width).toBe("100%");
        expect(
          Array.from(table.querySelectorAll("col"), (col) =>
            Number.parseFloat(col.style.width)
          )
        ).toEqual(widths);
        const saved = document.createElement("div");
        saved.innerHTML = editor.getHTML();
        expect(
          Array.from(saved.querySelectorAll("col"), (col) =>
            Number.parseFloat(col.style.width)
          )
        ).toEqual(widths);
        editor.commands.setContent(editor.getHTML());
      }
    }
  );

  it("imports merged percentage columns without using nested cell weights", () => {
    editor = createTableEditor(
      `<table data-table-layout="auto"><colgroup><col style="width: 20%"><col style="width: 30%"><col style="width: 50%"></colgroup><tr><td colspan="2"><p>Merged</p><table data-table-layout="fixed"><tr><td colwidth="60"><p>Nested</p></td></tr></table></td><td><p>End</p></td></tr></table>`
    );
    const before = getTableNode(editor).node;
    expect(before.firstChild!.firstChild!.attrs.colwidth).toHaveLength(2);
    const outer = editor.view.dom.querySelector("table")!;
    expect(
      Array.from(
        outer.querySelectorAll<HTMLTableColElement>(":scope > colgroup > col"),
        (col) => Number.parseFloat(col.style.width)
      )
    ).toEqual([20, 30, 50]);
    expect(
      before.firstChild!.firstChild!.child(1).firstChild!.firstChild!.attrs
        .colwidth
    ).toEqual([60]);
    editor.commands.setContent(editor.getHTML());
    expect(getTableNode(editor).node.eq(before)).toBe(true);
  });

  it("removes unsafe pasted markup while preserving table content", () => {
    const sanitized = sanitizePastedTableHTML(
      '<table onclick="alert(1)"><tr><td><script>bad()</script><a href="javascript:bad()">safe</a></td></tr></table>'
    );

    expect(sanitized).toContain("<table>");
    expect(sanitized).toContain("safe");
    expect(sanitized).not.toContain("script");
    expect(sanitized).not.toContain("onclick");
    expect(sanitized).not.toContain("javascript:");
  });

  it("sanitizes only pasted HTML that contains a table", () => {
    const iframe = '<iframe src="https://example.com/embed"></iframe>';

    expect(transformPastedTableHTML(iframe)).toBe(iframe);
    expect(transformPastedTableHTML("<p>hello</p>")).toBe("<p>hello</p>");
    expect(
      transformPastedTableHTML(
        "<TABLE><tr><td><script>bad()</script></td></tr></TABLE>"
      )
    ).not.toContain("script");
  });
});
