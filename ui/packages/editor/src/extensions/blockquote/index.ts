import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import type { BlockquoteOptions } from "@tiptap/extension-blockquote";
import TiptapBlockquote from "@tiptap/extension-blockquote";
import { markRaw } from "vue";
import MingcuteBlockquoteLine from "~icons/mingcute/blockquote-line";

export type ExtensionBlockquoteOptions = Partial<BlockquoteOptions> &
  ExtensionOptions;

export const ExtensionBlockquote =
  TiptapBlockquote.extend<ExtensionBlockquoteOptions>({
    addOptions() {
      return {
        ...this.parent?.(),
        getToolbarItems({ editor }: { editor: Editor }) {
          return {
            priority: 90,
            component: markRaw(ToolbarItem),
            props: {
              editor,
              isActive: editor.isActive(TiptapBlockquote.name),
              icon: markRaw(MingcuteBlockquoteLine),
              title: i18n.global.t("editor.common.quote"),
              action: () => {
                editor.commands.toggleBlockquote();
              },
            },
          };
        },
      };
    },
  });
