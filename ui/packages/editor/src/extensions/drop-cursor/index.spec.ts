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
import { afterEach, describe, expect, it } from "vitest";
import { Editor, type JSONContent, type PMNode } from "@/tiptap";
import { NodeSelection } from "@/tiptap/pm";
import { ExtensionIndent, type ExtensionIndentOptions } from "../indent";
import { computeIndentDropPreview, ExtensionDropcursor } from "./index";

const editors: Editor[] = [];

afterEach(() => {
  editors.splice(0).forEach((editor) => editor.destroy());
});

describe("ExtensionDropcursor", () => {
  it("offers every indentation level up to the adjacent target", () => {
    const editor = createEditor();
    mockInternalDrag(editor);

    expect(computeIndentDropPreview(editor.view, point(50), 24)?.level).toBe(0);
    expect(computeIndentDropPreview(editor.view, point(74), 24)?.level).toBe(1);
    expect(computeIndentDropPreview(editor.view, point(98), 24)?.level).toBe(2);
    expect(computeIndentDropPreview(editor.view, point(170), 24)?.level).toBe(
      2
    );
  });

  it("shows the chosen level and applies it to the dropped block", () => {
    const editor = createEditor();
    mockInternalDrag(editor);
    runDragOver(editor, 74, 120);

    const cursor = editor.view.dom.querySelector<HTMLElement>(
      ".halo-indent-dropcursor"
    );
    expect(cursor?.dataset.indentLevel).toBe("1");
    expect(cursor?.style.getPropertyValue("--halo-drop-indent")).toBe("24px");

    editor.view.dispatch(
      editor.state.tr
        .setSelection(NodeSelection.create(editor.state.doc, 0))
        .setMeta("uiEvent", "drop")
    );

    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 24 });
    expect(editor.view.dom.querySelector(".halo-indent-dropcursor")).toBeNull();
  });

  it("caps drag indentation at the configured maximum", () => {
    const editor = createEditor(0, {
      indentRange: 24,
      maxIndentLevel: 24,
    });
    mockInternalDrag(editor);
    runDragOver(editor, 98, 120);

    const cursor = editor.view.dom.querySelector<HTMLElement>(
      ".halo-indent-dropcursor"
    );
    expect(cursor?.dataset.indentLevel).toBe("1");
    expect(cursor?.style.getPropertyValue("--halo-drop-indent")).toBe("24px");

    editor.view.dispatch(
      editor.state.tr
        .setSelection(NodeSelection.create(editor.state.doc, 0))
        .setMeta("uiEvent", "drop")
    );
    expect(editor.getJSON().content?.[0].attrs).toMatchObject({ indent: 24 });
  });

  it.each([
    ["zero", 50, 0],
    ["one", 74, 24],
  ] as const)(
    "replaces an existing indent with level %s",
    (_label, clientX, expectedIndent) => {
      const editor = createEditor(48);
      mockInternalDrag(editor);
      runDragOver(editor, clientX, 120);

      editor.view.dispatch(
        editor.state.tr
          .setSelection(NodeSelection.create(editor.state.doc, 0))
          .setMeta("uiEvent", "drop")
      );

      expect(editor.getJSON().content?.[0].attrs).toMatchObject({
        indent: expectedIndent,
      });
    }
  );

  it("anchors the preview below the exact list row", () => {
    const editor = createListTargetEditor();
    mockInternalDrag(editor);

    expect(computeIndentDropPreview(editor.view, point(50), 24)?.level).toBe(1);
    expect(computeIndentDropPreview(editor.view, point(74), 24)?.level).toBe(1);
    runDragOver(editor, 74, 130);

    const cursor = editor.view.dom.querySelector<HTMLElement>(
      ".halo-indent-dropcursor"
    );
    expect(cursor?.dataset.indentLevel).toBe("1");
    expect(cursor?.dataset.listItemTarget).toBe("true");
    expect(cursor?.style.getPropertyValue("--halo-drop-indent")).toBe("24px");
    expect(cursor?.style.getPropertyValue("--halo-drop-left")).toBe("74px");
    expect(cursor?.style.getPropertyValue("--halo-drop-top")).toBe("180px");
  });

  it("uses the pointer position to identify a list row", () => {
    const editor = createListTargetEditor();
    const rowPos = findParagraphPos(editor, "target-two");
    if (rowPos === null) {
      throw new Error("Unable to find the target row");
    }
    Object.defineProperty(editor.view, "posAtCoords", {
      configurable: true,
      value: ({ left }: { left: number }) =>
        left > 200
          ? { pos: 0, inside: -1 }
          : { pos: rowPos + 1, inside: rowPos },
    });
    mockInternalDrag(editor);

    const preview = computeIndentDropPreview(editor.view, point(74), 24);

    expect(preview?.normalizedListDrop).toBe(true);
    expect(preview?.listItemDrop).toBeDefined();
    expect(preview?.level).toBe(1);
  });

  it("drops the block inside the targeted list item", () => {
    const editor = createListTargetEditor();
    mockInternalDrag(editor);
    runDragOver(editor, 74, 130);

    const handled = runDrop(editor);

    expect(handled).toBe(true);
    const list = editor.state.doc.firstChild;
    const targetItem = list?.child(1);
    expect(editor.state.doc.childCount).toBe(1);
    expect(list?.type.name).toBe("bulletList");
    expect(list?.childCount).toBe(3);
    expect(targetItem?.childCount).toBe(2);
    expect(targetItem?.child(0).textContent).toBe("target-two");
    expect(targetItem?.child(1).textContent).toBe("source");
    expect(targetItem?.child(1).attrs).toMatchObject({ indent: 0 });
  });

  it.each(["111", "222", "333", "444"])(
    "drops immediately below nested list row %s",
    (label) => {
      const editor = createNestedListTargetEditor(label);
      const targetIndex = ["111", "222", "333", "444"].indexOf(label);
      mockInternalDrag(editor);

      const preview = computeIndentDropPreview(editor.view, point(50), 24);
      expect(preview?.level).toBe(targetIndex + 1);
      runDragOver(editor, 50, 130);

      const cursor = editor.view.dom.querySelector<HTMLElement>(
        ".halo-indent-dropcursor"
      );
      expect(cursor?.dataset.indentLevel).toBe(String(targetIndex + 1));
      expect(cursor?.dataset.listItemTarget).toBe("true");
      expect(cursor?.style.getPropertyValue("--halo-drop-left")).toBe(
        `${74 + targetIndex * 24}px`
      );
      expect(cursor?.style.getPropertyValue("--halo-drop-top")).toBe(
        `${140 + targetIndex * 40}px`
      );

      expect(runDrop(editor)).toBe(true);

      const targetItem = findListItem(editor, label);
      expect(editor.state.doc.childCount).toBe(1);
      expect(editor.state.doc.firstChild?.type.name).toBe("orderedList");
      expect(targetItem?.child(0).textContent).toBe(label);
      expect(targetItem?.child(1).type.name).toBe("codeBlock");
      expect(targetItem?.child(1).textContent).toBe("source");
      expect(targetItem?.child(1).attrs).toMatchObject({ indent: 0 });
      if (label === "444") {
        expect(targetItem?.childCount).toBe(2);
      } else {
        expect(targetItem?.child(2).type.name).toBe("orderedList");
      }
    }
  );

  it("recomputes the target row from the final drop coordinates", () => {
    const editor = createNestedListTargetEditor("111");
    mockInternalDrag(editor);
    runDragOver(editor, 50, 130);
    const targetPos = findParagraphPos(editor, "333");
    if (targetPos === null) {
      throw new Error("Unable to find the final target row");
    }
    Object.defineProperty(editor.view, "posAtCoords", {
      configurable: true,
      value: () => ({ pos: targetPos + 1, inside: targetPos }),
    });

    expect(runDrop(editor)).toBe(true);

    const targetItem = findListItem(editor, "333");
    expect(targetItem?.child(1).type.name).toBe("codeBlock");
    expect(targetItem?.child(1).textContent).toBe("source");
    expect(targetItem?.child(2).type.name).toBe("orderedList");
  });

  it("preserves task states when dropping into a task item", () => {
    const editor = createListTargetEditor("taskList");
    mockInternalDrag(editor);
    runDragOver(editor, 74, 130);

    expect(runDrop(editor)).toBe(true);

    const list = editor.state.doc.firstChild;
    expect(editor.state.doc.childCount).toBe(1);
    expect([
      list?.child(0).attrs.checked,
      list?.child(1).attrs.checked,
      list?.child(2).attrs.checked,
    ]).toEqual([false, true, false]);
    expect(list?.child(1).child(1).textContent).toBe("source");
  });

  it("shows a level-zero target line when dragging an entire list", () => {
    const editor = createListSourceEditor();
    mockInternalDrag(editor);

    const preview = computeIndentDropPreview(editor.view, point(122), 24);
    expect(preview).toMatchObject({ level: 0 });

    runDragOver(editor, 122, 120);
    const cursor = editor.view.dom.querySelector<HTMLElement>(
      ".halo-indent-dropcursor"
    );
    expect(cursor?.dataset.indentLevel).toBe("0");
    expect(cursor?.style.getPropertyValue("--halo-drop-indent")).toBe("0px");
  });

  it("does not offer a list row inside the dragged list as a target", () => {
    const editor = createListSourceEditor(true);
    mockInternalDrag(editor);

    expect(computeIndentDropPreview(editor.view, point(122), 24)).toBeNull();
  });

  it("targets a row in another list when dragging an entire list", () => {
    const editor = createListToListEditor();
    mockInternalDrag(editor);
    runDragOver(editor, 98, 130);

    const cursor = editor.view.dom.querySelector<HTMLElement>(
      ".halo-indent-dropcursor"
    );
    expect(cursor?.dataset.indentLevel).toBe("1");
    expect(cursor?.dataset.listItemTarget).toBe("true");
    expect(cursor?.style.getPropertyValue("--halo-drop-top")).toBe("180px");

    expect(runDrop(editor)).toBe(true);
    expect(editor.state.doc.childCount).toBe(1);
    const targetItem = editor.state.doc.firstChild?.firstChild;
    expect(targetItem?.child(0).textContent).toBe("target-list");
    expect(targetItem?.child(1).type.name).toBe("bulletList");
    expect(targetItem?.child(1).textContent).toBe("source-list");
  });
});

function createEditor(
  sourceIndent = 0,
  indentOptions: Partial<ExtensionIndentOptions> = {}
) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      CodeBlock,
      Text,
      ExtensionIndent.configure(indentOptions),
      ExtensionDropcursor,
    ],
    content: {
      type: "doc",
      content: [
        {
          type: "codeBlock",
          attrs: { indent: sourceIndent },
          content: [{ type: "text", text: "source" }],
        },
        {
          type: "paragraph",
          attrs: { indent: 48 },
          content: [{ type: "text", text: "target" }],
        },
      ],
    },
  });
  editors.push(editor);

  const targetPos = editor.state.doc.firstChild?.nodeSize ?? 0;
  setRect(editor.view.dom, 50, 0, 250, 400);
  setRect(editor.view.nodeDOM(targetPos) as HTMLElement, 98, 100, 202, 40);
  Object.defineProperty(editor.view, "posAtCoords", {
    configurable: true,
    value: ({ left }: { left: number }) => ({
      pos:
        left > 250
          ? editor.state.doc.content.size
          : editor.state.doc.firstChild?.nodeSize || 0,
      inside: -1,
    }),
  });
  return editor;
}

function createListTargetEditor(
  listType: "bulletList" | "orderedList" | "taskList" = "bulletList",
  start = 1,
  targetIndex = 1
) {
  const itemType = listType === "taskList" ? "taskItem" : "listItem";
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
      ExtensionIndent,
      ExtensionDropcursor,
    ],
    content: {
      type: "doc",
      content: [
        {
          type: "paragraph",
          content: [{ type: "text", text: "source" }],
        },
        {
          type: listType,
          attrs: listType === "orderedList" ? { start } : undefined,
          content: [
            {
              type: itemType,
              attrs: listType === "taskList" ? { checked: false } : undefined,
              content: [
                {
                  type: "paragraph",
                  content: [{ type: "text", text: "target-one" }],
                },
              ],
            },
            {
              type: itemType,
              attrs: listType === "taskList" ? { checked: true } : undefined,
              content: [
                {
                  type: "paragraph",
                  content: [{ type: "text", text: "target-two" }],
                },
              ],
            },
            {
              type: itemType,
              attrs: listType === "taskList" ? { checked: false } : undefined,
              content: [
                {
                  type: "paragraph",
                  content: [{ type: "text", text: "target-three" }],
                },
              ],
            },
          ],
        },
      ],
    },
  });
  editors.push(editor);

  const sourceSize = editor.state.doc.firstChild?.nodeSize ?? 0;
  const list = editor.state.doc.child(1);
  let targetItemPos = sourceSize + 1;
  for (let index = 0; index < targetIndex; index++) {
    targetItemPos += list.child(index).nodeSize;
  }
  const paragraphInsideListPos = targetItemPos + 1;
  setRect(editor.view.dom, 50, 0, 250, 400);
  setRect(editor.view.nodeDOM(sourceSize) as HTMLElement, 50, 100, 250, 120);
  setRect(
    editor.view.nodeDOM(paragraphInsideListPos) as HTMLElement,
    74,
    100 + targetIndex * 40,
    226,
    40
  );
  Object.defineProperty(editor.view, "posAtCoords", {
    configurable: true,
    value: () => ({
      pos: paragraphInsideListPos + 1,
      inside: paragraphInsideListPos,
    }),
  });
  expect(list.type.name).toBe(listType);
  return editor;
}

function createNestedListTargetEditor(targetLabel: string) {
  const labels = ["111", "222", "333", "444"];
  const targetIndex = labels.indexOf(targetLabel);
  if (targetIndex === -1) {
    throw new Error(`Unknown target label: ${targetLabel}`);
  }
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      CodeBlock,
      Text,
      ListItem,
      OrderedList,
      ExtensionIndent,
      ExtensionDropcursor,
    ],
    content: {
      type: "doc",
      content: [
        {
          type: "codeBlock",
          attrs: { indent: 48 },
          content: [{ type: "text", text: "source" }],
        },
        createNestedOrderedList(labels),
      ],
    },
  });
  editors.push(editor);

  const sourceSize = editor.state.doc.firstChild?.nodeSize ?? 0;
  const targetParagraphPos = findParagraphPos(editor, targetLabel);
  if (targetParagraphPos === null) {
    throw new Error(`Unable to find target paragraph: ${targetLabel}`);
  }
  setRect(editor.view.dom, 50, 0, 250, 400);
  setRect(editor.view.nodeDOM(sourceSize) as HTMLElement, 50, 80, 250, 240);
  setRect(
    editor.view.nodeDOM(targetParagraphPos) as HTMLElement,
    74 + targetIndex * 24,
    100 + targetIndex * 40,
    226 - targetIndex * 24,
    40
  );
  Object.defineProperty(editor.view, "posAtCoords", {
    configurable: true,
    value: () => ({
      pos: targetParagraphPos + 1,
      inside: targetParagraphPos,
    }),
  });
  return editor;
}

function createListSourceEditor(targetOwnRow = false) {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      ListItem,
      BulletList,
      OrderedList,
      ExtensionIndent,
      ExtensionDropcursor,
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
                  content: [{ type: "text", text: "source-list" }],
                },
              ],
            },
          ],
        },
        {
          type: "paragraph",
          attrs: { indent: 48 },
          content: [{ type: "text", text: "target" }],
        },
      ],
    },
  });
  editors.push(editor);

  const sourceSize = editor.state.doc.firstChild?.nodeSize ?? 0;
  const sourceParagraphPos = 2;
  setRect(editor.view.dom, 50, 0, 250, 400);
  setRect(
    editor.view.nodeDOM(sourceParagraphPos) as HTMLElement,
    74,
    80,
    226,
    40
  );
  setRect(editor.view.nodeDOM(sourceSize) as HTMLElement, 98, 100, 202, 40);
  Object.defineProperty(editor.view, "posAtCoords", {
    configurable: true,
    value: ({ left }: { left: number }) => ({
      pos: targetOwnRow && left < 250 ? sourceParagraphPos + 1 : sourceSize,
      inside: targetOwnRow && left < 250 ? sourceParagraphPos : -1,
    }),
  });
  return editor;
}

function createListToListEditor() {
  const editor = new Editor({
    element: document.createElement("div"),
    extensions: [
      Document,
      Paragraph,
      Text,
      ListItem,
      BulletList,
      OrderedList,
      ExtensionIndent,
      ExtensionDropcursor,
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
                  content: [{ type: "text", text: "source-list" }],
                },
              ],
            },
          ],
        },
        {
          type: "orderedList",
          content: [
            {
              type: "listItem",
              content: [
                {
                  type: "paragraph",
                  content: [{ type: "text", text: "target-list" }],
                },
              ],
            },
          ],
        },
      ],
    },
  });
  editors.push(editor);

  const sourceSize = editor.state.doc.firstChild?.nodeSize ?? 0;
  const targetParagraphPos = sourceSize + 2;
  setRect(editor.view.dom, 50, 0, 250, 400);
  setRect(editor.view.nodeDOM(sourceSize) as HTMLElement, 50, 100, 250, 120);
  setRect(
    editor.view.nodeDOM(targetParagraphPos) as HTMLElement,
    74,
    140,
    226,
    40
  );
  Object.defineProperty(editor.view, "posAtCoords", {
    configurable: true,
    value: () => ({
      pos: targetParagraphPos + 1,
      inside: targetParagraphPos,
    }),
  });
  return editor;
}

function createNestedOrderedList(labels: string[], index = 0): JSONContent {
  const content: JSONContent[] = [
    {
      type: "paragraph",
      content: [{ type: "text", text: labels[index] }],
    },
  ];
  if (index < labels.length - 1) {
    content.push(createNestedOrderedList(labels, index + 1));
  }
  return {
    type: "orderedList",
    content: [{ type: "listItem", content }],
  };
}

function findParagraphPos(editor: Editor, text: string) {
  let found: number | null = null;
  editor.state.doc.descendants((node, pos) => {
    if (
      found === null &&
      node.type.name === "paragraph" &&
      node.textContent === text
    ) {
      found = pos;
    }
  });
  return found;
}

function findListItem(editor: Editor, text: string): PMNode | null {
  let found: PMNode | null = null;
  editor.state.doc.descendants((node) => {
    if (
      found === null &&
      node.type.name === "listItem" &&
      node.firstChild?.textContent === text
    ) {
      found = node;
      return false;
    }
  });
  return found;
}

function mockInternalDrag(editor: Editor) {
  const sourceSize = editor.state.doc.firstChild?.nodeSize ?? 0;
  const node = NodeSelection.create(editor.state.doc, 0);
  editor.view.dispatch(editor.state.tr.setSelection(node));
  editor.view.dragging = {
    slice: editor.state.doc.slice(0, sourceSize),
    move: true,
    node,
  } as typeof editor.view.dragging;
}

function point(clientX: number) {
  return { clientX, clientY: 120 };
}

function runDragOver(editor: Editor, clientX: number, clientY: number) {
  const event = new MouseEvent("dragover", {
    bubbles: true,
    cancelable: true,
    clientX,
    clientY,
  }) as DragEvent;
  return editor.view.someProp("handleDOMEvents", (handlers) => {
    return handlers.dragover?.(editor.view, event) ?? false;
  });
}

function runDrop(editor: Editor) {
  const event = new MouseEvent("drop", {
    bubbles: true,
    cancelable: true,
    clientX: 74,
    clientY: 130,
  }) as DragEvent;
  const slice = editor.view.dragging?.slice;
  if (!slice) {
    throw new Error("Expected an active drag slice");
  }
  return editor.view.someProp("handleDrop", (handler) =>
    handler(editor.view, event, slice, true)
  );
}

function setRect(
  element: HTMLElement,
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
