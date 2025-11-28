import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor, Range } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import {
  ListItem,
  BulletList as TiptapBulletList,
  type BulletListOptions,
} from "@tiptap/extension-list";
import { markRaw } from "vue";
import MdiFormatListBulleted from "~icons/mdi/format-list-bulleted";

export type ExtensionBulletListOptions = Partial<BulletListOptions> &
  ExtensionOptions;

export const ExtensionBulletList = TiptapBulletList.extend<ExtensionOptions>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 130,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("bulletList"),
            icon: markRaw(MdiFormatListBulleted),
            title: i18n.global.t("editor.common.bullet_list"),
            action: () => editor.chain().focus().toggleBulletList().run(),
          },
        };
      },
      getCommandMenuItems() {
        return {
          priority: 130,
          icon: markRaw(MdiFormatListBulleted),
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
