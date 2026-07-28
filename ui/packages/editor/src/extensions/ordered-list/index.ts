import {
  OrderedList as TiptapOrderedList,
  type OrderedListOptions,
} from "@tiptap/extension-list";
import { markRaw } from "vue";
import MingcuteListOrderedLine from "~icons/mingcute/list-ordered-line";
import type { Editor, Range } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { ExtensionListItem } from "../list-item";

export type ExtensionOrderedListOptions = Partial<OrderedListOptions> &
  ExtensionOptions;

export const ExtensionOrderedList =
  TiptapOrderedList.extend<ExtensionOrderedListOptions>({
    addHaloEditorMetadata() {
      return {
        ai: {
          description: "An ordered list whose item sequence is meaningful.",
          aliases: ["numbered list"],
          exposure: "recommended",
          useWhen: ["Presenting steps, rankings, or another ordered sequence."],
          avoidWhen: ["Item order is not significant."],
          attributeGuidance: {
            start: {
              description: "Starting ordinal for the first list item.",
              examples: [1, 3, 10],
              omitWhen: ["The list starts at 1."],
            },
            type: {
              description: "HTML numbering style.",
              allowedValues: ["1", "a", "A", "i", "I", null],
              omitWhen: ["Default decimal numbering is appropriate."],
            },
          },
          generation: {
            mode: "direct-html",
          },
          examples: [
            "<ol><li><p>First step</p></li><li><p>Second step</p></li></ol>",
            '<ol start="3" type="I"><li><p>Third numbered item</p></li><li><p>Fourth numbered item</p></li></ol>',
          ],
        },
      };
    },

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
      return [ExtensionListItem];
    },
  });
