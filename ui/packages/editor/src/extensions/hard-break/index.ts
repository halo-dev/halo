import { HardBreak as TiptapHardBreak } from "@tiptap/extension-hard-break";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";

export const ExtensionHardBreak = TiptapHardBreak.extend({
  addKeyboardShortcuts() {
    return defineHaloKeyboardShortcuts(this, [
      {
        id: "editor.structure.hardBreak",
        keys: ["Shift-Enter", "Mod-Enter"],
        label: () => i18n.global.t("editor.shortcuts.commands.hard_break"),
        category: "structure",
        priority: 140,
      },
    ]);
  },

  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A line break inside the current text block without starting a new paragraph.",
        exposure: "available",
        useWhen: ["A semantic line break is required within one block."],
        avoidWhen: ["A new paragraph better represents the separation."],
        generation: {
          mode: "direct-html",
        },
        examples: ["<p>First line<br>Second line</p>"],
      },
    };
  },
});
