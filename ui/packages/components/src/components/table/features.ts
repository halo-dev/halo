import {
  columnVisibilityFeature,
  createColumnHelper,
  createExpandedRowModel,
  createSortedRowModel,
  metaHelper,
  rowExpandingFeature,
  rowSelectionFeature,
  rowSortingFeature,
  sortFn_alphanumeric,
  sortFn_basic,
  sortFn_datetime,
  sortFn_text,
  tableFeatures,
  type RowData,
} from "@tanstack/vue-table";

/**
 * Per-column presentation metadata understood by `VTable`.
 */
export interface TableColumnMeta {
  /**
   * Horizontal alignment of the header and body cells.
   */
  align?: "left" | "center" | "right";
  /**
   * Column width. Numbers are treated as pixels.
   */
  width?: string | number;
  /**
   * Minimum column width. Numbers are treated as pixels.
   */
  minWidth?: string | number;
  /**
   * Extra classes appended to the header cell.
   */
  thClass?: string;
  /**
   * Extra classes appended to the body cells.
   */
  tdClass?: string;
}

/**
 * Features registered for every `VTable`. Declared outside components so the
 * feature set stays static and shared by all table instances.
 */
export const vTableFeatures = tableFeatures({
  rowSortingFeature,
  rowSelectionFeature,
  rowExpandingFeature,
  columnVisibilityFeature,
  sortedRowModel: createSortedRowModel(),
  expandedRowModel: createExpandedRowModel(),
  sortFns: {
    alphanumeric: sortFn_alphanumeric,
    text: sortFn_text,
    datetime: sortFn_datetime,
    basic: sortFn_basic,
  },
  columnMeta: metaHelper<TableColumnMeta>(),
});

export type VTableFeatures = typeof vTableFeatures;

/**
 * Creates a column helper bound to the features used by `VTable`.
 *
 * @example
 * ```ts
 * const column = createTableColumnHelper<Post>();
 * const columns = column.columns([
 *   column.accessor("title", { header: "Title" }),
 *   column.accessor("creationTimestamp", {
 *     header: "Created",
 *     sortFn: "datetime",
 *   }),
 * ]);
 * ```
 */
export function createTableColumnHelper<TData extends RowData>() {
  return createColumnHelper<VTableFeatures, TData>();
}
