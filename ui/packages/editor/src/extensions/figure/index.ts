import {
  findChildren,
  mergeAttributes,
  Node,
  Plugin,
  PluginKey,
  type CommandProps,
} from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { ExtensionParagraph } from "../paragraph";
import { RangeSelection } from "../range-selection";
import { ExtensionFigureCaption } from "./figure-caption";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    figure: {
      setFigure: (attrs?: Record<string, unknown>) => ReturnType;
      unsetFigure: () => ReturnType;
      updateFigureContainerWidth: (width: string) => ReturnType;
    };
  }
}

export interface ExtensionFigureOptions extends ExtensionOptions {
  HTMLAttributes: Record<string, unknown>;
}

export const ExtensionFigure = Node.create<ExtensionFigureOptions>({
  name: "figure",
  group: "block",
  content: "(image|video|audio)? figureCaption?",
  isolating: true,
  // Priority must be higher than paragraph (1000) and code-block to ensure
  // the Backspace shortcut handles figure selection correctly.

  addOptions() {
    return {
      HTMLAttributes: {},
      getToolbarItems() {
        return [];
      },
    };
  },

  addAttributes() {
    return {
      contentType: {
        default: null,
        parseHTML: (element) => element.getAttribute("data-content-type"),
        renderHTML: (attributes) => {
          return {
            "data-content-type": attributes.contentType,
          };
        },
      },
      position: {
        default: "left",
        parseHTML: (element) => {
          return element.getAttribute("data-position");
        },
        renderHTML: (attributes) => {
          return { "data-position": attributes.position };
        },
      },
    };
  },

  parseHTML() {
    return [
      {
        tag: "figure",
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    const alignItemsMap: Record<string, string> = {
      left: "start",
      center: "center",
      right: "end",
    };
    const alignItems =
      alignItemsMap[HTMLAttributes["data-position"]] || "start";
    return [
      "figure",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, {
        style: `display: flex; flex-direction: column; align-items: ${alignItems}`,
      }),
      0,
    ];
  },

  addExtensions() {
    return [ExtensionFigureCaption];
  },

  addKeyboardShortcuts() {
    return {
      Backspace: ({ editor }) => {
        const { state } = editor;
        const { selection, doc } = state;
        const { $from, empty } = selection;
        if (!empty || $from.parentOffset !== 0) {
          return false;
        }

        if ($from.parent.type.name !== ExtensionParagraph.name) {
          return false;
        }

        const beforePos = $from.before($from.depth);
        if (beforePos <= 0) {
          return false;
        }

        const beforeResolve = doc.resolve(beforePos - 1);
        const nodeBefore = beforeResolve.nodeBefore;

        if (
          !nodeBefore ||
          nodeBefore.type.name !== ExtensionFigureCaption.name
        ) {
          return false;
        }
        let depth = beforeResolve.depth;
        while (depth > 0) {
          const node = beforeResolve.node(depth);
          if (node.type.name === this.name) {
            const figurePos = beforeResolve.before(depth);
            const rangeSelection = RangeSelection.create(
              doc,
              figurePos,
              figurePos + node.nodeSize
            );
            const tr = state.tr.setSelection(rangeSelection);
            editor.view.dispatch(tr);
            return true;
          }
          depth--;
        }
        return false;
      },
    };
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("figureAutoDelete"),
        appendTransaction: (transactions, _oldState, newState) => {
          const docChanged = transactions.some((tr) => tr.docChanged);
          if (!docChanged) {
            return null;
          }
          const tr = newState.tr;
          const nodesToDelete: { pos: number; size: number }[] = [];

          newState.doc.descendants((node, pos) => {
            if (node.type.name !== this.name) {
              return;
            }

            let hasValidContent = false;

            node.forEach((child) => {
              if (
                child.type.name !== ExtensionParagraph.name ||
                child.childCount > 0 ||
                child.textContent.trim().length > 0
              ) {
                hasValidContent = true;
              }
            });

            if (!hasValidContent) {
              nodesToDelete.push({
                pos: pos,
                size: node.nodeSize,
              });
            }
          });

          nodesToDelete
            .sort((a, b) => b.pos - a.pos)
            .forEach(({ pos, size }) => {
              tr.delete(pos, pos + size);
            });

          return nodesToDelete.length > 0 ? tr : null;
        },
      }),
    ];
  },

  addCommands() {
    return {
      setFigure:
        (attrs?: Record<string, unknown>) =>
        ({ commands }: CommandProps) => {
          return commands.wrapIn(this.name, attrs);
        },
      unsetFigure:
        () =>
        ({ commands }: CommandProps) => {
          return commands.lift(this.name);
        },
      updateFigureContainerWidth:
        (width: string) =>
        ({ state }: CommandProps) => {
          // Here we need to use this.editor instead of the dispatch and tr obtained from CommandProps
          // Because the dispatch and tr in CommandProps are outdated
          const { dispatch } = this.editor.view;
          const { tr } = this.editor.state;
          const { selection } = state;
          const { $from } = selection;

          const figureCaptionNodes = findChildren(
            $from.node(),
            (node) => node.type.name === ExtensionFigureCaption.name
          );

          if (figureCaptionNodes.length === 0) {
            return false;
          }

          const figureCaptionNode = figureCaptionNodes[0];
          tr.setNodeMarkup(figureCaptionNode.pos + 1, undefined, {
            width: width,
          });
          dispatch?.(tr);
          return true;
        },
    };
  },
});
