import { ExtensionTextStyle } from "@/extensions/text-style";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import type { ColorOptions } from "@tiptap/extension-color";
import TiptapColor from "@tiptap/extension-color";
import { markRaw } from "vue";
import MdiFormatColor from "~icons/mdi/format-color";
import ColorToolbarItem from "./ColorToolbarItem.vue";

export type ExtensionColorOptions = Partial<ColorOptions> & ExtensionOptions;

export const ExtensionColor = TiptapColor.extend<ExtensionColorOptions>({
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
    return [ExtensionTextStyle];
  },
});
