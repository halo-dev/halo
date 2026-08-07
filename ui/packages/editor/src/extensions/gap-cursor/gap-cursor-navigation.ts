import type { EditorState } from "@/tiptap";
import {
  GapCursor,
  NodeSelection,
  Selection,
  TextSelection,
  type Command,
  type EditorView,
  type ResolvedPos,
  type Transaction,
} from "@/tiptap/pm";
import {
  findGapCursorFrom,
  isGapCursorPosition,
  isGapCursorTargetNode,
  type GapCursorDirection,
  type GapCursorSide,
} from "@/utils";
import {
  getGapCursorSelectionSide,
  HaloGapCursor,
} from "./gap-cursor-selection";

export type GapCursorArrowAxis = "horizontal" | "vertical";

export function createGapCursorArrowCommand(
  axis: GapCursorArrowAxis,
  direction: GapCursorDirection
): Command {
  const textblockDirection = getTextblockDirection(axis, direction);

  return (state, dispatch, view) => {
    const { selection } = state;
    const side = getGapCursorSelectionSide(selection);

    if (selection instanceof GapCursor) {
      if (axis === "horizontal") {
        return enterGapCursorTarget(state, dispatch, direction, side);
      }
      if (moveToAdjacentTextblock(state, dispatch, direction)) {
        return true;
      }
      if (moveBetweenSharedGapStops(state, dispatch, direction, side)) {
        return true;
      }
    }

    let $start = direction > 0 ? selection.$to : selection.$from;
    let mustMove = selection.empty;
    const preferredSide = getPreferredGapSide(selection, direction);

    if (selection instanceof TextSelection) {
      if (!canLeaveTextblock(view, textblockDirection, $start)) {
        return false;
      }

      const $detailsGap = findCollapsedDetailsGap($start, direction);
      if ($detailsGap) {
        if (isGapCursorPosition($detailsGap)) {
          selectGap(state, dispatch, $detailsGap, preferredSide);
          return true;
        }
      }

      mustMove = false;
      const boundaryPos = direction > 0 ? $start.after() : $start.before();
      $start = state.doc.resolve(boundaryPos);
    }

    const $found = findGapCursorFrom($start, direction, mustMove);
    if (!$found) {
      return false;
    }

    selectGap(state, dispatch, $found, preferredSide);
    return true;
  };
}

export function selectAdjacentBlock(direction: GapCursorDirection): Command {
  return (state, dispatch) => {
    const { selection } = state;
    if (selection instanceof TextSelection) {
      return selectStructuralBlockFromText(
        state,
        dispatch,
        selection,
        direction
      );
    }
    if (!(selection instanceof GapCursor)) {
      return false;
    }
    return selectStructuralBlockFromGap(state, dispatch, selection, direction);
  };
}

function getTextblockDirection(
  axis: GapCursorArrowAxis,
  direction: GapCursorDirection
): "up" | "down" | "left" | "right" {
  if (axis === "vertical") {
    return direction > 0 ? "down" : "up";
  }
  return direction > 0 ? "right" : "left";
}

function getPreferredGapSide(
  selection: Selection,
  direction: GapCursorDirection
): GapCursorSide {
  if (selection instanceof TextSelection) {
    return direction > 0 ? "before" : "after";
  }
  return direction > 0 ? "after" : "before";
}

function canLeaveTextblock(
  view: EditorView | undefined,
  direction: "up" | "down" | "left" | "right",
  $start: ResolvedPos
): boolean {
  if (!view) {
    return false;
  }
  if (!view.endOfTextblock(direction)) {
    return false;
  }
  return $start.depth > 0;
}

function moveToAdjacentTextblock(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  direction: GapCursorDirection
): boolean {
  const selection = findAdjacentTextblockStart(state.selection, direction);
  if (!selection) {
    return false;
  }
  if (dispatch) {
    dispatch(state.tr.setSelection(selection).scrollIntoView());
  }
  return true;
}

function findAdjacentTextblockStart(
  selection: Selection,
  direction: GapCursorDirection
): TextSelection | null {
  if (selection.$from.depth !== 0) {
    return null;
  }

  const adjacent =
    direction < 0 ? selection.$from.nodeBefore : selection.$from.nodeAfter;
  if (!adjacent?.isTextblock) {
    return null;
  }
  if (isGapCursorTargetNode(adjacent)) {
    return null;
  }

  const nodeStart =
    direction < 0 ? selection.from - adjacent.nodeSize : selection.from;
  return TextSelection.create(selection.$from.doc, nodeStart + 1);
}

function moveBetweenSharedGapStops(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  direction: GapCursorDirection,
  side: GapCursorSide | null
): boolean {
  const { selection } = state;
  if (!isGapCursorTargetNode(selection.$from.nodeBefore)) {
    return false;
  }
  if (!isGapCursorTargetNode(selection.$from.nodeAfter)) {
    return false;
  }

  const movingToOtherStop =
    direction > 0 ? side === "after" : side === "before";
  if (!movingToOtherStop) {
    return false;
  }

  const nextSide: GapCursorSide = direction > 0 ? "before" : "after";
  selectGap(state, dispatch, selection.$from, nextSide);
  return true;
}

function enterGapCursorTarget(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  direction: GapCursorDirection,
  side: GapCursorSide | null
): boolean {
  const { selection } = state;
  const target = getTargetToEnter(selection, direction, side);
  if (!target) {
    return false;
  }
  if (!isGapCursorTargetNode(target)) {
    return false;
  }
  if (target.isLeaf) {
    return false;
  }

  const insidePos = selection.from + (side === "before" ? 1 : -1);
  const inside = Selection.findFrom(
    state.doc.resolve(insidePos),
    direction,
    true
  );
  if (!inside) {
    return false;
  }
  if (inside instanceof GapCursor) {
    return false;
  }

  const targetStart =
    side === "before" ? selection.from : selection.from - target.nodeSize;
  if (inside.from <= targetStart) {
    return false;
  }
  if (inside.to >= targetStart + target.nodeSize) {
    return false;
  }

  if (dispatch) {
    dispatch(state.tr.setSelection(inside).scrollIntoView());
  }
  return true;
}

function getTargetToEnter(
  selection: Selection,
  direction: GapCursorDirection,
  side: GapCursorSide | null
) {
  if (side === "before") {
    return direction > 0 ? selection.$from.nodeAfter : null;
  }
  if (side === "after") {
    return direction < 0 ? selection.$from.nodeBefore : null;
  }
  return null;
}

function findCollapsedDetailsGap(
  $pos: ResolvedPos,
  direction: GapCursorDirection
): ResolvedPos | null {
  if ($pos.parent.type.name !== "detailsSummary") {
    return null;
  }

  for (let depth = $pos.depth - 1; depth > 0; depth--) {
    const node = $pos.node(depth);
    if (node.type.name !== "details") {
      continue;
    }
    if (node.attrs.open) {
      return null;
    }
    const gapPos = direction > 0 ? $pos.after(depth) : $pos.before(depth);
    return $pos.doc.resolve(gapPos);
  }
  return null;
}

function selectStructuralBlockFromText(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  selection: TextSelection,
  direction: GapCursorDirection
): boolean {
  if (!selection.empty) {
    return false;
  }
  if (selection.$from.depth === 0) {
    return false;
  }

  const atTextblockEdge =
    direction < 0
      ? selection.$from.parentOffset === 0
      : selection.$from.parentOffset === selection.$from.parent.content.size;
  if (!atTextblockEdge) {
    return false;
  }

  const gapPos =
    direction < 0 ? selection.$from.before() : selection.$from.after();
  const $gap = state.doc.resolve(gapPos);
  const adjacent = direction < 0 ? $gap.nodeBefore : $gap.nodeAfter;
  if (!isGapCursorTargetNode(adjacent)) {
    return false;
  }
  if (!isGapCursorPosition($gap)) {
    return false;
  }

  const side: GapCursorSide = direction < 0 ? "after" : "before";
  selectGap(state, dispatch, $gap, side);
  return true;
}

function selectStructuralBlockFromGap(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  selection: GapCursor,
  direction: GapCursorDirection
): boolean {
  const adjacent =
    direction < 0 ? selection.$from.nodeBefore : selection.$from.nodeAfter;
  if (!adjacent) {
    return false;
  }
  if (!isGapCursorTargetNode(adjacent)) {
    return false;
  }
  if (!NodeSelection.isSelectable(adjacent)) {
    // Keep the gap intact instead of allowing a core command to delete a
    // non-selectable structural node without an intermediate state.
    return true;
  }

  const nodePos =
    direction < 0 ? selection.from - adjacent.nodeSize : selection.from;
  if (dispatch) {
    dispatch(
      state.tr
        .setSelection(NodeSelection.create(state.doc, nodePos))
        .scrollIntoView()
    );
  }
  return true;
}

function selectGap(
  state: EditorState,
  dispatch: ((tr: Transaction) => void) | undefined,
  $pos: ResolvedPos,
  side: GapCursorSide
) {
  if (dispatch) {
    dispatch(
      state.tr.setSelection(new HaloGapCursor($pos, side)).scrollIntoView()
    );
  }
}
