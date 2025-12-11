import {
  Extension,
  PluginKey,
  posToDOMRect,
  VueRenderer,
  type AnyExtension,
  type Editor,
  type Range,
} from "@/tiptap";
import type { CommandMenuItemType } from "@/types";
import { computePosition, flip, shift } from "@floating-ui/dom";
import Suggestion, { type SuggestionOptions } from "@tiptap/suggestion";
import CommandsView from "./CommandsView.vue";

export const ExtensionCommandsMenu = Extension.create({
  name: "commands-menu",

  addProseMirrorPlugins() {
    const commandMenuItems = getToolbarItemsFromExtensions(this.editor);

    const suggestionPlugin: SuggestionOptions = {
      editor: this.editor,
      command: ({
        editor,
        range,
        props,
      }: {
        editor: Editor;
        range: Range;
        props: CommandMenuItemType;
      }) => {
        props.command({ editor, range });
      },
      items: ({ query }: { query: string }) => {
        return commandMenuItems.filter((item) =>
          [...item.keywords, item.title].some((keyword) =>
            keyword.includes(query)
          )
        );
      },
      render: () => {
        let component: VueRenderer | null = null;
        return {
          onStart: (props) => {
            component = new VueRenderer(CommandsView, {
              props,
              editor: props.editor,
            });
            if (!props.clientRect) {
              return;
            }
            if (!component.element) {
              return;
            }
            if (!(component.element instanceof HTMLElement)) {
              return;
            }
            component.element.style.position = "absolute";

            document.body.appendChild(component.element);

            updatePosition(props.editor, component.element);
          },

          onUpdate(props) {
            if (!component) {
              return;
            }
            if (!component.element) {
              return;
            }
            if (!(component.element instanceof HTMLElement)) {
              return;
            }
            component.updateProps(props);

            if (!props.clientRect) {
              return;
            }

            updatePosition(props.editor, component.element);
          },

          onKeyDown(props) {
            if (!component) {
              return false;
            }
            if (props.event.key === "Escape") {
              if (!component.element) {
                return false;
              }
              component.destroy();
              component.element.remove();
              return true;
            }

            return component.ref?.onKeyDown(props);
          },

          onExit() {
            if (!component) {
              return;
            }
            if (!component.element) {
              return;
            }
            component.destroy();
            component.element.remove();
          },
        };
      },
    };

    return [
      Suggestion({
        pluginKey: new PluginKey("commands-menu-english"),
        char: "/",
        ...suggestionPlugin,
      }),
      Suggestion({
        pluginKey: new PluginKey("commands-menu-chinese"),
        char: "、",
        ...suggestionPlugin,
      }),
    ];
  },
});

const updatePosition = (editor: Editor, element: HTMLElement) => {
  const virtualElement = {
    getBoundingClientRect: () =>
      posToDOMRect(
        editor.view,
        editor.state.selection.from,
        editor.state.selection.to
      ),
  };

  computePosition(virtualElement, element, {
    placement: "bottom-start",
    strategy: "absolute",
    middleware: [shift(), flip()],
  }).then(({ x, y, strategy }) => {
    element.style.position = strategy;
    element.style.left = `${x}px`;
    element.style.top = `${y}px`;
  });
};

const getToolbarItemsFromExtensions = (editor: Editor) => {
  const extensionManager = editor?.extensionManager;
  return extensionManager.extensions
    .reduce((acc: CommandMenuItemType[], extension: AnyExtension) => {
      const { getCommandMenuItems } = extension.options;

      if (!getCommandMenuItems) {
        return acc;
      }

      const items = getCommandMenuItems();

      if (Array.isArray(items)) {
        return [...acc, ...items];
      }

      return [...acc, items];
    }, [])
    .sort((a, b) => a.priority - b.priority);
};
