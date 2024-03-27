import type { EditorState, MarkRange, Transaction } from "@/tiptap";
import { AddMarkStep, CellSelection, RemoveMarkStep } from "@/tiptap/pm";

/**
 * get its marks through the first text node in the selector
 *
 * @param state editor state
 * @returns the marks of the current first text node
 */
export const getMarksByFirstTextNode = (state: EditorState): MarkRange[] => {
  const marks: MarkRange[] = [];
  const { doc, selection } = state;
  const { from, to, empty } = selection;
  if (empty) {
    return marks;
  }

  let flag = false;
  doc.nodesBetween(from, to, (node, pos) => {
    if (!node || node?.nodeSize === undefined) {
      return;
    }

    if (node.isText && !flag) {
      flag = true;
      marks.push(
        ...node.marks.map((mark) => ({
          from: pos,
          to: pos + node.nodeSize,
          mark,
        }))
      );
      return false;
    }
  });
  return marks;
};

/**
 *
 * Set marks for the text in the currently selected content. This method will first remove all marks
 * from the currently selected text, and then add marks again.
 *
 * For CellSelection, it is necessary to iterate through ranges and set marks for each range.
 *
 * @param state editor state
 * @param marks the marks to be set
 * @param transaction transaction
 *
 * @returns transaction
 *
 * */
export const setMarks = (
  state: EditorState,
  marks: MarkRange[],
  transaction?: Transaction
): Transaction => {
  const { selection } = state;
  const tr = transaction || state.tr;
  const { from, to } = selection;

  // When selection is CellSelection, iterate through ranges
  if (selection instanceof CellSelection) {
    selection.ranges.forEach((cellRange) => {
      const range = {
        from: cellRange.$from.pos,
        to: cellRange.$to.pos,
      };
      setMarksByRange(tr, state, range, marks);
    });
  } else {
    setMarksByRange(
      tr,
      state,
      {
        from,
        to,
      },
      marks
    );
  }

  return tr;
};

export const setMarksByRange = (
  tr: Transaction,
  state: EditorState,
  range: {
    from: number;
    to: number;
  },
  marks: MarkRange[]
) => {
  const { from, to } = range;
  state.doc.nodesBetween(from, to, (node, pos) => {
    if (!node || node?.nodeSize === undefined) {
      return;
    }

    if (node.isText) {
      // the range of the current text node
      const range = {
        from: Math.max(pos, from),
        to: Math.min(pos + node.nodeSize, to),
      };
      // remove all marks of the current text node
      node.marks.forEach((mark) => {
        tr.step(new RemoveMarkStep(range.from, range.to, mark));
      });
      // add all marks of the current text node
      marks.forEach((mark) => {
        tr.step(new AddMarkStep(range.from, range.to, mark.mark));
      });
    }

    return true;
  });
};
