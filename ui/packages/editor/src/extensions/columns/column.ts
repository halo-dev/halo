import { mergeAttributes, Node } from "@/tiptap/vue-3";

export type ExtensionColumnOptions = {
  HTMLAttributes: {
    class: string;
  };
};

export const ExtensionColumn = Node.create<ExtensionColumnOptions>({
  name: "column",
  content: "block+",
  isolating: true,
  fakeSelection: true,

  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "One block-content column inside a columns layout. It is not a top-level content block.",
        exposure: "available",
        useWhen: ["Adding a column to a multi-column layout."],
        avoidWhen: ["There is no containing columns layout."],
        attributeGuidance: {
          index: {
            description:
              "Sequential zero-based position of this column in its layout.",
            examples: [0, 1, 2],
          },
          style: {
            description:
              "Layout styles used to size the column within its container.",
            format: "CSS declarations",
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          '<div class="columns" cols="1"><div class="column" index="0"><p>Column content</p></div></div>',
        ],
      },
      structure: {
        allowedParents: ["columns"],
        minPerParent: 1,
        description:
          "column may appear only inside columns, and each columns layout contains at least one column.",
      },
    };
  },

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
        parseHTML: (element) => {
          const index = Number(element.getAttribute("index"));
          return Number.isInteger(index) && index >= 0 ? index : 0;
        },
      },
      style: {
        default: "min-width: 0;flex: 1 1;box-sizing: border-box;",
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
