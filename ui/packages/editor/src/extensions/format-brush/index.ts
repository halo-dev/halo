import ToolbarItem from "@/components/toolbar/ToolbarItem.vue";
import { i18n } from "@/locales";
import { CoreEditor, Extension, Plugin, PluginKey } from "@/tiptap";
import { markRaw } from "vue";
import MdiBrushVariant from "~icons/mdi/brush-variant";
import { getMarksByFirstTextNode, setMarks } from "./util";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    formatBrush: {
      copyFormatBrush: () => ReturnType;
      pasteFormatBrush: () => ReturnType;
    };
  }
}

export interface FormatBrushStore {
  formatBrush: boolean;
  formatBrushMarks: any[];
}

const formatBrush = Extension.create<any, FormatBrushStore>({
  addOptions() {
    return {
      ...this.parent?.(),
      getToolbarItems({ editor }: { editor: CoreEditor }) {
        const formatBrush =
          editor.view.dom.classList.contains("format-brush-mode");
        return {
          priority: 25,
          component: markRaw(ToolbarItem),
          props: {
            editor,
            isActive: formatBrush,
            icon: markRaw(MdiBrushVariant),
            title: formatBrush
              ? i18n.global.t(
                  "editor.extensions.format_brush.toolbar_item.cancel"
                )
              : i18n.global.t(
                  "editor.extensions.format_brush.toolbar_item.title"
                ),
            action: () => {
              if (formatBrush) {
                editor.commands.pasteFormatBrush();
              } else {
                editor.commands.copyFormatBrush();
              }
            },
          },
        };
      },
    };
  },

  addCommands() {
    return {
      copyFormatBrush:
        () =>
        ({ state }) => {
          const markRange = getMarksByFirstTextNode(state);
          this.storage.formatBrushMarks = markRange;
          this.storage.formatBrush = true;
          this.editor.view.dom.classList.add("format-brush-mode");
          return true;
        },
      pasteFormatBrush: () => () => {
        this.storage.formatBrushMarks = [];
        this.storage.formatBrush = false;
        this.editor.view.dom.classList.remove("format-brush-mode");
        return true;
      },
    };
  },

  addStorage() {
    return {
      formatBrush: false,
      formatBrushMarks: [],
    };
  },

  addProseMirrorPlugins() {
    const storage = this.storage;
    const editor = this.editor;
    return [
      new Plugin({
        key: new PluginKey("formatBrushPlugin"),
        props: {
          handleDOMEvents: {
            mouseup(view) {
              if (!storage.formatBrush) {
                return;
              }
              editor
                .chain()
                .command(({ tr }) => {
                  setMarks(view.state, storage.formatBrushMarks, tr);
                  return true;
                })
                .pasteFormatBrush()
                .run();
            },
          },
        },
      }),
    ];
  },

  addKeyboardShortcuts() {
    return {
      "Shift-Mod-c": () => {
        this.editor.commands.copyFormatBrush();
        return true;
      },
    };
  },
});

export default formatBrush;
