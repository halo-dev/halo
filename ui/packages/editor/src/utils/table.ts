import type { Selection, Transaction } from "@/tiptap";
import {
  CellSelection,
  type Node as ProseMirrorNode,
  TableMap,
} from "@/tiptap/pm";
import { findParentNode } from "@/tiptap/vue-3";

export interface TablePosition {
  pos: number;
  start: number;
  depth: number;
  node: ProseMirrorNode;
}

export interface TableCellPosition {
  pos: number;
  start: number;
  node: ProseMirrorNode;
}

export interface TableHeaderState {
  row: boolean;
  column: boolean;
}

export const findTable = (selection: Selection): TablePosition | undefined => {
  return findParentNode((node) => node.type.spec.tableRole === "table")(
    selection
  ) as TablePosition | undefined;
};

function getCellsInRect(
  selection: Selection,
  indexes: number | number[],
  axis: "row" | "column"
): TableCellPosition[] {
  const table = findTable(selection);
  if (!table) {
    return [];
  }

  const map = TableMap.get(table.node);
  const positions = new Map<number, TableCellPosition>();
  const targetIndexes = Array.isArray(indexes) ? indexes : [indexes];

  targetIndexes.forEach((index) => {
    const limit = axis === "row" ? map.height : map.width;
    if (index < 0 || index >= limit) {
      return;
    }

    const rect =
      axis === "row"
        ? {
            left: 0,
            right: map.width,
            top: index,
            bottom: index + 1,
          }
        : {
            left: index,
            right: index + 1,
            top: 0,
            bottom: map.height,
          };

    map.cellsInRect(rect).forEach((nodePos) => {
      const node = table.node.nodeAt(nodePos);
      if (!node) {
        return;
      }

      const pos = table.start + nodePos;
      positions.set(pos, { pos, start: pos + 1, node });
    });
  });

  return Array.from(positions.values());
}

export const getCellsInColumn =
  (columnIndex: number | number[]) =>
  (selection: Selection): TableCellPosition[] => {
    return getCellsInRect(selection, columnIndex, "column");
  };

export const getCellsInRow =
  (rowIndex: number | number[]) =>
  (selection: Selection): TableCellPosition[] => {
    return getCellsInRect(selection, rowIndex, "row");
  };

export const selectTable = (tr: Transaction) => {
  const table = findTable(tr.selection);
  if (!table) {
    return tr;
  }

  const map = TableMap.get(table.node);
  if (!map.map.length) {
    return tr;
  }

  return tr.setSelection(
    new CellSelection(
      tr.doc.resolve(table.start + map.map[map.map.length - 1]),
      tr.doc.resolve(table.start + map.map[0])
    )
  );
};

export const selectRow = (rowIndex: number) => (tr: Transaction) => {
  const cells = getCellsInRow(rowIndex)(tr.selection);
  if (!cells.length) {
    return tr;
  }

  return tr.setSelection(
    CellSelection.rowSelection(
      tr.doc.resolve(cells[0].pos),
      tr.doc.resolve(cells[cells.length - 1].pos)
    )
  );
};

export const selectColumn = (columnIndex: number) => (tr: Transaction) => {
  const cells = getCellsInColumn(columnIndex)(tr.selection);
  if (!cells.length) {
    return tr;
  }

  return tr.setSelection(
    CellSelection.colSelection(
      tr.doc.resolve(cells[0].pos),
      tr.doc.resolve(cells[cells.length - 1].pos)
    )
  );
};

export const isCellSelection = (
  selection: Selection
): selection is CellSelection => {
  return selection instanceof CellSelection;
};

export function getTableHeaderState(selection: Selection): TableHeaderState {
  const table = findTable(selection);
  if (!table) {
    return { row: false, column: false };
  }

  const map = TableMap.get(table.node);
  const isHeaderCell = (pos: number) =>
    table.node.nodeAt(pos)?.type.spec.tableRole === "header_cell";
  const firstRow = new Set(
    map.cellsInRect({ left: 0, right: map.width, top: 0, bottom: 1 })
  );
  const firstColumn = new Set(
    map.cellsInRect({ left: 0, right: 1, top: 0, bottom: map.height })
  );

  return {
    row: firstRow.size > 0 && Array.from(firstRow).every(isHeaderCell),
    column: firstColumn.size > 0 && Array.from(firstColumn).every(isHeaderCell),
  };
}

function getSelectedCellPositions(
  selection: CellSelection,
  tableStart: number
) {
  const map = TableMap.get(selection.$anchorCell.node(-1));
  return new Set(
    map
      .cellsInRect(
        map.rectBetween(
          selection.$anchorCell.pos - tableStart,
          selection.$headCell.pos - tableStart
        )
      )
      .map((pos) => tableStart + pos)
  );
}

export const isTableSelected = (selection: Selection) => {
  if (!isCellSelection(selection)) {
    return false;
  }

  const map = TableMap.get(selection.$anchorCell.node(-1));
  const tableStart = selection.$anchorCell.start(-1);
  const selectedCells = getSelectedCellPositions(selection, tableStart);

  return selectedCells.size > 0 && selectedCells.size === new Set(map.map).size;
};

function isAxisSelected(
  selection: Selection,
  index: number,
  axis: "row" | "column"
) {
  if (!isCellSelection(selection)) {
    return false;
  }

  const table = findTable(selection);
  if (!table) {
    return false;
  }

  const expected =
    axis === "row"
      ? getCellsInRow(index)(selection)
      : getCellsInColumn(index)(selection);
  const selected = getSelectedCellPositions(selection, table.start);

  return expected.length > 0 && expected.every(({ pos }) => selected.has(pos));
}

export const isRowSelected = (rowIndex: number) => (selection: Selection) => {
  return isAxisSelected(selection, rowIndex, "row");
};

export const isColumnSelected =
  (columnIndex: number) => (selection: Selection) => {
    return isAxisSelected(selection, columnIndex, "column");
  };

export const hasTableBefore = (selection: Selection) => {
  const { $anchor } = selection;
  if ($anchor.parentOffset !== 0 || $anchor.depth < 1) {
    return false;
  }

  const index = $anchor.index($anchor.depth - 1);
  if (index <= 0) {
    return false;
  }

  return $anchor.node($anchor.depth - 1).child(index - 1).type.name === "table";
};
