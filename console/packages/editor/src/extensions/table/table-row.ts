import { TableRow as BuiltInTableRow } from "@tiptap/extension-table-row";

const TableRow = BuiltInTableRow.extend({
  allowGapCursor: false,
});

export default TableRow;
