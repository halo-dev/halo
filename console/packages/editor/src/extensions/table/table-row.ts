import { TableRow as BuiltInTableRow } from "@tiptap/extension-table-row";

const TableRow = BuiltInTableRow.extend({
  allowGapCursor: false,

  addAttributes() {
    return {
      ...this.parent?.(),
      style: {
        default: "height: 60px;",
        parseHTML: (element) => element.getAttribute("style"),
      },
    };
  },
});

export default TableRow;
