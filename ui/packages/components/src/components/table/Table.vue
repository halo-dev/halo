<script lang="ts" setup generic="TData extends RowData">
import {
  FlexRender,
  functionalUpdate,
  useTable,
  type CellContext,
  type ColumnVisibilityState,
  type ExpandedState,
  type Header,
  type RowData,
  type RowSelectionState,
  type SortDirection,
  type SortingState,
  type Table,
  type Updater,
} from "@tanstack/vue-table";
import {
  computed,
  h,
  ref,
  watch,
  type Component,
  type CSSProperties,
} from "vue";
import {
  IconArrowDownLine,
  IconArrowRight,
  IconArrowUpDownLine,
  IconArrowUpLine,
} from "../../icons/icons";
import { VLoading } from "../loading";
import {
  vTableFeatures,
  type TableColumnMeta,
  type VTableFeatures,
} from "./features";
import type {
  TableCellSlotProps,
  TableColumn,
  TableExpandedSlotProps,
  TableHeaderSlotProps,
  TableUi,
} from "./types";

const props = withDefaults(
  defineProps<{
    /**
     * Rows displayed by the table.
     */
    data: TData[];
    /**
     * Column definitions. Use `createTableColumnHelper` for value inference.
     */
    columns: TableColumn<TData>[];
    /**
     * Property name or function used as the stable row id. Falls back to the
     * row index, which is unstable when the data is sorted or replaced.
     */
    rowKey?: string | ((row: TData) => string | number);
    /**
     * Whether data is being loaded. When there are no rows, a loading state is
     * displayed instead of the empty state.
     */
    loading?: boolean;
    /**
     * Text displayed when there are no rows.
     */
    emptyText?: string;
    /**
     * Whether the header stays visible while the table scrolls. The wrapper
     * needs a height constraint for this to take effect.
     */
    sticky?: boolean;
    /**
     * Whether to render a selection column with select-all support.
     */
    selectable?: boolean;
    /**
     * Whether to render an expand control and the expanded detail row.
     */
    expandable?: boolean;
    /**
     * Whether sorting is handled by the caller. When enabled, the table emits
     * the sorting state without reordering rows.
     */
    manualSorting?: boolean;
    /**
     * Controlled sorting state.
     */
    sorting?: SortingState;
    /**
     * Controlled row selection state.
     */
    rowSelection?: RowSelectionState;
    /**
     * Controlled expanded state.
     */
    expanded?: ExpandedState;
    /**
     * Controlled column visibility state.
     */
    columnVisibility?: ColumnVisibilityState;
    /**
     * Class overrides appended to the table elements.
     */
    ui?: TableUi;
    /**
     * Table caption, also used as its accessible name.
     */
    caption?: string;
    /**
     * Accessible label of the select-all checkbox.
     */
    selectAllLabel?: string;
    /**
     * Prefix of the accessible label of each row checkbox.
     */
    selectRowLabel?: string;
  }>(),
  {
    rowKey: undefined,
    loading: false,
    emptyText: "暂无数据",
    sticky: false,
    selectable: false,
    expandable: false,
    manualSorting: false,
    sorting: undefined,
    rowSelection: undefined,
    expanded: undefined,
    columnVisibility: undefined,
    ui: () => ({}),
    caption: undefined,
    selectAllLabel: "选择全部",
    selectRowLabel: "选择",
  }
);

const emit = defineEmits<{
  (event: "update:sorting", value: SortingState): void;
  (event: "update:rowSelection", value: RowSelectionState): void;
  (event: "update:expanded", value: ExpandedState): void;
  (event: "update:columnVisibility", value: ColumnVisibilityState): void;
  (event: "row-click", row: TData): void;
}>();

defineSlots<
  {
    empty?: () => unknown;
    loading?: () => unknown;
    caption?: () => unknown;
    expanded?: (props: TableExpandedSlotProps<TData>) => unknown;
  } & {
    [key: `cell-${string}`]: (props: TableCellSlotProps<TData>) => unknown;
    [key: `header-${string}`]: (props: TableHeaderSlotProps<TData>) => unknown;
  }
>();

const sorting = ref<SortingState>(props.sorting ?? []);
const rowSelection = ref<RowSelectionState>(props.rowSelection ?? {});
const expanded = ref<ExpandedState>(props.expanded ?? {});
const columnVisibility = ref<ColumnVisibilityState>(
  props.columnVisibility ?? {}
);

watch(
  () => props.sorting,
  (value) => {
    if (value !== undefined) {
      sorting.value = value;
    }
  },
  { deep: true }
);

watch(
  () => props.rowSelection,
  (value) => {
    if (value !== undefined) {
      rowSelection.value = value;
    }
  },
  { deep: true }
);

watch(
  () => props.expanded,
  (value) => {
    if (value !== undefined) {
      expanded.value = value;
    }
  },
  { deep: true }
);

watch(
  () => props.columnVisibility,
  (value) => {
    if (value !== undefined) {
      columnVisibility.value = value;
    }
  },
  { deep: true }
);

const selectColumn: TableColumn<TData> = {
  id: "__select",
  enableSorting: false,
  enableHiding: false,
  meta: { width: 48 },
  header: ({ table }) =>
    h("input", {
      type: "checkbox",
      checked: table.getIsAllPageRowsSelected(),
      indeterminate:
        table.getIsSomePageRowsSelected() && !table.getIsAllPageRowsSelected(),
      "aria-label": props.selectAllLabel,
      onClick: table.getToggleAllPageRowsSelectedHandler(),
    }),
  cell: ({ row }) =>
    h("input", {
      type: "checkbox",
      checked: row.getIsSelected(),
      disabled: !row.getCanSelect(),
      "aria-label": `${props.selectRowLabel} ${row.index + 1}`,
      onClick: row.getToggleSelectedHandler(),
    }),
};

const expandColumn: TableColumn<TData> = {
  id: "__expand",
  enableSorting: false,
  enableHiding: false,
  meta: { width: 48 },
  header: () => null,
  cell: ({ row }) =>
    row.getCanExpand()
      ? h(
          "button",
          {
            type: "button",
            class:
              "inline-flex h-6 w-6 cursor-pointer items-center justify-center rounded text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900",
            "aria-label": row.getIsExpanded() ? "收起" : "展开",
            "aria-expanded": row.getIsExpanded() ? "true" : "false",
            onClick: row.getToggleExpandedHandler(),
          },
          h(IconArrowRight, {
            "aria-hidden": "true",
            class: [
              "h-4 w-4 transition-transform",
              row.getIsExpanded() ? "rotate-90" : "",
            ],
          })
        )
      : null,
};

const resolvedColumns = computed<TableColumn<TData>[]>(() => {
  const result = [...props.columns];

  if (props.expandable) {
    result.unshift(expandColumn);
  }

  if (props.selectable) {
    result.unshift(selectColumn);
  }

  return result;
});

const getRowId = computed(() => (row: TData, index: number): string => {
  if (!props.rowKey) {
    return String(index);
  }

  const value =
    typeof props.rowKey === "function"
      ? props.rowKey(row)
      : (row as Record<string, unknown>)[props.rowKey];

  return value === undefined || value === null ? String(index) : String(value);
});

const table: Table<VTableFeatures, TData> = useTable({
  features: vTableFeatures,
  data: computed<TData[]>(() => props.data),
  columns: computed<TableColumn<TData>[]>(() => resolvedColumns.value),
  defaultColumn: {
    enableSorting: false,
    cell: (context: CellContext<VTableFeatures, TData>) => {
      const value = context.renderValue();

      return value === null || value === undefined || value === ""
        ? "-"
        : String(value);
    },
  },
  getRowId,
  state: {
    get sorting() {
      return sorting.value;
    },
    get rowSelection() {
      return rowSelection.value;
    },
    get expanded() {
      return expanded.value;
    },
    get columnVisibility() {
      return columnVisibility.value;
    },
  },
  enableRowSelection: computed<boolean>(() => props.selectable),
  manualSorting: computed<boolean>(() => props.manualSorting),
  getRowCanExpand: () => props.expandable,
  onSortingChange: (updater: Updater<SortingState>) => {
    sorting.value = functionalUpdate(updater, sorting.value);
    emit("update:sorting", sorting.value);
  },
  onRowSelectionChange: (updater: Updater<RowSelectionState>) => {
    rowSelection.value = functionalUpdate(updater, rowSelection.value);
    emit("update:rowSelection", rowSelection.value);
  },
  onExpandedChange: (updater: Updater<ExpandedState>) => {
    expanded.value = functionalUpdate(updater, expanded.value);
    emit("update:expanded", expanded.value);
  },
  onColumnVisibilityChange: (updater: Updater<ColumnVisibilityState>) => {
    columnVisibility.value = functionalUpdate(updater, columnVisibility.value);
    emit("update:columnVisibility", columnVisibility.value);
  },
});

const tableRef = ref<HTMLTableElement | null>(null);

const headerGroups = computed(() => table.getHeaderGroups());
const rows = computed(() => table.getRowModel().rows);
const visibleColumnCount = computed(() =>
  Math.max(1, table.getVisibleLeafColumns().length)
);

const interactiveSelector = "a,button,input,label,select,textarea";

function onRowClick(event: MouseEvent, original: TData) {
  const target = event.target;

  if (target instanceof Element && target.closest(interactiveSelector)) {
    return;
  }

  emit("row-click", original);
}

function resolveSize(value: string | number | undefined) {
  if (value === undefined) {
    return undefined;
  }

  return typeof value === "number" ? `${value}px` : value;
}

function getSizeStyle(meta?: TableColumnMeta): CSSProperties | undefined {
  const width = resolveSize(meta?.width);
  const minWidth = resolveSize(meta?.minWidth);

  if (width === undefined && minWidth === undefined) {
    return undefined;
  }

  return { width, minWidth };
}

function getAlignClass(meta?: TableColumnMeta) {
  if (meta?.align === "center") {
    return "text-center";
  }

  if (meta?.align === "right") {
    return "text-right";
  }

  return "text-left";
}

function getJustifyClass(meta?: TableColumnMeta) {
  if (meta?.align === "center") {
    return "justify-center";
  }

  if (meta?.align === "right") {
    return "justify-end";
  }

  return "justify-start";
}

function shouldRenderHeaderContent(
  header: Header<VTableFeatures, TData, unknown>
) {
  return !header.isPlaceholder || header.rowSpan > 1;
}

function getAriaSort(header: Header<VTableFeatures, TData, unknown>) {
  if (!shouldRenderHeaderContent(header) || !header.column.getCanSort()) {
    return undefined;
  }

  const sorted = header.column.getIsSorted();

  return sorted === "asc"
    ? "ascending"
    : sorted === "desc"
      ? "descending"
      : "none";
}

function getSortIcon(sorted: SortDirection | false): Component {
  if (sorted === "asc") {
    return IconArrowUpLine;
  }

  if (sorted === "desc") {
    return IconArrowDownLine;
  }

  return IconArrowUpDownLine;
}

function getSortIconClass(sorted: SortDirection | false) {
  return [
    "h-4 w-4 shrink-0 transition-colors",
    sorted ? "text-gray-900" : "text-gray-400",
  ];
}

defineExpose({
  tableApi: table,
  get tableRef() {
    return tableRef.value;
  },
});
</script>

<template>
  <div class="relative w-full overflow-x-auto" :class="ui.root">
    <table
      ref="tableRef"
      class="w-full border-spacing-0 text-sm"
      :class="ui.table"
      :aria-busy="loading ? 'true' : undefined"
    >
      <caption
        v-if="caption || $slots.caption"
        class="px-4 py-3 text-left text-sm text-gray-500"
      >
        <slot name="caption">{{ caption }}</slot>
      </caption>
      <thead
        class="bg-gray-50"
        :class="[ui.thead, sticky ? 'sticky top-0 z-10' : undefined]"
      >
        <tr v-for="headerGroup in headerGroups" :key="headerGroup.id">
          <template v-for="header in headerGroup.headers" :key="header.id">
            <th
              v-if="header.rowSpan !== 0"
              scope="col"
              :colspan="header.colSpan"
              :rowspan="header.rowSpan > 1 ? header.rowSpan : undefined"
              :aria-sort="getAriaSort(header)"
              :style="getSizeStyle(header.column.columnDef.meta)"
              :class="[
                'whitespace-nowrap px-4 py-3 text-sm font-semibold text-gray-900',
                getAlignClass(header.column.columnDef.meta),
                header.column.columnDef.meta?.thClass,
                ui.th,
              ]"
            >
              <template v-if="shouldRenderHeaderContent(header)">
                <button
                  v-if="header.column.getCanSort()"
                  type="button"
                  class="inline-flex w-full cursor-pointer items-center gap-1"
                  :class="getJustifyClass(header.column.columnDef.meta)"
                  @click="header.column.getToggleSortingHandler()?.($event)"
                >
                  <slot
                    v-if="$slots[`header-${header.column.id}`]"
                    :name="`header-${header.column.id}`"
                    :header="header"
                    :column="header.column"
                    :table="table"
                  />
                  <FlexRender v-else :header="header" />
                  <component
                    :is="getSortIcon(header.column.getIsSorted())"
                    aria-hidden="true"
                    :class="getSortIconClass(header.column.getIsSorted())"
                  />
                </button>
                <slot
                  v-else-if="$slots[`header-${header.column.id}`]"
                  :name="`header-${header.column.id}`"
                  :header="header"
                  :column="header.column"
                  :table="table"
                />
                <FlexRender v-else :header="header" />
              </template>
            </th>
          </template>
        </tr>
      </thead>
      <tbody class="divide-y divide-gray-100 bg-white" :class="ui.tbody">
        <template v-if="rows.length === 0">
          <tr>
            <td
              :colspan="visibleColumnCount"
              class="px-4 py-8 text-center text-sm text-gray-500"
              :class="[ui.td, loading ? ui.loading : ui.empty]"
            >
              <slot v-if="loading" name="loading">
                <VLoading />
              </slot>
              <slot v-else name="empty">{{ emptyText }}</slot>
            </td>
          </tr>
        </template>
        <template v-else>
          <template v-for="row in rows" :key="row.id">
            <tr
              :class="[
                row.getIsSelected() ? 'bg-gray-100' : 'hover:bg-gray-50',
                ui.tr,
              ]"
              @click="onRowClick($event, row.original)"
            >
              <td
                v-for="cell in row.getVisibleCells()"
                :key="cell.id"
                :style="getSizeStyle(cell.column.columnDef.meta)"
                :class="[
                  'whitespace-nowrap px-4 py-3 align-middle text-sm text-gray-500',
                  getAlignClass(cell.column.columnDef.meta),
                  cell.column.columnDef.meta?.tdClass,
                  ui.td,
                ]"
              >
                <slot
                  v-if="$slots[`cell-${cell.column.id}`]"
                  :name="`cell-${cell.column.id}`"
                  :row="row"
                  :original="row.original"
                  :value="cell.getValue()"
                  :column="cell.column"
                  :cell="cell"
                  :table="table"
                />
                <FlexRender v-else :cell="cell" />
              </td>
            </tr>
            <tr
              v-if="expandable && row.getIsExpanded()"
              :key="`${row.id}-expanded`"
            >
              <td
                :colspan="visibleColumnCount"
                class="bg-gray-50 px-4 py-4 align-top text-sm text-gray-500"
                :class="ui.td"
              >
                <slot name="expanded" :row="row" :table="table" />
              </td>
            </tr>
          </template>
        </template>
      </tbody>
    </table>
  </div>
</template>
