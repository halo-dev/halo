import type { CommandProps } from "@tiptap/core";
import {
  CellSelection,
  __insertCells as insertTableCells,
  __pastedCells as getPastedTableCells,
  addColumn,
  addRow,
  closeHistory,
  deleteColumn as deleteProseMirrorTableColumn,
  deleteRow as deleteProseMirrorTableRow,
  deleteTable as deleteProseMirrorTable,
  type EditorState,
  type EditorView,
  type Node as ProseMirrorNode,
  type Transaction,
  TableMap,
  TextSelection,
  moveTableColumn as moveProseMirrorTableColumn,
  moveTableRow as moveProseMirrorTableRow,
  selectedRect,
} from "@/tiptap/pm";
import {
  findTable,
  getCellsInColumn,
  getCellsInRow,
  selectColumn,
  selectRow,
  selectTable,
} from "@/utils/table";
import {
  normalizeCellVerticalAlign,
  normalizeCssColor,
  normalizeRowHeight,
  normalizeTableLayoutMode,
  type CellVerticalAlign,
  type TableLayoutMode,
} from "./attributes";
import { writeTableToClipboard } from "./clipboard";

declare module "@tiptap/core" {
  interface Commands<ReturnType> {
    haloTable: {
      setTableLayout: (layoutMode: TableLayoutMode) => ReturnType;
      fitTableToWidth: () => ReturnType;
      setTableRowHeight: (height: number | null) => ReturnType;
      setTableCellBackground: (color: string | null) => ReturnType;
      setTableCellVerticalAlign: (
        alignment: CellVerticalAlign | null
      ) => ReturnType;
      clearTableCellFormatting: () => ReturnType;
      clearSelectedTableRow: () => ReturnType;
      clearSelectedTableColumn: () => ReturnType;
      duplicateTableRow: () => ReturnType;
      duplicateTableColumn: () => ReturnType;
      moveTableRow: (direction: -1 | 1) => ReturnType;
      moveTableColumn: (direction: -1 | 1) => ReturnType;
      moveTableRowTo: (target: number) => ReturnType;
      moveTableColumnTo: (target: number) => ReturnType;
      selectCurrentTable: () => ReturnType;
      selectTableRow: (index: number) => ReturnType;
      selectTableColumn: (index: number) => ReturnType;
      copyTable: () => ReturnType;
    };
  }
}

export function selectTableCommand(
  axis: "table" | "row" | "column",
  index = 0
): (props: CommandProps) => boolean {
  return ({ state, dispatch }) => {
    const table = findTable(state.selection);
    if (!table) {
      return false;
    }

    const map = TableMap.get(table.node);
    if (
      (axis === "row" && (index < 0 || index >= map.height)) ||
      (axis === "column" && (index < 0 || index >= map.width))
    ) {
      return false;
    }

    if (dispatch) {
      const tr =
        axis === "table"
          ? selectTable(state.tr)
          : axis === "row"
            ? selectRow(index)(state.tr)
            : selectColumn(index)(state.tr);
      dispatch(tr);
    }
    return true;
  };
}

export function copyTableCommand(): (props: CommandProps) => boolean {
  return ({ editor, state }) => {
    if (
      !findTable(state.selection) ||
      typeof navigator === "undefined" ||
      !navigator.clipboard
    ) {
      return false;
    }
    void writeTableToClipboard(editor).catch(() => undefined);
    return true;
  };
}

export function setTableLayoutCommand(
  layoutMode: TableLayoutMode
): (props: CommandProps) => boolean {
  return ({ state, tr, dispatch, view }) => {
    const normalized = normalizeTableLayoutMode(layoutMode);
    const table = findTable(state.selection);
    if (!normalized || !table) {
      return false;
    }

    if (!dispatch) {
      return true;
    }

    if (normalized === "auto") {
      clearColumnWidths(table.node, table.start, tr);
    } else if (!hasColumnWidths(table.node)) {
      materializeColumnWidths(table.node, table.start, table.pos, tr, view);
    }

    tr.setNodeMarkup(table.pos, undefined, {
      ...table.node.attrs,
      layoutMode: normalized,
    });
    dispatch(tr);
    return true;
  };
}

export function fitTableToWidthCommand(): (props: CommandProps) => boolean {
  return setTableLayoutCommand("auto");
}

export function setTableRowHeightCommand(
  height: number | null
): (props: CommandProps) => boolean {
  return ({ state, tr, dispatch }) => {
    const table = findTable(state.selection);
    if (!table) {
      return false;
    }

    const normalized = height === null ? null : normalizeRowHeight(height);
    if (height !== null && normalized === null) {
      return false;
    }

    const rect = selectedRect(state);
    const rowIndexes = Array.from(
      { length: Math.max(1, rect.bottom - rect.top) },
      (_, index) => rect.top + index
    );
    const rows = getRowPositions(table.node, table.start).filter(({ index }) =>
      rowIndexes.includes(index)
    );

    if (!rows.length) {
      return false;
    }

    if (dispatch) {
      rows.forEach(({ pos, node }) => {
        tr.setNodeMarkup(pos, undefined, {
          ...node.attrs,
          rowHeight: normalized,
        });
      });
      dispatch(tr);
    }
    return true;
  };
}

export function setCellAttributeCommand(
  attribute: "backgroundColor" | "verticalAlign",
  value: string | null
): (props: CommandProps) => boolean {
  return ({ state, commands }) => {
    if (!findTable(state.selection)) {
      return false;
    }

    const normalized =
      attribute === "backgroundColor"
        ? value === null
          ? null
          : normalizeCssColor(value)
        : value === null
          ? null
          : normalizeCellVerticalAlign(value);
    if (value !== null && normalized === null) {
      return false;
    }

    return commands.setCellAttribute(attribute, normalized);
  };
}

export function clearTableCellFormattingCommand(): (
  props: CommandProps
) => boolean {
  return ({ state, tr, dispatch }) => {
    const cells = getSelectedCells(state);
    if (!cells.length) {
      return false;
    }

    if (dispatch) {
      closeHistory(tr);
      cells.forEach(({ pos, node }) => {
        tr.setNodeMarkup(pos, undefined, {
          ...node.attrs,
          align: null,
          verticalAlign: null,
          backgroundColor: null,
        });
      });
      dispatch(tr);
    }
    return true;
  };
}

export function clearSelectedAxisCommand(
  axis: "row" | "column"
): (props: CommandProps) => boolean {
  return ({ state, tr, dispatch }) => {
    const rect = selectedRect(state);
    const index = axis === "row" ? rect.top : rect.left;
    const cells =
      axis === "row"
        ? getCellsInRow(index)(state.selection)
        : getCellsInColumn(index)(state.selection);

    if (!cells.length) {
      return false;
    }

    const replacements = cells
      .map(({ pos, node }) => ({
        pos,
        node: node.type.createAndFill(node.attrs),
      }))
      .filter(
        (replacement): replacement is { pos: number; node: ProseMirrorNode } =>
          Boolean(replacement.node)
      )
      .sort((a, b) => b.pos - a.pos);

    if (dispatch) {
      closeHistory(tr);
      replacements.forEach(({ pos, node }) => {
        tr.replaceWith(pos, pos + state.doc.nodeAt(pos)!.nodeSize, node);
      });
      dispatch(tr);
    }
    return true;
  };
}

export function duplicateAxisCommand(
  axis: "row" | "column"
): (props: CommandProps) => boolean {
  return ({ state, dispatch }) => {
    const table = findTable(state.selection);
    if (!table) {
      return false;
    }

    const map = TableMap.get(table.node);
    const rect = selectedRect(state);
    const index = axis === "row" ? rect.top : rect.left;
    const firstCell =
      axis === "row" ? map.map[index * map.width] : map.map[index];
    const lastCell =
      axis === "row"
        ? map.map[index * map.width + map.width - 1]
        : map.map[(map.height - 1) * map.width + index];
    const $firstCell = state.doc.resolve(table.start + firstCell);
    const $lastCell = state.doc.resolve(table.start + lastCell);
    const selection =
      axis === "row"
        ? CellSelection.rowSelection($firstCell, $lastCell)
        : CellSelection.colSelection($firstCell, $lastCell);
    const cells = getPastedTableCells(selection.content());
    if (!cells) {
      return false;
    }

    if (dispatch) {
      const tr = state.tr;
      if (axis === "row") {
        addRow(tr, rect, index + 1);
      } else {
        addColumn(tr, rect, index + 1);
      }
      const interimState = state.apply(tr);
      const interimTable = findTable(interimState.selection);
      if (!interimTable) {
        return false;
      }
      insertTableCells(
        interimState,
        (transaction) => {
          transaction.steps.forEach((step) => tr.step(step));
          if (transaction.selection instanceof CellSelection) {
            tr.setSelection(
              CellSelection.create(
                tr.doc,
                transaction.selection.$anchorCell.pos,
                transaction.selection.$headCell.pos
              )
            );
          }
          closeHistory(tr);
          dispatch(tr);
        },
        interimTable.start,
        {
          top: axis === "row" ? index + 1 : 0,
          bottom: axis === "row" ? index + 1 : map.height,
          left: axis === "column" ? index + 1 : 0,
          right: axis === "column" ? index + 1 : map.width,
        },
        cells
      );
    }
    return true;
  };
}

export function deleteAxisCommand(
  axis: "row" | "column"
): (props: CommandProps) => boolean {
  return ({ state, dispatch }) => {
    const table = findTable(state.selection);
    if (!table) {
      return false;
    }

    const map = TableMap.get(table.node);
    const rect = selectedRect(state);
    const deletesWholeTable =
      axis === "row"
        ? rect.top === 0 && rect.bottom === map.height
        : rect.left === 0 && rect.right === map.width;
    const command = deletesWholeTable
      ? deleteProseMirrorTable
      : axis === "row"
        ? deleteProseMirrorTableRow
        : deleteProseMirrorTableColumn;

    return command(
      state,
      dispatch
        ? (transaction) => {
            if (!deletesWholeTable) {
              keepCursorInTable(transaction, table.pos);
            }
            closeHistory(transaction);
            dispatch(transaction);
          }
        : undefined
    );
  };
}

/**
 * Keep the selection inside the table being edited after an axis deletion.
 * ProseMirror tables do not set a new selection after deleting rows or columns,
 * so a cursor in the last axis can otherwise map into following content.
 */
function keepCursorInTable(transaction: Transaction, tablePos: number) {
  const mappedTablePos = transaction.mapping.map(tablePos);
  const selectedTable = findTable(transaction.selection);
  if (selectedTable?.pos === mappedTablePos) {
    return;
  }

  const tableNode = transaction.doc.nodeAt(mappedTablePos);
  if (tableNode?.type.spec.tableRole !== "table") {
    return;
  }

  const endOfTable = mappedTablePos + tableNode.nodeSize - 1;
  transaction.setSelection(
    TextSelection.near(transaction.doc.resolve(endOfTable), -1)
  );
}

export function moveAxisCommand(
  axis: "row" | "column",
  direction: -1 | 1
): (props: CommandProps) => boolean {
  return ({ state, dispatch }) => {
    const table = findTable(state.selection);
    if (!table) {
      return false;
    }

    const rect = selectedRect(state);
    const index = axis === "row" ? rect.top : rect.left;
    const target = index + direction;
    return moveAxisToCommand(axis, target)({ state, dispatch } as CommandProps);
  };
}

export function moveAxisToCommand(
  axis: "row" | "column",
  target: number
): (props: CommandProps) => boolean {
  return ({ state, dispatch }) => {
    const table = findTable(state.selection);
    if (!table) {
      return false;
    }

    const map = TableMap.get(table.node);
    const rect = selectedRect(state);
    const index = axis === "row" ? rect.top : rect.left;
    const limit = axis === "row" ? map.height : map.width;
    if (target < 0 || target >= limit) {
      return false;
    }
    if (target === index) {
      return true;
    }

    const move =
      axis === "row"
        ? moveProseMirrorTableRow({ from: index, to: target })
        : moveProseMirrorTableColumn({ from: index, to: target });
    return move(
      state,
      dispatch
        ? (transaction) => {
            closeHistory(transaction);
            dispatch(transaction);
          }
        : undefined
    );
  };
}

export function tableLayoutTransitionPluginAppendTransaction(
  transactions: readonly Transaction[],
  oldState: EditorState,
  newState: EditorState
) {
  if (!transactions.some((transaction) => transaction.docChanged)) {
    return null;
  }

  const tr = newState.tr;
  let changed = false;

  newState.doc.descendants((node, pos) => {
    if (
      node.type.spec.tableRole !== "table" ||
      node.attrs.layoutMode !== "auto" ||
      !hasColumnWidths(node)
    ) {
      return;
    }

    const oldNode =
      pos <= oldState.doc.content.size ? oldState.doc.nodeAt(pos) : null;
    if (oldNode?.type === node.type && hasColumnWidths(oldNode)) {
      return;
    }

    tr.setNodeMarkup(pos, undefined, {
      ...node.attrs,
      layoutMode: "fixed",
    });
    changed = true;
  });

  return changed ? tr : null;
}

function getSelectedCells(state: EditorState) {
  const table = findTable(state.selection);
  if (!table) {
    return [];
  }

  const rect = selectedRect(state);
  return rect.map
    .cellsInRect(rect)
    .map((relativePos) => {
      const node = table.node.nodeAt(relativePos);
      return node
        ? {
            pos: table.start + relativePos,
            node,
          }
        : null;
    })
    .filter(
      (cell): cell is { pos: number; node: ProseMirrorNode } => cell !== null
    );
}

function getRowPositions(table: ProseMirrorNode, tableStart: number) {
  const rows: Array<{ index: number; pos: number; node: ProseMirrorNode }> = [];
  table.forEach((node, offset, index) => {
    rows.push({ index, pos: tableStart + offset, node });
  });
  return rows;
}

function clearColumnWidths(
  table: ProseMirrorNode,
  tableStart: number,
  tr: Transaction
) {
  table.descendants((node, relativePos) => {
    if (
      (node.type.spec.tableRole === "cell" ||
        node.type.spec.tableRole === "header_cell") &&
      node.attrs.colwidth
    ) {
      tr.setNodeMarkup(tableStart + relativePos, undefined, {
        ...node.attrs,
        colwidth: null,
      });
    }
  });
}

function hasColumnWidths(table: ProseMirrorNode) {
  let hasWidths = false;
  table.descendants((node) => {
    if (
      (node.type.spec.tableRole === "cell" ||
        node.type.spec.tableRole === "header_cell") &&
      Array.isArray(node.attrs.colwidth) &&
      node.attrs.colwidth.some((width: unknown) => Number(width) > 0)
    ) {
      hasWidths = true;
      return false;
    }
  });
  return hasWidths;
}

function materializeColumnWidths(
  table: ProseMirrorNode,
  tableStart: number,
  tablePos: number,
  tr: Transaction,
  view?: EditorView
) {
  const tableDom = view?.nodeDOM(tablePos) as HTMLElement | null;
  const map = TableMap.get(table);
  const renderedColumns =
    tableDom?.querySelectorAll<HTMLElement>("colgroup > col");
  const measuredColumnWidths = renderedColumns
    ? Array.from(
        renderedColumns,
        (column) => column.getBoundingClientRect().width
      )
    : [];
  const cells = tableDom?.querySelectorAll<HTMLElement>(
    "tr:first-child > th, tr:first-child > td"
  );
  const measuredCellWidths = cells
    ? Array.from(cells).flatMap((cell) => {
        const colspan = Math.max(1, Number(cell.getAttribute("colspan")) || 1);
        const width = cell.getBoundingClientRect().width / colspan;
        return Array.from({ length: colspan }, () => width);
      })
    : [];
  const measuredWidths =
    measuredColumnWidths.length === map.width &&
    measuredColumnWidths.every((width) => width > 0)
      ? measuredColumnWidths
      : measuredCellWidths;
  const widths = roundMeasuredColumnWidths(
    Array.from(
      { length: map.width },
      (_, index) => measuredWidths[index] ?? 100
    )
  );

  table.descendants((node, relativePos) => {
    if (
      node.type.spec.tableRole !== "cell" &&
      node.type.spec.tableRole !== "header_cell"
    ) {
      return;
    }

    const rect = map.findCell(relativePos);
    tr.setNodeMarkup(tableStart + relativePos, undefined, {
      ...node.attrs,
      colwidth: widths.slice(rect.left, rect.right),
    });
  });
}

function roundMeasuredColumnWidths(widths: number[]) {
  const normalized = widths.map((width) => Math.max(25, width));
  const rounded = normalized.map(Math.floor);
  let remainder = Math.round(
    normalized.reduce((total, width) => total + width, 0) -
      rounded.reduce((total, width) => total + width, 0)
  );
  const fractionalOrder = normalized
    .map((width, index) => ({ index, fraction: width - Math.floor(width) }))
    .sort(
      (left, right) =>
        right.fraction - left.fraction || left.index - right.index
    );

  for (
    let index = 0;
    index < fractionalOrder.length && remainder > 0;
    index += 1
  ) {
    rounded[fractionalOrder[index].index] += 1;
    remainder -= 1;
  }
  return rounded;
}
