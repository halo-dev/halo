import Document from "@tiptap/extension-document";
import Paragraph from "@tiptap/extension-paragraph";
import Text from "@tiptap/extension-text";
import { UndoRedo } from "@tiptap/extensions";
import { Editor } from "@/tiptap";
import { ExtensionTable } from "./index";

export function createTableEditor(content = "<p></p>") {
  const element = document.createElement("div");
  document.body.appendChild(element);

  return new Editor({
    element,
    extensions: [Document, Paragraph, Text, UndoRedo, ExtensionTable],
    content,
  });
}

export function insertTable(
  editor: Editor,
  options: { rows?: number; cols?: number; withHeaderRow?: boolean } = {}
) {
  editor.commands.insertTable({
    rows: options.rows ?? 3,
    cols: options.cols ?? 3,
    withHeaderRow: options.withHeaderRow ?? false,
  });
}

export function getCellPositions(editor: Editor) {
  const positions: number[] = [];
  editor.state.doc.descendants((node, pos) => {
    if (
      node.type.spec.tableRole === "cell" ||
      node.type.spec.tableRole === "header_cell"
    ) {
      positions.push(pos);
    }
  });
  return positions;
}

export function getTableNode(editor: Editor) {
  let table:
    | { node: ReturnType<typeof editor.state.doc.nodeAt>; pos: number }
    | undefined;
  editor.state.doc.descendants((node, pos) => {
    if (node.type.spec.tableRole === "table") {
      table = { node, pos };
      return false;
    }
  });
  if (!table?.node) {
    throw new Error("Expected the editor to contain a table.");
  }
  return { node: table.node, pos: table.pos };
}
