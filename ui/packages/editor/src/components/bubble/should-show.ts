import { isNodeRangeSelection } from "@/extensions/range-selection";
import type { Editor, EditorState, EditorView } from "@/tiptap";
import type { NodeBubbleMenuType } from "@/types";

export interface BubbleMenuShouldShowProps {
  editor: Editor;
  element: HTMLElement;
  view: EditorView;
  state: EditorState;
  oldState?: EditorState;
  from: number;
  to: number;
}

export function shouldShowBubbleMenu(
  props: BubbleMenuShouldShowProps,
  bubbleMenu: NodeBubbleMenuType
) {
  if (!props.editor.isEditable || isNodeRangeSelection(props.state.selection)) {
    return false;
  }

  return bubbleMenu.shouldShow?.(props);
}
