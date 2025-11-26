import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import TiptapSuperscript, {
  type SuperscriptExtensionOptions,
} from "@tiptap/extension-superscript";
import { markRaw } from "vue";
import MdiFormatSuperscript from "~icons/mdi/format-superscript";

export type ExtensionSuperscriptOptions = Partial<SuperscriptExtensionOptions> &
  ExtensionOptions;

export const ExtensionSuperscript =
  TiptapSuperscript.extend<ExtensionSuperscriptOptions>({
    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 110,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive("superscript"),
              icon: markRaw(MdiFormatSuperscript),
              title: i18n.global.t("editor.common.superscript"),
              action: () => editor.chain().focus().toggleSuperscript().run(),
            },
          };
        },
      };
    },
  });
