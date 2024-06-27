import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import type { ExtensionOptions } from "@/types";
import type { StrikeOptions } from "@tiptap/extension-strike";
import TiptapStrike from "@tiptap/extension-strike";
import { markRaw } from "vue";
import MdiFormatStrikethrough from "~icons/mdi/format-strikethrough";

const Strike = TiptapStrike.extend<ExtensionOptions & StrikeOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 70,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("strike"),
            icon: markRaw(MdiFormatStrikethrough),
            title: i18n.global.t("editor.common.strike"),
            action: () => editor.chain().focus().toggleStrike().run(),
          },
        };
      },
    };
  },
});

export default Strike;
