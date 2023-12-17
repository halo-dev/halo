import type { Editor } from "@/tiptap/vue-3";
import TiptapSuperscript from "@tiptap/extension-superscript";
import type { SuperscriptExtensionOptions } from "@tiptap/extension-superscript";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiFormatSuperscript from "~icons/mdi/format-superscript";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const Superscript = TiptapSuperscript.extend<
  ExtensionOptions & SuperscriptExtensionOptions
>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 110,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("superscript"),
            icon: markRaw(MdiFormatSuperscript),
            title: i18n.global.t("editor.common.superscript"),
            action: () => editor.chain().focus().toggleSuperscript().run(),
          },
        };
      },
    };
  },
});

export default Superscript;
