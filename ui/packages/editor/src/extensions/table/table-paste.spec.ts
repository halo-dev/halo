// @vitest-environment jsdom

import Document from "@tiptap/extension-document";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { UndoRedo } from "@tiptap/extensions";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { Editor, Node } from "@/tiptap";
import { CellSelection, Slice, TableMap } from "@/tiptap/pm";
import { ExtensionTable } from "./index";
import { getTableNode } from "./test-editor";

const TestCodeBlock = Node.create({
  name: "codeBlock",
  group: "block",
  content: "text*",
  marks: "",
  code: true,
  parseHTML: () => [{ tag: "pre" }],
  renderHTML: () => ["pre", 0],
});

function createPasteEditor(content = "<p></p>") {
  const element = document.createElement("div");
  document.body.appendChild(element);

  return new Editor({
    element,
    extensions: [
      Document,
      Paragraph,
      Text,
      UndoRedo,
      TestCodeBlock,
      ExtensionTable,
    ],
    content,
  });
}

function createPasteEvent(data: Record<string, string>) {
  return {
    clipboardData: {
      getData: (type: string) => data[type] ?? "",
    },
    preventDefault() {},
  } as unknown as ClipboardEvent;
}

function dispatchPaste(editor: Editor, event: ClipboardEvent) {
  // Mirror prosemirror-view's doPaste: first handlePaste prop to return
  // true wins.
  for (const plugin of editor.view.state.plugins) {
    const handlePaste = plugin.props.handlePaste;
    if (
      handlePaste &&
      handlePaste.call(plugin, editor.view, event, Slice.empty)
    ) {
      return true;
    }
  }
  return false;
}

describe("ExtensionTable tab-separated paste", () => {
  let editor: Editor | undefined;

  afterEach(() => {
    editor?.destroy();
    document.body.replaceChildren();
  });

  it("converts tab-separated text into a table in regular content", () => {
    editor = createPasteEditor();

    const handled = dispatchPaste(
      editor,
      createPasteEvent({ "text/plain": "A\tB\nC\tD" })
    );

    expect(handled).toBe(true);
    expect(editor.getHTML()).toContain("halo-table-wrapper");
    expect(editor.state.doc.textContent).toContain("A");
  });

  it("does not intercept tab-separated text pasted inside a code block", () => {
    editor = createPasteEditor("<pre>existing</pre>");
    editor.commands.setTextSelection(2);

    const before = editor.state.doc.toJSON();
    const handled = dispatchPaste(
      editor,
      createPasteEvent({ "text/plain": "const a = 1;\n\treturn a;" })
    );

    expect(handled).toBe(false);
    expect(editor.state.doc.toJSON()).toEqual(before);
  });

  it("does not intercept code copied from VSCode", () => {
    editor = createPasteEditor();

    const before = editor.state.doc.toJSON();
    const handled = dispatchPaste(
      editor,
      createPasteEvent({
        "text/plain": "if (a) {\n\treturn b;\n}",
        "vscode-editor-data": JSON.stringify({ mode: "typescript" }),
      })
    );

    expect(handled).toBe(false);
    expect(editor.state.doc.toJSON()).toEqual(before);
  });
});

describe("ExtensionTable cross-table paste", () => {
  const editors: Editor[] = [];
  const sourceHTML =
    '<table data-table-layout="auto"><colgroup><col style="width: 20%"><col style="width: 80%"></colgroup><tr><td style="background-color: #ff0000"><p>Copied</p></td><td><p>Other</p></td></tr></table>';

  function createEditor(content = "<p></p>") {
    const editor = createPasteEditor(content);
    editors.push(editor);
    return editor;
  }

  function selectCells(editor: Editor, lastColumn = 0) {
    const { node, pos } = getTableNode(editor);
    const map = TableMap.get(node);
    editor.view.dispatch(
      editor.state.tr.setSelection(
        CellSelection.create(
          editor.state.doc,
          pos + 1 + map.map[0],
          pos + 1 + map.map[lastColumn]
        )
      )
    );
  }

  function copyCell(source: Editor) {
    selectCells(source);
    return source.view.serializeForClipboard(source.state.selection.content())
      .dom.innerHTML;
  }

  function pasteHTML(editor: Editor, html: string) {
    expect(
      editor.view.pasteHTML(html, createPasteEvent({ "text/html": html }))
    ).toBe(true);
  }

  afterEach(() => {
    editors.splice(0).forEach((editor) => editor.destroy());
    document.body.replaceChildren();
  });

  it.each(
    ["fixed", "auto"].flatMap((layout) =>
      [1, 2].flatMap((rows) =>
        ["cell", "text"].map((selection) => ({ layout, rows, selection }))
      )
    )
  )(
    "preserves $layout widths when pasting into a $selection selection in a $rows-row table",
    ({ layout, rows, selection }) => {
      const source = createEditor(sourceHTML);
      const html = copyCell(source);
      const editor = createEditor(
        `<table data-table-layout="${layout}">${'<tr><td colwidth="100"><p>A</p></td><td colwidth="300"><p>B</p></td></tr>'.repeat(rows)}</table>`
      );
      if (selection === "cell") {
        selectCells(editor);
      } else {
        editor.commands.setTextSelection(4);
      }
      const before = editor.getJSON();

      pasteHTML(editor, html);

      const { node } = getTableNode(editor);
      node.forEach((row) => {
        expect(row.child(0).attrs.colwidth).toEqual([100]);
        expect(row.child(1).attrs.colwidth).toEqual([300]);
      });
      expect(node.firstChild!.firstChild!.textContent).toBe("Copied");
      expect(node.firstChild!.firstChild!.attrs.backgroundColor).toBe(
        "rgb(255, 0, 0)"
      );
      const table = editor.view.dom.querySelector("table")!;
      expect(table.style.width).toBe(layout === "fixed" ? "400px" : "100%");
      expect(
        Array.from(table.querySelectorAll("col"), (col) => col.style.width)
      ).toEqual(layout === "fixed" ? ["100px", "300px"] : ["25%", "75%"]);
      const after = editor.getJSON();
      expect(editor.commands.undo()).toBe(true);
      expect(editor.getJSON()).toEqual(before);
      expect(editor.commands.redo()).toBe(true);
      expect(editor.getJSON()).toEqual(after);
      editor.commands.setContent(editor.getHTML());
      expect(editor.getJSON()).toEqual(after);
    }
  );

  it.each(["fixed", "auto"])(
    "keeps unspecified %s target widths instead of importing foreign weights",
    (layout) => {
      const html = copyCell(createEditor(sourceHTML));
      const editor = createEditor(
        `<table data-table-layout="${layout}"><tr><td><p>A</p></td><td><p>B</p></td></tr></table>`
      );
      selectCells(editor);

      pasteHTML(editor, html);

      const { node } = getTableNode(editor);
      expect(node.firstChild!.firstChild!.textContent).toBe("Copied");
      node.firstChild!.forEach((cell) =>
        expect(cell.attrs.colwidth).toBeNull()
      );
    }
  );

  it.each(["fixed", "auto"])(
    "uses each %s destination column's width when repeating a pasted cell",
    (layout) => {
      const html = copyCell(createEditor(sourceHTML));
      const editor = createEditor(
        `<table data-table-layout="${layout}"><tr><td colwidth="100"><p>A</p></td><td colwidth="300"><p>B</p></td></tr></table>`
      );
      selectCells(editor, 1);

      pasteHTML(editor, html);

      const row = getTableNode(editor).node.firstChild!;
      expect(row.child(0).attrs.colwidth).toEqual([100]);
      expect(row.child(1).attrs.colwidth).toEqual([300]);
      expect(row.textContent).toBe("CopiedCopied");
    }
  );

  it("preserves destination widths through rowspans, colspans and table growth", () => {
    const editor = createEditor(
      '<table data-table-layout="auto"><tr><td colwidth="100"><p>A</p></td><td colwidth="300"><p>B</p></td></tr></table>'
    );
    editor.commands.setTextSelection(4);

    pasteHTML(
      editor,
      '<table data-table-layout="auto"><tr><td rowspan="2" colwidth="2000"><p>X</p></td><td colspan="2" colwidth="3000,5000"><p>Y</p></td></tr><tr><td colwidth="3000"><p>Z</p></td><td colwidth="5000"><p>W</p></td></tr></table>'
    );

    const { node } = getTableNode(editor);
    expect(TableMap.get(node).width).toBe(3);
    expect(TableMap.get(node).height).toBe(2);
    expect(TableMap.get(node).problems).toBeNull();
    expect(node.child(0).child(0).attrs).toMatchObject({
      rowspan: 2,
      colwidth: [100],
    });
    expect(node.child(0).child(1).attrs).toMatchObject({
      colspan: 2,
      colwidth: [300, 0],
    });
    expect(node.child(1).child(0).attrs.colwidth).toEqual([300]);
    expect(node.child(1).child(1).attrs.colwidth).toBeNull();
    expect(node.textContent).toBe("XYZW");
  });

  it("leaves nested table widths intact inside a pasted cell", () => {
    const source = createEditor(
      '<table data-table-layout="auto"><tr><td colwidth="2000"><p>Copied</p><table data-table-layout="auto"><tr><td colwidth="1"><p>Inner A</p></td><td colwidth="3"><p>Inner B</p></td></tr></table></td></tr></table>'
    );
    const html = copyCell(source);
    const editor = createEditor(
      '<table data-table-layout="fixed"><tr><td colwidth="100"><p>A</p></td></tr></table>'
    );
    selectCells(editor);

    pasteHTML(editor, html);

    const cell = getTableNode(editor).node.firstChild!.firstChild!;
    expect(cell.attrs.colwidth).toEqual([100]);
    const nestedRow = cell.child(1).firstChild!;
    expect(nestedRow.child(0).attrs.colwidth).toEqual([1]);
    expect(nestedRow.child(1).attrs.colwidth).toEqual([3]);
  });

  it("uses the selected nested table's column widths without changing its parent", () => {
    const html = copyCell(createEditor(sourceHTML));
    const editor = createEditor(
      '<table data-table-layout="fixed"><tr><td colwidth="600"><p>Outer</p><table data-table-layout="auto"><tr><td colwidth="1"><p>Inner A</p></td><td colwidth="3"><p>Inner B</p></td></tr></table></td></tr></table>'
    );
    let target = 0;
    editor.state.doc.descendants((node, pos) => {
      if (
        node.type.spec.tableRole === "cell" &&
        node.textContent === "Inner B"
      ) {
        target = pos;
        return false;
      }
    });
    editor.commands.setCellSelection({ anchorCell: target });

    pasteHTML(editor, html);

    const cell = getTableNode(editor).node.firstChild!.firstChild!;
    expect(cell.attrs.colwidth).toEqual([600]);
    const nestedRow = cell.child(1).firstChild!;
    expect(nestedRow.child(0).attrs.colwidth).toEqual([1]);
    expect(nestedRow.child(1).attrs.colwidth).toEqual([3]);
    expect(nestedRow.child(1).textContent).toBe("Copied");
  });

  it("keeps automatic proportions when pasting cells from a fixed table", () => {
    const source = createEditor(
      '<table data-table-layout="fixed"><tr><td colwidth="400"><p>Copied</p></td></tr></table>'
    );
    const editor = createEditor(
      '<table data-table-layout="auto"><tr><td colwidth="1"><p>A</p></td><td colwidth="3"><p>B</p></td></tr></table>'
    );
    selectCells(editor);

    pasteHTML(editor, copyCell(source));

    const row = getTableNode(editor).node.firstChild!;
    expect(row.child(0).attrs.colwidth).toEqual([1]);
    expect(row.child(1).attrs.colwidth).toEqual([3]);
    expect(row.child(0).textContent).toBe("Copied");
  });

  it("preserves column widths when pasting tab-separated text into a table", () => {
    const editor = createEditor(
      '<table data-table-layout="fixed"><tr><td colwidth="100"><p>A</p></td><td colwidth="300"><p>B</p></td></tr></table>'
    );
    selectCells(editor, 1);

    expect(
      dispatchPaste(editor, createPasteEvent({ "text/plain": "X\tY" }))
    ).toBe(true);

    const row = getTableNode(editor).node.firstChild!;
    expect(row.child(0).attrs.colwidth).toEqual([100]);
    expect(row.child(1).attrs.colwidth).toEqual([300]);
    expect(row.textContent).toBe("XY");
  });

  it("retains source proportions when inserting a whole table into regular content", () => {
    const source = createEditor(sourceHTML);
    const editor = createEditor();

    pasteHTML(editor, source.getHTML());

    const { node } = getTableNode(editor);
    expect(node.attrs.layoutMode).toBe("auto");
    expect(node.firstChild!.child(0).attrs.colwidth).toEqual([2000]);
    expect(node.firstChild!.child(1).attrs.colwidth).toEqual([8000]);
    expect(
      Array.from(
        editor.view.dom.querySelectorAll("col"),
        (col) => col.style.width
      )
    ).toEqual(["20%", "80%"]);
  });
});
