import TiptapTextAlign, {
  type TextAlignOptions,
} from "@tiptap/extension-text-align";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

export type ExtensionTextAlignOptions = ExtensionOptions &
  Partial<TextAlignOptions>;

export const ExtensionTextAlign =
  TiptapTextAlign.extend<ExtensionTextAlignOptions>({
    addKeyboardShortcuts() {
      return defineHaloKeyboardShortcuts(this, [
        {
          id: "editor.format.alignLeft",
          keys: ["Mod-Shift-l"],
          label: () => i18n.global.t("editor.common.align_left"),
          category: "formatting",
          priority: 80,
          command: () => this.editor.commands.setTextAlign("left"),
        },
        {
          id: "editor.format.alignCenter",
          keys: ["Mod-Shift-c", "Mod-Shift-e"],
          label: () => i18n.global.t("editor.common.align_center"),
          category: "formatting",
          priority: 90,
          command: () => this.editor.commands.setTextAlign("center"),
        },
        {
          id: "editor.format.alignRight",
          keys: ["Mod-Shift-r"],
          label: () => i18n.global.t("editor.common.align_right"),
          category: "formatting",
          priority: 100,
          command: () => this.editor.commands.setTextAlign("right"),
        },
        {
          id: "editor.format.alignJustify",
          keys: ["Mod-Shift-j"],
          label: () => i18n.global.t("editor.common.align_justify"),
          category: "formatting",
          priority: 110,
          command: () => this.editor.commands.setTextAlign("justify"),
        },
      ]);
    },

    addHaloEditorMetadata() {
      return {
        contributions: (this.options.types ?? []).map((name) => ({
          targets: [{ kind: "node" as const, name }],
          metadata: {
            ai: {
              attributeGuidance: {
                textAlign: {
                  description: "Horizontal alignment of text in this block.",
                  allowedValues: this.options.alignments,
                  omitWhen: ["The editor default alignment is appropriate."],
                },
              },
            },
          },
        })),
      };
    },
  }).configure({
    types: ["heading", "paragraph"],
  });
