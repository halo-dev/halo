import type {
  Cell,
  Column,
  ColumnDef,
  Header,
  Row,
  RowData,
  Table,
} from "@tanstack/vue-table";
import type { VTableFeatures } from "./features";

/**
 * Column definition accepted by `VTable`.
 *
 * `any` keeps columns with different accessor value types assignable to the
 * same array, which is how TanStack's `createColumnHelper` is designed to be
 * consumed.
 */
/* eslint-disable @typescript-eslint/no-explicit-any */
export type TableColumn<TData extends RowData> = ColumnDef<
  VTableFeatures,
  TData,
  any
>;
/* eslint-enable @typescript-eslint/no-explicit-any */

/**
 * Props passed to a `#cell-<columnId>` slot.
 */
export interface TableCellSlotProps<TData extends RowData> {
  row: Row<VTableFeatures, TData>;
  original: TData;
  value: unknown;
  column: Column<VTableFeatures, TData>;
  cell: Cell<VTableFeatures, TData>;
  table: Table<VTableFeatures, TData>;
}

/**
 * Props passed to a `#header-<columnId>` slot.
 */
export interface TableHeaderSlotProps<TData extends RowData> {
  header: Header<VTableFeatures, TData>;
  column: Column<VTableFeatures, TData>;
  table: Table<VTableFeatures, TData>;
}

/**
 * Props passed to the `#expanded` slot.
 */
export interface TableExpandedSlotProps<TData extends RowData> {
  row: Row<VTableFeatures, TData>;
  table: Table<VTableFeatures, TData>;
}

/**
 * Class overrides appended to the corresponding table elements.
 */
export interface TableUi {
  root?: string;
  table?: string;
  thead?: string;
  tbody?: string;
  tr?: string;
  th?: string;
  td?: string;
  empty?: string;
  loading?: string;
}
