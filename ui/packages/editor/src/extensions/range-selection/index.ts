import NodeRange, {
  getNodeRangeDecorations,
  getSelectionRanges,
  isNodeRangeSelection,
  NodeRangeSelection,
  type NodeRangeOptions,
} from "@tiptap/extension-node-range";
import {
  callOrReturn,
  Decoration,
  DecorationSet,
  getExtensionField,
  Plugin,
  PluginKey,
  type EditorState,
  type EditorView,
  type ParentConfig,
  type ResolvedPos,
  TextSelection,
} from "@/tiptap";

declare module "@tiptap/core" {
  export interface NodeConfig<Options, Storage> {
    /**
     * Whether to display a fake selection state on the node.
     *
     * Used to render node-aligned decorations when a text selection fully
     * contains a complex block.
     */
    fakeSelection?:
      | boolean
      | null
      | ((this: {
          name: string;
          options: Options;
          storage: Storage;
          parent: ParentConfig<NodeConfig<Options>>["fakeSelection"];
        }) => boolean | null);
  }
}

export interface ExtensionRangeSelectionOptions extends NodeRangeOptions {
  /**
   * Whether Shift-ArrowUp and Shift-ArrowDown should extend the node range.
   *
   * Disabled by default to preserve native keyboard text selection.
   */
  arrowShortcuts: boolean;
}

interface MouseFallbackPluginState {
  active: boolean;
  decorations: DecorationSet;
}

interface MouseFallbackPluginMeta {
  active?: boolean;
  decorations?: DecorationSet;
}

const mouseFallbackPluginKey = new PluginKey<MouseFallbackPluginState>(
  "nodeRangeMouseFallback"
);

export function isNodeRangeMouseSelectionActive(state: EditorState) {
  return mouseFallbackPluginKey.getState(state)?.active ?? false;
}

/**
 * Track pointer positions for selection cases that native DOM selection cannot
 * represent reliably, such as dragging across empty blocks or out of a NodeView
 * content region. Text-originated selection remains a TextSelection, while a
 * non-text origin can still fall back to NodeRangeSelection.
 */
function isMouseSelectionEnabled(
  event: MouseEvent,
  key: NodeRangeOptions["key"]
) {
  const isMac = /Mac/.test(navigator.platform);
  const isMod = isMac ? event.metaKey : event.ctrlKey;

  return (
    key === null ||
    key === undefined ||
    (key === "Shift" && event.shiftKey) ||
    (key === "Control" && event.ctrlKey) ||
    (key === "Alt" && event.altKey) ||
    (key === "Meta" && event.metaKey) ||
    (key === "Mod" && isMod)
  );
}

function isEditorUiTarget(target: EventTarget | null) {
  return (
    target instanceof Element &&
    target.closest(
      '[data-editor-ui="true"], [data-drag-handle], .column-resize-handle, .row-resize-handle'
    ) !== null
  );
}

function getTargetElement(target: EventTarget | null) {
  if (target instanceof Element) {
    return target;
  }
  if (target instanceof globalThis.Node) {
    return target.parentElement;
  }
  return null;
}

function isNativeTextSelectionTarget(
  view: EditorView,
  target: EventTarget | null
) {
  const element = getTargetElement(target);
  return (
    element !== null &&
    element !== view.dom &&
    view.dom.contains(element) &&
    element.closest('[contenteditable="false"]') === null
  );
}

interface EditableNodeViewRange {
  from: number;
  to: number;
}

function getEditableNodeViewRange(
  view: EditorView,
  target: EventTarget | null
): EditableNodeViewRange | undefined {
  const content = getTargetElement(target)?.closest<HTMLElement>(
    "[data-node-view-content]"
  );
  if (!content) {
    return;
  }

  try {
    const from = view.posAtDOM(content, 0);
    const to = view.posAtDOM(content, content.childNodes.length);
    return from <= to ? { from, to } : { from: to, to: from };
  } catch {
    return;
  }
}

function clampPosition(position: number, range: EditableNodeViewRange) {
  return Math.min(range.to, Math.max(range.from, position));
}

function getMixedSelectionDecorations(state: EditorState) {
  const { doc, selection } = state;
  if (!(selection instanceof TextSelection) || selection.empty) {
    return DecorationSet.empty;
  }

  const decorations: Decoration[] = [];
  doc.nodesBetween(selection.from, selection.to, (node, pos) => {
    if (
      node.type.spec.fakeSelection &&
      pos >= selection.from &&
      pos + node.nodeSize <= selection.to
    ) {
      decorations.push(
        Decoration.node(pos, pos + node.nodeSize, {
          class: "no-selection range-fake-selection",
        })
      );
    }
  });

  return DecorationSet.create(doc, decorations);
}

function getTopLevelNodeStart($pos: ResolvedPos) {
  return $pos.depth > 0 ? $pos.before(1) : $pos.posAtIndex($pos.index(0), 0);
}

export const ExtensionRangeSelection =
  NodeRange.extend<ExtensionRangeSelectionOptions>({
    name: "rangeSelectionExtension",

    addOptions() {
      return {
        ...this.parent?.(),
        depth: undefined,
        key: null,
        arrowShortcuts: false,
      };
    },

    addKeyboardShortcuts() {
      const shortcuts = { ...(this.parent?.() ?? {}) };

      if (!this.options.arrowShortcuts) {
        delete shortcuts["Shift-ArrowUp"];
        delete shortcuts["Shift-ArrowDown"];
      }

      return shortcuts;
    },

    addProseMirrorPlugins() {
      let activeView: EditorView | undefined;
      let anchorPos: number | undefined;
      let headPos: number | undefined;
      let movedAcrossPosition = false;
      let preferNativeTextSelection = false;
      let editableNodeViewRange: EditableNodeViewRange | undefined;

      const updatePointerPreview = (view: EditorView) => {
        const { doc, selection, tr } = view.state;
        let decorations = DecorationSet.empty;

        if (
          movedAcrossPosition &&
          anchorPos !== undefined &&
          headPos !== undefined &&
          anchorPos !== headPos &&
          !isNodeRangeSelection(selection)
        ) {
          const $anchor = doc.resolve(anchorPos);
          const $head = doc.resolve(headPos);

          if (getTopLevelNodeStart($anchor) !== getTopLevelNodeStart($head)) {
            if (preferNativeTextSelection) {
              if (selection.$anchor.sameParent(selection.$head)) {
                tr.setSelection(TextSelection.between($anchor, $head));
              }
            } else {
              const ranges = getSelectionRanges(
                $anchor.min($head),
                $anchor.max($head),
                this.options.depth
              );
              decorations = getNodeRangeDecorations(ranges);
            }
          }
        }

        tr.setMeta(mouseFallbackPluginKey, {
          active: true,
          decorations,
        } satisfies MouseFallbackPluginMeta);
        view.dispatch(tr);
      };

      const updateHeadPosition = (event: MouseEvent) => {
        if (!activeView) {
          return;
        }

        if (editableNodeViewRange) {
          event.preventDefault();
        }

        const result = activeView.posAtCoords({
          left: event.clientX,
          top: event.clientY,
        });
        if (!result) {
          return;
        }

        if (!editableNodeViewRange && headPos === result.pos) {
          return;
        }

        const pointerPos = result.pos;
        headPos = editableNodeViewRange
          ? clampPosition(pointerPos, editableNodeViewRange)
          : pointerPos;
        movedAcrossPosition ||= pointerPos !== anchorPos;

        if (
          editableNodeViewRange &&
          anchorPos !== undefined &&
          movedAcrossPosition
        ) {
          const { doc, selection, tr } = activeView.state;
          const textSelection = TextSelection.create(doc, anchorPos, headPos);
          if (!selection.eq(textSelection)) {
            tr.setSelection(textSelection);
          }
          tr.setMeta(mouseFallbackPluginKey, {
            active: true,
            decorations: DecorationSet.empty,
          } satisfies MouseFallbackPluginMeta);
          activeView.dispatch(tr);
          return;
        }

        updatePointerPreview(activeView);
      };

      const resetMouseSelection = () => {
        document.removeEventListener("mousemove", updateHeadPosition);
        document.removeEventListener("mouseup", finishMouseSelection);
        activeView = undefined;
        anchorPos = undefined;
        headPos = undefined;
        movedAcrossPosition = false;
        preferNativeTextSelection = false;
        editableNodeViewRange = undefined;
      };

      const finishMouseSelection = (event: MouseEvent) => {
        const view = activeView;
        updateHeadPosition(event);

        const anchor = anchorPos;
        const head = headPos;
        const hasMoved = movedAcrossPosition;
        const shouldPreserveNativeSelection = preferNativeTextSelection;
        const constrainedToEditableNodeView =
          editableNodeViewRange !== undefined;
        resetMouseSelection();

        if (!view) {
          return;
        }

        const { doc, selection } = view.state;
        const tr = view.state.tr.setMeta(mouseFallbackPluginKey, {
          active: false,
          decorations: DecorationSet.empty,
        } satisfies MouseFallbackPluginMeta);

        if (
          !hasMoved ||
          anchor === undefined ||
          head === undefined ||
          anchor === head
        ) {
          view.dispatch(tr);
          return;
        }

        if (constrainedToEditableNodeView) {
          view.dispatch(tr);
          return;
        }

        if (isNodeRangeSelection(selection)) {
          view.dispatch(tr);
          return;
        }

        const $anchor = doc.resolve(anchor);
        const $head = doc.resolve(head);

        if (shouldPreserveNativeSelection) {
          if (
            getTopLevelNodeStart($anchor) !== getTopLevelNodeStart($head) &&
            selection.$anchor.sameParent(selection.$head)
          ) {
            tr.setSelection(TextSelection.between($anchor, $head));
          }
          view.dispatch(tr);
          return;
        }

        if (getTopLevelNodeStart($anchor) === getTopLevelNodeStart($head)) {
          view.dispatch(tr);
          return;
        }

        const $from = $anchor.min($head);
        const $to = $anchor.max($head);
        if (!getSelectionRanges($from, $to, this.options.depth).length) {
          view.dispatch(tr);
          return;
        }

        tr.setSelection(
          NodeRangeSelection.create(doc, anchor, head, this.options.depth)
        );
        view.dispatch(tr);
      };

      return [
        new Plugin<MouseFallbackPluginState>({
          key: mouseFallbackPluginKey,
          state: {
            init: () => ({
              active: false,
              decorations: DecorationSet.empty,
            }),
            apply: (tr, value) => {
              const meta = tr.getMeta(mouseFallbackPluginKey) as
                | MouseFallbackPluginMeta
                | undefined;

              return {
                active: meta?.active ?? value.active,
                decorations:
                  meta?.decorations ??
                  value.decorations.map(tr.mapping, tr.doc),
              };
            },
          },
          view: () => ({
            destroy: resetMouseSelection,
          }),
          props: {
            decorations: (state) => {
              const { selection } = state;
              if (isNodeRangeSelection(selection)) {
                return getNodeRangeDecorations([...selection.ranges]);
              }

              const pointerDecorations =
                mouseFallbackPluginKey.getState(state)?.decorations ??
                DecorationSet.empty;
              if (pointerDecorations.find().length) {
                return pointerDecorations;
              }

              const mixedSelectionDecorations =
                getMixedSelectionDecorations(state);
              return mixedSelectionDecorations.find().length
                ? mixedSelectionDecorations
                : null;
            },
            attributes: (state) => {
              const isNodeRange = isNodeRangeSelection(state.selection);
              const hasPointerPreview = Boolean(
                mouseFallbackPluginKey.getState(state)?.decorations.find()
                  .length
              );

              return {
                class:
                  isNodeRange || hasPointerPreview
                    ? "ProseMirror-noderangeselection"
                    : "",
              };
            },
            handleDOMEvents: {
              mousedown: (view, event) => {
                if (
                  event.button !== 0 ||
                  isEditorUiTarget(event.target) ||
                  !isMouseSelectionEnabled(event, this.options.key)
                ) {
                  return false;
                }

                const result = view.posAtCoords({
                  left: event.clientX,
                  top: event.clientY,
                });
                if (!result) {
                  return false;
                }

                const nextEditableNodeViewRange = getEditableNodeViewRange(
                  view,
                  event.target
                );
                if (nextEditableNodeViewRange && event.detail > 1) {
                  return false;
                }

                resetMouseSelection();
                activeView = view;
                editableNodeViewRange = nextEditableNodeViewRange;
                anchorPos = editableNodeViewRange
                  ? clampPosition(result.pos, editableNodeViewRange)
                  : result.pos;
                headPos = anchorPos;
                preferNativeTextSelection =
                  this.options.key == null &&
                  isNativeTextSelectionTarget(view, event.target);
                const tr = view.state.tr.setMeta(mouseFallbackPluginKey, {
                  active: true,
                  decorations: DecorationSet.empty,
                } satisfies MouseFallbackPluginMeta);
                if (editableNodeViewRange) {
                  tr.setSelection(
                    TextSelection.create(view.state.doc, anchorPos)
                  );
                }
                view.dispatch(tr);
                document.addEventListener("mousemove", updateHeadPosition);
                document.addEventListener("mouseup", finishMouseSelection);

                if (editableNodeViewRange) {
                  view.focus();
                  event.preventDefault();
                  return true;
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
        fakeSelection:
          callOrReturn(
            getExtensionField(extension, "fakeSelection", context)
          ) ?? false,
      };
    },
  });

export {
  getNodeRangeDecorations,
  NodeRange as TiptapNodeRange,
  type GetSelectionRangesOptions,
  type NodeRangeOptions,
} from "@tiptap/extension-node-range";
export { getSelectionRanges, isNodeRangeSelection, NodeRangeSelection };
export { RangeSelection } from "./range-selection";
