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
