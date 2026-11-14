import {
  FontSize as TiptapFontSize,
  type FontSizeOptions,
} from "@tiptap/extension-text-style";
import { markRaw } from "vue";
import MingcuteFontSizeLine from "~icons/mingcute/font-size-line";
import { ToolbarItem, ToolbarSubItem } from "@/components";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import { type Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

const FONT_SIZES = [8, 10, 12, 14, 16, 18, 20, 24, 30, 36, 48, 60, 72] as const;

function changeFontSize(editor: Editor, direction: -1 | 1): boolean {
  const currentValue = Number.parseFloat(
    editor.getAttributes("textStyle").fontSize ?? "16"
  );
  const target =
    direction > 0
      ? FONT_SIZES.find((size) => size > currentValue)
      : [...FONT_SIZES].reverse().find((size) => size < currentValue);

  if (!target) {
    return false;
  }
  return editor.chain().focus().setFontSize(`${target}px`).run();
}

export type ExtensionFontSizeOptions = Partial<FontSizeOptions> &
  ExtensionOptions;

export const ExtensionFontSize =
  TiptapFontSize.extend<ExtensionFontSizeOptions>({
    name: "fontSize",

    addKeyboardShortcuts() {
      return defineHaloKeyboardShortcuts(this, [
        {
          id: "editor.format.increaseFontSize",
          keys: ["Mod-Alt-+"],
          label: () =>
            i18n.global.t("editor.shortcuts.commands.increase_font_size"),
          category: "formatting",
          priority: 45,
          command: () => changeFontSize(this.editor, 1),
        },
        {
          id: "editor.format.decreaseFontSize",
          keys: ["Mod-Alt--"],
          label: () =>
            i18n.global.t("editor.shortcuts.commands.decrease_font_size"),
          category: "formatting",
          priority: 46,
          command: () => changeFontSize(this.editor, -1),
        },
      ]);
    },

    addHaloEditorMetadata() {
      return {
        contributions: (this.options.types ?? []).map((name) => ({
          targets: [{ kind: "mark" as const, name }],
          metadata: {
            ai: {
              attributeGuidance: {
                fontSize: {
                  description: "CSS font size for the inline text.",
                  format: "CSS length",
                  allowedValues: FONT_SIZES.map((size) => `${size}px`),
                  omitWhen: ["The inherited font size is appropriate."],
                },
              },
            },
          },
        })),
      };
    },

    addOptions() {
      return {
        ...this.parent?.(),
        types: ["textStyle"],
        getToolbarItems({ editor }: { editor: Editor }) {
          const currentFontSize = editor.getAttributes("textStyle").fontSize as
            | string
            | undefined;
          return {
            priority: 31,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: !!currentFontSize,
              icon: markRaw(MingcuteFontSizeLine),
              title: i18n.global.t("editor.extensions.font_size.title"),
              shortcutIds: [
                "editor.format.increaseFontSize",
                "editor.format.decreaseFontSize",
              ],
            },
            children: [
              {
                priority: 0,
                component: markRaw(ToolbarSubItem),
                props: {
                  editor,
                  isActive: !currentFontSize,
                  selectionIndicator: "leading",
                  title: i18n.global.t("editor.common.text.default"),
                  action: () => editor.chain().focus().unsetFontSize().run(),
                },
              },
              ...FONT_SIZES.map((size) => {
                return {
                  priority: size,
                  component: markRaw(ToolbarSubItem),
                  props: {
                    editor,
                    isActive: Number.parseFloat(currentFontSize ?? "") === size,
                    selectionIndicator: "leading",
                    title: `${size} px`,
                    action: () => {
                      return editor
                        .chain()
                        .focus()
                        .setFontSize(`${size}px`)
                        .run();
                    },
                  },
                };
              }),
            ],
          };
        },
      };
    },
  });
