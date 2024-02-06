import type { Editor } from "@/tiptap/vue-3";
import TiptapItalic from "@tiptap/extension-italic";
import type { ItalicOptions } from "@tiptap/extension-italic";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiFormatItalic from "~icons/mdi/format-italic";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const Italic = TiptapItalic.extend<ExtensionOptions & ItalicOptions>({
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

export default Italic;
