// @vitest-environment jsdom

import Document from "@tiptap/extension-document";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { UndoRedo } from "@tiptap/extensions";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { Editor, Node } from "@/tiptap";
import { Slice } from "@/tiptap/pm";
import { ExtensionTable } from "./index";

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
