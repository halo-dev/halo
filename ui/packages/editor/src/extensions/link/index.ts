import TiptapLink from "@tiptap/extension-link";
import type { LinkOptions } from "@tiptap/extension-link";
import type { ExtensionOptions } from "@/types";
import { mergeAttributes } from "@/tiptap/vue-3";

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

  renderHTML({ HTMLAttributes }) {
    const href = HTMLAttributes.href;
    // False positive; we're explicitly checking for javascript: links to ignore them
    // eslint-disable-next-line no-script-url
    if (href?.toString().startsWith("javascript:")) {
      // strip out the href
      return [
        "a",
        mergeAttributes(this.options.HTMLAttributes, {
          ...HTMLAttributes,
          href: "",
        }),
        0,
      ];
    }
    return [
      "a",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      0,
    ];
  },
});

export default Link;
