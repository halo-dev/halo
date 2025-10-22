import { ToolbarItem, ToolbarSubItem } from "@/components";
import { i18n } from "@/locales";
import { type Editor } from "@/tiptap/vue-3";
import { FontSize as TiptapFontSize } from "@tiptap/extension-text-style";
import { markRaw } from "vue";
import MdiFormatSize from "~icons/mdi/format-size";

export type FontSizeOptions = {
  types: string[];
};

const FontSize = TiptapFontSize.extend<FontSizeOptions>({
  name: "fontSize",

  addOptions() {
    return {
      types: ["textStyle"],
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 31,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: false,
            icon: markRaw(MdiFormatSize),
          },
          children: [
            {
              priority: 0,
              component: markRaw(ToolbarSubItem),
              props: {
                editor,
                isActive: false,
                title: i18n.global.t("editor.common.text.default"),
                action: () => editor.chain().focus().unsetFontSize().run(),
              },
            },
            ...[8, 10, 12, 14, 16, 18, 20, 24, 30, 36, 48, 60, 72].map(
              (size) => {
                return {
                  priority: size,
                  component: markRaw(ToolbarSubItem),
                  props: {
                    editor,
                    isActive: false,
                    title: `${size} px`,
                    action: () => {
                      return editor
                        .chain()
                        .focus()
                        .setFontSize(`${size}px`)
                        .run();
                    },
                  },
                };
              }
            ),
          ],
        };
      },
    };
  },
});

export default FontSize;
