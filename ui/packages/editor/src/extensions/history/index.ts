import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import type { ExtensionOptions } from "@/types";
import type { HistoryOptions } from "@tiptap/extension-history";
import TiptapHistory from "@tiptap/extension-history";
import { markRaw } from "vue";
import MdiRedoVariant from "~icons/mdi/redo-variant";
import MdiUndoVariant from "~icons/mdi/undo-variant";

const History = TiptapHistory.extend<ExtensionOptions & HistoryOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 10,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: false,
              icon: markRaw(MdiUndoVariant),
              title: i18n.global.t("editor.menus.undo"),
              action: () => editor.chain().undo().focus().run(),
            },
          },
          {
            priority: 20,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: false,
              icon: markRaw(MdiRedoVariant),
              title: i18n.global.t("editor.menus.redo"),
              action: () => editor.chain().redo().focus().run(),
            },
          },
        ];
      },
    };
  },
});

export default History;
