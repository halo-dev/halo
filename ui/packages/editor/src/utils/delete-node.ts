import { NodeSelection, Transaction, type ResolvedPos } from "@/tiptap/pm";
import type { Editor } from "@/tiptap/vue-3";

export const deleteNodeByPos = ($pos: ResolvedPos) => {
  return (tr: Transaction) => {
    if ($pos.depth) {
      for (let d = $pos.depth; d > 0; d--) {
        tr.delete($pos.before(d), $pos.after(d)).scrollIntoView();
        return true;
      }
    } else {
      const node = $pos.parent;
      if (!node.isTextblock && node.nodeSize) {
        tr.setSelection(
          NodeSelection.create($pos.doc, $pos.pos)
        ).deleteSelection();
        return true;
      }
    }

    const pos = $pos.pos;

    if (pos) {
      tr.delete(pos, pos + $pos.node().nodeSize);
      return true;
    }
    return false;
  };
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
