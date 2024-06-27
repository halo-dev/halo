import {
  Decoration,
  DecorationSet,
  EditorView,
  Extension,
  Plugin,
  PluginKey,
  callOrReturn,
  getExtensionField,
  type ParentConfig,
} from "@/tiptap";
import RangeSelection from "./range-selection";

declare module "@tiptap/core" {
  export interface NodeConfig<Options, Storage> {
    /**
     * Whether to allow displaying a fake selection state on the node.
     *
     * Typically, it is only necessary to display a fake selection state on child nodes,
     * so the parent node can be set to false.
     *
     * default: true
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

const range = {
  anchor: 0,
  head: 0,
  enable: false,
};
const ExtensionRangeSelection = Extension.create({
  priority: 100,
  name: "rangeSelectionExtension",

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("rangeSelectionPlugin"),
        props: {
          decorations: ({ doc, selection }) => {
            const { isEditable, isFocused } = this.editor;
            if (!isEditable || !isFocused) {
              return null;
            }
            if (!(selection instanceof RangeSelection)) {
              return null;
            }
            const { $from, $to } = selection;
            const decorations: Decoration[] = [];
            doc.nodesBetween($from.pos, $to.pos, (node, pos) => {
              if (node.isText || node.type.name === "paragraph") {
                return;
              }
              if (node.type.spec.fakeSelection) {
                decorations.push(
                  Decoration.node(pos, pos + node.nodeSize, {
                    class: "no-selection range-fake-selection",
                  })
                );
              }
            });
            return DecorationSet.create(doc, decorations);
          },

          createSelectionBetween: (view, anchor, head) => {
            if (anchor.pos === head.pos) {
              return null;
            }
            return RangeSelection.valid(view.state, anchor.pos, head.pos)
              ? new RangeSelection(anchor, head)
              : null;
          },
          handleDOMEvents: {
            mousedown: (view: EditorView, event) => {
              const coords = { left: event.clientX, top: event.clientY };
              const $pos = view.posAtCoords(coords);
              if (!$pos || !$pos.pos) {
                return;
              }
              range.enable = true;
              range.anchor = $pos.pos;
            },
            mousemove: (view, event) => {
              if (!range.enable) {
                return;
              }
              const coords = { left: event.clientX, top: event.clientY };
              const $pos = view.posAtCoords(coords);
              if (
                !$pos ||
                !$pos.pos ||
                $pos.pos === range.anchor ||
                $pos.pos === range.head
              ) {
                return;
              }
              range.head = $pos.pos;
              const selection = RangeSelection.between(
                view.state.doc.resolve(range.anchor),
                view.state.doc.resolve(range.head)
              );
              if (selection) {
                view.dispatch(view.state.tr.setSelection(selection));
              }
            },
            mouseup: () => {
              range.enable = false;
              range.anchor = 0;
              range.head = 0;
            },
            mouseleave: () => {
              range.enable = false;
              range.anchor = 0;
              range.head = 0;
            },
          },
        },
      }),
    ];
  },

  addKeyboardShortcuts() {
    return {
      "Mod-a": ({ editor }) => {
        editor.view.dispatch(
          editor.view.state.tr.setSelection(
            RangeSelection.allRange(editor.view.state.doc)
          )
        );
        return true;
      },
    };
  },

  extendNodeSchema(extension) {
    const context = {
      name: extension.name,
      options: extension.options,
      storage: extension.storage,
    };

    return {
      fakeSelection:
        callOrReturn(getExtensionField(extension, "fakeSelection", context)) ??
        false,
    };
  },
});

export { ExtensionRangeSelection, RangeSelection };
