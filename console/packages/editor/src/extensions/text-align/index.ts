import type { Editor } from "@/tiptap/vue-3";
import TiptapTextAlign from "@tiptap/extension-text-align";
import type { TextAlignOptions } from "@tiptap/extension-text-align";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiFormatAlignLeft from "~icons/mdi/format-align-left";
import MdiFormatAlignCenter from "~icons/mdi/format-align-center";
import MdiFormatAlignRight from "~icons/mdi/format-align-right";
import MdiFormatAlignJustify from "~icons/mdi/format-align-justify";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const TextAlign = TiptapTextAlign.extend<ExtensionOptions & TextAlignOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 180,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive({ textAlign: "left" }),
              icon: markRaw(MdiFormatAlignLeft),
              title: i18n.global.t("editor.common.align_left"),
              action: () => editor.chain().focus().setTextAlign("left").run(),
            },
          },
          {
            priority: 190,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive({ textAlign: "center" }),
              icon: markRaw(MdiFormatAlignCenter),
              title: i18n.global.t("editor.common.align_center"),
              action: () => editor.chain().focus().setTextAlign("center").run(),
            },
          },
          {
            priority: 200,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive({ textAlign: "right" }),
              icon: markRaw(MdiFormatAlignRight),
              title: i18n.global.t("editor.common.align_right"),
              action: () => editor.chain().focus().setTextAlign("right").run(),
            },
          },
          {
            priority: 210,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive({ textAlign: "justify" }),
              icon: markRaw(MdiFormatAlignJustify),
              title: i18n.global.t("editor.common.align_justify"),
              action: () =>
                editor.chain().focus().setTextAlign("justify").run(),
            },
          },
        ];
      },
    };
  },
});

export default TextAlign;
