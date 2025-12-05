import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import type { HighlightOptions } from "@tiptap/extension-highlight";
import TiptapHighlight from "@tiptap/extension-highlight";
import { markRaw } from "vue";
import MingcuteMarkPenLine from "~icons/mingcute/mark-pen-line";
import HighlightToolbarItem from "./HighlightToolbarItem.vue";

export type ExtensionHighlightOptions = ExtensionOptions &
  Partial<HighlightOptions>;

export const ExtensionHighlight =
  TiptapHighlight.extend<ExtensionHighlightOptions>({
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
