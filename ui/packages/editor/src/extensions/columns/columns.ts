import { BlockActionSeparator, ToolboxItem } from "@/components";
import MdiDeleteForeverOutline from "@/components/icon/MdiDeleteForeverOutline.vue";
import { i18n } from "@/locales";
import {
  Editor,
  findParentNode,
  isActive,
  mergeAttributes,
  Node,
  posToDOMRect,
  type Range,
} from "@/tiptap";
import type { NodeType, Schema } from "@/tiptap/pm";
import {
  EditorState,
  PluginKey,
  Node as PMNode,
  TextSelection,
} from "@/tiptap/pm";
import type { ExtensionOptions } from "@/types";
import { deleteNode } from "@/utils";
import { markRaw } from "vue";
import MdiCollage from "~icons/mdi/collage";
import RiDeleteColumn from "~icons/ri/delete-column";
import RiInsertColumnLeft from "~icons/ri/insert-column-left";
import RiInsertColumnRight from "~icons/ri/insert-column-right";
import { ExtensionColumn } from "./column";

declare module "@/tiptap" {
  interface Commands<ReturnType> {
    columns: {
      insertColumns: (attrs?: { cols: number }) => ReturnType;
      addColBefore: () => ReturnType;
      addColAfter: () => ReturnType;
      deleteCol: () => ReturnType;
    };
  }
}

export const COLUMNS_BUBBLE_MENU_KEY = new PluginKey("columnsBubbleMenu");

const createColumns = (schema: Schema, colsCount: number) => {
  const types = getColumnsNodeTypes(schema);
  const cols = [];

  for (let index = 0; index < colsCount; index += 1) {
    const col = types.column.createAndFill({ index });

    if (col) {
      cols.push(col);
    }
  }
  return types.columns.createChecked({ cols: colsCount }, cols);
};

const getColumnsNodeTypes = (
  schema: Schema
): {
  columns: NodeType;
  column: NodeType;
} => {
  if (schema.cached.columnsNodeTypes) {
    return schema.cached.columnsNodeTypes;
  }

  const roles = {
    columns: schema.nodes["columns"],
    column: schema.nodes["column"],
  };

  schema.cached.columnsNodeTypes = roles;

  return roles;
};

type ColOperateType = "addBefore" | "addAfter" | "delete";
const addOrDeleteCol = (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  dispatch: any,
  state: EditorState,
  type: ColOperateType
) => {
  const maybeColumns = findParentNode(
    (node) => node.type.name === ExtensionColumns.name
  )(state.selection);
  const maybeColumn = findParentNode(
    (node) => node.type.name === ExtensionColumn.name
  )(state.selection);
  if (dispatch && maybeColumns && maybeColumn) {
    const cols = maybeColumns.node;
    const colIndex = maybeColumn.node.attrs.index;
    const colsJSON = cols.toJSON();

    let nextIndex = colIndex;

    if (type === "delete") {
      nextIndex = colIndex - 1;
      colsJSON.content.splice(colIndex, 1);
    } else {
      nextIndex = type === "addBefore" ? colIndex : colIndex + 1;
      colsJSON.content.splice(nextIndex, 0, {
        type: "column",
        attrs: {
          index: colIndex,
        },
        content: [
          {
            type: "paragraph",
          },
        ],
      });
    }

    colsJSON.attrs.cols = colsJSON.content.length;

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    colsJSON.content.forEach((colJSON: any, index: number) => {
      colJSON.attrs.index = index;
    });

    const nextCols = PMNode.fromJSON(state.schema, colsJSON);

    let nextSelectPos = maybeColumns.pos;
    nextCols.content.forEach((col, _pos, index) => {
      if (index < nextIndex) {
        nextSelectPos += col.nodeSize;
      }
    });

    const tr = state.tr.setTime(Date.now());

    tr.replaceWith(
      maybeColumns.pos,
      maybeColumns.pos + maybeColumns.node.nodeSize,
      nextCols
    ).setSelection(TextSelection.near(tr.doc.resolve(nextSelectPos)));

    dispatch(tr);
  }
  return true;
};

type GotoColType = "before" | "after";
// eslint-disable-next-line @typescript-eslint/no-explicit-any
const gotoCol = (state: EditorState, dispatch: any, type: GotoColType) => {
  const maybeColumns = findParentNode(
    (node) => node.type.name === ExtensionColumns.name
  )(state.selection);
  const maybeColumn = findParentNode(
    (node) => node.type.name === ExtensionColumn.name
  )(state.selection);

  if (dispatch && maybeColumns && maybeColumn) {
    const cols = maybeColumns.node;
    const colIndex = maybeColumn.node.attrs.index;

    let nextIndex = 0;

    if (type === "before") {
      nextIndex = (colIndex - 1 + cols.attrs.cols) % cols.attrs.cols;
    } else {
      nextIndex = (colIndex + 1) % cols.attrs.cols;
    }

    let nextSelectPos = maybeColumns.pos;
    cols.content.forEach((col, _pos, index) => {
      if (index < nextIndex) {
        nextSelectPos += col.nodeSize;
      }
    });

    const tr = state.tr.setTime(Date.now());

    tr.setSelection(TextSelection.near(tr.doc.resolve(nextSelectPos)));
    dispatch(tr);
    return true;
  }

  return false;
};

export interface ExtensionColumnsOptions extends ExtensionOptions {
  HTMLAttributes: {
    class: string;
  };
}

export const ExtensionColumns = Node.create<ExtensionColumnsOptions>({
  name: "columns",
  group: "block",
  priority: 10,
  defining: true,
  isolating: true,
  allowGapCursor: true,
  content: "column{1,}",
  fakeSelection: false,

  addOptions() {
    return {
      HTMLAttributes: {
        class: "columns",
      },
      getToolboxItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 70,
            component: markRaw(ToolboxItem),
            props: {
              editor,
              icon: markRaw(MdiCollage),
              title: i18n.global.t("editor.extensions.commands_menu.columns"),
              action: () => {
                editor
                  .chain()
                  .focus()
                  .insertColumns({
                    cols: 2,
                  })
                  .run();
              },
            },
          },
        ];
      },
      getCommandMenuItems() {
        return {
          priority: 70,
          icon: markRaw(MdiCollage),
          title: "editor.extensions.commands_menu.columns",
          keywords: ["fenlan", "columns"],
          command: ({ editor, range }: { editor: Editor; range: Range }) => {
            editor
              .chain()
              .focus()
              .deleteRange(range)
              .insertColumns({
                cols: 2,
              })
              .run();
          },
        };
      },
      getBubbleMenu() {
        return {
          pluginKey: COLUMNS_BUBBLE_MENU_KEY,
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, ExtensionColumns.name);
          },
          options: {
            placement: "bottom-start",
          },
          getReferencedVirtualElement() {
            const editor = this.editor;
            if (!editor) {
              return null;
            }
            const parentNode = findParentNode(
              (node) => node.type.name === ExtensionColumn.name
            )(editor.state.selection);
            if (parentNode) {
              const domRect = posToDOMRect(
                editor.view,
                parentNode.pos,
                parentNode.pos + parentNode.node.nodeSize
              );
              return {
                getBoundingClientRect: () => domRect,
                getClientRects: () => [domRect],
              };
            }
            return null;
          },
          items: [
            {
              priority: 10,
              props: {
                icon: markRaw(RiInsertColumnLeft),
                title: i18n.global.t(
                  "editor.extensions.columns.add_column_before"
                ),
                action: ({ editor }: { editor: Editor }) => {
                  editor.chain().focus().addColBefore().run();
                },
              },
            },
            {
              priority: 20,
              props: {
                icon: markRaw(RiInsertColumnRight),
                title: i18n.global.t(
                  "editor.extensions.columns.add_column_after"
                ),
                action: ({ editor }: { editor: Editor }) => {
                  editor.chain().focus().addColAfter().run();
                },
              },
            },
            {
              priority: 30,
              props: {
                icon: markRaw(RiDeleteColumn),
                title: i18n.global.t("editor.extensions.columns.delete_column"),
                action: ({ editor }: { editor: Editor }) => {
                  editor.chain().focus().deleteCol().run();
                },
              },
            },
            {
              priority: 40,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 50,
              props: {
                icon: markRaw(MdiDeleteForeverOutline),
                title: i18n.global.t("editor.common.button.delete"),
                action: ({ editor }: { editor: Editor }) => {
                  deleteNode(ExtensionColumns.name, editor);
                },
              },
            },
          ],
        };
      },
    };
  },

  addAttributes() {
    return {
      cols: {
        default: 2,
        parseHTML: (element) => element.getAttribute("cols"),
      },
      style: {
        default: "display: flex;width: 100%;gap: 1em;",
        parseHTML: (element) => element.getAttribute("style"),
      },
    };
  },

  renderHTML({ HTMLAttributes }) {
    return [
      "div",
      mergeAttributes(this.options.HTMLAttributes, HTMLAttributes),
      0,
    ];
  },

  addCommands() {
    return {
      insertColumns:
        (attrs) =>
        ({ tr, dispatch, editor }) => {
          const node = createColumns(editor.schema, (attrs && attrs.cols) || 3);

          if (dispatch) {
            const offset = tr.selection.anchor + 1;

            tr.replaceSelectionWith(node)
              .scrollIntoView()
              .setSelection(TextSelection.near(tr.doc.resolve(offset)));
          }

          return true;
        },
      addColBefore:
        () =>
        ({ dispatch, state }) => {
          return addOrDeleteCol(dispatch, state, "addBefore");
        },
      addColAfter:
        () =>
        ({ dispatch, state }) => {
          return addOrDeleteCol(dispatch, state, "addAfter");
        },
      deleteCol:
        () =>
        ({ dispatch, state }) => {
          return addOrDeleteCol(dispatch, state, "delete");
        },
    };
  },

  addKeyboardShortcuts() {
    return {
      "Mod-Alt-G": () => this.editor.commands.insertColumns(),
      Tab: () => {
        return gotoCol(this.editor.state, this.editor.view.dispatch, "after");
      },
      "Shift-Tab": () => {
        return gotoCol(this.editor.state, this.editor.view.dispatch, "before");
      },
    };
  },
  addExtensions() {
    return [ExtensionColumn];
  },
});
