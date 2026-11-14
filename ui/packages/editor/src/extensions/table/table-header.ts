import type { Attribute } from "@tiptap/core";
import {
  TableHeader as TiptapTableHeader,
  type TableHeaderOptions,
} from "@tiptap/extension-table";
import {
  createTableCellAttributes,
  renderTableCellAttributes,
} from "./table-cell-attributes";

export const TableHeader = TiptapTableHeader.extend<TableHeaderOptions>({
  fakeSelection: true,

  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A header cell inside a table row. It is not a top-level content block.",
        aliases: ["th"],
        exposure: "recommended",
        useWhen: ["Labeling the meaning of a table row or column."],
        avoidWhen: ["The cell contains ordinary table data."],
        attributeGuidance: {
          colspan: {
            description: "Number of columns spanned by this header cell.",
            examples: [1, 2, 3],
          },
          rowspan: {
            description: "Number of rows spanned by this header cell.",
            examples: [1, 2, 3],
          },
          colwidth: {
            description: "Column widths associated with the header cell.",
            format: "array of pixel widths",
            examples: [null],
            omitWhen: ["Automatic table sizing is appropriate."],
          },
          align: {
            description: "Horizontal alignment of the header cell content.",
            examples: ["left", "center", "right"],
            omitWhen: ["Left alignment is appropriate."],
          },
          verticalAlign: {
            description: "Vertical alignment of the header cell content.",
            examples: ["top", "middle", "bottom"],
            omitWhen: ["Top alignment is appropriate."],
          },
          backgroundColor: {
            description: "Background color of the header cell.",
            format: "CSS color",
            examples: ["#fee2e2"],
            omitWhen: ["The default header background is appropriate."],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<table><tbody><tr><th><p>Label</p></th></tr></tbody></table>",
          '<table><tbody><tr><th rowspan="2"><p>Grouped label</p></th><th><p>First value</p></th></tr><tr><th><p>Second value</p></th></tr></tbody></table>',
        ],
      },
      structure: {
        allowedParents: ["tableRow"],
        description: "tableHeader may appear only inside tableRow.",
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
      "th",
      renderTableCellAttributes(
        this.options.HTMLAttributes,
        HTMLAttributes,
        node.attrs
      ),
      0,
    ];
  },
});

export default TableHeader;
