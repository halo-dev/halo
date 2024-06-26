import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import ToolbarSubItem from "@/components/toolbar/ToolbarSubItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions, ToolbarItem as TypeToolbarItem } from "@/types";
import type { ParagraphOptions } from "@tiptap/extension-paragraph";
import TiptapParagraph from "@tiptap/extension-paragraph";
import { markRaw } from "vue";
import TablerLineHeight from "~icons/tabler/line-height";

const Paragraph = TiptapParagraph.extend<ExtensionOptions & ParagraphOptions>({
  addAttributes() {
    return {
      lineHeight: {
        default: null,
        parseHTML: (element) => {
          return element.style.lineHeight;
        },
        renderHTML: (attributes) => {
          const lineHeight = attributes.lineHeight;
          if (!lineHeight) {
            return {};
          }
          return {
            style: `line-height: ${lineHeight}`,
          };
        },
      },
    };
  },
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
      getToolbarItems({ editor }: { editor: Editor }): TypeToolbarItem {
        return {
          priority: 220,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: !!editor.getAttributes(Paragraph.name)?.lineHeight,
            icon: markRaw(TablerLineHeight),
            title: i18n.global.t("editor.common.line_height"),
          },
          children: [0, 1, 1.5, 2, 2.5, 3].map((lineHeight) => {
            return {
              priority: lineHeight,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive:
                  editor.getAttributes(Paragraph.name)?.lineHeight ===
                  lineHeight,
                title: !lineHeight
                  ? i18n.global.t("editor.common.text.default")
                  : String(lineHeight),
                action: () =>
                  editor
                    .chain()
                    .focus()
                    .updateAttributes(Paragraph.name, {
                      lineHeight,
                    })
                    .run(),
              },
            };
          }),
        };
      },
    };
  },
});

export default Paragraph;
