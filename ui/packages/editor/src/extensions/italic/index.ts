import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import TiptapItalic, { type ItalicOptions } from "@tiptap/extension-italic";
import { markRaw } from "vue";
import MdiFormatItalic from "~icons/mdi/format-italic";

export type ExtensionItalicOptions = ExtensionOptions & Partial<ItalicOptions>;

export const ExtensionItalic = TiptapItalic.extend<ExtensionItalicOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 50,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("italic"),
            icon: markRaw(MdiFormatItalic),
            title: i18n.global.t("editor.common.italic"),
            action: () => editor.chain().focus().toggleItalic().run(),
          },
        };
      },
    };
  },
});
