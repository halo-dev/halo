import type { Editor, Range } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import {
  ListItem,
  BulletList as TiptapBulletList,
  type BulletListOptions,
} from "@tiptap/extension-list";
import { markRaw } from "vue";
import MingcuteListCheckLine from "~icons/mingcute/list-check-line";

export type ExtensionBulletListOptions = Partial<BulletListOptions> &
  ExtensionOptions;

export const ExtensionBulletList = TiptapBulletList.extend<ExtensionOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getCommandMenuItems() {
        return {
          priority: 130,
          icon: markRaw(MingcuteListCheckLine),
          title: "editor.common.bullet_list",
          keywords: ["bulletlist", "wuxuliebiao"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor.chain().focus().deleteRange(range).toggleBulletList().run();
          },
        };
      },
    };
  },
  addExtensions() {
    return [ListItem];
  },
});
