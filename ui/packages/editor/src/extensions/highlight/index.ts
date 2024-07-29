import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import type { ExtensionOptions } from "@/types";
import type { HighlightOptions } from "@tiptap/extension-highlight";
import TiptapHighlight from "@tiptap/extension-highlight";
import { markRaw } from "vue";
import MdiFormatColorHighlight from "~icons/mdi/format-color-highlight";
import HighlightToolbarItem from "./HighlightToolbarItem.vue";

const Highlight = TiptapHighlight.extend<ExtensionOptions & HighlightOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 80,
          component: markRaw(HighlightToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("highlight"),
            icon: markRaw(MdiFormatColorHighlight),
            title: i18n.global.t("editor.common.highlight"),
          },
        };
      },
    };
  },
}).configure({ multicolor: true });

export default Highlight;
