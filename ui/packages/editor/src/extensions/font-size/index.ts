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
                  allowedValues: [
                    "8px",
                    "10px",
                    "12px",
                    "14px",
                    "16px",
                    "18px",
                    "20px",
                    "24px",
                    "30px",
                    "36px",
                    "48px",
                    "60px",
                    "72px",
                  ],
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
              ...[8, 10, 12, 14, 16, 18, 20, 24, 30, 36, 48, 60, 72].map(
                (size) => {
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
                }
              ),
            ],
          };
        },
      };
    },
  });
