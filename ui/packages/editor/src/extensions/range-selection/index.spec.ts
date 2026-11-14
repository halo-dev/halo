// @vitest-environment jsdom

import Document from "@tiptap/extension-document";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { Editor, Node } from "@/tiptap";
import { TextSelection } from "@/tiptap/pm";
import { ExtensionRangeSelection } from "./index";

const FakeSelectionBlock = Node.create({
  name: "fakeSelectionBlock",
  group: "block",
  atom: true,
  fakeSelection: true,
  parseHTML: () => [{ tag: "div[data-fake-selection-block]" }],
  renderHTML: () => ["div", { "data-fake-selection-block": "" }],
});

const editors: Editor[] = [];

afterEach(() => {
  editors.splice(0).forEach((editor) => editor.destroy());
});

describe("range selection decorations", () => {
  it("marks a complex block fully covered by a text selection", () => {
    const editor = new Editor({
      extensions: [
        Document,
        Paragraph,
        Text,
        FakeSelectionBlock,
        ExtensionRangeSelection,
      ],
      content: "<p>Before</p><div data-fake-selection-block></div><p>After</p>",
    });
    editors.push(editor);

    const block = editor.view.dom.querySelector("[data-fake-selection-block]");
    expect(block).not.toBeNull();
    expect(block?.classList.contains("range-fake-selection")).toBe(false);

    editor.view.dispatch(
      editor.state.tr.setSelection(
        TextSelection.create(
          editor.state.doc,
          1,
          editor.state.doc.content.size - 1
        )
      )
    );
    expect(block?.classList.contains("range-fake-selection")).toBe(true);

    editor.commands.setTextSelection(1);
    expect(block?.classList.contains("range-fake-selection")).toBe(false);
  });
});
