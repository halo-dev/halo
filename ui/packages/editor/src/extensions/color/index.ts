import type { ColorOptions } from "@tiptap/extension-color";
import TiptapColor from "@tiptap/extension-color";
import { markRaw } from "vue";
import MingcuteTextColorLine from "~icons/mingcute/text-color-line";
import { ExtensionTextStyle } from "@/extensions/text-style";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import ColorToolbarItem from "./ColorToolbarItem.vue";

export type ExtensionColorOptions = Partial<ColorOptions> & ExtensionOptions;

export const ExtensionColor = TiptapColor.extend<ExtensionColorOptions>({
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
          },
        };
      },
    };
  },
  addExtensions() {
    return [ExtensionTextStyle];
  },
});
