// @vitest-environment jsdom

import Document from "@tiptap/extension-document";
import {
  BulletList,
  ListItem,
  OrderedList,
  TaskItem,
  TaskList,
} from "@tiptap/extension-list";
import TiptapParagraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { afterEach, describe, expect, it } from "vitest";
import {
  getAdjacentBlockIndent,
  insertCommandBlockAfter,
} from "@/components/drag/insert-command-block";
import {
  createHaloEditorManifest,
  getHaloEditorIndentationSettings,
} from "@/editor-metadata";
import { Editor, Node, type JSONContent } from "@/tiptap";
import { NodeSelection, TextSelection } from "@/tiptap/pm";
import { prepareBlockCommandFromList } from "@/utils";
import { ExtensionCodeBlock } from "../code-block";
import { setCodeBlockWithIndent } from "../code-block/set-code-block-with-indent";
import { ExtensionColumns } from "../columns";
import { ExtensionDetails } from "../details";
import { ExtensionGapCursor, HaloGapCursor } from "../gap-cursor";
import { ExtensionHistory } from "../history";
import { ExtensionTable } from "../table";
import { ExtensionIndent, type ExtensionIndentOptions } from "./index";

const Paragraph = TiptapParagraph.extend({
  haloEditorIndentation: {
    legacyLineIndent: true,
  },
});

const TestCard = Node.create({
  name: "testCard",
  group: "block",
  atom: true,
  parseHTML: () => [{ tag: "div[data-test-card]" }],
  renderHTML: ({ HTMLAttributes }) => [
    "div",
    { ...HTMLAttributes, "data-test-card": "" },
  ],
  addNodeView() {
    return () => {
      const dom = document.createElement("div");
      dom.setAttribute("data-test-card", "");
      return { dom };
    };
  },
});

const TestImage = Node.create({
  name: "image",
  group: "block",
  atom: true,
  parseHTML: () => [{ tag: "div[data-test-image]" }],
  renderHTML: ({ HTMLAttributes }) => [
    "div",
    { ...HTMLAttributes, "data-test-image": "" },
  ],
  addNodeView() {
    return () => {
      const dom = document.createElement("div");
      dom.className = "w-full";
      dom.setAttribute("data-test-image", "");
      return { dom };
    };
  },
});

const TestFixedCard = Node.create({
  name: "testFixedCard",
  group: "block",
  atom: true,
  haloEditorIndentation: false,
  parseHTML: () => [{ tag: "div[data-test-fixed-card]" }],
  renderHTML: () => ["div", { "data-test-fixed-card": "" }],
});

const editors: Editor[] = [];

afterEach(() => {
  editors.splice(0).forEach((editor) => editor.destroy());
});

describe("ExtensionIndent", () => {
  it("discovers third-party block nodes without a configured name list", () => {
    const editor = createCardEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new HaloGapCursor(editor.state.doc.resolve(0), "before")
      )
    );

    expect(editor.commands.indent()).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "testCard",
      attrs: { indent: 24 },
    });
    const component = createHaloEditorManifest(editor).components.find(
      ({ name }) => name === "testCard"
    );
    expect(component?.attributes.map(({ name }) => name)).toContain("indent");
  });

  it("lets a third-party block opt out through node metadata", () => {
    const editor = new Editor({
      element: document.createElement("div"),
      extensions: [
        Document,
        Paragraph,
        Text,
        TestFixedCard,
        ExtensionIndent,
        ExtensionGapCursor,
      ],
      content: {
        type: "doc",
        content: [{ type: "testFixedCard" }, { type: "paragraph" }],
      },
    });
    editors.push(editor);
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new HaloGapCursor(editor.state.doc.resolve(0), "before")
      )
    );

    expect(editor.commands.indent()).toBe(false);
    expect(
      editor.schema.nodes.testFixedCard.spec.attrs?.indent
    ).toBeUndefined();
  });

  it("honors custom indentation bounds and derives the default maximum", () => {
    const derivedEditor = createEditor(
      {
        content: { type: "doc", content: [{ type: "paragraph" }] },
      },
      { indentRange: 32 }
    );
    expect(getHaloEditorIndentationSettings(derivedEditor)).toMatchObject({
      indentRange: 32,
      minIndentLevel: 0,
      maxIndentLevel: 320,
      defaultIndentLevel: 0,
    });

    const boundedEditor = createEditor(
      {
        content: { type: "doc", content: [{ type: "paragraph" }] },
      },
      {
        indentRange: 32,
        minIndentLevel: 16,
        maxIndentLevel: 80,
        defaultIndentLevel: 16,
      }
    );
    boundedEditor.commands.setTextSelection(1);

    expect(runKey(boundedEditor, "Tab")).toBe(true);
    expect(boundedEditor.getJSON().content?.[0].attrs).toMatchObject({
      indent: 48,
    });
    expect(runKey(boundedEditor, "Tab")).toBe(true);
    expect(runKey(boundedEditor, "Tab")).toBe(true);
    expect(boundedEditor.getJSON().content?.[0].attrs).toMatchObject({
      indent: 80,
    });
    expect(runKey(boundedEditor, "Backspace")).toBe(true);
    expect(boundedEditor.getJSON().content?.[0].attrs).toMatchObject({
      indent: 48,
    });
  });

  it("keeps legacy margin-left HTML readable and emits a stable indent attribute", () => {
    const editor = createEditor({
      content: '<p style="margin-left: 48px">content</p>',
    });

    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 48 });
    expect(editor.getHTML()).toContain('data-indent="48"');
    expect(editor.getHTML()).toContain("margin-left: 48px");
  });

  it("merges configured HTML attributes with serialized indentation", () => {
    const editor = createEditor(
      {
        content: {
          type: "doc",
          content: [{ type: "paragraph", attrs: { indent: 24 } }],
        },
      },
      { HTMLAttributes: { class: "indentable", "data-scope": "plugin" } }
    );

    expect(editor.getHTML()).toContain('class="indentable"');
    expect(editor.getHTML()).toContain('data-scope="plugin"');
    expect(editor.getHTML()).toContain('data-indent="24"');
    expect(editor.getHTML()).toContain("margin-left: 24px");
  });

  it("does not carry first-line indentation to a paragraph created with Enter", () => {
    const editor = createEditor({
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            attrs: { lineIndent: true },
            content: [{ type: "text", text: "first" }],
          },
        ],
      },
    });
    editor.commands.setTextSelection(6);

    expect(runKey(editor, "Enter")).toBe(true);
    expect(editor.getJSON().content).toEqual([
      expect.objectContaining({
        type: "paragraph",
        attrs: expect.objectContaining({ lineIndent: true }),
      }),
      expect.objectContaining({
        type: "paragraph",
        attrs: expect.objectContaining({ lineIndent: false }),
      }),
    ]);
  });

  it("uses block indentation for Tab and carries it to the next paragraph", () => {
    const editor = createEditor({
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            content: [{ type: "text", text: "first" }],
          },
        ],
      },
    });
    editor.commands.setTextSelection(1);

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({
      indent: 24,
      lineIndent: false,
    });

    editor.commands.setTextSelection(6);
    expect(runKey(editor, "Enter")).toBe(true);
    expect(editor.getJSON().content).toEqual([
      expect.objectContaining({
        type: "paragraph",
        attrs: expect.objectContaining({ indent: 24 }),
      }),
      expect.objectContaining({
        type: "paragraph",
        attrs: expect.objectContaining({ indent: 24 }),
      }),
    ]);
  });

  it("clears paragraph indentation when an input rule converts it to a list", () => {
    const editor = createListEditor({
      type: "doc",
      content: [
        {
          type: "paragraph",
          attrs: { indent: 48 },
          content: [{ type: "text", text: "-" }],
        },
      ],
    });
    editor.commands.setTextSelection(2);

    expect(runTextInput(editor, " ")).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "bulletList",
      content: [
        {
          type: "listItem",
          content: [
            {
              type: "paragraph",
              attrs: { indent: 0, lineIndent: false },
            },
          ],
        },
      ],
    });
    expect(editor.getHTML()).not.toContain("margin-left");
  });

  it.each([
    ["ordered list", "toggleOrderedList", "orderedList"],
    ["task list", "toggleTaskList", "taskList"],
  ] as const)(
    "does not combine block indentation with a %s",
    (_label, command, listName) => {
      const editor = createListEditor({
        type: "doc",
        content: [
          {
            type: "paragraph",
            attrs: { indent: 48 },
            content: [{ type: "text", text: "item" }],
          },
        ],
      });
      editor.commands.setTextSelection(1);

      expect(editor.commands[command]()).toBe(true);
      expect(editor.getJSON().content?.[0]).toMatchObject({
        type: listName,
        content: [
          {
            content: [
              {
                type: "paragraph",
                attrs: { indent: 0, lineIndent: false },
              },
            ],
          },
        ],
      });
    }
  );

  it("turns nested list depth into an equally indented block command", () => {
    const editor = createCodeBlockEditor(
      [
        {
          type: "bulletList",
          content: [
            {
              type: "listItem",
              content: [
                { type: "paragraph", content: [{ type: "text", text: "a" }] },
                {
                  type: "bulletList",
                  content: [
                    {
                      type: "listItem",
                      content: [
                        {
                          type: "paragraph",
                          content: [{ type: "text", text: "/" }],
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
      32
    );
    const slashPos = findTextPosition(editor, "/");
    editor.commands.setTextSelection(slashPos + 1);
    const originalContent = editor.getJSON();

    const range = prepareBlockCommandFromList(editor, {
      from: slashPos,
      to: slashPos + 1,
    });
    expect(setCodeBlockWithIndent(editor, range)).toBe(true);

    expect(editor.getJSON().content?.at(-1)).toMatchObject({
      type: "codeBlock",
      attrs: { indent: 64 },
    });
    expect(editor.commands.undo()).toBe(true);
    expect(editor.getJSON()).toEqual(originalContent);
    expect(editor.commands.undo()).toBe(false);
  });

  it("aligns a block inserted after a list to one visual list level", () => {
    const editor = createListEditor(
      {
        type: "doc",
        content: [
          {
            type: "bulletList",
            content: [
              {
                type: "listItem",
                content: [{ type: "paragraph" }],
              },
            ],
          },
        ],
      },
      32
    );
    const list = editor.state.doc.firstChild;

    expect(list && getAdjacentBlockIndent(editor, list)).toBe(32);
    expect(list && insertCommandBlockAfter(editor, list, 0)).toBe(true);
    expect(editor.getJSON().content?.at(-1)).toMatchObject({
      type: "paragraph",
      attrs: { indent: 32, lineIndent: false },
      content: [{ type: "text", text: "/" }],
    });
  });

  it("indents the whole paragraph instead of inserting a tab character", () => {
    const editor = createEditor({
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            content: [{ type: "text", text: "content" }],
          },
        ],
      },
    });
    editor.commands.setTextSelection(4);

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.state.doc.textContent).toBe("content");
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 24 });
  });

  it("uses the same indentation step for Tab and Backspace", () => {
    const editor = createEditor({
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            content: [{ type: "text", text: "content" }],
          },
        ],
      },
    });
    editor.commands.setTextSelection(1);

    expect(runKey(editor, "Tab")).toBe(true);
    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 48 });
    expect(runKey(editor, "Backspace")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 24 });
    expect(runKey(editor, "Backspace")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 0 });
  });

  it("indents and outdents every text block in a selection", () => {
    const editor = createEditor({
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            content: [{ type: "text", text: "one" }],
          },
          {
            type: "paragraph",
            content: [{ type: "text", text: "two" }],
          },
        ],
      },
    });
    editor.commands.setTextSelection({ from: 1, to: 9 });

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content).toEqual([
      expect.objectContaining({
        attrs: expect.objectContaining({ indent: 24 }),
      }),
      expect.objectContaining({
        attrs: expect.objectContaining({ indent: 24 }),
      }),
    ]);
    expect(runKey(editor, "Tab", true)).toBe(true);
    expect(editor.getJSON().content).toEqual([
      expect.objectContaining({
        attrs: expect.objectContaining({ indent: 0 }),
      }),
      expect.objectContaining({
        attrs: expect.objectContaining({ indent: 0 }),
      }),
    ]);
  });

  it("indents text inside details without moving the details wrapper", () => {
    const editor = createDetailsEditor();
    const summaryPos = findNodePosition(editor, "detailsSummary");
    const contentParagraphPos = findNodePosition(
      editor,
      "paragraph",
      "Details content"
    );

    editor.commands.setTextSelection(summaryPos + 1);
    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "details",
      attrs: { indent: 0 },
      content: [
        {
          type: "detailsSummary",
          attrs: { indent: 24 },
        },
        {
          type: "detailsContent",
          content: [
            {
              type: "paragraph",
              attrs: { indent: 0 },
            },
          ],
        },
      ],
    });

    editor.commands.setTextSelection(contentParagraphPos + 1);
    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      attrs: { indent: 0 },
      content: [
        {
          attrs: { indent: 24 },
        },
        {
          content: [
            {
              attrs: { indent: 24 },
            },
          ],
        },
      ],
    });
    expect(editor.getHTML()).toContain('<summary data-indent="24"');
  });

  it("keeps Tab and Shift-Tab inside the editor at indentation limits", () => {
    const editor = createCardEditor({
      type: "doc",
      content: [
        { type: "testCard", attrs: { indent: 240 } },
        { type: "paragraph" },
      ],
    });
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new HaloGapCursor(editor.state.doc.resolve(0), "before")
      )
    );

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content).toHaveLength(2);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 240 });

    const paragraphEditor = createEditor({
      content: { type: "doc", content: [{ type: "paragraph" }] },
    });
    paragraphEditor.commands.setTextSelection(1);
    expect(runKey(paragraphEditor, "Tab", true)).toBe(true);
    expect(paragraphEditor.getJSON().content?.[0].attrs).toMatchObject({
      indent: 0,
    });
  });

  it("removes first-line indentation with Backspace at the beginning", () => {
    const editor = createEditor({
      content: {
        type: "doc",
        content: [
          {
            type: "paragraph",
            attrs: { lineIndent: true },
            content: [{ type: "text", text: "first" }],
          },
        ],
      },
    });
    editor.commands.setTextSelection(1);

    expect(runKey(editor, "Backspace")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({
      lineIndent: false,
    });
  });

  it("indents the block on the visual side of a gap cursor", () => {
    const editor = createCardEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new HaloGapCursor(editor.state.doc.resolve(0), "before")
      )
    );

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 24 });
    expect(editor.state.selection).toBeInstanceOf(HaloGapCursor);

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 48 });
    expect(runKey(editor, "Backspace")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 24 });
    expect(runKey(editor, "Delete")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 0 });
  });

  it("consumes Tab without indenting from a trailing gap cursor", () => {
    const editor = createCardEditor();
    const card = editor.state.doc.firstChild;
    if (!card) {
      throw new Error("Expected a structural block");
    }
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new HaloGapCursor(editor.state.doc.resolve(card.nodeSize), "after")
      )
    );
    const original = editor.getJSON();

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON()).toEqual(original);
    expect(editor.state.selection).toBeInstanceOf(HaloGapCursor);
    expect(editor.commands.indent()).toBe(false);
  });

  it("inherits indentation when an indented paragraph becomes a block node", () => {
    const editor = createCardEditor({
      type: "doc",
      content: [
        {
          type: "paragraph",
          attrs: { indent: 48 },
          content: [{ type: "text", text: "/" }],
        },
      ],
    });
    editor.commands.setTextSelection(2);

    expect(
      editor
        .chain()
        .deleteRange({ from: 1, to: 2 })
        .insertContent({ type: "testCard" })
        .run()
    ).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "testCard",
      attrs: { indent: 48 },
    });

    expect(editor.commands.undo()).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "paragraph",
      attrs: { indent: 48 },
      content: [{ type: "text", text: "/" }],
    });
    expect(editor.commands.redo()).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "testCard",
      attrs: { indent: 48 },
    });
  });

  it("does not leak indentation to an insertion outside the active block", () => {
    const editor = createCardEditor({
      type: "doc",
      content: [
        {
          type: "paragraph",
          attrs: { indent: 48 },
          content: [{ type: "text", text: "active" }],
        },
        {
          type: "paragraph",
          content: [{ type: "text", text: "elsewhere" }],
        },
      ],
    });
    editor.commands.setTextSelection(1);

    expect(
      editor.commands.insertContentAt(editor.state.doc.content.size, {
        type: "testCard",
      })
    ).toBe(true);
    expect(editor.getJSON().content?.at(-1)).toMatchObject({
      type: "testCard",
      attrs: { indent: 0 },
    });
  });

  it("indents a selected NodeView and decorates its outer DOM", () => {
    const editor = createCardEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(NodeSelection.create(editor.state.doc, 0))
    );

    expect(editor.commands.indent()).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 24 });
    const nodeDOM = editor.view.nodeDOM(0) as HTMLElement;
    expect(nodeDOM.getAttribute("data-indent")).toBe("24");
    expect(nodeDOM.style.marginLeft).toBe("24px");
    expect(nodeDOM.style.maxWidth).toBe("calc(100% - 24px)");
  });

  it("constrains an indented full-width columns block to the editor width", () => {
    const editor = createColumnsEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new HaloGapCursor(editor.state.doc.resolve(0), "before")
      )
    );

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "columns",
      attrs: { indent: 24 },
    });
    const columnsDOM = editor.view.nodeDOM(0) as HTMLElement;
    expect(columnsDOM.style.marginLeft).toBe("24px");
    expect(columnsDOM.style.maxWidth).toBe("calc(100% - 24px)");
    expect(editor.getHTML()).toContain(
      "max-width: calc(100% - 24px)!important"
    );
  });

  it("constrains an indented full-width image NodeView to the editor width", () => {
    const editor = createImageEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new HaloGapCursor(editor.state.doc.resolve(0), "before")
      )
    );

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "image",
      attrs: { indent: 24 },
    });
    const imageDOM = editor.view.nodeDOM(0) as HTMLElement;
    expect(imageDOM.style.marginLeft).toBe("24px");
    expect(imageDOM.style.maxWidth).toBe("calc(100% - 24px)");
  });

  it("keeps table indentation on its scroll wrapper and round-trips it", () => {
    const editor = createTableEditor({
      type: "doc",
      content: [
        {
          type: "table",
          attrs: { indent: 24 },
          content: [
            {
              type: "tableRow",
              content: [
                {
                  type: "tableCell",
                  content: [{ type: "paragraph" }],
                },
              ],
            },
          ],
        },
      ],
    });

    const html = editor.getHTML();
    const container = document.createElement("div");
    container.innerHTML = html;
    const wrapper = container.querySelector<HTMLElement>(".halo-table-wrapper");
    const table = container.querySelector<HTMLTableElement>("table");
    expect(wrapper?.style.marginLeft).toBe("24px");
    expect(wrapper?.style.maxWidth).toBe("calc(100% - 24px)");
    expect(table?.dataset.indent).toBe("24");
    expect(table?.style.marginLeft).toBe("");

    const parsedEditor = createTableEditor(html);
    expect(parsedEditor.getJSON().content?.[0].attrs).toMatchObject({
      indent: 24,
    });
  });

  it("uses Tiptap code indentation inside a code block", () => {
    const editor = createCodeBlockEditor([
      {
        type: "codeBlock",
        content: [{ type: "text", text: "const value = 1" }],
      },
    ]);
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 1))
    );

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.state.doc.firstChild?.textContent).toBe("  const value = 1");
    expect(runKey(editor, "Tab", true)).toBe(true);
    expect(editor.state.doc.firstChild?.textContent).toBe("const value = 1");
  });

  it("indents a whole code block from its leading gap cursor", () => {
    const editor = createCodeBlockEditor([
      {
        type: "codeBlock",
        content: [{ type: "text", text: "const value = 1" }],
      },
      { type: "paragraph" },
    ]);
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new HaloGapCursor(editor.state.doc.resolve(0), "before")
      )
    );

    expect(runKey(editor, "Tab")).toBe(true);
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 24 });
    expect(editor.getHTML()).toContain('<pre data-indent="24"');
  });

  it("inherits paragraph indentation when converting it to a code block", () => {
    const editor = createCodeBlockEditor([
      {
        type: "paragraph",
        attrs: { indent: 48 },
        content: [{ type: "text", text: "/" }],
      },
    ]);
    editor.commands.setTextSelection(2);

    expect(setCodeBlockWithIndent(editor, { from: 1, to: 2 })).toBe(true);
    expect(editor.getJSON().content?.[0]).toMatchObject({
      type: "codeBlock",
      attrs: { indent: 48 },
    });
  });

  it("keeps existing nested lists intact when inserting an indented code block", () => {
    const editor = createCodeBlockEditor(
      [
        {
          type: "bulletList",
          content: [
            {
              type: "listItem",
              content: [
                {
                  type: "paragraph",
                  content: [{ type: "text", text: "parent" }],
                },
                {
                  type: "bulletList",
                  content: [
                    {
                      type: "listItem",
                      content: [
                        {
                          type: "paragraph",
                          content: [{ type: "text", text: "existing" }],
                        },
                      ],
                    },
                    {
                      type: "listItem",
                      content: [
                        {
                          type: "paragraph",
                          content: [{ type: "text", text: "/" }],
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
      32
    );
    const slashPos = findTextPosition(editor, "/");
    editor.commands.setTextSelection(slashPos + 1);
    const originalContent = editor.getJSON();

    expect(
      setCodeBlockWithIndent(editor, {
        from: slashPos,
        to: slashPos + 1,
      })
    ).toBe(true);

    const content = editor.getJSON().content ?? [];
    expect(content.at(-1)).toMatchObject({
      type: "codeBlock",
      attrs: { indent: 64 },
    });
    expect(JSON.stringify(content[0])).toContain("existing");
    expect(JSON.stringify(content[0]).match(/bulletList/g)).toHaveLength(2);

    expect(editor.commands.undo()).toBe(true);
    expect(editor.getJSON()).toEqual(originalContent);
    expect(editor.commands.undo()).toBe(false);
  });
});

function createEditor(
  { content }: { content: JSONContent | string },
  indentOptions: Partial<ExtensionIndentOptions> = {}
) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      ExtensionIndent.configure(indentOptions),
    ],
    content,
  });
  editors.push(editor);
  return editor;
}

function createCardEditor(content?: JSONContent) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      TestCard,
      ExtensionIndent,
      ExtensionGapCursor,
      ExtensionHistory,
    ],
    content: content ?? {
      type: "doc",
      content: [{ type: "testCard" }, { type: "paragraph" }],
    },
  });
  editors.push(editor);
  return editor;
}

function createCodeBlockEditor(content: JSONContent[], indentRange = 24) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      ListItem,
      BulletList,
      ExtensionCodeBlock,
      ExtensionIndent.configure({ indentRange }),
      ExtensionGapCursor,
      ExtensionHistory,
    ],
    content: { type: "doc", content },
  });
  editors.push(editor);
  return editor;
}

function createListEditor(content: JSONContent, indentRange = 24) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      ListItem,
      BulletList,
      OrderedList,
      TaskItem.configure({ nested: true }),
      TaskList,
      ExtensionIndent.configure({ indentRange }),
      ExtensionHistory,
    ],
    content,
  });
  editors.push(editor);
  return editor;
}

function createTableEditor(content: JSONContent | string) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [Document, Paragraph, Text, ExtensionTable, ExtensionIndent],
    content,
  });
  editors.push(editor);
  return editor;
}

function createDetailsEditor() {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [Document, Paragraph, Text, ExtensionDetails, ExtensionIndent],
    content: {
      type: "doc",
      content: [
        {
          type: "details",
          attrs: { open: true },
          content: [
            {
              type: "detailsSummary",
              content: [{ type: "text", text: "Details title" }],
            },
            {
              type: "detailsContent",
              content: [
                {
                  type: "paragraph",
                  content: [{ type: "text", text: "Details content" }],
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

function createColumnsEditor() {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      ExtensionColumns,
      ExtensionIndent,
      ExtensionGapCursor,
    ],
    content: {
      type: "doc",
      content: [
        {
          type: "columns",
          attrs: { cols: 2 },
          content: [
            {
              type: "column",
              attrs: { index: 0 },
              content: [{ type: "paragraph" }],
            },
            {
              type: "column",
              attrs: { index: 1 },
              content: [{ type: "paragraph" }],
            },
          ],
        },
        { type: "paragraph" },
      ],
    },
  });
  editors.push(editor);
  return editor;
}

function createImageEditor() {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      TestImage,
      ExtensionIndent,
      ExtensionGapCursor,
    ],
    content: {
      type: "doc",
      content: [{ type: "image" }, { type: "paragraph" }],
    },
  });
  editors.push(editor);
  return editor;
}

function findTextPosition(editor: Editor, text: string) {
  let result = -1;
  editor.state.doc.descendants((node, pos) => {
    if (node.isText && node.text === text) {
      result = pos;
      return false;
    }
    return result < 0;
  });
  return result;
}

function findNodePosition(editor: Editor, type: string, text?: string) {
  let result = -1;
  editor.state.doc.descendants((node, pos) => {
    if (
      result < 0 &&
      node.type.name === type &&
      (text === undefined || node.textContent === text)
    ) {
      result = pos;
      return false;
    }
    return result < 0;
  });
  if (result < 0) {
    throw new Error(`Unable to find ${type}${text ? `: ${text}` : ""}`);
  }
  return result;
}

function runKey(editor: Editor, key: string, shiftKey = false) {
  const event = new KeyboardEvent("keydown", {
    key,
    shiftKey,
    bubbles: true,
    cancelable: true,
  });
  return editor.view.someProp("handleKeyDown", (handler) =>
    handler(editor.view, event)
  );
}

function runTextInput(editor: Editor, text: string) {
  const { from, to } = editor.state.selection;
  return editor.view.someProp("handleTextInput", (handler) =>
    handler(editor.view, from, to, text, () => editor.state.tr)
  );
}
