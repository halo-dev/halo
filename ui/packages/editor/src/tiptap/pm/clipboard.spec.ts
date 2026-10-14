// @vitest-environment jsdom

import { afterEach, describe, expect, it } from "vite-plus/test";
import { EditorState, EditorView, Schema, type Slice } from "./index";

const schema = new Schema({
  nodes: {
    doc: { content: "block+" },
    paragraph: {
      group: "block",
      content: "text*",
      parseDOM: [{ tag: "p" }],
      toDOM: () => ["p", 0],
    },
    blockquote: {
      group: "block",
      content: "block+",
      attrs: { cite: { default: "", validate: "string" } },
      parseDOM: [{ tag: "blockquote" }],
      toDOM: (node) => ["blockquote", { cite: node.attrs.cite }, 0],
    },
    text: { group: "inline" },
  },
});

describe("ProseMirror clipboard context", () => {
  let view: EditorView | undefined;

  afterEach(() => {
    view?.destroy();
    document.body.replaceChildren();
  });

  function pasteWithContext(cite: unknown) {
    let pasted: Slice | undefined;
    view = new EditorView(document.body, {
      state: EditorState.create({ schema }),
      handlePaste(_view, _event, slice) {
        pasted = slice;
        return true;
      },
    });

    const paragraph = document.createElement("p");
    paragraph.textContent = "Pasted text";
    paragraph.setAttribute(
      "data-pm-slice",
      `1 1 ${JSON.stringify(["blockquote", { cite }])}`
    );

    const event = new Event("paste") as ClipboardEvent;
    expect(view.pasteHTML(paragraph.outerHTML, event)).toBe(true);
    return pasted!;
  }

  it("preserves valid node attributes from copied content", () => {
    const slice = pasteWithContext("https://example.com");

    expect(slice.content.firstChild?.type.name).toBe("blockquote");
    expect(slice.content.firstChild?.attrs.cite).toBe("https://example.com");
    expect(slice.content.firstChild?.textContent).toBe("Pasted text");
  });

  it("rejects invalid node attributes in pasted context metadata", () => {
    const slice = pasteWithContext(["img", { src: "invalid" }]);

    expect(slice.content.firstChild?.type.name).toBe("paragraph");
    expect(slice.content.firstChild?.textContent).toBe("Pasted text");
  });
});
