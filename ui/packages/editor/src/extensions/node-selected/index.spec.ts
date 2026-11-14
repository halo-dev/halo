// @vitest-environment jsdom

import Document from "@tiptap/extension-document";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { Editor } from "@/tiptap";
import { ExtensionNodeSelected } from "./index";

const editors: Editor[] = [];

afterEach(() => {
  editors.splice(0).forEach((editor) => editor.destroy());
});

describe("node selected decorations", () => {
  it("shows selected nodes only while the editor is focused", () => {
    const editor = new Editor({
      extensions: [
        Document,
        Paragraph.extend({
          addAttributes() {
            return { selected: { default: false } };
          },
        }),
        Text,
        ExtensionNodeSelected,
      ],
      content: '<p selected="true">Selected</p>',
    });
    editors.push(editor);
    document.body.appendChild(editor.view.dom);

    const paragraph = editor.view.dom.querySelector("p");
    expect(editor.getJSON().content?.[0].attrs?.selected).toBeTruthy();
    expect(paragraph?.classList.contains("has-node-selected")).toBe(false);

    editor.view.focus();
    expect(editor.isFocused).toBe(true);
    expect(
      editor.view.dom
        .querySelector("p")
        ?.classList.contains("has-node-selected")
    ).toBe(true);

    editor.setEditable(false);
    expect(
      editor.view.dom
        .querySelector("p")
        ?.classList.contains("has-node-selected")
    ).toBe(false);
    editor.setEditable(true);
    expect(
      editor.view.dom
        .querySelector("p")
        ?.classList.contains("has-node-selected")
    ).toBe(true);

    editor.view.dom.blur();
    expect(
      editor.view.dom
        .querySelector("p")
        ?.classList.contains("has-node-selected")
    ).toBe(false);
  });
});
