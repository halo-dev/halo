import { type Editor, type Range } from "@/tiptap";
import { findAncestorListItems, getBlockIndentAtSelection } from "@/utils";

export function setCodeBlockWithIndent(
  editor: Editor,
  range?: Range,
  attributes: Record<string, unknown> = {}
) {
  const listItemNames = findAncestorListItems(editor.state.selection.$from).map(
    ({ node }) => node.type.name
  );
  const indent = getBlockIndentAtSelection(editor);
  const codeBlockAttributes = {
    ...attributes,
    language:
      typeof attributes.language === "string" ? attributes.language : "",
    ...(indent > 0 ? { indent } : {}),
  };
  const chain = editor.chain().focus();
  if (range) {
    chain.deleteRange(range);
  }
  for (const name of listItemNames) {
    chain.liftListItem(name);
  }
  return chain.setCodeBlock(codeBlockAttributes).run();
}
