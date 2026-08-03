import { VTooltipComponent } from "@halo-dev/components";
import { h, render } from "vue";
import MdiPlus from "~icons/mdi/plus";
import { i18n } from "@/locales";
import {
  addColumnAfter,
  Decoration,
  DecorationSet,
  Plugin,
  PluginKey,
} from "@/tiptap/pm";
import { mergeAttributes, Node } from "@/tiptap/vue-3";
import { getCellsInRow, isColumnSelected, selectColumn } from "./util";

export interface TableCellOptions {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  HTMLAttributes: Record<string, any>;
}

const markEditorUiElement = (element: HTMLElement) => {
  element.dataset.editorUi = "true";
};

const TableHeader = Node.create<TableCellOptions>({
  name: "tableHeader",
  content: "block+",
  tableRole: "header_cell",
  isolating: true,
  fakeSelection: true,

  addHaloEditorMetadata() {
    return {
      ai: {
        description:
          "A header cell inside a table row. It is not a top-level content block.",
        aliases: ["th"],
        exposure: "recommended",
        useWhen: ["Labeling the meaning of a table row or column."],
        avoidWhen: ["The cell contains ordinary table data."],
        attributeGuidance: {
          colspan: {
            description: "Number of columns spanned by this header cell.",
            examples: [1, 2, 3],
          },
          rowspan: {
            description: "Number of rows spanned by this header cell.",
            examples: [1, 2, 3],
          },
          colwidth: {
            description: "Column widths associated with the header cell.",
            format: "array of pixel widths",
            examples: [null],
            omitWhen: ["Automatic table sizing is appropriate."],
          },
          style: {
            description: "Optional CSS declarations for the header cell.",
            format: "CSS declarations",
            omitWhen: ["Default header styling is appropriate."],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<table><tbody><tr><th><p>Label</p></th></tr></tbody></table>",
          '<table><tbody><tr><th rowspan="2"><p>Grouped label</p></th><th><p>First value</p></th></tr><tr><th><p>Second value</p></th></tr></tbody></table>',
        ],
      },
      structure: {
        allowedParents: ["tableRow"],
        description: "tableHeader may appear only inside tableRow.",
      },
    };
  },

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
                      markEditorUiElement(grip);
                      const instance = h(
                        VTooltipComponent,
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
