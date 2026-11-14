import { HorizontalRule as TiptapHorizontalRule } from "@tiptap/extension-horizontal-rule";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";

export const ExtensionHorizontalRule = TiptapHorizontalRule.extend({
  addKeyboardShortcuts() {
    return defineHaloKeyboardShortcuts(this, [
      {
        id: "editor.structure.horizontalRule",
        keys: ["Mod-Alt-s"],
        label: () => i18n.global.t("editor.common.horizontal_rule"),
        category: "structure",
        priority: 120,
        command: () => this.editor.commands.setHorizontalRule(),
      },
    ]);
  },

  addHaloEditorMetadata() {
    return {
      ai: {
        description: "A thematic break between sections of content.",
        exposure: "available",
        useWhen: ["Separating major shifts in topic or scene."],
        avoidWhen: ["A heading or paragraph break already provides structure."],
        generation: {
          mode: "direct-html",
        },
        examples: ["<hr>"],
      },
    };
  },
});
