// @vitest-environment jsdom

import Document from "@tiptap/extension-document";
import { FindAndReplacePluginKey } from "@tiptap/extension-find-and-replace";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { afterEach, describe, expect, it } from "vite-plus/test";
import { Editor } from "@/tiptap";
import { ExtensionSearchAndReplace } from "./index";

describe("ExtensionSearchAndReplace", () => {
  let editor: Editor | undefined;

  afterEach(() => {
    editor?.destroy();
    document.body.replaceChildren();
  });

  function createEditor(content: string) {
    editor = new Editor({
      element: document.createElement("div"),
      extensions: [
        Document,
        Paragraph,
        Text,
        ExtensionSearchAndReplace.configure({ searchDebounceMs: 0 }),
      ],
      content,
    });
    return editor;
  }

  it("does not match across block boundaries by default", () => {
    const instance = createEditor("<p>foo</p><p>bar</p>");

    instance.commands.setSearchTerm("foobar");
    expect(FindAndReplacePluginKey.getState(instance.state)?.results).toEqual(
      []
    );

    instance.commands.setSearchTerm("foo");
    expect(
      FindAndReplacePluginKey.getState(instance.state)?.results
    ).toHaveLength(1);
  });
});
