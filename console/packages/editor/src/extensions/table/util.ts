import { findParentNode } from "@/tiptap/vue-3";
import { Node, CellSelection, TableMap } from "@/tiptap/pm";
import type { Selection, Transaction } from "@/tiptap/pm";

export const selectTable = (tr: Transaction) => {
  const table = findTable(tr.selection);
  if (table) {
    const { map } = TableMap.get(table.node);
    if (map && map.length) {
      const head = table.start + map[0];
      const anchor = table.start + map[map.length - 1];
      const $head = tr.doc.resolve(head);
      const $anchor = tr.doc.resolve(anchor);
      return tr.setSelection(new CellSelection($anchor, $head));
    }
  }
  return tr;
};

const select =
  (type: "row" | "column") => (index: number) => (tr: Transaction) => {
    const table = findTable(tr.selection);
    const isRowSelection = type === "row";

    if (table) {
      const map = TableMap.get(table.node);
      if (index >= 0 && index < (isRowSelection ? map.height : map.width)) {
        const cells = isRowSelection
          ? map.cellsInRect({
              left: 0,
              right: 1,
              top: 0,
              bottom: map.height,
            })
          : map.cellsInRect({
              left: 0,
              right: map.width,
              top: 0,
              bottom: 1,
            });

        const startCellRect = map.findCell(cells[index]);
        const rect = {
          left: isRowSelection ? map.width - 1 : startCellRect.left,
          right: isRowSelection ? map.width : startCellRect.right,
          top: isRowSelection ? startCellRect.top : map.height - 1,
          bottom: isRowSelection ? startCellRect.bottom : map.height,
        };
        let endCellRect = map.cellsInRect(rect);
        while (endCellRect.length === 0) {
          if (isRowSelection) {
            rect.left -= 1;
          } else {
            rect.top -= 1;
          }
          endCellRect = map.cellsInRect(rect);
        }

        const head = table.start + cells[index];
        const anchor = table.start + endCellRect[endCellRect.length - 1];
        const $head = tr.doc.resolve(head);
        const $anchor = tr.doc.resolve(anchor);

        return tr.setSelection(new CellSelection($anchor, $head));
      }
    }
    return tr;
  };

export const selectColumn = select("column");

export const selectRow = select("row");

export const getCellsInColumn =
  (columnIndex: number | number[]) => (selection: Selection) => {
    const table = findTable(selection);
    if (table) {
      const map = TableMap.get(table.node);
      const indexes = Array.isArray(columnIndex)
        ? columnIndex
        : Array.from([columnIndex]);
      return indexes.reduce((acc, index) => {
        if (index >= 0 && index <= map.width - 1) {
          const cells = map.cellsInRect({
            left: index,
            right: index + 1,
            top: 0,
            bottom: map.height,
          });
          return acc.concat(
            cells.map((nodePos: number) => {
              const node = table.node.nodeAt(nodePos);
              const pos = nodePos + table.start;
              return { pos, start: pos + 1, node };
            }) as unknown as {
              pos: number;
              start: number;
              node: Node | null | undefined;
            }[]
          );
        }
        return acc;
      }, [] as { pos: number; start: number; node: Node | null | undefined }[]);
    }
  };

export const getCellsInRow =
  (rowIndex: number | number[]) => (selection: Selection) => {
    const table = findTable(selection);
    if (table) {
      const map = TableMap.get(table.node);
      const indexes = Array.isArray(rowIndex)
        ? rowIndex
        : Array.from([rowIndex]);
      return indexes.reduce((acc, index) => {
        if (index >= 0 && index <= map.height - 1) {
          const cells = map.cellsInRect({
            left: 0,
            right: map.width,
            top: index,
            bottom: index + 1,
          });
          return acc.concat(
            cells.map((nodePos) => {
              const node = table.node.nodeAt(nodePos);
              const pos = nodePos + table.start;
              return { pos, start: pos + 1, node };
            }) as unknown as {
              pos: number;
              start: number;
              node: Node | null | undefined;
            }[]
          );
        }
        return acc;
      }, [] as { pos: number; start: number; node: Node | null | undefined }[]);
    }
  };

export const findTable = (selection: Selection) => {
  return findParentNode((node) => node.type.spec.tableRole === "table")(
    selection
  ) as
    | {
        pos: number;
        start: number;
        depth: number;
        node: Node;
      }
    | undefined;
};

export const isRectSelected = (rect: any) => (selection: CellSelection) => {
  const map = TableMap.get(selection.$anchorCell.node(-1));
  const start = selection.$anchorCell.start(-1);
  const cells = map.cellsInRect(rect);
  const selectedCells = map.cellsInRect(
    map.rectBetween(
      selection.$anchorCell.pos - start,
      selection.$headCell.pos - start
    )
  );

  for (let i = 0, count = cells.length; i < count; i++) {
    if (selectedCells.indexOf(cells[i]) === -1) {
      return false;
    }
  }

  return true;
};

export const isCellSelection = (selection: any) => {
  return selection instanceof CellSelection;
};

export const isColumnSelected = (columnIndex: number) => (selection: any) => {
  if (isCellSelection(selection)) {
    const map = TableMap.get(selection.$anchorCell.node(-1));
    const cells = map.cellsInRect({
      left: 0,
      right: map.width,
      top: 0,
      bottom: 1,
    });
    if (columnIndex >= cells.length) {
      return false;
    }
    const startCellRect = map.findCell(cells[columnIndex]);
    const isSelect = isRectSelected({
      left: startCellRect.left,
      right: startCellRect.right,
      top: 0,
      bottom: map.height,
    })(selection);
    return isSelect;
  }

  return false;
};

export const isRowSelected = (rowIndex: number) => (selection: any) => {
  if (isCellSelection(selection)) {
    const map = TableMap.get(selection.$anchorCell.node(-1));
    const cells = map.cellsInRect({
      left: 0,
      right: 1,
      top: 0,
      bottom: map.height,
    });
    if (rowIndex >= cells.length) {
      return false;
    }
    const startCellRect = map.findCell(cells[rowIndex]);
    return isRectSelected({
      left: 0,
      right: map.width,
      top: startCellRect.top,
      bottom: startCellRect.bottom,
    })(selection);
  }

  return false;
};

export const isTableSelected = (selection: any) => {
  if (isCellSelection(selection)) {
    const map = TableMap.get(selection.$anchorCell.node(-1));
    return isRectSelected({
      left: 0,
      right: map.width,
      top: 0,
      bottom: map.height,
    })(selection);
  }

  return false;
};
