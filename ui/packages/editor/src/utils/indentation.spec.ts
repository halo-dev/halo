// @vitest-environment jsdom

import Document from "@tiptap/extension-document";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { ExtensionHistory } from "@/extensions/history";
import { ExtensionIndent } from "@/extensions/indent";
import { Editor, Node } from "@/tiptap";
import {
  findAncestorListItems,
  getBlockIndentAtSelection,
  prepareBlockCommandFromList,
} from "./indentation";

const ThirdPartyList = Node.create({
  name: "thirdPartyList",
  group: "block list",
  content: "thirdPartyListItem+",
  parseHTML: () => [{ tag: "div[data-third-party-list]" }],
  renderHTML: () => ["div", { "data-third-party-list": "" }, 0],
});

const ThirdPartyListItem = Node.create({
  name: "thirdPartyListItem",
  group: "listItem",
  content: "paragraph block*",
  parseHTML: () => [{ tag: "div[data-third-party-list-item]" }],
  renderHTML: () => ["div", { "data-third-party-list-item": "" }, 0],
});

describe("indentation utils", () => {
  const editors: Editor[] = [];

  afterEach(() => {
    for (const editor of editors) {
      editor.destroy();
    }
    editors.length = 0;
  });

  it("finds third-party list items by schema group", () => {
    const editor = createEditor();
    const textPos = findTextPosition(editor, "nested");
    editor.commands.setTextSelection(textPos);

    const items = findAncestorListItems(editor.state.selection.$from);

    expect(items).toHaveLength(2);
    expect(items.map(({ node }) => node.type.name)).toEqual([
      "thirdPartyListItem",
      "thirdPartyListItem",
    ]);
    expect(items[0].depth).toBeGreaterThan(items[1].depth);
  });

  it("converts list depth to configured and clamped block indentation", () => {
    const editor = createEditor({ indentRange: 32, maxIndentLevel: 48 });
    const textPos = findTextPosition(editor, "nested");
    editor.commands.setTextSelection(textPos);

    expect(getBlockIndentAtSelection(editor)).toBe(48);
  });

  it("prepares third-party list content for a block command", () => {
    const editor = createEditor({ indentRange: 32 });
    const textPos = findTextPosition(editor, "nested");
    editor.commands.setTextSelection(textPos + "nested".length);

    const range = prepareBlockCommandFromList(editor, {
      from: textPos,
      to: textPos + "nested".length,
    });

    expect(range.from).toBe(range.to);
    expect(findAncestorListItems(editor.state.selection.$from)).toHaveLength(0);
    expect(editor.state.selection.$from.parent.attrs.indent).toBe(64);
    expect(editor.commands.undo()).toBe(true);
    expect(findAncestorListItems(editor.state.selection.$from)).toHaveLength(2);
  });

  function createEditor(
    indentOptions: Parameters<typeof ExtensionIndent.configure>[0] = {}
  ) {
    const editor = new Editor({
      element: document.createElement("div"),
      extensions: [
        Document,
        Paragraph,
        Text,
        ThirdPartyList,
        ThirdPartyListItem,
        ExtensionIndent.configure(indentOptions),
        ExtensionHistory,
      ],
      content: {
        type: "doc",
        content: [
          {
            type: "thirdPartyList",
            content: [
              {
                type: "thirdPartyListItem",
                content: [
                  { type: "paragraph" },
                  {
                    type: "thirdPartyList",
                    content: [
                      {
                        type: "thirdPartyListItem",
                        content: [
                          {
                            type: "paragraph",
                            content: [{ type: "text", text: "nested" }],
                          },
                        ],
                      },
                    ],
                  },
                ],
              },
            ],
          },
        ],
      },
    });
    editors.push(editor);
    return editor;
  }
});

function findTextPosition(editor: Editor, text: string) {
  let result = -1;
  editor.state.doc.descendants((node, pos) => {
    if (node.isText && node.text === text) {
      result = pos;
      return false;
    }
    return result < 0;
  });
  if (result < 0) {
    throw new Error(`Unable to find text: ${text}`);
  }
  return result;
}
