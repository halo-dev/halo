import type { Attribute } from "@tiptap/core";
import {
  TableCell as TiptapTableCell,
  type TableCellOptions,
} from "@tiptap/extension-table";
import {
  createTableCellAttributes,
  renderTableCellAttributes,
} from "./table-cell-attributes";

export const TableCell = TiptapTableCell.extend<TableCellOptions>({
  fakeSelection: true,

  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A data cell inside a table row. It is not a top-level content block.",
        aliases: ["td"],
        exposure: "recommended",
        useWhen: ["Adding an ordinary value to a table row."],
        avoidWhen: [
          "The cell labels a row or column and should be a header cell.",
        ],
        attributeGuidance: {
          colspan: {
            description: "Number of columns spanned by this cell.",
            examples: [1, 2, 3],
          },
          rowspan: {
            description: "Number of rows spanned by this cell.",
            examples: [1, 2, 3],
          },
          colwidth: {
            description: "Column widths associated with the cell.",
            format: "array of pixel widths",
            examples: [null],
            omitWhen: ["Automatic table sizing is appropriate."],
          },
          align: {
            description: "Horizontal alignment of the cell content.",
            examples: ["left", "center", "right"],
            omitWhen: ["Left alignment is appropriate."],
          },
          verticalAlign: {
            description: "Vertical alignment of the cell content.",
            examples: ["top", "middle", "bottom"],
            omitWhen: ["Top alignment is appropriate."],
          },
          backgroundColor: {
            description: "Background color of the cell.",
            format: "CSS color",
            examples: ["#fee2e2"],
            omitWhen: ["The default cell background is appropriate."],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<table><tbody><tr><td><p>Value</p></td></tr></tbody></table>",
          '<table><tbody><tr><td colspan="2"><p>Value across two columns</p></td></tr></tbody></table>',
        ],
      },
      structure: {
        allowedParents: ["tableRow"],
        description: "tableCell may appear only inside tableRow.",
      },
    };
  },

  addAttributes() {
    const parentAttributes = (this.parent?.() ?? {}) as Record<
      string,
      Attribute
    >;
    return createTableCellAttributes(parentAttributes);
  },

  renderHTML({ node, HTMLAttributes }) {
    return [
      "td",
      renderTableCellAttributes(
        this.options.HTMLAttributes,
        HTMLAttributes,
        node.attrs
      ),
      0,
    ];
  },
});

export default TableCell;
