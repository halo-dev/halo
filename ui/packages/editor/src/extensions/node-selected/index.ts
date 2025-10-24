import {
  Decoration,
  DecorationSet,
  Extension,
  Plugin,
  PluginKey,
} from "@/tiptap";
export interface NodeSelectedOptions {
  className: string;
}

const NodeSelected = Extension.create<NodeSelectedOptions>({
  name: "nodeSelected",

  addOptions() {
    return {
      className: "has-node-selected",
    };
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey("nodeSelectedByAttr"),
        props: {
          decorations: ({ doc }) => {
            const { isEditable, isFocused } = this.editor;
            const decorations: Decoration[] = [];

            if (!isEditable || !isFocused) {
              return DecorationSet.create(doc, []);
            }

            doc.descendants((node, pos) => {
              if (node.isText) {
                return false;
              }

              const isSelected = node.attrs.selected;
              if (!isSelected) {
                return false;
              }

              decorations.push(
                Decoration.node(pos, pos + node.nodeSize, {
                  class: this.options.className,
                })
              );
            });

            return DecorationSet.create(doc, decorations);
          },
        },
      }),
    ];
  },
});

export default NodeSelected;
