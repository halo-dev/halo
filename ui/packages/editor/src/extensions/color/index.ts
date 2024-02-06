import type { ExtensionOptions } from "@/types";
import TiptapColor from "@tiptap/extension-color";
import type { ColorOptions } from "@tiptap/extension-color";
import TiptapTextStyle from "@tiptap/extension-text-style";
import type { Editor } from "@/tiptap/vue-3";
import { markRaw } from "vue";
import MdiFormatColor from "~icons/mdi/format-color";
import ColorToolbarItem from "./ColorToolbarItem.vue";
import { i18n } from "@/locales";

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
    return [TiptapTextStyle];
  },
});

export default Color;
