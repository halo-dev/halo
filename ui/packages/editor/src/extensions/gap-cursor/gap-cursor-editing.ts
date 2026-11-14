import type { EditorState } from "@/tiptap";
import {
  GapCursor,
  Selection,
  Slice,
  TextSelection,
  type Transaction,
} from "@/tiptap/pm";
import {
  createTextblockFragmentAt,
  isGapCursorPosition,
  isGapCursorTargetNode,
  type GapCursorDirection,
  type GapCursorSide,
} from "@/utils";
import { skipTrailingNodeMeta } from "../trailing-node";
import { selectAdjacentBlock } from "./gap-cursor-navigation";
import {
  getGapCursorSelectionSide,
  HaloGapCursor,
} from "./gap-cursor-selection";

export function createTextblockAtGapCursor(
  state: EditorState,
  keepGapCursor = false
): Transaction | null {
  if (!(state.selection instanceof GapCursor)) {
    return null;
  }

  const { $from } = state.selection;
  const fragment = createTextblockFragmentAt(state, $from);
  if (!fragment) {
    return null;
  }

  const tr = state.tr.replace($from.pos, $from.pos, new Slice(fragment, 0, 0));
  if (keepGapCursor) {
    const gapPos = $from.pos + fragment.size;
    tr.setSelection(new HaloGapCursor(tr.doc.resolve(gapPos), "before"));
    return tr;
  }

  tr.setSelection(TextSelection.near(tr.doc.resolve($from.pos + 1)));
  return tr;
}

export function backspaceAtGapCursor(
  state: EditorState,
  dispatch?: (tr: Transaction) => void
): boolean {
  const { selection } = state;
  if (selection instanceof TextSelection) {
    if (deleteEmptyTextblockNextToStructuralBlock(state, dispatch, -1)) {
      return true;
    }
    return selectAdjacentBlock(-1)(state, dispatch);
  }
  if (!(selection instanceof GapCursor)) {
    return false;
  }

  const side = getGapCursorSelectionSide(selection);
  if (side === "after") {
    return replaceBlockAtAfterGap(state, dispatch, selection);
  }
  return deleteBeforeGap(state, dispatch, selection);
}

export function deleteForwardAtGapCursor(
  state: EditorState,
  dispatch?: (tr: Transaction) => void
): boolean {
  if (deleteEmptyTextblockNextToStructuralBlock(state, dispatch, 1)) {
    return true;
  }
  return selectAdjacentBlock(1)(state, dispatch);
}

export function insertTextblockAtGapCursor(
  state: EditorState,
  dispatch?: (tr: Transaction) => void
): boolean {
  const tr = createTextblockAtGapCursor(state);
  if (!tr) {
    return false;
  }
  if (dispatch) {
    dispatch(tr.scrollIntoView());
  }
  return true;
}

export function insertTextblockAndKeepGapCursor(
  state: EditorState,
  dispatch?: (tr: Transaction) => void
): boolean {
  const side = getGapCursorSelectionSide(state.selection);
  const tr = createTextblockAtGapCursor(state, side === "before");
  if (!tr) {
    return false;
  }
  if (dispatch) {
    dispatch(tr.scrollIntoView());
  }
  return true;
}

function replaceBlockAtAfterGap(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  selection: GapCursor
): boolean {
  const target = selection.$from.nodeBefore;
  if (!isGapCursorTargetNode(target)) {
    return true;
  }
  if (!target) {
    return true;
  }
  if (!dispatch) {
    return true;
  }

  const from = selection.from - target.nodeSize;
  const fragment = createTextblockFragmentAt(state, selection.$from);
  if (!fragment) {
    return true;
  }

  const tr = state.tr.replace(from, selection.from, new Slice(fragment, 0, 0));
  tr.setSelection(TextSelection.near(tr.doc.resolve(from + 1)));
  dispatch(tr.scrollIntoView());
  return true;
}

function deleteBeforeGap(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  selection: GapCursor
): boolean {
  const previous = selection.$from.nodeBefore;
  if (!previous) {
    return true;
  }
  if (previous.isTextblock && previous.content.size === 0) {
    deleteEmptyTextblockBeforeGap(
      state,
      dispatch,
      selection,
      previous.nodeSize
    );
    return true;
  }
  if (isGapCursorTargetNode(previous)) {
    deleteStructuralBlockBeforeGap(
      state,
      dispatch,
      selection,
      previous.nodeSize
    );
    return true;
  }

  const previousSelection = Selection.findFrom(selection.$from, -1, true);
  if (!previousSelection) {
    return true;
  }
  if (dispatch) {
    dispatch(state.tr.setSelection(previousSelection).scrollIntoView());
  }
  return true;
}

function deleteEmptyTextblockBeforeGap(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  selection: GapCursor,
  nodeSize: number
) {
  if (!dispatch) {
    return;
  }

  const gapPos = selection.from - nodeSize;
  const tr = state.tr.delete(gapPos, selection.from);
  tr.setSelection(new HaloGapCursor(tr.doc.resolve(gapPos), "before"));
  dispatch(tr.scrollIntoView());
}

function deleteStructuralBlockBeforeGap(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  selection: GapCursor,
  nodeSize: number
) {
  if (!dispatch) {
    return;
  }

  const gapPos = selection.from - nodeSize;
  const tr = state.tr.delete(gapPos, selection.from);
  const $mappedGap = tr.doc.resolve(gapPos);
  const nextSelection = isGapCursorPosition($mappedGap)
    ? new HaloGapCursor($mappedGap, "before")
    : Selection.near($mappedGap, 1);
  tr.setSelection(nextSelection);
  dispatch(tr.scrollIntoView());
}

function deleteEmptyTextblockNextToStructuralBlock(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  direction: GapCursorDirection
): boolean {
  const { selection } = state;
  if (!isEmptyTopLevelTextblockSelection(selection)) {
    return false;
  }

  const from = selection.$from.before();
  const to = selection.$from.after();
  const adjacent =
    direction < 0
      ? state.doc.resolve(from).nodeBefore
      : state.doc.resolve(to).nodeAfter;
  if (!isGapCursorTargetNode(adjacent)) {
    return false;
  }

  if (dispatch) {
    const tr = state.tr.delete(from, to);
    if (direction < 0) {
      // Prevent TrailingNode from recreating a final empty paragraph during
      // any appendTransaction round belonging to this deletion.
      tr.setMeta(skipTrailingNodeMeta, true);
    }

    const $mappedGap = tr.doc.resolve(from);
    const side: GapCursorSide = direction < 0 ? "after" : "before";
    const nextSelection = isGapCursorPosition($mappedGap)
      ? new HaloGapCursor($mappedGap, side)
      : Selection.near($mappedGap, direction);
    tr.setSelection(nextSelection);
    dispatch(tr.scrollIntoView());
  }
  return true;
}

function isEmptyTopLevelTextblockSelection(
  selection: Selection
): selection is TextSelection {
  if (!(selection instanceof TextSelection)) {
    return false;
  }
  if (!selection.empty) {
    return false;
  }
  if (selection.$from.depth !== 1) {
    return false;
  }

  const { $from } = selection;
  if (!$from.parent.isTextblock) {
    return false;
  }
  if (isGapCursorTargetNode($from.parent)) {
    return false;
  }
  if ($from.parent.content.size !== 0) {
    return false;
  }
  return $from.parentOffset === 0;
}
