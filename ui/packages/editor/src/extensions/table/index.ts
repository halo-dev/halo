import type { KeyboardShortcutCommand } from "@tiptap/core";
import {
  createColGroup,
  Table as TiptapTable,
  type TableOptions,
} from "@tiptap/extension-table";
import { markRaw } from "vue";
import MdiTable from "~icons/mdi/table";
import MdiTablePlus from "~icons/mdi/table-plus";
import { CONVERT_TO_KEY } from "@/components/drag/default-drag";
import { i18n } from "@/locales";
import {
  Editor,
  findParentNode,
  isActive,
  isNodeActive,
  mergeAttributes,
  posToDOMRect,
  type Range,
} from "@/tiptap";
import {
  Fragment,
  handlePaste as handleTablePaste,
  Plugin,
  PluginKey,
  Slice,
  type DOMOutputSpec,
  type EditorState,
  type EditorView,
} from "@/tiptap/pm";
import type { ExtensionOptions, NodeBubbleMenuType } from "@/types";
import {
  hasTableBefore,
  isCellSelection,
  isTableSelected,
  selectTable,
} from "@/utils/table";
import {
  joinStyles,
  parseTableLayoutMode,
  type CellVerticalAlign,
  type TableLayoutMode,
} from "./attributes";
import {
  fitTableToWidthCommand,
  clearSelectedAxisCommand,
  clearTableCellFormattingCommand,
  copyTableCommand,
  deleteAxisCommand,
  duplicateAxisCommand,
  moveAxisCommand,
  moveAxisToCommand,
  selectTableCommand,
  setCellAttributeCommand,
  setTableLayoutCommand,
  setTableRowHeightCommand,
  tableLayoutTransitionPluginAppendTransaction,
} from "./commands";
import TableBubbleMenu from "./components/TableBubbleMenu.vue";
import TableInsertToolboxItem from "./components/TableInsertToolboxItem.vue";
import TableCell from "./table-cell";
import { TableControls } from "./table-controls";
import TableHeader from "./table-header";
import TableRow from "./table-row";
import { HaloTableView } from "./table-view";

export * from "./attributes";
export * from "./table-view";

export const TABLE_BUBBLE_MENU_KEY = new PluginKey("tableBubbleMenu");
const TABLE_LAYOUT_PLUGIN_KEY = new PluginKey("haloTableLayout");

export type ExtensionTableOptions = ExtensionOptions & Partial<TableOptions>;

export const ExtensionTable = TiptapTable.extend<ExtensionTableOptions>({
  priority: 1000,

  allowGapCursor: true,

  addHaloEditorMetadata() {
    return {
      ai: {
        description: "A table of rows containing header or data cells.",
        exposure: "recommended",
        useWhen: ["Comparing structured values across rows and columns."],
        avoidWhen: [
          "The information is a simple list or the table would be used only for visual layout.",
        ],
        contentGuidelines: [
          "Use header cells where they clarify row or column meaning.",
          "Keep each cell focused on one value or concise piece of content.",
        ],
        attributeGuidance: {
          layoutMode: {
            description:
              "Table layout mode. Automatic layout fills the available content width; fixed layout preserves deliberate column widths.",
            examples: ["auto", "fixed"],
            omitWhen: ["Automatic table sizing is appropriate."],
          },
        },
        generation: {
          mode: "direct-html",
        },
        examples: [
          "<table><tbody><tr><th><p>Name</p></th><th><p>Status</p></th></tr><tr><td><p>Halo</p></td><td><p>Active</p></td></tr></tbody></table>",
          '<table><tbody><tr><th colspan="2"><p>Quarterly results</p></th></tr><tr><td rowspan="2"><p>Revenue</p></td><td><p>Q1</p></td></tr><tr><td><p>Q2</p></td></tr></tbody></table>',
        ],
      },
    };
  },

  addExtensions() {
    return [TableCell, TableRow, TableHeader, TableControls];
  },

  addOptions() {
    return {
      ...this.parent?.(),
      HTMLAttributes: {},
      resizable: true,
      renderWrapper: true,
      handleWidth: 5,
      cellMinWidth: 25,
      View: HaloTableView,
      lastColumnResizable: true,
      allowTableNodeSelection: false,
      getToolboxItems({ editor }: { editor: Editor }) {
        return {
          priority: 40,
          component: markRaw(TableInsertToolboxItem),
          props: {
            editor,
            icon: markRaw(MdiTablePlus),
            title: i18n.global.t("editor.menus.table.add"),
            description: i18n.global.t("editor.menus.table.insert_description"),
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
              .fitTableToWidth()
              .run();
          },
        };
      },
      getBubbleMenu({ editor }): NodeBubbleMenuType {
        return {
          pluginKey: TABLE_BUBBLE_MENU_KEY,
          component: markRaw(TableBubbleMenu),
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, "table");
          },
          options: {
            placement: "top-start",
            offset: 8,
            flip: {
              padding: 8,
              fallbackPlacements: ["bottom-start"],
            },
            shift: {
              padding: 8,
              crossAxis: true,
            },
          },
          getReferencedVirtualElement() {
            return getTableBubbleMenuVirtualElement(editor);
          },
        };
      },
      getDraggableMenuItems() {
        return {
          extendsKey: CONVERT_TO_KEY,
          visible({ editor }): boolean {
            return !isActive(editor.state, "table");
          },
        };
      },
    };
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      layoutMode: {
        default: "auto",
        parseHTML: parseTableLayoutMode,
        renderHTML: ({ layoutMode }: { layoutMode: TableLayoutMode }) => ({
          "data-table-layout": layoutMode,
        }),
      },
    };
  },

  addCommands() {
    return {
      ...this.parent?.(),
      deleteRow: () => deleteAxisCommand("row"),
      deleteColumn: () => deleteAxisCommand("column"),
      setTableLayout: (layoutMode: TableLayoutMode) =>
        setTableLayoutCommand(layoutMode),
      fitTableToWidth: () => fitTableToWidthCommand(),
      setTableRowHeight: (height: number | null) =>
        setTableRowHeightCommand(height),
      setTableCellBackground: (color: string | null) => (props) =>
        setCellAttributeCommand("backgroundColor", color)(props),
      setTableCellVerticalAlign:
        (alignment: CellVerticalAlign | null) => (props) =>
          setCellAttributeCommand("verticalAlign", alignment)(props),
      clearTableCellFormatting: () => clearTableCellFormattingCommand(),
      clearSelectedTableRow: () => clearSelectedAxisCommand("row"),
      clearSelectedTableColumn: () => clearSelectedAxisCommand("column"),
      duplicateTableRow: () => duplicateAxisCommand("row"),
      duplicateTableColumn: () => duplicateAxisCommand("column"),
      moveTableRow: (direction: -1 | 1) => moveAxisCommand("row", direction),
      moveTableColumn: (direction: -1 | 1) =>
        moveAxisCommand("column", direction),
      moveTableRowTo: (target: number) => moveAxisToCommand("row", target),
      moveTableColumnTo: (target: number) =>
        moveAxisToCommand("column", target),
      selectCurrentTable: () => selectTableCommand("table"),
      selectTableRow: (index: number) => selectTableCommand("row", index),
      selectTableColumn: (index: number) => selectTableCommand("column", index),
      copyTable: () => copyTableCommand(),
    };
  },

  addKeyboardShortcuts() {
    const parentShortcuts = this.parent?.() ?? {};
    const handleBackspace = (
      fallback: KeyboardShortcutCommand | undefined,
      props: Parameters<KeyboardShortcutCommand>[0]
    ): boolean => {
      const { editor } = this;
      if (editor.commands.undoInputRule()) {
        return true;
      }

      const { selection } = editor.state;
      if (
        !isNodeActive(editor.state, "table") &&
        hasTableBefore(selection) &&
        selection.empty
      ) {
        editor.commands.selectNodeBackward();
        return true;
      }

      if (isNodeActive(editor.state, "table") && isTableSelected(selection)) {
        editor.commands.deleteTable();
        return true;
      }

      return fallback?.(props) ?? false;
    };

    return {
      ...parentShortcuts,
      Backspace: (props) => handleBackspace(parentShortcuts.Backspace, props),
      "Mod-Backspace": (props) =>
        handleBackspace(parentShortcuts["Mod-Backspace"], props),
      "Mod-a": ({ editor }) => {
        if (!isNodeActive(editor.state, "table")) {
          return false;
        }

        const { tr, selection } = editor.state;
        if (isTableSelected(selection)) {
          return editor.commands.selectAll();
        }

        if (isCellSelection(selection)) {
          editor.view.dispatch(selectTable(tr));
          return true;
        }

        const cell =
          findParentNode((node) => node.type.name === TableCell.name)(
            selection
          ) ??
          findParentNode((node) => node.type.name === TableHeader.name)(
            selection
          );
        if (!cell) {
          return false;
        }

        return editor.commands.setCellSelection({
          anchorCell: cell.pos,
        });
      },
    };
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: TABLE_LAYOUT_PLUGIN_KEY,
        appendTransaction: tableLayoutTransitionPluginAppendTransaction,
        props: {
          handlePaste: handleTabSeparatedPaste,
          handleDOMEvents: {
            mousedown: (_view, event) => {
              const target = event.target;
              if (
                target instanceof Element &&
                target.closest(".column-resize-handle") &&
                this.editor.getAttributes("table").layoutMode !== "fixed"
              ) {
                this.editor.commands.setTableLayout("fixed");
              }
              return false;
            },
          },
        },
      }),
      ...(this.parent?.() ?? []),
    ];
  },

  transformPastedHTML: transformPastedTableHTML,

  renderHTML({ node, HTMLAttributes }) {
    const layoutMode =
      (node.attrs.layoutMode as TableLayoutMode | undefined) ?? "auto";
    const cellMinWidth = this.options.cellMinWidth ?? 25;
    const { colgroup, tableWidth, tableMinWidth } = createColGroup(
      node,
      cellMinWidth
    );

    const configuredStyle = this.options.HTMLAttributes?.style as
      | string
      | undefined;
    const contentStyle = HTMLAttributes.style as string | undefined;
    const layoutStyle =
      layoutMode === "auto"
        ? "display: table; width: 100%; min-width: 100%; table-layout: auto"
        : joinStyles(
            "display: table",
            `width: ${tableWidth || "100%"}`,
            tableMinWidth && `min-width: ${tableMinWidth}`,
            "table-layout: fixed"
          );
    const tableAttributes = mergeAttributes(
      this.options.HTMLAttributes ?? {},
      HTMLAttributes ?? {},
      {
        "data-table-layout": layoutMode,
        style: joinStyles(configuredStyle, contentStyle, layoutStyle),
      }
    );

    const table: DOMOutputSpec = [
      "table",
      tableAttributes,
      ...(layoutMode === "fixed" ? [colgroup] : []),
      ["tbody", 0],
    ];

    return [
      "div",
      {
        class: "halo-table-wrapper",
        "data-table-layout": layoutMode,
        style:
          "box-sizing: border-box; overflow-x: auto; overflow-y: hidden; width: 100%; max-width: 100%; min-width: 0;",
      },
      table,
    ];
  },
}).configure({ resizable: true });

export function getTableBubbleMenuVirtualElement(editor: Editor) {
  const parentNode = findParentNode((node) => node.type.name === "table")(
    editor.state.selection
  );
  if (!parentNode) {
    return null;
  }

  const nodeDom = editor.view.nodeDOM(parentNode.pos);
  const nodeElement =
    nodeDom instanceof Element
      ? nodeDom
      : nodeDom instanceof Node
        ? nodeDom.parentElement
        : null;
  const wrapper = nodeElement?.matches(".halo-table-wrapper")
    ? nodeElement
    : nodeElement?.closest<HTMLElement>(".halo-table-wrapper");

  if (wrapper instanceof HTMLElement) {
    return {
      contextElement: wrapper,
      getBoundingClientRect: () => wrapper.getBoundingClientRect(),
      getClientRects: () => [wrapper.getBoundingClientRect()],
    };
  }

  const domRect = posToDOMRect(
    editor.view,
    parentNode.start,
    parentNode.start + parentNode.node.nodeSize - 2
  );
  return {
    getBoundingClientRect: () => domRect,
    getClientRects: () => [domRect],
  };
}

export function transformPastedTableHTML(html: string) {
  // Only sanitize markup that actually contains a table; leave other
  // pasted HTML (e.g. iframe embeds) to their own extensions.
  if (!/<table[\s>]/i.test(html)) {
    return html;
  }
  return sanitizePastedTableHTML(html);
}

export function sanitizePastedTableHTML(html: string) {
  const document = new DOMParser().parseFromString(html, "text/html");
  document
    .querySelectorAll("script, style, iframe, object, embed, link, meta")
    .forEach((element) => element.remove());
  document.querySelectorAll<HTMLElement>("*").forEach((element) => {
    Array.from(element.attributes).forEach((attribute) => {
      if (
        attribute.name.toLowerCase().startsWith("on") ||
        ((attribute.name === "href" || attribute.name === "src") &&
          /^\s*javascript:/i.test(attribute.value))
      ) {
        element.removeAttribute(attribute.name);
      }
    });
  });
  return document.body.innerHTML;
}

function handleTabSeparatedPaste(view: EditorView, event: ClipboardEvent) {
  const clipboard = event.clipboardData;
  const html = clipboard?.getData("text/html");
  const text = clipboard?.getData("text/plain");
  if (html || !text?.includes("\t")) {
    return false;
  }

  // Don't hijack pastes that belong to code blocks: pasting into an
  // existing code block, or code copied from VSCode (handled by the
  // code block extension's own paste plugin).
  if (view.state.selection.$from.parent.type.spec.code) {
    return false;
  }
  if (clipboard?.getData("vscode-editor-data")) {
    return false;
  }

  const values = text
    .replace(/\r\n?/g, "\n")
    .replace(/\n$/, "")
    .split("\n")
    .map((row) => row.split("\t"));
  const columnCount = Math.max(...values.map((row) => row.length));
  if (!values.length || !columnCount) {
    return false;
  }

  const { schema } = view.state;
  const rowType = schema.nodes.tableRow;
  const cellType = schema.nodes.tableCell;
  const paragraphType = schema.nodes.paragraph;
  const tableType = schema.nodes.table;
  if (!rowType || !cellType || !paragraphType || !tableType) {
    return false;
  }

  const rows = values.map((row) =>
    rowType.create(
      null,
      Array.from({ length: columnCount }, (_, columnIndex) => {
        const value = row[columnIndex] ?? "";
        const paragraph = paragraphType.create(
          null,
          value ? schema.text(value) : undefined
        );
        return cellType.create(null, paragraph);
      })
    )
  );
  const slice = new Slice(Fragment.from(rows), 0, 0);

  if (handleTablePaste(view, event, slice)) {
    event.preventDefault();
    return true;
  }

  const table = tableType.create({ layoutMode: "auto" }, rows);
  view.dispatch(view.state.tr.replaceSelectionWith(table).scrollIntoView());
  event.preventDefault();
  return true;
}
