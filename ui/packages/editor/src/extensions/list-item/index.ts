import { ListItem as TiptapListItem } from "@tiptap/extension-list";

export const ExtensionListItem = TiptapListItem.extend({
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A single item inside a bullet list or ordered list. It is not a top-level content block.",
        exposure: "recommended",
        useWhen: ["Adding an item to a bullet list or ordered list."],
        avoidWhen: ["There is no containing list."],
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<ul><li><p>An unordered list item</p></li></ul>",
          "<ol><li><p>An ordered list item</p></li></ol>",
          "<ul><li><p>An item with another block</p><blockquote><p>Supporting quotation</p></blockquote></li></ul>",
        ],
      },
      structure: {
        allowedParents: ["bulletList", "orderedList"],
        minPerParent: 1,
        description:
          "listItem may appear only inside bulletList or orderedList, and each such list contains at least one item.",
      },
    };
  },
});
