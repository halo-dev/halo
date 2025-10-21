import type { ExtensionOptions } from "@/types";
import TiptapLink, { type LinkOptions } from "@tiptap/extension-link";

const Link = TiptapLink.extend<ExtensionOptions & Partial<LinkOptions>>({
  addOptions() {
    return {
      ...this.parent?.(),
      ...{
        HTMLAttributes: {
          rel: null,
        },
      },
    };
  },

  renderHTML({ HTMLAttributes }) {
    return ["a", HTMLAttributes, 0];
  },

  addPasteRules() {
    // Remove the function of pasted text parsing as a link
    return [];
  },
});

export default Link;
