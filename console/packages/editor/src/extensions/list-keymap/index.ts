import {
  listHelpers,
  ListKeymap,
  type ListKeymapOptions,
} from "@tiptap/extension-list-keymap";

/**
 *  Optimize the listKeymap extension until the issue with @tiptap/extension-list-keymap is resolved.
 *  https://github.com/ueberdosis/tiptap/issues/4395
 */
const ExtensionListKeymap = ListKeymap.extend<ListKeymapOptions>({
  addKeyboardShortcuts() {
    const backspaceHandle = (editor) => {
      let handled = false;

      if (!editor.state.selection.empty) {
        editor.commands.deleteSelection();
        return true;
      }

      this.options.listTypes.forEach(({ itemName, wrapperNames }) => {
        if (listHelpers.handleBackspace(editor, itemName, wrapperNames)) {
          handled = true;
        }
      });

      return handled;
    };

    return {
      Backspace: ({ editor }) => backspaceHandle(editor),

      "Mod-Backspace": ({ editor }) => backspaceHandle(editor),
    };
  },
});

export default ExtensionListKeymap;
