import { TrailingNode, type TrailingNodeOptions } from "@tiptap/extensions";

export const ExtensionTrailingNode = TrailingNode.extend<TrailingNodeOptions>({
  addOptions() {
    return {
      ...this.parent!(),
      node: "paragraph",
      notAfter: ["paragraph"],
    };
  },

  addProseMirrorPlugins() {
    if (this.editor.options.editable === false) {
      return [];
    }

    return this.parent?.() ?? [];
  },
});

export {
  skipTrailingNodeMeta,
  type TrailingNodeOptions,
} from "@tiptap/extensions";
