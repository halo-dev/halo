import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { UndoRedo as TiptapHistory } from "@tiptap/extensions";
import { markRaw } from "vue";
import MdiRedoVariant from "~icons/mdi/redo-variant";
import MdiUndoVariant from "~icons/mdi/undo-variant";

export const ExtensionHistory = TiptapHistory.extend<ExtensionOptions>({
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
