import {
  mergeAttributes,
  Node,
  Plugin,
  PluginKey,
  type CommandProps,
} from "@/tiptap";
import type { ExtensionOptions } from "@/types";
import { isEmpty } from "lodash-es";
import Paragraph from "../paragraph";
import { RangeSelection } from "../range-selection";
import FigureCaption from "./figure-caption";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    figure: {
      setFigure: (attrs?: Record<string, unknown>) => ReturnType;
      unsetFigure: () => ReturnType;
      setFigureAlignItems: (
        align: "start" | "center" | "end" | "stretch"
      ) => ReturnType;
    };
  }
}

export interface FigureOptions {
  HTMLAttributes: Record<string, unknown>;
}

const Figure = Node.create<ExtensionOptions & FigureOptions>({
  name: "figure",
  group: "block",
  content: "block+? figureCaption?",
  isolating: true,
  fakeSelection: true,
  // Priority must be higher than paragraph (1000) and code-block to ensure
  // the Backspace shortcut handles figure selection correctly.
  priority: 1100,

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
      alignItems: {
        default: null,
        parseHTML: (element) => {
          const style = element.getAttribute("style") || "";
          const match = style.match(/align-items:\s*([^;]+)/);
          if (match) {
            return match[1].trim();
          }
          return null;
        },
        renderHTML: (attributes) => {
          const alignItems = attributes.alignItems;
          if (isEmpty(alignItems)) {
            return {};
          }
          return {
            style: `align-items: ${alignItems};`,
          };
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
    return [
      "figure",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, {
        style: `display: flex; flex-direction: column;`,
      }),
      0,
    ];
  },

  addExtensions() {
    return [FigureCaption];
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

        if ($from.parent.type.name !== Paragraph.name) {
          return false;
        }

        const beforePos = $from.before($from.depth);
        if (beforePos <= 0) {
          return false;
        }

        const beforeResolve = doc.resolve(beforePos - 1);
        const nodeBefore = beforeResolve.nodeBefore;

        if (!nodeBefore || nodeBefore.type.name !== FigureCaption.name) {
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
                child.type.name !== Paragraph.name ||
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
      setFigureAlignItems:
        (align: "start" | "center" | "end" | "stretch") =>
        ({ commands }: CommandProps) => {
          return commands.updateAttributes(this.name, { alignItems: align });
        },
    };
  },
});

export default Figure;
