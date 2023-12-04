import type { Editor } from "@/tiptap/vue-3";
import TiptapUnderline from "@tiptap/extension-underline";
import type { UnderlineOptions } from "@tiptap/extension-underline";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiFormatUnderline from "~icons/mdi/format-underline";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const Underline = TiptapUnderline.extend<ExtensionOptions & UnderlineOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 60,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("underline"),
            icon: markRaw(MdiFormatUnderline),
            title: i18n.global.t("editor.common.underline"),
            action: () => editor.chain().focus().toggleUnderline().run(),
          },
        };
      },
    };
  },
});

export default Underline;
