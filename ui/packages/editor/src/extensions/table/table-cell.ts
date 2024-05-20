import { mergeAttributes, Node } from "@/tiptap/vue-3";
import {
  Plugin,
  PluginKey,
  Decoration,
  DecorationSet,
  addRowAfter,
} from "@/tiptap/pm";
import {
  getCellsInColumn,
  isRowSelected,
  isTableSelected,
  selectRow,
  selectTable,
} from "./util";
import { Tooltip } from "floating-vue";
import { h } from "vue";
import MdiPlus from "~icons/mdi/plus";
import { render } from "vue";
import { i18n } from "@/locales";

export interface TableCellOptions {
  HTMLAttributes: Record<string, any>;
}

const TableCell = Node.create<TableCellOptions>({
  name: "tableCell",
  content: "block+",
  tableRole: "cell",
  isolating: true,

  addOptions() {
    return {
      HTMLAttributes: {},
    };
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      colspan: {
        default: 1,
        parseHTML: (element) => {
          const colspan = element.getAttribute("colspan");
          const value = colspan ? parseInt(colspan, 10) : 1;
          return value;
        },
      },
      rowspan: {
        default: 1,
        parseHTML: (element) => {
          const rowspan = element.getAttribute("rowspan");
          const value = rowspan ? parseInt(rowspan, 10) : 1;
          return value;
        },
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
    return [{ tag: "td" }];
  },

  renderHTML({ HTMLAttributes }) {
    return [
      "td",
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
        key: new PluginKey("table-cell-control"),
        props: {
          decorations(state) {
            const { doc, selection } = state;
            const decorations: Decoration[] = [];
            const cells = getCellsInColumn(0)(selection);
            if (cells) {
              cells.forEach(({ pos }, index) => {
                if (index === 0) {
                  decorations.push(
                    Decoration.widget(pos + 1, () => {
                      const key = "table" + index;
                      let className = "grip-table";
                      const selected = isTableSelected(selection);
                      if (selected) {
                        className += " selected";
                      }
                      let grip = storage.gripMap.get(key);
                      if (!grip) {
                        grip = document.createElement("a") as HTMLElement;
                        grip.addEventListener("mousedown", (event: Event) => {
                          event.preventDefault();
                          event.stopImmediatePropagation();
                          editor.view.dispatch(selectTable(editor.state.tr));
                        });
                      }
                      grip.className = className;
                      storage.gripMap.set(key, grip);
                      return grip;
                    })
                  );
                }

                decorations.push(
                  Decoration.widget(pos + 1, () => {
                    const key = "row" + index;
                    const rowSelected = isRowSelected(index)(selection);
                    let className = "grip-row";
                    if (rowSelected) {
                      className += " selected";
                    }
                    if (index === 0) {
                      className += " first";
                    }
                    if (index === cells.length - 1) {
                      className += " last";
                    }

                    let grip = storage.gripMap.get(key);
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
                            i18n.global.t("editor.menus.table.add_row_after"),
                        }
                      );
                      render(instance, grip);
                      grip.addEventListener(
                        "mousedown",
                        (event: Event) => {
                          event.preventDefault();
                          event.stopImmediatePropagation();

                          editor.view.dispatch(
                            selectRow(index)(editor.state.tr)
                          );

                          if (event.target !== grip) {
                            addRowAfter(editor.state, editor.view.dispatch);
                          }
                        },
                        true
                      );
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

export default TableCell;
