import {
  clampIndentLevel,
  getHaloEditorIndentationSettings,
} from "@/editor-metadata/indentation";
import type { Editor, PMNode } from "@/tiptap";

export function insertCommandBlockAfter(
  editor: Editor,
  node: PMNode,
  pos: number
) {
  const insertPos = pos + node.nodeSize;
  const indent = getAdjacentBlockIndent(editor, node);
  const inserted = editor.commands.insertContentAt(
    insertPos,
    [
      {
        type: "paragraph",
        attrs: { indent, lineIndent: false },
        content: [{ type: "text", text: "/" }],
      },
    ],
    { updateSelection: true }
  );
  if (inserted) {
    editor.commands.focus(insertPos + 2, { scrollIntoView: true });
  }
  return inserted;
}

export function getAdjacentBlockIndent(editor: Editor, node: PMNode) {
  const settings = getHaloEditorIndentationSettings(editor);
  if (node.type.isInGroup("list")) {
    return clampIndentLevel(settings.indentRange, settings);
  }
  return clampIndentLevel(Number(node.attrs.indent) || 0, settings);
}
