import type { Node as ProseMirrorNode } from "@/tiptap/pm";

export function getTableColumnWidths(node: ProseMirrorNode): number[] {
  const widths: number[] = [];
  node.firstChild?.forEach((cell) => {
    for (let index = 0; index < cell.attrs.colspan; index++) {
      widths.push(cell.attrs.colwidth?.[index] || 0);
    }
  });
  const definedWidths = widths.filter((width) => width > 0);
  if (!definedWidths.length) {
    return [];
  }
  // Newly inserted columns have no width yet. Give them an average share.
  const defaultWidth =
    definedWidths.reduce((sum, width) => sum + width, 0) / definedWidths.length;
  return widths.map((width) => width || defaultWidth);
}

export function getColumnPercentages(widths: number[]): string[] {
  const total = widths.reduce((sum, width) => sum + width, 0);
  return widths.map((width) => `${(width / total) * 100}%`);
}

export function applyAutoColumnWidths(
  table: HTMLTableElement,
  colgroup: HTMLTableColElement,
  widths: number[],
  cellMinWidth: number
) {
  table.style.width = "100%";
  table.style.minWidth = "100%";
  table.style.tableLayout = widths.length ? "fixed" : "auto";
  const percentages = getColumnPercentages(widths);
  Array.from(colgroup.children).forEach((column, index) => {
    const element = column as HTMLTableColElement;
    element.style.width = percentages[index] ?? "";
    element.style.minWidth = `${cellMinWidth}px`;
  });
}
