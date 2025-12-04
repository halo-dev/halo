import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import TiptapUnderline, {
  type UnderlineOptions,
} from "@tiptap/extension-underline";
import { markRaw } from "vue";
import MingcuteUnderlineLine from "~icons/mingcute/underline-line";

export type ExtensionUnderlineOptions = ExtensionOptions &
  Partial<UnderlineOptions>;

export const ExtensionUnderline =
  TiptapUnderline.extend<ExtensionUnderlineOptions>({
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
