import TiptapItalic, { type ItalicOptions } from "@tiptap/extension-italic";
import { markRaw } from "vue";
import MingcuteItalicLine from "~icons/mingcute/italic-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionItalicOptions = ExtensionOptions & Partial<ItalicOptions>;

export const ExtensionItalic = TiptapItalic.extend<ExtensionItalicOptions>({
  addKeyboardShortcuts() {
    return defineHaloKeyboardShortcuts(this, [
      {
        id: "editor.format.italic",
        keys: ["Mod-i"],
        label: () => i18n.global.t("editor.common.italic"),
        category: "formatting",
        priority: 20,
      },
    ]);
  },

  addHaloEditorMetadata() {
    return {
      ai: {
        description: "Emphasis for inline text, usually rendered in italics.",
        exposure: "recommended",
        useWhen: ["Adding semantic emphasis or marking a title or term."],
        generation: {
          mode: "direct-html",
        },
        examples: ["<p>This point is <em>especially relevant</em>.</p>"],
      },
    };
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 50,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive(TiptapItalic.name),
            icon: markRaw(MingcuteItalicLine),
            title: i18n.global.t("editor.common.italic"),
            shortcutId: "editor.format.italic",
            action: () => editor.chain().focus().toggleItalic().run(),
          },
        };
      },
    };
  },
});
