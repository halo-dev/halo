import type { Dispatch, Editor } from "@/tiptap/vue-3";
import { type ResolvedPos, type EditorState, NodeSelection } from "@/tiptap/pm";

export const deleteNodeByPos = (
  $pos: ResolvedPos,
  state: EditorState,
  dispatch: Dispatch
) => {
  const done = false;

  if ($pos.depth) {
    for (let d = $pos.depth; d > 0; d--) {
      if (dispatch) {
        dispatch(
          state.tr.delete($pos.before(d), $pos.after(d)).scrollIntoView()
        );
      }
      return true;
    }
  } else {
    const node = $pos.parent;
    if (!node.isTextblock && node.nodeSize && dispatch) {
      dispatch(
        state.tr
          .setSelection(NodeSelection.create(state.doc, $pos.pos))
          .deleteSelection()
      );
      return true;
    }
  }

  if (!done) {
    const pos = $pos.pos;

    if (pos) {
      if (dispatch) {
        dispatch(state.tr.delete(pos, pos + $pos.node().nodeSize));
        return true;
      }
    }
  }
  return done;
};

export const deleteNode = (nodeType: string, editor: Editor) => {
  const { state } = editor;
  const $pos = state.selection.$anchor;
  const done = false;

  if ($pos.depth) {
    for (let d = $pos.depth; d > 0; d--) {
      const node = $pos.node(d);
      if (node.type.name === nodeType) {
        // @ts-ignore
        if (editor.dispatchTransaction)
          // @ts-ignore
          editor.dispatchTransaction(
            state.tr.delete($pos.before(d), $pos.after(d)).scrollIntoView()
          );
        return true;
      }
    }
  } else {
    // @ts-ignore
    const node = state.selection.node;
    if (node && node.type.name === nodeType) {
      editor.chain().deleteSelection().run();
      return true;
    }
  }

  if (!done) {
    const pos = $pos.pos;

    if (pos) {
      const node = state.tr.doc.nodeAt(pos);

      if (node && node.type.name === nodeType) {
        // @ts-ignore
        if (editor.dispatchTransaction)
          // @ts-ignore
          editor.dispatchTransaction(state.tr.delete(pos, pos + node.nodeSize));
        return true;
      }
    }
  }

  return done;
};
