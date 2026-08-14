import { markRaw } from "vue";
import MingcuteEraserLine from "~icons/mingcute/eraser-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import { Extension } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionClearFormatOptions = ExtensionOptions;

export const ExtensionClearFormat =
  Extension.create<ExtensionClearFormatOptions>({
    name: "clearFormat",

    addOptions() {
      return {
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 23,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: false,
              icon: markRaw(MingcuteEraserLine),
              title: i18n.global.t("editor.common.clear_format"),
              shortcutId: "editor.format.clear",
              action: () => editor.chain().focus().unsetAllMarks().run(),
            },
          };
        },
      };
    },

    addKeyboardShortcuts() {
      return defineHaloKeyboardShortcuts(this, [
        {
          id: "editor.format.clear",
          keys: ["Mod-\\"],
          label: () => i18n.global.t("editor.common.clear_format"),
          category: "formatting",
          priority: 120,
          command: () => this.editor.chain().focus().unsetAllMarks().run(),
        },
      ]);
    },
  });
