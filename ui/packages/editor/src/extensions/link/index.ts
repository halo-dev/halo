import type { ExtensionOptions } from "@/types";
import TiptapLink, { type LinkOptions } from "@tiptap/extension-link";

export type ExtensionLinkOptions = ExtensionOptions & Partial<LinkOptions>;

export const ExtensionLink = TiptapLink.extend<ExtensionLinkOptions>({
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
}).configure({
  autolink: false,
  openOnClick: false,
});
