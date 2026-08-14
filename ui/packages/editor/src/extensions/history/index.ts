import { UndoRedo as TiptapHistory } from "@tiptap/extensions";
import { markRaw } from "vue";
import MdiRedoVariant from "~icons/mdi/redo-variant";
import MdiUndoVariant from "~icons/mdi/undo-variant";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export const ExtensionHistory = TiptapHistory.extend<ExtensionOptions>({
  addKeyboardShortcuts() {
    return defineHaloKeyboardShortcuts(this, [
      {
        id: "editor.history.undo",
        keys: ["Mod-z"],
        label: () => i18n.global.t("editor.shortcuts.commands.undo"),
        category: "general",
        priority: 10,
      },
      {
        id: "editor.history.redo",
        keys: ["Shift-Mod-z", "Mod-y"],
        label: () => i18n.global.t("editor.shortcuts.commands.redo"),
        category: "general",
        priority: 20,
      },
    ]);
  },

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
              shortcutId: "editor.history.undo",
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
              shortcutId: "editor.history.redo",
              action: () => editor.chain().redo().focus().run(),
            },
          },
        ];
      },
    };
  },
});
