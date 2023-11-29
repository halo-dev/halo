import type { Editor, Range } from "@/tiptap/vue-3";
import TiptapBulletList from "@tiptap/extension-bullet-list";
import type { BulletListOptions } from "@tiptap/extension-bullet-list";
import ExtensionListItem from "@tiptap/extension-list-item";
import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import MdiFormatListBulleted from "~icons/mdi/format-list-bulleted";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions } from "@/types";

const BulletList = TiptapBulletList.extend<
  ExtensionOptions & BulletListOptions
>({
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
      getDraggable() {
        return {
          getRenderContainer({ dom }) {
            let container = dom;
            while (container && !(container.tagName === "LI")) {
              container = container.parentElement as HTMLElement;
            }
            return {
              el: container,
              dragDomOffset: {
                x: -12,
              },
            };
          },
        };
      },
    };
  },
  addExtensions() {
    return [ExtensionListItem];
  },
});

export default BulletList;
