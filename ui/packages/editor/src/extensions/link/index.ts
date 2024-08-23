import type { ExtensionOptions } from "@/types";
import type { LinkOptions } from "@tiptap/extension-link";
import TiptapLink from "@tiptap/extension-link";

const Link = TiptapLink.extend<ExtensionOptions & LinkOptions>({
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

  addPasteRules() {
    // Remove the function of pasted text parsing as a link
    return [];
  },
});

export default Link;
