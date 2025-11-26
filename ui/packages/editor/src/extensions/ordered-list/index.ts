import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import type { Editor, Range } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import {
  ListItem,
  OrderedList as TiptapOrderedList,
  type OrderedListOptions,
} from "@tiptap/extension-list";
import { markRaw } from "vue";
import MdiFormatListNumbered from "~icons/mdi/format-list-numbered";

export type ExtensionOrderedListOptions = Partial<OrderedListOptions> &
  ExtensionOptions;

export const ExtensionOrderedList =
  TiptapOrderedList.extend<ExtensionOrderedListOptions>({
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
              editor
                .chain()
                .focus()
                .deleteRange(range)
                .toggleOrderedList()
                .run();
            },
          };
        },
      };
    },
    addExtensions() {
      return [ListItem];
    },
  });
