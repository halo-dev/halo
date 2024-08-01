import { ToolbarItem, ToolbarSubItem } from "@/components";
import { i18n } from "@/locales";
import { Extension, type Editor } from "@/tiptap/vue-3";
import TextStyle from "@/extensions/text-style";
import { markRaw } from "vue";
import MdiFormatSize from "~icons/mdi/format-size";

export type FontSizeOptions = {
  types: string[];
};

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    fontSize: {
      setFontSize: (size: number) => ReturnType;
      unsetFontSize: () => ReturnType;
    };
  }
}

const FontSize = Extension.create<FontSizeOptions>({
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
                    action: () =>
                      editor.chain().focus().setFontSize(size).run(),
                  },
                };
              }
            ),
          ],
        };
      },
    };
  },

  addGlobalAttributes() {
    return [
      {
        types: this.options.types,
        attributes: {
          fontSize: {
            default: null,
            parseHTML: (element) => {
              return element.style.fontSize || "";
            },
            renderHTML: (attributes) => {
              if (!attributes.fontSize) {
                return attributes;
              }

              return {
                style: `font-size: ${attributes.fontSize
                  .toString()
                  .replace("px", "")}px`,
              };
            },
          },
        },
      },
    ];
  },

  addCommands() {
    return {
      setFontSize:
        (size) =>
        ({ chain }) => {
          return chain().setMark("textStyle", { fontSize: size }).run();
        },
      unsetFontSize:
        () =>
        ({ chain }) => {
          return chain()
            .setMark("textStyle", { fontSize: null })
            .removeEmptyTextStyle()
            .run();
        },
    };
  },
  addExtensions() {
    return [TextStyle];
  },
});

export default FontSize;
