import { mergeAttributes, Node, type CommandProps } from "@/tiptap";
import type { ExtensionOptions } from "@/types";
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
  content: "block+ figureCaption?",
  isolating: true,

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
          return {
            style: `align-items: ${attributes.alignItems};`,
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

  renderHTML({ node, HTMLAttributes }) {
    return [
      "figure",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes, {
        style: `display: flex; flex-direction: column; align-items: ${node.attrs.alignItems};`,
      }),
      0,
    ];
  },

  addExtensions() {
    return [FigureCaption];
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
