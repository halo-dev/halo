import { TableRow as BuiltInTableRow } from "@tiptap/extension-table";

const TableRow = BuiltInTableRow.extend({
  allowGapCursor: false,

  addAttributes() {
    return {
      style: {
        default: "height: 60px;",
        parseHTML: (element: HTMLElement) => element.getAttribute("style"),
      },
    };
  },
});

export default TableRow;
