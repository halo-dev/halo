import type { ColorOptions } from "@tiptap/extension-color";
import TiptapColor from "@tiptap/extension-color";
import { markRaw } from "vue";
import MingcuteTextColorLine from "~icons/mingcute/text-color-line";
import { ExtensionTextStyle } from "@/extensions/text-style";
import { defineHaloKeyboardShortcuts } from "@/keyboard-shortcuts";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import ColorToolbarItem from "./ColorToolbarItem.vue";

export type ExtensionColorOptions = Partial<ColorOptions> & ExtensionOptions;

export interface ExtensionColorStorage {
  openToolbarColorPicker?: () => void;
}

declare module "@/tiptap" {
  interface Storage {
    color: ExtensionColorStorage;
  }
}

export const ExtensionColor = TiptapColor.extend<
  ExtensionColorOptions,
  ExtensionColorStorage
>({
  addStorage() {
    return {
      openToolbarColorPicker: undefined,
    };
  },

  addKeyboardShortcuts() {
    return defineHaloKeyboardShortcuts(this, [
      {
        id: "editor.format.color",
        keys: ["Mod-Alt-c"],
        label: () => i18n.global.t("editor.common.color"),
        category: "formatting",
        priority: 70,
        command: () => {
          const openColorPicker = this.storage.openToolbarColorPicker;
          if (!openColorPicker) {
            return false;
          }
          openColorPicker();
          return true;
        },
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
              color: {
                description:
                  "CSS foreground color. Use a value from the active editor palette when one is known.",
                format: "CSS color",
                examples: ["#1f2937", "#2563eb"],
                omitWhen: ["The inherited text color is appropriate."],
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
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 81,
          component: markRaw(ColorToolbarItem),
          props: {
            editor,
            isActive: false,
            icon: markRaw(MingcuteTextColorLine),
            title: i18n.global.t("editor.common.color"),
            shortcutId: "editor.format.color",
          },
        };
      },
    };
  },
  addExtensions() {
    return [ExtensionTextStyle];
  },
});
