import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import type { ExtensionOptions } from "@/types";
import type { BoldOptions } from "@tiptap/extension-bold";
import TiptapBold from "@tiptap/extension-bold";
import { markRaw } from "vue";
import MdiFormatBold from "~icons/mdi/format-bold";

const Bold = TiptapBold.extend<ExtensionOptions & BoldOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 40,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("bold"),
            icon: markRaw(MdiFormatBold),
            title: i18n.global.t("editor.common.bold"),
            action: () => {
              editor.chain().focus().toggleBold().run();
            },
          },
        };
      },
    };
  },
});

export default Bold;
