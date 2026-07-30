import NodeRange, {
  getNodeRangeDecorations,
  getSelectionRanges,
  isNodeRangeSelection,
  NodeRangeSelection,
  type NodeRangeOptions,
} from "@tiptap/extension-node-range";
import {
  callOrReturn,
  DecorationSet,
  getExtensionField,
  Plugin,
  PluginKey,
  type EditorState,
  type EditorView,
  type ParentConfig,
  type ResolvedPos,
} from "@/tiptap";

declare module "@tiptap/core" {
  export interface NodeConfig<Options, Storage> {
    /**
     * Whether to display a fake selection state on the node.
     *
     * @deprecated NodeRange renders node-aligned selection decorations without
     * requiring nodes to opt in.
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
 * NodeRange converts the native selection on mouseup. Empty text blocks and
 * complex NodeViews may not produce a cross-parent native selection, so the
 * fallback keeps the pointer positions needed to render the same official
 * preview and construct the final NodeRangeSelection.
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
      const parentPlugins = this.parent?.() ?? [];
      let activeView: EditorView | undefined;
      let anchorPos: number | undefined;
      let headPos: number | undefined;
      let movedAcrossPosition = false;

      const updatePointerPreview = (view: EditorView) => {
        const { doc, selection, tr } = view.state;
        let decorations = DecorationSet.empty;

        if (
          movedAcrossPosition &&
          anchorPos !== undefined &&
          headPos !== undefined &&
          anchorPos !== headPos &&
          !isNodeRangeSelection(selection) &&
          selection.$anchor.sameParent(selection.$head)
        ) {
          const $anchor = doc.resolve(anchorPos);
          const $head = doc.resolve(headPos);

          if (getTopLevelNodeStart($anchor) !== getTopLevelNodeStart($head)) {
            const ranges = getSelectionRanges(
              $anchor.min($head),
              $anchor.max($head),
              this.options.depth
            );
            decorations = getNodeRangeDecorations(ranges);
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

        const result = activeView.posAtCoords({
          left: event.clientX,
          top: event.clientY,
        });
        if (!result) {
          return;
        }

        if (headPos === result.pos) {
          return;
        }

        headPos = result.pos;
        movedAcrossPosition ||= headPos !== anchorPos;
        updatePointerPreview(activeView);
      };

      const resetMouseSelection = () => {
        document.removeEventListener("mousemove", updateHeadPosition);
        document.removeEventListener("mouseup", finishMouseSelection);
        activeView = undefined;
        anchorPos = undefined;
        headPos = undefined;
        movedAcrossPosition = false;
      };

      const finishMouseSelection = (event: MouseEvent) => {
        const view = activeView;
        updateHeadPosition(event);

        const anchor = anchorPos;
        const head = headPos;
        const hasMoved = movedAcrossPosition;
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

        if (
          isNodeRangeSelection(selection) ||
          !selection.$anchor.sameParent(selection.$head)
        ) {
          view.dispatch(tr);
          return;
        }

        const $anchor = doc.resolve(anchor);
        const $head = doc.resolve(head);
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
        ...parentPlugins,
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
            decorations: (state) =>
              mouseFallbackPluginKey.getState(state)?.decorations ?? null,
            attributes: (state) => {
              const hasPointerPreview = Boolean(
                mouseFallbackPluginKey.getState(state)?.decorations.find()
                  .length
              );

              return {
                class: hasPointerPreview
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

                resetMouseSelection();
                activeView = view;
                anchorPos = result.pos;
                headPos = result.pos;
                view.dispatch(
                  view.state.tr.setMeta(mouseFallbackPluginKey, {
                    active: true,
                    decorations: DecorationSet.empty,
                  } satisfies MouseFallbackPluginMeta)
                );
                document.addEventListener("mousemove", updateHeadPosition);
                document.addEventListener("mouseup", finishMouseSelection);

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
