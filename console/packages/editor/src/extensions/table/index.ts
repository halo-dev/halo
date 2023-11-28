import TiptapTable, { type TableOptions } from "@tiptap/extension-table";
import { isActive, type Editor, type Range } from "@/tiptap/vue-3";
import type {
  Node as ProseMirrorNode,
  NodeView,
  EditorState,
} from "@/tiptap/pm";
import TableCell from "./table-cell";
import TableRow from "./table-row";
import TableHeader from "./table-header";
import MdiTable from "~icons/mdi/table";
import MdiTablePlus from "~icons/mdi/table-plus";
import MdiTableColumnPlusBefore from "~icons/mdi/table-column-plus-before";
import MdiTableColumnPlusAfter from "~icons/mdi/table-column-plus-after";
import MdiTableRowPlusAfter from "~icons/mdi/table-row-plus-after";
import MdiTableRowPlusBefore from "~icons/mdi/table-row-plus-before";
import MdiTableColumnRemove from "~icons/mdi/table-column-remove";
import MdiTableRowRemove from "~icons/mdi/table-row-remove";
import MdiTableRemove from "~icons/mdi/table-remove";
import MdiTableHeadersEye from "~icons/mdi/table-headers-eye";
import MdiTableMergeCells from "~icons/mdi/table-merge-cells";
import MdiTableSplitCell from "~icons/mdi/table-split-cell";
import FluentTableColumnTopBottom24Regular from "~icons/fluent/table-column-top-bottom-24-regular";
import { markRaw } from "vue";
import { i18n } from "@/locales";
import type { ExtensionOptions, NodeBubbleMenu } from "@/types";
import { BlockActionSeparator, ToolboxItem } from "@/components";

function updateColumns(
  node: ProseMirrorNode,
  colgroup: Element,
  table: HTMLElement,
  cellMinWidth: number,
  overrideCol?: number,
  overrideValue?: any
) {
  let totalWidth = 0;
  let fixedWidth = true;
  let nextDOM = colgroup.firstChild as HTMLElement;
  const row = node.firstChild;
  if (!row) return;

  for (let i = 0, col = 0; i < row.childCount; i += 1) {
    const { colspan, colwidth } = row.child(i).attrs;

    for (let j = 0; j < colspan; j += 1, col += 1) {
      const hasWidth =
        overrideCol === col ? overrideValue : colwidth && colwidth[j];
      const cssWidth = hasWidth ? `${hasWidth}px` : "";

      totalWidth += hasWidth || cellMinWidth;

      if (!hasWidth) {
        fixedWidth = false;
      }

      if (!nextDOM) {
        colgroup.appendChild(document.createElement("col")).style.width =
          cssWidth;
      } else {
        if (nextDOM.style.width !== cssWidth) {
          nextDOM.style.width = cssWidth;
        }

        nextDOM = nextDOM.nextSibling as HTMLElement;
      }
    }
  }

  while (nextDOM) {
    const after = nextDOM.nextSibling as HTMLElement;

    nextDOM.parentNode?.removeChild(nextDOM);
    nextDOM = after;
  }

  if (fixedWidth) {
    table.style.width = `${totalWidth}px`;
    table.style.minWidth = "";
  } else {
    table.style.width = "";
    table.style.minWidth = `${totalWidth}px`;
  }
}

class TableView implements NodeView {
  node: ProseMirrorNode;

  cellMinWidth: number;

  dom: HTMLElement;

  scrollDom: HTMLElement;

  table: HTMLElement;

  colgroup: HTMLElement;

  contentDOM: HTMLElement;

  constructor(node: ProseMirrorNode, cellMinWidth: number) {
    this.node = node;
    this.cellMinWidth = cellMinWidth;
    this.dom = document.createElement("div");
    this.dom.className = "tableWrapper";

    this.scrollDom = document.createElement("div");
    this.scrollDom.className = "scrollWrapper";
    this.dom.appendChild(this.scrollDom);

    this.table = this.scrollDom.appendChild(document.createElement("table"));
    this.colgroup = this.table.appendChild(document.createElement("colgroup"));
    updateColumns(node, this.colgroup, this.table, cellMinWidth);
    this.contentDOM = this.table.appendChild(document.createElement("tbody"));
  }

  update(node: ProseMirrorNode) {
    if (node.type !== this.node.type) {
      return false;
    }

    this.node = node;
    updateColumns(node, this.colgroup, this.table, this.cellMinWidth);

    return true;
  }

  ignoreMutation(
    mutation: MutationRecord | { type: "selection"; target: Element }
  ) {
    return (
      mutation.type === "attributes" &&
      (mutation.target === this.table ||
        this.colgroup.contains(mutation.target))
    );
  }
}

const Table = TiptapTable.extend<ExtensionOptions & TableOptions>({
  addExtensions() {
    return [TableCell, TableRow, TableHeader];
  },
  addOptions() {
    return {
      ...this.parent?.(),
      HTMLAttributes: {},
      resizable: true,
      handleWidth: 5,
      cellMinWidth: 25,
      View: TableView as unknown as NodeView,
      lastColumnResizable: true,
      allowTableNodeSelection: false,
      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 15,
          component: markRaw(ToolboxItem),
          props: {
            editor,
            icon: markRaw(MdiTablePlus),
            title: i18n.global.t("editor.menus.table.add"),
            action: () =>
              editor
                .chain()
                .focus()
                .insertTable({ rows: 3, cols: 3, withHeaderRow: true })
                .run(),
          },
        };
      },
      getCommandMenuItems() {
        return {
          priority: 120,
          icon: markRaw(MdiTable),
          title: "editor.extensions.commands_menu.table",
          keywords: ["table", "biaoge"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertTable({ rows: 3, cols: 3, withHeaderRow: true })
              .run();
          },
        };
      },
      getBubbleMenu({ editor }): NodeBubbleMenu {
        return {
          pluginKey: "tableBubbleMenu",
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, Table.name);
          },
          getRenderContainer(node) {
            let container = node;
            if (container.nodeName === "#text") {
              container = node.parentElement as HTMLElement;
            }
            while (
              container &&
              container.classList &&
              !container.classList.contains("tableWrapper")
            ) {
              container = container.parentElement as HTMLElement;
            }
            return container;
          },
          tippyOptions: {
            offset: [26, 0],
          },
          items: [
            {
              priority: 10,
              props: {
                icon: markRaw(MdiTableColumnPlusBefore),
                title: i18n.global.t("editor.menus.table.add_column_before"),
                action: () => {
                  editor.chain().focus().addColumnBefore().run();
                },
              },
            },
            {
              priority: 20,
              props: {
                icon: markRaw(MdiTableColumnPlusAfter),
                title: i18n.global.t("editor.menus.table.add_column_after"),
                action: () => editor.chain().focus().addColumnAfter().run(),
              },
            },
            {
              priority: 30,
              props: {
                icon: markRaw(MdiTableColumnRemove),
                title: i18n.global.t("editor.menus.table.delete_column"),
                action: () => editor.chain().focus().deleteColumn().run(),
              },
            },
            {
              priority: 40,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 50,
              props: {
                icon: markRaw(MdiTableRowPlusBefore),
                title: i18n.global.t("editor.menus.table.add_row_before"),
                action: () => editor.chain().focus().addRowBefore().run(),
              },
            },
            {
              priority: 60,
              props: {
                icon: markRaw(MdiTableRowPlusAfter),
                title: i18n.global.t("editor.menus.table.add_row_after"),
                action: () => editor.chain().focus().addRowAfter().run(),
              },
            },
            {
              priority: 70,
              props: {
                icon: markRaw(MdiTableRowRemove),
                title: i18n.global.t("editor.menus.table.delete_row"),
                action: () => editor.chain().focus().deleteRow().run(),
              },
            },
            {
              priority: 80,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 90,
              props: {
                icon: markRaw(MdiTableHeadersEye),
                title: i18n.global.t("editor.menus.table.toggle_header_column"),
                action: () => editor.chain().focus().toggleHeaderColumn().run(),
              },
            },
            {
              priority: 100,
              props: {
                icon: markRaw(MdiTableHeadersEye),
                title: i18n.global.t("editor.menus.table.toggle_header_row"),
                action: () => editor.chain().focus().toggleHeaderRow().run(),
              },
            },
            {
              priority: 101,
              props: {
                icon: markRaw(FluentTableColumnTopBottom24Regular),
                title: i18n.global.t("editor.menus.table.toggle_header_cell"),
                action: () => editor.chain().focus().toggleHeaderCell().run(),
              },
            },
            {
              priority: 110,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 120,
              props: {
                icon: markRaw(MdiTableMergeCells),
                title: i18n.global.t("editor.menus.table.merge_cells"),
                action: () => editor.chain().focus().mergeCells().run(),
              },
            },
            {
              priority: 130,
              props: {
                icon: markRaw(MdiTableSplitCell),
                title: i18n.global.t("editor.menus.table.split_cell"),
                action: () => editor.chain().focus().splitCell().run(),
              },
            },
            {
              priority: 140,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 150,
              props: {
                icon: markRaw(MdiTableRemove),
                title: i18n.global.t("editor.menus.table.delete_table"),
                action: () => editor.chain().focus().deleteTable().run(),
              },
            },
          ],
        };
      },
      getDraggable() {
        return {
          getRenderContainer({ dom }) {
            let container = dom;
            while (container && !container.classList.contains("tableWrapper")) {
              container = container.parentElement as HTMLElement;
            }
            return {
              el: container,
              dragDomOffset: {
                x: 20,
                y: 20,
              },
            };
          },
          handleDrop({ view, event, slice, insertPos }) {
            const { state } = view;
            const $pos = state.selection.$anchor;
            for (let d = $pos.depth; d > 0; d--) {
              const node = $pos.node(d);
              if (node.type.spec["tableRole"] == "table") {
                const eventPos = view.posAtCoords({
                  left: event.clientX,
                  top: event.clientY,
                });
                if (!eventPos) {
                  return;
                }
                if (!slice) {
                  return;
                }

                let tr = state.tr;
                tr = tr.delete($pos.before(d), $pos.after(d));
                const pos = tr.mapping.map(insertPos);
                tr = tr.replaceRange(pos, pos, slice).scrollIntoView();

                if (tr) {
                  view.dispatch(tr);
                  event.preventDefault();
                  return true;
                }

                return false;
              }
            }
          },
        };
      },
    };
  },
}).configure({ resizable: true });

export default Table;
