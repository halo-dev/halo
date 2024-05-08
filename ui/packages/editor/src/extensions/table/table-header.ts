import { mergeAttributes, Node } from "@/tiptap/vue-3";
import {
  Plugin,
  PluginKey,
  Decoration,
  DecorationSet,
  addColumnAfter,
} from "@/tiptap/pm";
import { getCellsInRow, isColumnSelected, selectColumn } from "./util";
import { render } from "vue";
import { Tooltip } from "floating-vue";
import { h } from "vue";
import MdiPlus from "~icons/mdi/plus";
import { i18n } from "@/locales";

export interface TableCellOptions {
  HTMLAttributes: Record<string, any>;
}

const TableHeader = Node.create<TableCellOptions>({
  name: "tableHeader",
  content: "block+",
  tableRole: "header_cell",
  isolating: true,

  addOptions() {
    return {
      HTMLAttributes: {},
    };
  },

  addAttributes() {
    return {
      colspan: {
        default: 1,
      },
      rowspan: {
        default: 1,
      },
      colwidth: {
        default: [100],
        parseHTML: (element) => {
          const colwidth = element.getAttribute("colwidth");
          const value = colwidth
            ? colwidth.split(",").map((width) => parseInt(width, 10))
            : null;
          return value;
        },
      },
      style: {
        default: null,
      },
    };
  },

  parseHTML() {
    return [{ tag: "th" }];
  },

  renderHTML({ HTMLAttributes }) {
    return [
      "th",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      0,
    ];
  },

  addStorage() {
    const gripMap = new Map<string, HTMLElement>();
    return {
      gripMap,
    };
  },

  onDestroy() {
    this.storage.gripMap.clear();
  },

  addProseMirrorPlugins() {
    const editor = this.editor;
    const storage = this.storage;
    return [
      new Plugin({
        key: new PluginKey("table-header-control"),
        props: {
          decorations(state) {
            const { doc, selection } = state;
            const decorations: Decoration[] = [];
            const cells = getCellsInRow(0)(selection);
            if (cells) {
              cells.forEach(({ pos }, index) => {
                decorations.push(
                  Decoration.widget(pos + 1, () => {
                    const key = "column" + index;
                    const colSelected = isColumnSelected(index)(selection);
                    let className = "grip-column";
                    if (colSelected) {
                      className += " selected";
                    }
                    if (index === 0) {
                      className += " first";
                    } else if (index === cells.length - 1) {
                      className += " last";
                    }

                    let grip = storage.gripMap.get(key) as HTMLElement;
                    if (!grip) {
                      grip = document.createElement("a");
                      const instance = h(
                        Tooltip,
                        {
                          triggers: ["hover"],
                        },
                        {
                          default: () => h(MdiPlus, { class: "plus-icon" }),
                          popper: () =>
                            i18n.global.t(
                              "editor.menus.table.add_column_after"
                            ),
                        }
                      );
                      render(instance, grip);
                      grip.addEventListener("mousedown", (event) => {
                        event.preventDefault();
                        event.stopImmediatePropagation();

                        editor.view.dispatch(
                          selectColumn(index)(editor.state.tr)
                        );

                        if (event.target !== grip) {
                          addColumnAfter(editor.state, editor.view.dispatch);
                        }
                      });
                    }
                    grip.className = className;
                    storage.gripMap.set(key, grip);
                    return grip;
                  })
                );
              });
            }
            return DecorationSet.create(doc, decorations);
          },
        },
      }),
    ];
  },
});

export default TableHeader;
