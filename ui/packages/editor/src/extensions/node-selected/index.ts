import { Decoration, Extension } from "@/tiptap/core";

export interface ExtensionNodeSelectedOptions {
  className: string;
}

export const ExtensionNodeSelected =
  Extension.create<ExtensionNodeSelectedOptions>({
    name: "nodeSelected",

    addOptions() {
      return {
        className: "has-node-selected",
      };
    },

    onUpdate({ transaction }) {
      if (!transaction.docChanged) {
        this.editor.commands.updateDecorations(this.name);
      }
    },

    addDecorations() {
      return {
        create: ({ state }) => {
          const { isEditable, isFocused } = this.editor;
          const decorations: Decoration[] = [];

          if (!isEditable || !isFocused) {
            return decorations;
          }

          state.doc.descendants((node, pos) => {
            if (node.isText) {
              return false;
            }

            if (node.attrs.selected) {
              decorations.push(
                Decoration.Node(pos, pos + node.nodeSize, {
                  class: this.options.className,
                })
              );
            }
            return true;
          });

          return decorations;
        },
        shouldUpdate: ({ tr }) =>
          tr.docChanged ||
          tr.getMeta("focus") !== undefined ||
          tr.getMeta("blur") !== undefined,
      };
    },
  });
