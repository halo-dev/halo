// @vitest-environment jsdom

import CodeBlock from "@tiptap/extension-code-block";
import Document from "@tiptap/extension-document";
import {
  BulletList,
  ListItem,
  OrderedList,
  TaskItem,
  TaskList,
} from "@tiptap/extension-list";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { afterEach, beforeEach, describe, expect, it } from "vitest";
import { nextTick } from "vue";
import { Editor, Extension, Node, type JSONContent } from "@/tiptap";
import { GapCursor, NodeSelection, Plugin, TextSelection } from "@/tiptap/pm";
import { isGapCursorTargetNode } from "@/utils";
import { ExtensionColumns } from "../columns";
import { ExtensionCommandsMenu } from "../commands-menu";
import { ExtensionDetails } from "../details";
import { ExtensionTrailingNode, skipTrailingNodeMeta } from "../trailing-node";
import { ExtensionGapCursor } from "./index";

const TestCard = Node.create({
  name: "testCard",
  group: "block",
  atom: true,
  parseHTML: () => [{ tag: "div[data-test-card]" }],
  renderHTML: () => ["div", { "data-test-card": "" }],
});

const TestContainer = Node.create({
  name: "testContainer",
  group: "block",
  content: "paragraph+",
  isolating: true,
  parseHTML: () => [{ tag: "section[data-test-container]" }],
  renderHTML: () => ["section", { "data-test-container": "" }, 0],
});

const TestNodeViewTextblock = Node.create({
  name: "testNodeViewTextblock",
  group: "block",
  content: "text*",
  parseHTML: () => [{ tag: "div[data-test-node-view-textblock]" }],
  renderHTML: () => ["div", { "data-test-node-view-textblock": "" }, 0],
  addNodeView() {
    return () => {
      const dom = document.createElement("div");
      return { dom, contentDOM: dom };
    };
  },
});

const TestOfficialGapParent = Node.create({
  name: "testOfficialGapParent",
  group: "block",
  content: "paragraph+",
  allowGapCursor: false,
  parseHTML: () => [{ tag: "section[data-test-official-gap-parent]" }],
  renderHTML: () => ["section", { "data-test-official-gap-parent": "" }, 0],
});

const TestGapOptOut = Node.create({
  name: "testGapOptOut",
  group: "block",
  atom: true,
  createGapCursor: false,
  parseHTML: () => [{ tag: "div[data-test-gap-opt-out]" }],
  renderHTML: () => ["div", { "data-test-gap-opt-out": "" }],
});

const TestLateNoopAppender = Extension.create({
  name: "testLateNoopAppender",
  // Keep this after TrailingNode so it reproduces a later appendTransaction
  // round where only the appended transaction is visible to TrailingNode.
  priority: 50,
  addProseMirrorPlugins() {
    return [
      new Plugin({
        appendTransaction(transactions, _oldState, newState) {
          if (
            !transactions.some((transaction) =>
              transaction.getMeta(skipTrailingNodeMeta)
            )
          ) {
            return null;
          }

          return newState.tr.setMeta("testLateNoopAppender", true);
        },
      }),
    ];
  },
});

const editors: Editor[] = [];

beforeEach(() => {
  const rect = new DOMRect(0, 0, 1, 18);
  Object.defineProperties(Range.prototype, {
    getBoundingClientRect: {
      configurable: true,
      value: () => rect,
    },
    getClientRects: {
      configurable: true,
      value: () => [rect],
    },
  });
});

afterEach(() => {
  editors.splice(0).forEach((editor) => editor.destroy());
});

describe("ExtensionGapCursor", () => {
  it("uses the official gap selection at real positions around inferred blocks", () => {
    const editor = createEditor();
    const { doc } = editor.state;

    expect(editor.schema.nodes.testCard.spec.createGapCursor).toBe(true);
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(doc.resolve(1)))
    );

    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);
    expect(editor.state.selection.$from.depth).toBe(0);
    const cursor = editor.view.dom.querySelector(".ProseMirror-gapcursor");
    expect(cursor).not.toBeNull();
    expect(cursor?.classList.contains("halo-gap-cursor--after")).toBe(true);
    expect(cursor?.previousElementSibling?.hasAttribute("data-test-card")).toBe(
      true
    );
  });

  it("places mouse clicks in the left and right gutters before and after a block", () => {
    const editor = createEditor();
    const card = editor.view.nodeDOM(0) as HTMLElement;
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(card, 50, 50, 200, 100);

    expect(runMouseDown(editor, 280, 100)).toBe(true);

    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);
    const afterCursor = editor.view.dom.querySelector<HTMLElement>(
      ".halo-gap-cursor--after"
    );
    expect(afterCursor).not.toBeNull();
    expect(afterCursor?.style.transform).toBe("translate(250px, 150px)");

    expect(runMouseDown(editor, 20, 100)).toBe(true);

    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);
    const beforeCursor = editor.view.dom.querySelector<HTMLElement>(
      ".halo-gap-cursor--before"
    );
    expect(beforeCursor).not.toBeNull();
    expect(beforeCursor?.style.transform).toBe("translate(50px, 50px)");
  });

  it("keeps an indented node's own margin area available to the gap cursor", () => {
    const editor = createEditor();
    const card = editor.view.nodeDOM(0) as HTMLElement;
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(card, 98, 50, 152, 100);

    expect(runMouseDown(editor, 30, 100, card)).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);
  });

  it("places a click just inside a block's top-left corner before it", () => {
    const editor = createEditor();
    const card = editor.view.nodeDOM(0) as HTMLElement;
    card.dataset.nodeViewWrapper = "";
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(card, 50, 50, 200, 100);

    const event = new MouseEvent("mousedown", {
      bubbles: true,
      cancelable: true,
      button: 0,
      clientX: 58,
      clientY: 58,
    });
    card.dispatchEvent(event);

    expect(event.defaultPrevented).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--before")
    ).not.toBeNull();

    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );
    expect(runMouseDown(editor, 63, 58, card)).toBeFalsy();
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.from).toBe(2);
  });

  it("keeps interactive content in the top-left corner clickable", () => {
    const editor = createEditor();
    const card = editor.view.nodeDOM(0) as HTMLElement;
    card.dataset.nodeViewWrapper = "";
    const button = document.createElement("button");
    const codeMirror = document.createElement("div");
    codeMirror.className = "cm-editor";
    const stopPropagation = (event: Event) => event.stopPropagation();
    button.addEventListener("mousedown", stopPropagation);
    codeMirror.addEventListener("mousedown", stopPropagation);
    card.append(button, codeMirror);
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(card, 50, 50, 200, 100);
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );

    const buttonEvent = dispatchMouseDown(button, 58, 58);
    const codeMirrorEvent = dispatchMouseDown(codeMirror, 58, 58);
    expect(buttonEvent.defaultPrevented).toBe(false);
    expect(codeMirrorEvent.defaultPrevented).toBe(false);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.from).toBe(2);
  });

  it("places a click in the whitespace below a block after it", () => {
    const editor = createEditor();
    const card = editor.view.nodeDOM(0) as HTMLElement;
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(card, 50, 50, 200, 100);

    expect(runMouseDown(editor, 100, 160)).toBe(true);

    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--after")
    ).not.toBeNull();
  });

  it("places mouse gaps around a code block nested in a list item", () => {
    const editor = createListCodeBlockEditor();
    let codeBlockPos = -1;
    editor.state.doc.descendants((node, pos) => {
      if (node.type.name === "codeBlock") {
        codeBlockPos = pos;
        return false;
      }
      return true;
    });
    const codeBlock = editor.view.nodeDOM(codeBlockPos) as HTMLElement;
    const list = codeBlock.closest("ul, ol");
    expect(list).not.toBeNull();
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(codeBlock, 50, 50, 200, 100);

    expect(runMouseDown(editor, 20, 100, list ?? codeBlock)).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(codeBlockPos);
    expect(editor.state.selection.$from.parent.type.name).toBe("listItem");

    expect(runMouseDown(editor, 280, 100, list ?? codeBlock)).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(
      codeBlockPos + (editor.state.doc.nodeAt(codeBlockPos)?.nodeSize ?? 0)
    );
  });

  it("uses an explicit visual anchor for NodeView chrome and cursor placement", () => {
    const editor = createEditor();
    const card = editor.view.nodeDOM(0) as HTMLElement;
    const visualAnchor = document.createElement("div");
    card.dataset.gapCursorClickArea = "";
    visualAnchor.dataset.gapCursorAnchor = "";
    card.appendChild(visualAnchor);
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(card, 50, 50, 200, 100);
    setRect(visualAnchor, 50, 50, 100, 100);

    expect(runMouseDown(editor, 220, 100, card)).toBe(true);

    const cursor = editor.view.dom.querySelector<HTMLElement>(
      ".halo-gap-cursor--after"
    );
    expect(editor.state.selection.from).toBe(1);
    expect(cursor?.style.transform).toBe("translate(150px, 150px)");

    expect(runMouseDown(editor, 58, 58, visualAnchor)).toBe(true);
    expect(editor.state.selection.from).toBe(0);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--before")
    ).not.toBeNull();

    expect(runMouseDown(editor, 100, 100, visualAnchor)).toBeFalsy();
  });

  it("does not replace a selection when the click is inside the block", () => {
    const editor = createEditor();
    const card = editor.view.nodeDOM(0) as HTMLElement;
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(card, 50, 50, 200, 100);
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );

    expect(runMouseDown(editor, 100, 100)).toBeFalsy();

    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.from).toBe(2);
  });

  it("infers textblock components from their NodeView without extra configuration", () => {
    const editor = new Editor({
      element: document.createElement("div"),
      extensions: [Document, Text, TestNodeViewTextblock, ExtensionGapCursor],
      content: {
        type: "doc",
        content: [{ type: "testNodeViewTextblock" }],
      },
    });
    editors.push(editor);

    expect(editor.schema.nodes.testNodeViewTextblock.spec.createGapCursor).toBe(
      true
    );
  });

  it("infers an isolating built-in component without gap configuration", () => {
    const editor = new Editor({
      element: document.createElement("div"),
      extensions: [
        Document,
        Paragraph,
        Text,
        ExtensionColumns,
        ExtensionGapCursor,
      ],
      content: {
        type: "doc",
        content: [
          {
            type: "columns",
            attrs: { cols: 1 },
            content: [
              {
                type: "column",
                content: [{ type: "paragraph" }],
              },
            ],
          },
        ],
      },
    });
    editors.push(editor);

    expect(editor.schema.nodes.columns.spec.createGapCursor).toBe(true);
  });

  it("preserves allowGapCursor as the parent-position setting", () => {
    const editor = new Editor({
      element: document.createElement("div"),
      extensions: [
        Document,
        Paragraph,
        Text,
        TestOfficialGapParent,
        ExtensionGapCursor,
      ],
      content: {
        type: "doc",
        content: [
          {
            type: "testOfficialGapParent",
            content: [{ type: "paragraph" }],
          },
        ],
      },
    });
    editors.push(editor);

    const spec = editor.schema.nodes.testOfficialGapParent.spec;
    expect(spec.allowGapCursor).toBe(false);
    expect(spec.createGapCursor).toBeUndefined();
  });

  it("lets a structural node explicitly opt out", () => {
    const editor = new Editor({
      element: document.createElement("div"),
      extensions: [
        Document,
        Paragraph,
        Text,
        TestGapOptOut,
        ExtensionGapCursor,
      ],
      content: {
        type: "doc",
        content: [{ type: "testGapOptOut" }, { type: "paragraph" }],
      },
    });
    editors.push(editor);
    const card = editor.view.nodeDOM(0) as HTMLElement;
    setRect(editor.view.dom, 0, 0, 300, 300);
    setRect(card, 50, 50, 200, 100);

    expect(editor.schema.nodes.testGapOptOut.spec.createGapCursor).toBe(false);
    expect(runMouseDown(editor, 20, 100)).toBeFalsy();
  });

  it("does not infer editable list containers as gap cursor targets", () => {
    const editor = createListEditor();

    expect(editor.state.doc.childCount).toBe(4);
    for (let index = 0; index < 3; index++) {
      expect(isGapCursorTargetNode(editor.state.doc.child(index))).toBe(false);
    }
    expect(editor.schema.nodes.bulletList.spec.createGapCursor).not.toBe(true);
    expect(editor.schema.nodes.orderedList.spec.createGapCursor).not.toBe(true);
    expect(editor.schema.nodes.taskList.spec.createGapCursor).not.toBe(true);
  });

  it("moves from text to the gap after a block without trapping later movement", () => {
    const editor = createEditor();
    Object.defineProperty(editor.view, "endOfTextblock", {
      configurable: true,
      value: () => true,
    });
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );

    expect(runKey(editor, "ArrowUp")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);

    expect(runKey(editor, "ArrowUp")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);

    expect(runKey(editor, "ArrowDown")).toBe(true);
    expect(editor.state.selection.from).toBe(1);
  });

  it("moves across a structured block without entering its child content", () => {
    const editor = createStructuredEditor();
    expect(editor.schema.nodes.testContainer.spec.createGapCursor).toBe(true);
    Object.defineProperty(editor.view, "endOfTextblock", {
      configurable: true,
      value: () => true,
    });
    const containerSize = editor.state.doc.firstChild?.nodeSize ?? 0;
    editor.view.dispatch(
      editor.state.tr.setSelection(
        TextSelection.create(editor.state.doc, containerSize + 1)
      )
    );

    expect(runKey(editor, "ArrowUp")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(containerSize);

    expect(runKey(editor, "ArrowUp")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);
  });

  it("enters structured block content with the inward horizontal arrow", () => {
    const editor = createStructuredEditor();
    const containerSize = editor.state.doc.firstChild?.nodeSize ?? 0;

    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(0)))
    );
    expect(runKey(editor, "ArrowRight")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.$from.parent.type.name).toBe("paragraph");
    expect(editor.state.selection.from).toBe(2);

    editor.view.dispatch(
      editor.state.tr.setSelection(
        new GapCursor(editor.state.doc.resolve(containerSize))
      )
    );
    expect(runKey(editor, "ArrowLeft")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.$from.parent.type.name).toBe("paragraph");
    expect(editor.state.selection.from).toBe(containerSize - 2);
  });

  it("does not turn an outward horizontal arrow into vertical gap movement", () => {
    const editor = createStructuredEditor();
    const containerSize = editor.state.doc.firstChild?.nodeSize ?? 0;

    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(0)))
    );
    expect(runKey(editor, "ArrowLeft")).not.toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);

    editor.view.dispatch(
      editor.state.tr.setSelection(
        new GapCursor(editor.state.doc.resolve(containerSize))
      )
    );
    expect(runKey(editor, "ArrowRight")).not.toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(containerSize);
  });

  it("moves vertically from a block gap into an intervening empty paragraph", () => {
    const editor = createEditorWithContent([
      { type: "testCard" },
      { type: "paragraph" },
      { type: "testCard" },
    ]);
    const secondCardPos = 3;
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new GapCursor(editor.state.doc.resolve(secondCardPos))
      )
    );

    expect(runKey(editor, "ArrowUp")).toBe(true);

    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.$from.parent.type.name).toBe("paragraph");
    expect(editor.state.selection.$from.parentOffset).toBe(0);
    expect(editor.state.selection.from).toBe(2);
  });

  it("moves vertically to the start of an intervening text paragraph", () => {
    const editor = createEditorWithContent([
      { type: "testCard" },
      {
        type: "paragraph",
        content: [{ type: "text", text: "between" }],
      },
      { type: "testCard" },
    ]);
    const paragraphStart = 2;
    const secondCardPos = 10;

    editor.view.dispatch(
      editor.state.tr.setSelection(
        new GapCursor(editor.state.doc.resolve(secondCardPos))
      )
    );
    expect(runKey(editor, "ArrowUp")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.from).toBe(paragraphStart);
    expect(editor.state.selection.$from.parentOffset).toBe(0);

    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(1)))
    );
    expect(runKey(editor, "ArrowDown")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.from).toBe(paragraphStart);
    expect(editor.state.selection.$from.parentOffset).toBe(0);
  });

  it("keeps separate after and before visual stops between adjacent blocks", () => {
    const editor = createAdjacentCardsEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(0)))
    );

    expect(runKey(editor, "ArrowDown")).toBe(true);
    expect(editor.state.selection.from).toBe(1);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--after")
    ).not.toBeNull();

    expect(runKey(editor, "ArrowDown")).toBe(true);
    expect(editor.state.selection.from).toBe(1);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--before")
    ).not.toBeNull();

    expect(runKey(editor, "ArrowDown")).toBe(true);
    expect(editor.state.selection.from).toBe(2);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--after")
    ).not.toBeNull();
  });

  it("moves out of a collapsed details summary to its block boundary", () => {
    const editor = createDetailsEditor();
    Object.defineProperty(editor.view, "endOfTextblock", {
      configurable: true,
      value: () => true,
    });
    let summaryPos = -1;
    editor.state.doc.descendants((node, pos) => {
      if (node.type.name === "detailsSummary") {
        summaryPos = pos + 1;
        return false;
      }
      return true;
    });
    const detailsSize = editor.state.doc.firstChild?.nodeSize ?? 0;
    editor.view.dispatch(
      editor.state.tr.setSelection(
        TextSelection.create(editor.state.doc, summaryPos)
      )
    );

    expect(runKey(editor, "ArrowRight")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(detailsSize);

    editor.view.dispatch(
      editor.state.tr.setSelection(
        TextSelection.create(editor.state.doc, summaryPos)
      )
    );
    expect(runKey(editor, "ArrowLeft")).toBe(true);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);
  });

  it("creates a schema-valid text block and keeps the gap on Enter", () => {
    const editor = createEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(0)))
    );

    expect(runKey(editor, "Enter")).toBe(true);

    expect(editor.state.doc.childCount).toBe(3);
    expect(editor.state.doc.firstChild?.type.name).toBe("paragraph");
    expect(editor.state.doc.child(1).type.name).toBe("testCard");
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(2);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--before")
    ).not.toBeNull();
  });

  it("deletes the preceding empty text block and keeps the before gap", () => {
    const editor = createEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(0)))
    );
    runKey(editor, "Enter");

    expect(runKey(editor, "Backspace")).toBe(true);

    expect(editor.state.doc.childCount).toBe(2);
    expect(editor.state.doc.firstChild?.type.name).toBe("testCard");
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--before")
    ).not.toBeNull();
  });

  it("deletes an empty paragraph between structural blocks in one Backspace", () => {
    const editor = createEditorWithContent([
      { type: "testCard" },
      { type: "paragraph" },
      { type: "testCard" },
    ]);
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );

    expect(runKey(editor, "Backspace")).toBe(true);

    expect(editor.state.doc.childCount).toBe(2);
    expect(editor.state.doc.child(0).type.name).toBe("testCard");
    expect(editor.state.doc.child(1).type.name).toBe("testCard");
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--after")
    ).not.toBeNull();
  });

  it("deletes a final empty paragraph without recreating it in the same transaction", () => {
    const editor = createEditorWithTrailingNode([
      { type: "testCard" },
      { type: "paragraph" },
    ]);
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );

    expect(runKey(editor, "Backspace")).toBe(true);

    expect(editor.state.doc.childCount).toBe(1);
    expect(editor.state.doc.firstChild?.type.name).toBe("testCard");
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--after")
    ).not.toBeNull();
  });

  it("keeps the gap after typed text is cleared when a later plugin appends a transaction", () => {
    const editor = createEditorWithTrailingNode(
      [{ type: "testCard" }, { type: "paragraph" }],
      [TestLateNoopAppender]
    );
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );
    editor.view.dispatch(editor.state.tr.insertText("abc"));
    editor.view.dispatch(editor.state.tr.delete(2, 5));
    expect(editor.state.doc.lastChild?.content.size).toBe(0);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);

    expect(runKey(editor, "Backspace")).toBe(true);

    expect(editor.state.doc.childCount).toBe(1);
    expect(editor.state.doc.firstChild?.type.name).toBe("testCard");
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--after")
    ).not.toBeNull();
  });

  it("deletes an empty paragraph between structural blocks with forward Delete", () => {
    const editor = createEditorWithContent([
      { type: "testCard" },
      { type: "paragraph" },
      { type: "testCard" },
    ]);
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );

    expect(runKey(editor, "Delete")).toBe(true);

    expect(editor.state.doc.childCount).toBe(2);
    expect(editor.state.doc.child(0).type.name).toBe("testCard");
    expect(editor.state.doc.child(1).type.name).toBe("testCard");
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--before")
    ).not.toBeNull();
  });

  it("moves from a before gap to the end of preceding text", () => {
    const editor = createEditorWithContent([
      {
        type: "paragraph",
        content: [{ type: "text", text: "before" }],
      },
      { type: "testCard" },
      {
        type: "paragraph",
        content: [{ type: "text", text: "after" }],
      },
    ]);
    const cardPos = editor.state.doc.firstChild?.nodeSize ?? 0;
    editor.view.dispatch(
      editor.state.tr.setSelection(
        new GapCursor(editor.state.doc.resolve(cardPos))
      )
    );

    expect(runKey(editor, "Backspace")).toBe(true);

    expect(editor.state.doc.childCount).toBe(3);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.$from.parent.textContent).toBe("before");
    expect(editor.state.selection.$from.parentOffset).toBe(6);
  });

  it("deletes the preceding structural block from a before gap", () => {
    const editor = createAdjacentCardsEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(1)))
    );
    runKey(editor, "ArrowDown");
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--before")
    ).not.toBeNull();

    expect(runKey(editor, "Backspace")).toBe(true);

    expect(editor.state.doc.childCount).toBe(2);
    expect(editor.state.doc.firstChild?.type.name).toBe("testCard");
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(0);
    expect(
      editor.view.dom.querySelector(".halo-gap-cursor--before")
    ).not.toBeNull();
  });

  it("inserts typed text into a new text block in one transaction", () => {
    const editor = createEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(1)))
    );

    expect(runTextInput(editor, "Halo")).toBe(true);

    expect(editor.state.doc.childCount).toBe(3);
    expect(editor.state.doc.child(1).type.name).toBe("paragraph");
    expect(editor.state.doc.child(1).textContent).toBe("Halo");
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
  });

  it("creates an inline context before composition input", () => {
    const editor = createEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(1)))
    );
    const event = new InputEvent("beforeinput", {
      bubbles: true,
      cancelable: true,
      data: "你",
      inputType: "insertCompositionText",
    });

    editor.view.someProp("handleDOMEvents", (handlers) => {
      return handlers.beforeinput?.(editor.view, event) ?? false;
    });

    expect(editor.state.doc.child(1).type.name).toBe("paragraph");
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
  });

  it("keeps an extended gap selection mapped across document changes", () => {
    const editor = createEditor();
    Object.defineProperty(editor.view, "endOfTextblock", {
      configurable: true,
      value: () => true,
    });
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );
    runKey(editor, "ArrowUp");

    editor.view.dispatch(
      editor.state.tr.insertText("!", editor.state.doc.content.size - 1)
    );

    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);
  });

  it("restores an extended gap selection from a history bookmark", () => {
    const editor = createEditor();
    Object.defineProperty(editor.view, "endOfTextblock", {
      configurable: true,
      value: () => true,
    });
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );
    runKey(editor, "ArrowUp");

    const bookmark = editor.state.selection.getBookmark();
    const tr = editor.state.tr.insertText(
      "!",
      editor.state.doc.content.size - 1
    );
    const selection = bookmark.map(tr.mapping).resolve(tr.doc);

    expect(selection).toBeInstanceOf(GapCursor);
    expect(selection.from).toBe(1);
  });

  it("replaces a structural block with an empty paragraph from its after gap", () => {
    const editor = createEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );

    runKey(editor, "Backspace");

    expect(editor.state.doc.childCount).toBe(2);
    expect(editor.state.doc.firstChild?.type.name).toBe("testCard");
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);

    expect(runKey(editor, "Backspace")).toBe(true);

    expect(editor.state.doc.childCount).toBe(2);
    expect(editor.state.doc.firstChild?.type.name).toBe("paragraph");
    expect(editor.state.doc.firstChild?.content.size).toBe(0);
    expect(editor.state.doc.child(1).textContent).toBe("after");
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.from).toBe(1);
  });

  it("deletes adjacent structural blocks predictably across repeated Backspace", () => {
    const editor = createAdjacentCardsEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(2)))
    );

    expect(runKey(editor, "Backspace")).toBe(true);
    expect(editor.state.doc.toJSON().content).toEqual([
      { type: "testCard" },
      { type: "paragraph" },
      { type: "paragraph" },
    ]);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);

    expect(runKey(editor, "Backspace")).toBe(true);
    expect(editor.state.doc.toJSON().content).toEqual([
      { type: "testCard" },
      { type: "paragraph" },
    ]);
    expect(editor.state.selection).toBeInstanceOf(GapCursor);
    expect(editor.state.selection.from).toBe(1);

    expect(runKey(editor, "Backspace")).toBe(true);
    expect(editor.state.doc.toJSON().content).toEqual([
      { type: "paragraph" },
      { type: "paragraph" },
    ]);
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.from).toBe(1);
  });

  it("keeps the document editable after deleting its only structural block", () => {
    const editor = createEditorWithContent([{ type: "testCard" }]);
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(1)))
    );

    expect(runKey(editor, "Backspace")).toBe(true);

    expect(editor.state.doc.childCount).toBe(1);
    expect(editor.state.doc.firstChild?.type.name).toBe("paragraph");
    expect(editor.state.selection).toBeInstanceOf(TextSelection);
  });

  it("stages forward deletion through a node selection", () => {
    const editor = createEditor();
    editor.view.dispatch(
      editor.state.tr.setSelection(new GapCursor(editor.state.doc.resolve(0)))
    );

    runKey(editor, "Delete");

    expect(editor.state.doc.childCount).toBe(2);
    expect(editor.state.selection).toBeInstanceOf(NodeSelection);
    expect(editor.state.selection.from).toBe(0);
  });

  it("lets command-menu suggestions handle keys before structural navigation", () => {
    const editor = createEditor(true);
    const pluginKeys = editor.state.plugins.map((plugin) => {
      return (
        (plugin.spec.key as unknown as { key?: string } | undefined)?.key ?? ""
      );
    });
    const commandMenuIndex = pluginKeys.findIndex((key) =>
      key.startsWith("commands-menu-english")
    );
    const haloGapCursorIndex = pluginKeys.findIndex((key) =>
      key.startsWith("halo-gap-cursor")
    );

    expect(commandMenuIndex).toBeGreaterThanOrEqual(0);
    expect(haloGapCursorIndex).toBeGreaterThanOrEqual(0);
    expect(commandMenuIndex).toBeLessThan(haloGapCursorIndex);
  });

  it("keeps ArrowUp inside an active command menu next to a card", async () => {
    const editor = createEditor(true);
    Object.defineProperty(editor.view, "endOfTextblock", {
      configurable: true,
      value: () => true,
    });
    editor.view.dispatch(
      editor.state.tr.setSelection(TextSelection.create(editor.state.doc, 2))
    );
    editor.view.dispatch(editor.state.tr.insertText("/"));
    await nextTick();
    const selectionBeforeArrow = editor.state.selection;

    expect(runKey(editor, "ArrowUp")).toBe(true);

    expect(editor.state.selection).toBeInstanceOf(TextSelection);
    expect(editor.state.selection.eq(selectionBeforeArrow)).toBe(true);
  });
});

function createEditor(withCommandsMenu = false) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      TestCard,
      ...(withCommandsMenu ? [ExtensionCommandsMenu] : []),
      ExtensionGapCursor,
    ],
    content: {
      type: "doc",
      content: [
        { type: "testCard" },
        {
          type: "paragraph",
          content: [{ type: "text", text: "after" }],
        },
      ],
    },
  });
  editors.push(editor);
  return editor;
}

function createEditorWithContent(content: JSONContent[]) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [Document, Paragraph, Text, TestCard, ExtensionGapCursor],
    content: { type: "doc", content },
  });
  editors.push(editor);
  return editor;
}

function createEditorWithTrailingNode(
  content: JSONContent[],
  extraExtensions: Extension[] = []
) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      TestCard,
      ExtensionGapCursor,
      ExtensionTrailingNode,
      ...extraExtensions,
    ],
    content: { type: "doc", content },
  });
  editors.push(editor);
  return editor;
}

function createListCodeBlockEditor() {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      CodeBlock,
      Text,
      ListItem,
      BulletList,
      OrderedList,
      ExtensionGapCursor,
    ],
    content: {
      type: "doc",
      content: [
        {
          type: "bulletList",
          content: [
            {
              type: "listItem",
              content: [
                {
                  type: "paragraph",
                  content: [{ type: "text", text: "item" }],
                },
                {
                  type: "codeBlock",
                  content: [{ type: "text", text: "nested code" }],
                },
                {
                  type: "orderedList",
                  content: [
                    {
                      type: "listItem",
                      content: [
                        {
                          type: "paragraph",
                          content: [{ type: "text", text: "nested item" }],
                        },
                      ],
                    },
                  ],
                },
              ],
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

function createListEditor() {
  const listItem = {
    type: "listItem",
    content: [{ type: "paragraph", content: [{ type: "text", text: "item" }] }],
  };
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      ListItem,
      BulletList,
      OrderedList,
      TaskItem,
      TaskList,
      ExtensionGapCursor,
    ],
    content: {
      type: "doc",
      content: [
        { type: "bulletList", content: [listItem] },
        { type: "orderedList", content: [listItem] },
        {
          type: "taskList",
          content: [
            {
              type: "taskItem",
              attrs: { checked: false },
              content: [
                {
                  type: "paragraph",
                  content: [{ type: "text", text: "task" }],
                },
              ],
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

function createStructuredEditor() {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [Document, Paragraph, Text, TestContainer, ExtensionGapCursor],
    content: {
      type: "doc",
      content: [
        {
          type: "testContainer",
          content: [{ type: "paragraph" }],
        },
        { type: "paragraph" },
      ],
    },
  });
  editors.push(editor);
  return editor;
}

function createAdjacentCardsEditor() {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [Document, Paragraph, Text, TestCard, ExtensionGapCursor],
    content: {
      type: "doc",
      content: [
        { type: "testCard" },
        { type: "testCard" },
        { type: "paragraph" },
      ],
    },
  });
  editors.push(editor);
  return editor;
}

function createDetailsEditor() {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      ExtensionDetails,
      ExtensionGapCursor,
    ],
    content: {
      type: "doc",
      content: [
        {
          type: "details",
          attrs: { open: false },
          content: [
            { type: "detailsSummary" },
            {
              type: "detailsContent",
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

function runKey(editor: Editor, key: string) {
  const event = new KeyboardEvent("keydown", {
    key,
    bubbles: true,
    cancelable: true,
  });
  return editor.view.someProp("handleKeyDown", (handler) =>
    handler(editor.view, event)
  );
}

function setRect(
  element: Element,
  x: number,
  y: number,
  width: number,
  height: number
) {
  Object.defineProperty(element, "getBoundingClientRect", {
    configurable: true,
    value: () => new DOMRect(x, y, width, height),
  });
}

function runMouseDown(
  editor: Editor,
  clientX: number,
  clientY: number,
  target: EventTarget = editor.view.dom
) {
  const event = new MouseEvent("mousedown", {
    bubbles: true,
    cancelable: true,
    button: 0,
    clientX,
    clientY,
  });
  Object.defineProperty(event, "target", {
    configurable: true,
    value: target,
  });
  return editor.view.someProp("handleDOMEvents", (handlers) => {
    return handlers.mousedown?.(editor.view, event) ?? false;
  });
}

function dispatchMouseDown(target: Element, clientX: number, clientY: number) {
  const event = new MouseEvent("mousedown", {
    bubbles: true,
    cancelable: true,
    button: 0,
    clientX,
    clientY,
  });
  target.dispatchEvent(event);
  return event;
}

function runTextInput(editor: Editor, text: string) {
  const { from, to } = editor.state.selection;
  return editor.view.someProp("handleTextInput", (handler) =>
    handler(editor.view, from, to, text, () => editor.state.tr)
  );
}
