import {
  TableRow as TiptapTableRow,
  type TableRowOptions,
} from "@tiptap/extension-table";
import { mergeAttributes } from "@/tiptap";
import { joinStyles, parseRowHeight } from "./attributes";

export const TableRow = TiptapTableRow.extend<TableRowOptions>({
  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A row of header or data cells inside a table. It is not a top-level content block.",
        aliases: ["tr"],
        exposure: "recommended",
        useWhen: ["Adding one record or header row to a table."],
        avoidWhen: ["There is no containing table."],
        attributeGuidance: {
          rowHeight: {
            description: "Height of the row in pixels.",
            format: "number of pixels",
            examples: [60],
            omitWhen: ["Default row sizing is appropriate."],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<table><tbody><tr><td><p>Value</p></td></tr></tbody></table>",
          "<table><tbody><tr><th><p>Label</p></th><th><p>Status</p></th></tr></tbody></table>",
        ],
      },
      structure: {
        allowedParents: ["table"],
        minPerParent: 1,
        description:
          "tableRow may appear only inside table, and each table contains at least one row.",
      },
    };
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      rowHeight: {
        default: null,
        parseHTML: parseRowHeight,
        renderHTML: () => ({}),
      },
    };
  },

  renderHTML({ node, HTMLAttributes }) {
    const rowHeight = node.attrs.rowHeight as number | null;
    const attributes = mergeAttributes(
      this.options.HTMLAttributes,
      HTMLAttributes,
      rowHeight
        ? {
            "data-row-height": String(rowHeight),
            style: joinStyles(
              HTMLAttributes.style as string | undefined,
              `height: ${rowHeight}px`
            ),
          }
        : {}
    );

    return ["tr", attributes, 0];
  },
});

export default TableRow;
