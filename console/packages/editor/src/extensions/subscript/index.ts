import type { Editor } from "@/tiptap/vue-3";
import TiptapSubscript from "@tiptap/extension-subscript";
import type { SubscriptExtensionOptions } from "@tiptap/extension-subscript";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiFormatSubscript from "~icons/mdi/format-subscript";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const Subscript = TiptapSubscript.extend<
  ExtensionOptions & SubscriptExtensionOptions
>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 120,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("subscript"),
            icon: markRaw(MdiFormatSubscript),
            title: i18n.global.t("editor.common.subscript"),
            action: () => editor.chain().focus().toggleSubscript().run(),
          },
        };
      },
    };
  },
});

export default Subscript;
