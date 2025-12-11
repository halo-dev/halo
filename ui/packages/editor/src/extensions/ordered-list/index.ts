import type { Editor, Range } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import {
  ListItem,
  OrderedList as TiptapOrderedList,
  type OrderedListOptions,
} from "@tiptap/extension-list";
import { markRaw } from "vue";
import MingcuteListOrderedLine from "~icons/mingcute/list-ordered-line";

export type ExtensionOrderedListOptions = Partial<OrderedListOptions> &
  ExtensionOptions;

export const ExtensionOrderedList =
  TiptapOrderedList.extend<ExtensionOrderedListOptions>({
    addOptions() {
      return {
        ...this.parent?.(),
        getCommandMenuItems() {
          return {
            priority: 140,
            icon: markRaw(MingcuteListOrderedLine),
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
