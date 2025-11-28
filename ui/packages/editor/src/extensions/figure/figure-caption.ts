import { i18n } from "@/locales";
import {
  isActive,
  mergeAttributes,
  Node,
  Plugin,
  PluginKey,
  TextSelection,
  VueNodeViewRenderer,
} from "@/tiptap";
import FigureCaptionView from "./FigureCaptionView.vue";

export const ExtensionFigureCaption = Node.create({
  name: "figureCaption",
  content: "text*",
  inline: false,
  group: "block",

  addAttributes() {
    return {
      "data-placeholder": {
        default: null,
        parseHTML: (element) => element.getAttribute("data-placeholder"),
        renderHTML: (attributes) => {
          return {
            "data-placeholder":
              attributes.dataPlaceholder ||
              i18n.global.t(
                "editor.extensions.figure_caption.empty_placeholder"
              ),
          };
        },
      },
      width: {
        default: null,
        parseHTML: (element) => {
          const style = element.getAttribute("style");
          if (style) {
            const match = style.match(/width:\s*([^;]+)/);
            if (match) {
              return match[1].trim();
            }
          }
          return null;
        },
        renderHTML: (attributes) => {
          if (!attributes.width) return {};
          return {
            style: `width: ${attributes.width}; max-width: 100%; text-align: center;`,
          };
        },
      },
    };
  },

  addKeyboardShortcuts() {
    return {
      "Mod-a": ({ editor }) => {
        const { state } = editor;
        const { selection } = state;
        const { $from } = selection;
        if (isActive(state, this.name)) {
          const start = $from.start($from.depth);
          const end = $from.end($from.depth);
          editor.commands.setTextSelection({ from: start, to: end });
          return true;
        }
        return false;
      },
      Enter: ({ editor }) => {
        const { state } = editor;
        const { selection } = state;
        const { $from } = selection;

        let inCaption = false;
        let figureDepth = -1;
        for (let depth = $from.depth; depth > 0; depth--) {
          const node = $from.node(depth);
          if (node.type.name === this.name) {
            inCaption = true;
            figureDepth = depth - 1;
            break;
          }
        }

        if (!inCaption) {
          return false;
        }

        const figureNode = $from.node(figureDepth);
        const figurePos = $from.before(figureDepth);
        const afterFigurePos = figurePos + figureNode.nodeSize;

        editor
          .chain()
          .command(({ tr }) => {
            const paragraph = tr.doc.type.schema.nodes.paragraph.create();
            tr.insert(afterFigurePos, paragraph);
            tr.setSelection(
              TextSelection.near(tr.doc.resolve(afterFigurePos + 1))
            );
            return true;
          })
          .run();

        return true;
      },
    };
  },

  addProseMirrorPlugins() {
    const pluginKey = new PluginKey<{ previousCaptionPos: number | null }>(
      "figureCaptionAutoDelete"
    );

    return [
      new Plugin({
        key: pluginKey,
        state: {
          init() {
            return { previousCaptionPos: null as number | null };
          },
          apply(tr) {
            const { selection } = tr;
            const { $from } = selection;
            let currentCaptionPos: number | null = null;
            for (let depth = $from.depth; depth > 0; depth--) {
              const node = $from.node(depth);
              if (node.type.name === ExtensionFigureCaption.name) {
                currentCaptionPos = $from.before(depth);
                break;
              }
            }

            return { previousCaptionPos: currentCaptionPos };
          },
        },
        appendTransaction(transactions, oldState, newState) {
          const pluginState = pluginKey.getState(oldState);
          if (!pluginState) {
            return null;
          }

          const { previousCaptionPos } = pluginState;
          const { selection } = newState;
          const { $from } = selection;

          let currentInCaption = false;
          for (let depth = $from.depth; depth > 0; depth--) {
            const node = $from.node(depth);
            if (node.type.name === ExtensionFigureCaption.name) {
              currentInCaption = true;
              break;
            }
          }

          if (currentInCaption) {
            return null;
          }

          if (
            previousCaptionPos !== null &&
            transactions.some((tr) => tr.docChanged || tr.selectionSet)
          ) {
            const captionNode = oldState.doc.nodeAt(previousCaptionPos);

            if (
              captionNode &&
              captionNode.type.name === ExtensionFigureCaption.name
            ) {
              const isEmpty =
                captionNode.textContent.trim().length === 0 ||
                captionNode.childCount === 0;

              if (isEmpty) {
                const tr = newState.tr;
                tr.delete(
                  previousCaptionPos,
                  previousCaptionPos + captionNode.nodeSize
                );
                return tr;
              }
            }
          }

          return null;
        },
      }),
    ];
  },

  parseHTML() {
    return [
      {
        tag: "figcaption",
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return [
      "figcaption",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      0,
    ];
  },

  addNodeView() {
    return VueNodeViewRenderer(FigureCaptionView);
  },
});
