import type { HighlightOptions } from "@tiptap/extension-highlight";
import TiptapHighlight from "@tiptap/extension-highlight";
import { markRaw } from "vue";
import MingcuteMarkPenLine from "~icons/mingcute/mark-pen-line";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import HighlightToolbarItem from "./HighlightToolbarItem.vue";

export type ExtensionHighlightOptions = ExtensionOptions &
  Partial<HighlightOptions>;

export const ExtensionHighlight =
  TiptapHighlight.extend<ExtensionHighlightOptions>({
    addHaloEditorMetadata() {
      return {
        ai: {
          description:
            "Highlighted inline text with an optional background color.",
          exposure: "available",
          useWhen: ["Drawing attention to a short, especially notable phrase."],
          avoidWhen: ["Highlighting large sections of text."],
          attributeGuidance: {
            color: {
              description: "CSS color used as the highlight background.",
              format: "CSS color",
              examples: ["#fff3a3", "yellow"],
              omitWhen: ["The default highlight color is appropriate."],
            },
          },
          generation: {
            mode: "direct-html",
          },
          examples: [
            "<p>This is <mark>highlighted with the default color</mark>.</p>",
            '<p>This is <mark data-color="#fff3a3" style="background-color: #fff3a3">notable</mark>.</p>',
          ],
        },
      };
    },

    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 80,
            component: markRaw(HighlightToolbarItem),
            props: {
              editor,
              isActive: editor.isActive(TiptapHighlight.name),
              icon: markRaw(MingcuteMarkPenLine),
              title: i18n.global.t("editor.common.highlight"),
            },
          };
        },
      };
    },
  }).configure({ multicolor: true });
