import type { Editor } from "@/tiptap";
import { Extension } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { markRaw } from "vue";
import MdiEraser from "~icons/mdi/eraser";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";

const clearFormat = Extension.create<ExtensionOptions>({
  addOptions() {
    return {
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 23,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: false,
            icon: markRaw(MdiEraser),
            title: i18n.global.t("editor.common.clear_format"),
            action: () => editor.chain().focus().unsetAllMarks().run(),
          },
        };
      },
    };
  },

  addKeyboardShortcuts() {
    return {
      "Mod-\\": () => this.editor.chain().focus().unsetAllMarks().run(),
    };
  },
});

export default clearFormat;
