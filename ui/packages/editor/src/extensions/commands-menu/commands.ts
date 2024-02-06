import {
  Extension,
  VueRenderer,
  type Editor,
  type AnyExtension,
  type Range,
} from "@/tiptap/vue-3";
import Suggestion from "@tiptap/suggestion";
import type { CommandMenuItem } from "@/types";
import type { Instance } from "tippy.js";
import CommandsView from "./CommandsView.vue";
import tippy from "tippy.js";

export default Extension.create({
  name: "commands-menu",

  addProseMirrorPlugins() {
    const commandMenuItems = getToolbarItemsFromExtensions(
      this.editor as Editor
    );

    return [
      Suggestion({
        editor: this.editor,
        char: "/",
        // @ts-ignore
        command: ({
          editor,
          range,
          props,
        }: {
          editor: Editor;
          range: Range;
          props: CommandMenuItem;
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
          let component: VueRenderer;
          let popup: Instance[];

          return {
            onStart: (props: Record<string, any>) => {
              component = new VueRenderer(CommandsView, {
                props,
                editor: props.editor,
              });

              if (!props.clientRect) {
                return;
              }

              popup = tippy("body", {
                getReferenceClientRect: props.clientRect,
                appendTo: () => document.body,
                content: component.element,
                showOnCreate: true,
                interactive: true,
                trigger: "manual",
                placement: "bottom-start",
              });
            },

            onUpdate(props: Record<string, any>) {
              component.updateProps(props);

              if (!props.clientRect) {
                return;
              }

              popup[0].setProps({
                getReferenceClientRect: props.clientRect,
              });
            },

            onKeyDown(props: Record<string, any>) {
              if (props.event.key === "Escape") {
                popup[0].hide();

                return true;
              }

              return component.ref?.onKeyDown(props);
            },

            onExit() {
              popup[0].destroy();
              component.destroy();
            },
          };
        },
      }),
    ];
  },
});

function getToolbarItemsFromExtensions(editor: Editor) {
  const extensionManager = editor?.extensionManager;
  return extensionManager.extensions
    .reduce((acc: CommandMenuItem[], extension: AnyExtension) => {
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
}
