import TiptapSubscript, {
  type SubscriptExtensionOptions,
} from "@tiptap/extension-subscript";
import { markRaw } from "vue";
import PhTextSubscript from "~icons/ph/text-subscript";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionSubscriptOptions = Partial<SubscriptExtensionOptions> &
  ExtensionOptions;

export const ExtensionSubscript =
  TiptapSubscript.extend<ExtensionSubscriptOptions>({
    addKeyboardShortcuts() {
      return defineHaloKeyboardShortcuts(this, [
        {
          id: "editor.format.subscript",
          keys: ["Mod-Shift-,", "Mod-,"],
          label: () => i18n.global.t("editor.common.subscript"),
          category: "formatting",
          priority: 120,
          command: () => this.editor.commands.toggleSubscript(),
        },
      ]);
    },

    addHaloEditorMetadata() {
      return {
        ai: {
          description:
            "Inline subscript text for chemical formulas and similar notation.",
          exposure: "available",
          useWhen: ["Writing a subscript such as the 2 in H2O."],
          generation: {
            mode: "direct-html",
          },
          examples: ["<p>H<sub>2</sub>O</p>"],
        },
      };
    },

    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 120,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive(TiptapSubscript.name),
              icon: markRaw(PhTextSubscript),
              title: i18n.global.t("editor.common.subscript"),
              shortcutId: "editor.format.subscript",
              action: () => editor.chain().focus().toggleSubscript().run(),
            },
          };
        },
      };
    },
  });
