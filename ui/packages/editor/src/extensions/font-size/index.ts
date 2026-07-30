import {
  FontSize as TiptapFontSize,
  type FontSizeOptions,
} from "@tiptap/extension-text-style";
import { markRaw } from "vue";
import MingcuteFontSizeLine from "~icons/mingcute/font-size-line";
import { ToolbarItem, ToolbarSubItem } from "@/components";
import { i18n } from "@/locales";
import { type Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

const FONT_SIZES = [8, 10, 12, 14, 16, 18, 20, 24, 30, 36, 48, 60, 72] as const;

export type ExtensionFontSizeOptions = Partial<FontSizeOptions> &
  ExtensionOptions;

export const ExtensionFontSize =
  TiptapFontSize.extend<ExtensionFontSizeOptions>({
    name: "fontSize",

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
          return {
            priority: 31,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: false,
              icon: markRaw(MingcuteFontSizeLine),
              title: i18n.global.t("editor.extensions.font_size.title"),
            },
            children: [
              {
                priority: 0,
                component: markRaw(ToolbarSubItem),
                props: {
                  editor,
                  isActive: false,
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
                    isActive: false,
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
