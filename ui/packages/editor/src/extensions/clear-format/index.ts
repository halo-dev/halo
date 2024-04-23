import { Extension } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import type { Editor } from "@/tiptap";
import { markRaw } from "vue";
import IconParkSolidClearFormat from "~icons/icon-park-solid/clear-format";
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
            icon: markRaw(IconParkSolidClearFormat),
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
