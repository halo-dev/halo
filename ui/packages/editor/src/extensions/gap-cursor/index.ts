import type {
  Dispatch,
  EditorState,
  EditorView,
  ResolvedPos,
  Transaction,
} from "@/tiptap";
import {
  Extension,
  callOrReturn,
  getExtensionField,
  isActive,
  type ParentConfig,
} from "@/tiptap/core";
import {
  AllSelection,
  Decoration,
  DecorationSet,
  Plugin,
  PluginKey,
  TextSelection,
  keydownHandler,
  type Command,
} from "@/tiptap/pm";
import { deleteNodeByPos } from "@/utils";
import { isEmpty } from "@/utils/isNodeEmpty";
import GapCursorSelection from "./gap-cursor-selection";

declare module "@tiptap/core" {
  interface NodeConfig<Options, Storage> {
    allowGapCursor?:
      | boolean
      | null
      | ((this: {
          name: string;
          options: Options;
          storage: Storage;
          parent: ParentConfig<NodeConfig<Options>>["allowGapCursor"];
        }) => boolean | null);
  }
}

/**
 * Adds GapCursor to top-level nodes
 *
 * When the top-level nodes (nodes with a depth of 1 relative to the doc) have the {@link NodeConfig#allowGapCursor} attribute set to true,
 * a GapCursor can be inserted before and after these nodes.
 *
 * This extension provides the ability to navigate between these nodes using the arrow keys.
 *
 * Note that some nodes and shortcuts may conflict with GapCursor due to their own behaviors, such as:
 *  - CodeBlock nodes
 *  - Backspace on an empty line
 *  - Tab key
 */
const GapCursor = Extension.create({
  priority: 9999,
  name: "gapCursor",

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("custom-gap-cursor"),
        props: {
          decorations: drawGapCursor,

          // If a GapCursor can be created at the current position, use GapCursor instead of other selection types
          createSelectionBetween(_view, $anchor, $head) {
            // TODO: When clicking outside of a node, it will first generate a GapCursorSelection,
            // and then after handleClick returns false, it will turn into a TextSelection.
            // The reason for this issue is that createSelectionBetween is triggered first, at which point
            // GapCursorSelection.valid($head) validation fails, and then handleClick is triggered, at which point validation succeeds.
            return $anchor.pos == $head.pos && GapCursorSelection.valid($head)
              ? new GapCursorSelection($head)
              : null;
          },

          handleClick(view, pos, event) {
            if (!view || !view.editable) {
              return false;
            }
            const clickPos = view.posAtCoords({
              left: event.clientX,
              top: event.clientY,
            });
            // Skip if the click position is inside a node.
            if (clickPos && clickPos.inside > -1) {
              return false;
            }
            const $pos = view.state.doc.resolve(pos);
            if (!GapCursorSelection.valid($pos)) {
              return false;
            }
            view.dispatch(
              view.state.tr.setSelection(new GapCursorSelection($pos))
            );
            return true;
          },

          handleKeyDown: keydownHandler({
            ArrowLeft: arrow("horiz", -1),
            ArrowRight: arrow("horiz", 1),
            ArrowUp: arrow("vert", -1),
            ArrowDown: arrow("vert", 1),
            Enter: (state, dispatch) => {
              const tr = createParagraphNearByGapCursor(state, false);
              if (tr && dispatch) {
                dispatch(tr);
                return true;
              }
              return false;
            },
            Backspace: (state, dispatch) => {
              const { selection, tr } = state;
              if (
                isActive(state, "paragraph") &&
                isEmpty(state.selection.$from.parent) &&
                selection instanceof TextSelection &&
                selection.empty
              ) {
                const { $from } = selection;
                deleteNodeByPos($from)(tr);
                if (dispatch) {
                  const $found = arrowGapCursor(-1, "left", state)(tr);
                  if ($found) {
                    dispatch(tr);
                    return true;
                  }
                }
                return false;
              }

              if (!(selection instanceof GapCursorSelection) || !dispatch) {
                return false;
              }

              const { isStart, $from } = selection;
              const nodeOffset = state.doc.childBefore($from.pos);
              const index = nodeOffset.index;
              const pos = state.doc.resolve(0).posAtIndex(index);

              if (isStart) {
                return handleBackspaceAtStart(pos, state, dispatch);
              } else if (
                nodeOffset.node &&
                deleteNodeByPos(state.doc.resolve(pos))(tr)
              ) {
                dispatch(tr);
                return true;
              }

              return false;
            },
            Tab: (state, dispatch) => {
              const tr = createParagraphNearByGapCursor(state);
              if (tr && dispatch) {
                dispatch(tr);
                return true;
              }
              return false;
            },
          }),

          handleTextInput(view) {
            const { state, dispatch } = view;
            const tr = createParagraphNearByGapCursor(state);
            if (tr && dispatch) {
              dispatch(tr);
            }
            return false;
          },

          handleDOMEvents: {
            /**
             * Solve the issue of inserting text during composition input events, e.g., Chinese input
             */
            beforeinput: (view, event) => {
              const { state, dispatch } = view;
              if (
                event.inputType != "insertCompositionText" ||
                !(state.selection instanceof GapCursorSelection)
              ) {
                return false;
              }
              // TODO: After creating a new node, due to the change in selection, the content of the composition input
              // will be inserted into the newly created node, causing the first character to be created directly.
              const tr = createParagraphNearByGapCursor(state);
              if (tr && dispatch) {
                dispatch(tr);
              }
              return false;
            },
          },
        },
      }),
    ];
  },

  extendNodeSchema(extension) {
    const context = {
      name: extension.name,
      options: extension.options,
      storage: extension.storage,
    };

    return {
      allowGapCursor:
        callOrReturn(getExtensionField(extension, "allowGapCursor", context)) ??
        null,
    };
  },
});

export function handleBackspaceAtStart(
  pos: number,
  state: EditorState,
  dispatch: Dispatch
) {
  const { tr } = state;
  if (pos == 0) {
    return false;
  }

  const $beforePos = state.doc.resolve(pos - 1);
  const parent = $beforePos.parent;

  if (parent.inlineContent || parent.isTextblock) {
    return handleInlineContent($beforePos, state, dispatch);
  }

  if (GapCursorSelection.valid($beforePos) && dispatch) {
    dispatch(tr.setSelection(new GapCursorSelection($beforePos)));
    return true;
  }

  if (deleteNodeByPos($beforePos)(tr) && dispatch) {
    dispatch(tr);
    return true;
  }
  return false;
}

export function handleInlineContent(
  $beforePos: ResolvedPos,
  state: EditorState,
  dispatch: Dispatch
) {
  if ($beforePos.parentOffset == 0 && $beforePos.pos > 1 && dispatch) {
    dispatch(state.tr.delete($beforePos.pos - 1, $beforePos.pos));
    return true;
  }

  if (dispatch) {
    dispatch(
      state.tr.setSelection(TextSelection.create(state.doc, $beforePos.pos))
    );
  }
  return true;
}

/**
 * Handles arrow key navigation for GapCursor
 *
 * This function determines the direction (vertical or horizontal) and
 * the movement (positive or negative) based on the axis and direction parameters.
 *
 * @param {("vert" | "horiz")} axis - The axis of movement, either vertical ("vert") or horizontal ("horiz").
 * @param {number} dir - The direction of movement, positive (1) or negative (-1).
 */
export function arrow(axis: "vert" | "horiz", dir: number): Command {
  const dirStr =
    axis == "vert" ? (dir > 0 ? "down" : "up") : dir > 0 ? "right" : "left";
  return (state, dispatch, view) => {
    const { tr } = state;
    if (arrowGapCursor(dir, dirStr, state, view)(tr) && dispatch) {
      dispatch(tr);
      return true;
    }
    return false;
  };
}

export const arrowGapCursor = (
  dir: number,
  dirStr: any,
  state: EditorState,
  view?: EditorView
) => {
  return (tr: Transaction) => {
    const sel = state.selection;
    let $start = dir > 0 ? sel.$to : sel.$from;
    let mustMove = sel.empty;
    if (sel instanceof TextSelection) {
      // Do nothing if the next node is not at the end of the document or is at the root node.
      if ($start.depth == 0) {
        return;
      }
      if (view && !view.endOfTextblock(dirStr)) {
        return;
      }
      mustMove = false;
      $start = state.doc.resolve(dir > 0 ? $start.after() : $start.before());
      // If inside a node, check if it has reached the boundary of the node
      if ($start.depth > 0) {
        const pos = $start.pos;
        const start = $start.start(1) + 1;
        const end = $start.end(1) - 1;
        if (pos != start && pos != end) {
          return;
        }
      }
    }

    if (sel instanceof GapCursorSelection) {
      return;
    }

    const $found = GapCursorSelection.findGapCursorFrom($start, dir, mustMove);
    if (!$found) {
      return;
    }
    tr.setSelection(new GapCursorSelection($found));
    return $found;
  };
};

/**
 * Creates a new paragraph near the current GapCursor
 *
 * If the current selection is a GapCursorSelection, this function creates a new paragraph node
 * either before or after the current node depending on the selection direction.
 *
 * @param {EditorState} state - The current editor state.
 * @param {boolean} [changeSelection=true] - Whether to change the selection to the new paragraph.
 * @returns {Transaction|undefined} - The updated transaction or undefined if no changes are made.
 */
function createParagraphNearByGapCursor(
  state: EditorState,
  changeSelection = true
) {
  const { tr } = state;
  if (!(state.selection instanceof GapCursorSelection)) {
    return;
  }

  const { isStart, $from } = state.selection;
  // Create a new paragraph node before the current node
  if (state.selection instanceof AllSelection || $from.parent.inlineContent) {
    return;
  }

  const docPos = state.doc.resolve(0);
  const nodeOffset = state.doc.childBefore($from.pos);
  const index = isStart ? nodeOffset.index : nodeOffset.index + 1;
  const pos = docPos.posAtIndex(index);
  tr.insert(pos, state.schema.nodes.paragraph.create());
  if (changeSelection || !isStart) {
    tr.setSelection(TextSelection.create(tr.doc, pos + 1));
    tr.scrollIntoView();
  }

  return tr;
}

/**
 * Draws a visual representation of the GapCursor
 *
 * If the current selection is a GapCursorSelection, this function creates a decoration set
 * to visually indicate the presence of a GapCursor at the appropriate position.
 *
 * @param {EditorState} state - The current editor state.
 * @returns {DecorationSet|null} - The decoration set for the GapCursor or null if not applicable.
 */
function drawGapCursor(state: EditorState) {
  if (!(state.selection instanceof GapCursorSelection)) {
    return null;
  }

  const $head = state.selection.$head;
  if ($head.depth < 1) {
    return null;
  }
  const node = $head.node(1);
  const pos = $head.start(1) - 1;
  const isStart = state.selection.isStart;
  return DecorationSet.create(state.doc, [
    Decoration.node(pos, pos + node.nodeSize, {
      key: "node-gap-cursor",
      class: `card-gap-cursor ${isStart ? "start" : "end"}-card-gap-cursor`,
    }),
  ]);
}

export default GapCursor;
