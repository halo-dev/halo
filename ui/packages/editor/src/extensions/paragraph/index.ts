import TiptapParagraph from "@tiptap/extension-paragraph";
import type { ParagraphOptions } from "@tiptap/extension-paragraph";
import type { ExtensionOptions } from "@/types";

const Paragraph = TiptapParagraph.extend<ExtensionOptions & ParagraphOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getDraggable() {
        return {
          getRenderContainer({ dom }) {
            let container = dom;
            while (container && container.tagName !== "P") {
              container = container.parentElement as HTMLElement;
            }
            return {
              el: container,
              dragDomOffset: {
                y: -1,
              },
            };
          },
          allowPropagationDownward: true,
        };
      },
    };
  },
});

export default Paragraph;
