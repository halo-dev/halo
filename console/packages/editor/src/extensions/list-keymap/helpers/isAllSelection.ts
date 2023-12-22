import { AllSelection, EditorState, TextSelection } from "@tiptap/pm/state";

export function isAllSelection(value: unknown): value is AllSelection {
  return value instanceof AllSelection;
}

export function isWholeDocSelected(state: EditorState) {
  const selection = state.selection;
  const selectionAtStart = TextSelection.atStart(state.doc);
  const selectionAtEnd = TextSelection.atEnd(state.doc);

  const selectionHasSameParent =
    selectionAtStart.$from.sameParent(selection.$from) &&
    selectionAtEnd.$from.sameParent(selection.$to);

  const isAllTextSelection =
    selectionHasSameParent &&
    selectionAtStart.$from.pos === selection.$from.pos &&
    selectionAtEnd.$from.pos === selection.$to.pos;

  // allSelection covers cases where selection starts with a nodeSelection
  // we need to check for text selection that covers the whole document as well
  // ref: https://prosemirror.net/docs/ref/#state.AllSelection
  return isAllSelection(state.selection) || isAllTextSelection;
}
