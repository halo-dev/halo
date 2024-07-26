import { i18n } from "@/locales";
import type { Editor } from "@/tiptap/vue-3";
import type { ExtensionOptions } from "@/types";
import type { ColorOptions } from "@tiptap/extension-color";
import TiptapColor from "@tiptap/extension-color";
import TextStyle from "@/extensions/text-style";
import { markRaw } from "vue";
import MdiFormatColor from "~icons/mdi/format-color";
import ColorToolbarItem from "./ColorToolbarItem.vue";

const Color = TiptapColor.extend<ColorOptions & ExtensionOptions>({
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
            icon: markRaw(MdiFormatColor),
            title: i18n.global.t("editor.common.color"),
          },
        };
      },
    };
  },
  addExtensions() {
    return [TextStyle];
  },
});

export default Color;
