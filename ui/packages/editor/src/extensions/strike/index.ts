import TiptapStrike, { type StrikeOptions } from "@tiptap/extension-strike";
import { markRaw } from "vue";
import MingcuteStrikethroughLine from "~icons/mingcute/strikethrough-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionStrikeOptions = ExtensionOptions & Partial<StrikeOptions>;

export const ExtensionStrike = TiptapStrike.extend<ExtensionStrikeOptions>({
  addKeyboardShortcuts() {
    const inheritedShortcuts = { ...this.parent?.() };

    // Halo uses Mod-Shift-X for strike. Keeping Tiptap's Mod-Shift-S alias
    // would conflict with the format brush shortcut.
    delete inheritedShortcuts["Mod-Shift-s"];

    return defineHaloKeyboardShortcuts(
      {
        editor: this.editor,
        name: this.name,
        parent: () => inheritedShortcuts,
      },
      [
        {
          id: "editor.format.strike",
          keys: ["Mod-Shift-x"],
          label: () => i18n.global.t("editor.common.strike"),
          category: "formatting",
          priority: 40,
          command: () => this.editor.commands.toggleStrike(),
        },
      ]
    );
  },

  addHaloEditorMetadata() {
    return {
      ai: {
        description: "Inline text shown as deleted or no longer applicable.",
        exposure: "available",
        useWhen: ["Showing an obsolete value while preserving it for context."],
        avoidWhen: ["Merely emphasizing text."],
        generation: {
          mode: "direct-html",
        },
        examples: ["<p>The old value was <s>10</s> 12.</p>"],
      },
    };
  },

  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 70,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive(TiptapStrike.name),
            icon: markRaw(MingcuteStrikethroughLine),
            title: i18n.global.t("editor.common.strike"),
            shortcutId: "editor.format.strike",
            action: () => editor.chain().focus().toggleStrike().run(),
          },
        };
      },
    };
  },
});
