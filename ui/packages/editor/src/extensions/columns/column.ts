import { mergeAttributes, Node } from "@/tiptap/vue-3";

const Column = Node.create({
  name: "column",
  content: "block+",
  isolating: true,

  addOptions() {
    return {
      HTMLAttributes: {
        class: "column",
      },
    };
  },

  addAttributes() {
    return {
      index: {
        default: 0,
        parseHTML: (element) => element.getAttribute("index"),
      },
      style: {
        default: "min-width: 0;padding: 12px;flex: 1 1;box-sizing: border-box;",
        parseHTML: (element) => element.getAttribute("style"),
      },
    };
  },

  parseHTML() {
    return [
      {
        tag: "div[class=column]",
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return [
      "div",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      0,
    ];
  },
});

export default Column;
