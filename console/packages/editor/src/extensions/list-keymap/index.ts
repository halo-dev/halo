import {
  listHelpers,
  ListKeymap,
  type ListKeymapOptions,
} from "@tiptap/extension-list-keymap";
import { isWholeDocSelected } from "./helpers/isAllSelection";

/**
 *  Optimize the listKeymap extension until the issue with @tiptap/extension-list-keymap is resolved.
 *  https://github.com/ueberdosis/tiptap/issues/4395
 */
const ExtensionListKeymap = ListKeymap.extend<ListKeymapOptions>({
  addKeyboardShortcuts() {
    return {
      Delete: ({ editor }) => {
        let handled = false;
        if (isWholeDocSelected(editor.state)) {
          return false;
        }

        this.options.listTypes.forEach(({ itemName }) => {
          if (editor.state.schema.nodes[itemName] === undefined) {
            return;
          }

          if (listHelpers.handleDelete(editor, itemName)) {
            handled = true;
          }
        });

        return handled;
      },
      "Mod-Delete": ({ editor }) => {
        let handled = false;
        if (isWholeDocSelected(editor.state)) {
          return false;
        }

        this.options.listTypes.forEach(({ itemName }) => {
          if (editor.state.schema.nodes[itemName] === undefined) {
            return;
          }

          if (listHelpers.handleDelete(editor, itemName)) {
            handled = true;
          }
        });

        return handled;
      },
      Backspace: ({ editor }) => {
        let handled = false;
        if (isWholeDocSelected(editor.state)) {
          return false;
        }

        this.options.listTypes.forEach(({ itemName, wrapperNames }) => {
          if (editor.state.schema.nodes[itemName] === undefined) {
            return;
          }

          if (listHelpers.handleBackspace(editor, itemName, wrapperNames)) {
            handled = true;
          }
        });

        return handled;
      },
      "Mod-Backspace": ({ editor }) => {
        let handled = false;
        if (isWholeDocSelected(editor.state)) {
          return false;
        }

        this.options.listTypes.forEach(({ itemName, wrapperNames }) => {
          if (editor.state.schema.nodes[itemName] === undefined) {
            return;
          }

          if (listHelpers.handleBackspace(editor, itemName, wrapperNames)) {
            handled = true;
          }
        });

        return handled;
      },
    };
  },
});

export default ExtensionListKeymap;
