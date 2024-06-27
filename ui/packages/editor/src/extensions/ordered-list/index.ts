import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor, Range } from "@/tiptap/vue-3";
import type { ExtensionOptions } from "@/types";
import ExtensionListItem from "@tiptap/extension-list-item";
import type { OrderedListOptions } from "@tiptap/extension-ordered-list";
import TiptapOrderedList from "@tiptap/extension-ordered-list";
import { markRaw } from "vue";
import MdiFormatListNumbered from "~icons/mdi/format-list-numbered";

const OrderedList = TiptapOrderedList.extend<
  ExtensionOptions & OrderedListOptions
>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: Editor }) {
        return {
          priority: 140,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: editor.isActive("orderedList"),
            icon: markRaw(MdiFormatListNumbered),
            title: i18n.global.t("editor.common.ordered_list"),
            action: () => editor.chain().focus().toggleOrderedList().run(),
          },
        };
      },
      getCommandMenuItems() {
        return {
          priority: 140,
          icon: markRaw(MdiFormatListNumbered),
          title: "editor.common.ordered_list",
          keywords: ["orderedlist", "youxuliebiao"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor.chain().focus().deleteRange(range).toggleOrderedList().run();
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
                x: -16,
                y: -1,
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

export default OrderedList;
