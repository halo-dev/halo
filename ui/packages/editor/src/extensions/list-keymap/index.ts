import { Editor } from "@tiptap/core";
import {
  listHelpers,
  ListKeymap,
  type ListKeymapOptions,
} from "@tiptap/extension-list";

export type ExtensionListKeymapOptions = Partial<ListKeymapOptions>;

/**
 *  Optimize the listKeymap extension until the issue with @tiptap/extension-list is resolved.
 *  https://github.com/ueberdosis/tiptap/issues/4395
 */
export const ExtensionListKeymap =
  ListKeymap.extend<ExtensionListKeymapOptions>({
    addKeyboardShortcuts() {
      const backspaceHandle = (editor: Editor) => {
        let handled = false;

        if (!editor.state.selection.empty) {
          return false;
        }

        this.options.listTypes?.forEach(
          ({
            itemName,
            wrapperNames,
          }: {
            itemName: string;
            wrapperNames: string[];
          }) => {
            if (listHelpers.handleBackspace(editor, itemName, wrapperNames)) {
              handled = true;
            }
          }
        );

        return handled;
      };

      return {
        Backspace: ({ editor }: { editor: Editor }) => backspaceHandle(editor),

        "Mod-Backspace": ({ editor }: { editor: Editor }) =>
          backspaceHandle(editor),
      };
    },
  });
