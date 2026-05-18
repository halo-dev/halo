import { VTooltipComponent } from "@halo-dev/components";
import { h, render } from "vue";
import MdiPlus from "~icons/mdi/plus";
import { i18n } from "@/locales";
import {
  addRowAfter,
  Decoration,
  DecorationSet,
  Plugin,
  PluginKey,
  type EditorState,
  type EditorView,
} from "@/tiptap/pm";
import { mergeAttributes, Node } from "@/tiptap/vue-3";
import {
  findTable,
  getCellsInColumn,
  getCellsInRow,
  isRowSelected,
  isTableSelected,
  selectRow,
  selectTable,
} from "./util";

export interface TableCellOptions {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  HTMLAttributes: Record<string, any>;
}

const MIN_ROW_HEIGHT = 40;
const ROW_HEIGHT_STYLE = /(^|;)\s*height\s*:\s*[^;]+;?/i;

const markEditorUiElement = (element: HTMLElement) => {
  element.dataset.editorUi = "true";
};

const setRowHeightStyle = (style: string | null, height: number) => {
  const heightStyle = `height: ${height}px;`;
  if (!style) {
    return heightStyle;
  }

  if (ROW_HEIGHT_STYLE.test(style)) {
    return style.replace(
      ROW_HEIGHT_STYLE,
      (_, prefix: string) => `${prefix ? `${prefix} ` : ""}${heightStyle}`
    );
  }

  const trimmedStyle = style.trim();
  return trimmedStyle.endsWith(";")
    ? `${trimmedStyle} ${heightStyle}`
    : `${trimmedStyle}; ${heightStyle}`;
};

const resizeRowHeight = (rowIndex: number, height: number) => {
  return (state: EditorState) => {
    const table = findTable(state.selection);
    if (!table) {
      return;
    }

    let rowPos: number | undefined;
    table.node.forEach((_row, offset, index) => {
      if (index === rowIndex) {
        rowPos = table.start + offset;
      }
    });

    if (rowPos === undefined) {
      return;
    }

    const row = table.node.child(rowIndex);
    return state.tr.setNodeMarkup(rowPos, undefined, {
      ...row.attrs,
      style: setRowHeightStyle(row.attrs.style, height),
    });
  };
};

const setRowResizeHandlesActive = (
  editorView: EditorView,
  rowIndex: number,
  active: boolean
) => {
  editorView.dom
    .querySelectorAll(`.row-resize-handle[data-row-index="${rowIndex}"]`)
    .forEach((handle) => handle.classList.toggle("active", active));
};

const startResizeRow = (
  event: MouseEvent,
  rowIndex: number,
  editorView: EditorView
) => {
  event.preventDefault();
  event.stopImmediatePropagation();

  const handle = event.currentTarget as HTMLElement;
  const row = handle.closest("tr");
  if (!row) {
    return;
  }

  const startY = event.clientY;
  const startHeight = row.getBoundingClientRect().height;
  editorView.dom.classList.add("table-row-resizing");
  setRowResizeHandlesActive(editorView, rowIndex, true);

  const onMouseMove = (event: MouseEvent) => {
    const height = Math.max(
      MIN_ROW_HEIGHT,
      Math.round(startHeight + event.clientY - startY)
    );
    const tr = resizeRowHeight(rowIndex, height)(editorView.state);
    if (tr) {
      editorView.dispatch(tr);
    }
  };

  const onMouseUp = () => {
    editorView.dom.classList.remove("table-row-resizing");
    setRowResizeHandlesActive(editorView, rowIndex, false);
    document.removeEventListener("mousemove", onMouseMove);
    document.removeEventListener("mouseup", onMouseUp);
    editorView.focus();
  };

  document.addEventListener("mousemove", onMouseMove);
  document.addEventListener("mouseup", onMouseUp);
};

const TableCell = Node.create<TableCellOptions>({
  name: "tableCell",
  content: "block+",
  tableRole: "cell",
  isolating: true,
  fakeSelection: true,

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
                        markEditorUiElement(grip);
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
                      markEditorUiElement(grip);
                      const instance = h(
                        VTooltipComponent,
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

                getCellsInRow(index)(selection)?.forEach((cell, cellIndex) => {
                  decorations.push(
                    Decoration.widget(cell.pos + 1, () => {
                      const key = `row-resize-${index}-${cellIndex}`;
                      let handle = storage.gripMap.get(key);
                      if (!handle) {
                        handle = document.createElement("span");
                        markEditorUiElement(handle);
                        handle.addEventListener("mouseenter", () => {
                          setRowResizeHandlesActive(editor.view, index, true);
                        });
                        handle.addEventListener("mouseleave", () => {
                          if (
                            !editor.view.dom.classList.contains(
                              "table-row-resizing"
                            )
                          ) {
                            setRowResizeHandlesActive(
                              editor.view,
                              index,
                              false
                            );
                          }
                        });
                        handle.addEventListener(
                          "mousedown",
                          (event: MouseEvent) => {
                            startResizeRow(event, index, editor.view);
                          }
                        );
                      }
                      handle.className = "row-resize-handle";
                      handle.dataset.rowIndex = String(index);
                      storage.gripMap.set(key, handle);
                      return handle;
                    })
                  );
                });
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
