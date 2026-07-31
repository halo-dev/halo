import TiptapUnderline, {
  type UnderlineOptions,
} from "@tiptap/extension-underline";
import { markRaw } from "vue";
import MingcuteUnderlineLine from "~icons/mingcute/underline-line";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";

export type ExtensionUnderlineOptions = ExtensionOptions &
  Partial<UnderlineOptions>;

export const ExtensionUnderline =
  TiptapUnderline.extend<ExtensionUnderlineOptions>({
    addHaloEditorMetadata() {
      return {
        ai: {
          description: "Underlined inline text.",
          exposure: "available",
          useWhen: ["Underline is explicitly requested."],
          avoidWhen: ["The text could be mistaken for a hyperlink."],
          generation: {
            mode: "direct-html",
          },
          examples: ["<p><u>Underlined text</u></p>"],
        },
      };
    },

    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 60,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive(TiptapUnderline.name),
              icon: markRaw(MingcuteUnderlineLine),
              title: i18n.global.t("editor.common.underline"),
              action: () => editor.chain().focus().toggleUnderline().run(),
            },
          };
        },
      };
    },
  });
