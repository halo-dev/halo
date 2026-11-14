import { Plugin, PluginKey } from "@/tiptap/pm";

export interface SearchAndReplacePanel {
  element: HTMLElement;
  destroy: () => void;
}

export interface SearchAndReplacePanelPluginOptions {
  createPanel: () => SearchAndReplacePanel;
}

const searchAndReplacePanelPluginKey = new PluginKey("searchAndReplacePanel");

export function SearchAndReplacePanelPlugin({
  createPanel,
}: SearchAndReplacePanelPluginOptions) {
  return new Plugin({
    key: searchAndReplacePanelPluginKey,
    view: (view) => {
      const panel = createPanel();
      let mounted = false;

      const mount = () => {
        if (mounted) {
          return;
        }

        const editorMainElement = findEditorMainElement(view.dom);
        if (!editorMainElement) {
          return;
        }

        editorMainElement.insertAdjacentElement("afterbegin", panel.element);
        mounted = true;
      };

      mount();

      return {
        update: mount,
        destroy() {
          panel.destroy();
          panel.element.remove();
        },
      };
    },
  });
}

function findEditorMainElement(element: HTMLElement) {
  let currentElement: HTMLElement | null = element;

  while (currentElement) {
    if (currentElement.classList.contains("editor-main")) {
      return currentElement;
    }
    currentElement = currentElement.parentElement;
  }

  return null;
}
