import TiptapSuperscript, {
  type SuperscriptExtensionOptions,
} from "@tiptap/extension-superscript";
import { markRaw } from "vue";
import PhTextSuperscript from "~icons/ph/text-superscript";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionSuperscriptOptions = Partial<SuperscriptExtensionOptions> &
  ExtensionOptions;

export const ExtensionSuperscript =
  TiptapSuperscript.extend<ExtensionSuperscriptOptions>({
    addKeyboardShortcuts() {
      return defineHaloKeyboardShortcuts(this, [
        {
          id: "editor.format.superscript",
          keys: ["Mod-Shift-.", "Mod-."],
          label: () => i18n.global.t("editor.common.superscript"),
          category: "formatting",
          priority: 110,
          command: () => this.editor.commands.toggleSuperscript(),
        },
      ]);
    },

    addHaloEditorMetadata() {
      return {
        ai: {
          description:
            "Inline superscript text for exponents, ordinals, and references.",
          exposure: "available",
          useWhen: ["Writing an exponent or superscript annotation."],
          generation: {
            mode: "direct-html",
          },
          examples: ["<p>x<sup>2</sup></p>"],
        },
      };
    },

    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 110,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive(TiptapSuperscript.name),
              icon: markRaw(PhTextSuperscript),
              title: i18n.global.t("editor.common.superscript"),
              shortcutId: "editor.format.superscript",
              action: () => editor.chain().focus().toggleSuperscript().run(),
            },
          };
        },
      };
    },
  });
