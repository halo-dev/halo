<script lang="ts" setup generic="TData extends RowData">
import {
  FlexRender,
  functionalUpdate,
  useTable,
  type CellContext,
  type Column,
  type ColumnPinningState,
  type ColumnSizingState,
  type ColumnVisibilityState,
  type columnResizingState,
  type ExpandedState,
  type Header,
  type Row,
  type RowData,
  type RowSelectionState,
  type SortDirection,
  type SortingState,
  type Table,
  type Updater,
} from "@tanstack/vue-table";
import { useVirtualizer } from "@tanstack/vue-virtual";
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
  IconMore,
} from "../../icons/icons";
import { VDropdown, VDropdownDivider, VDropdownItem } from "../dropdown";
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
  TableHeaderMenuLabels,
  TableHeaderSlotProps,
  TableUi,
  TableVirtualizeOptions,
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
     * Whether columns can be resized by dragging the header edge.
     */
    resizable?: boolean;
    /**
     * Controlled column sizing state, keyed by column id.
     */
    columnSizing?: ColumnSizingState;
    /**
     * Whether columns can be pinned to the start or end of the table.
     */
    pinnable?: boolean;
    /**
     * Controlled column pinning state.
     */
    columnPinning?: ColumnPinningState;
    /**
     * Whether to render the header column menu with sorting, hiding, and
     * pinning actions. The menu reserves 40px on the right of the header cell,
     * so columns with a menu need a few extra pixels of width (about 80px
     * minimum for short headers).
     */
    headerMenu?: boolean;
    /**
     * Labels used by the header column menu and the resize handle.
     */
    headerMenuLabels?: Partial<TableHeaderMenuLabels>;
    /**
     * Whether to virtualize the rows. Requires a height constraint on the
     * table wrapper, a uniform row height, and is intended to be used together
     * with `sticky`. Cannot be combined with `expandable`.
     */
    virtualize?: boolean | TableVirtualizeOptions;
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
    resizable: false,
    columnSizing: undefined,
    pinnable: false,
    columnPinning: undefined,
    headerMenu: false,
    headerMenuLabels: () => ({}),
    virtualize: false,
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
  (event: "update:columnSizing", value: ColumnSizingState): void;
  (event: "update:columnPinning", value: ColumnPinningState): void;
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
const columnSizing = ref<ColumnSizingState>(props.columnSizing ?? {});
const columnResizing = ref<columnResizingState>({
  columnSizingStart: [],
  deltaOffset: null,
  deltaPercentage: null,
  isResizingColumn: false,
  startOffset: null,
  startSize: null,
});
const columnPinning = ref<ColumnPinningState>(
  props.columnPinning ?? { start: [], end: [] }
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

watch(
  () => props.columnSizing,
  (value) => {
    if (value !== undefined) {
      columnSizing.value = value;
    }
  },
  { deep: true }
);

watch(
  () => props.columnPinning,
  (value) => {
    if (value !== undefined) {
      columnPinning.value = value;
    }
  },
  { deep: true }
);

const selectColumn: TableColumn<TData> = {
  id: "__select",
  enableSorting: false,
  enableHiding: false,
  enablePinning: false,
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
  enablePinning: false,
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

function withColumnSize(column: TableColumn<TData>): TableColumn<TData> {
  const meta = column.meta;
  const size =
    column.size ?? (typeof meta?.width === "number" ? meta.width : undefined);
  const minSize =
    column.minSize ??
    (typeof meta?.minWidth === "number" ? meta.minWidth : undefined);
  const children = (column as { columns?: TableColumn<TData>[] }).columns;

  if (
    size === column.size &&
    minSize === column.minSize &&
    children === undefined
  ) {
    return column;
  }

  const next = {
    ...column,
  } as TableColumn<TData> & { columns?: TableColumn<TData>[] };

  if (size !== undefined) {
    next.size = size;
  }

  if (minSize !== undefined) {
    next.minSize = minSize;
  }

  if (children) {
    next.columns = children.map(withColumnSize);
  }

  return next;
}

const resolvedColumns = computed<TableColumn<TData>[]>(() => {
  const result = [...props.columns];

  if (props.expandable) {
    result.unshift(expandColumn);
  }

  if (props.selectable) {
    result.unshift(selectColumn);
  }

  return result.map(withColumnSize);
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
    get columnSizing() {
      return columnSizing.value;
    },
    get columnResizing() {
      return columnResizing.value;
    },
    get columnPinning() {
      return columnPinning.value;
    },
  },
  enableRowSelection: computed<boolean>(() => props.selectable),
  enableColumnResizing: computed<boolean>(() => props.resizable),
  enableColumnPinning: computed<boolean>(() => props.pinnable),
  columnResizeMode: "onChange",
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
  onColumnSizingChange: (updater: Updater<ColumnSizingState>) => {
    columnSizing.value = functionalUpdate(updater, columnSizing.value);
    emit("update:columnSizing", columnSizing.value);
  },
  onColumnResizingChange: (updater: Updater<columnResizingState>) => {
    columnResizing.value = functionalUpdate(updater, columnResizing.value);
  },
  onColumnPinningChange: (updater: Updater<ColumnPinningState>) => {
    columnPinning.value = functionalUpdate(updater, columnPinning.value);
    emit("update:columnPinning", columnPinning.value);
  },
});

const tableRef = ref<HTMLTableElement | null>(null);
const rootRef = ref<HTMLDivElement | null>(null);

const headerGroups = computed(() => table.getHeaderGroups());
const rows = computed(() => table.getRowModel().rows);
const visibleLeafColumns = computed(() => [
  ...table.getStartVisibleLeafColumns(),
  ...table.getCenterVisibleLeafColumns(),
  ...table.getEndVisibleLeafColumns(),
]);
const visibleColumnCount = computed(() =>
  Math.max(1, visibleLeafColumns.value.length)
);

const defaultHeaderMenuLabels: TableHeaderMenuLabels = {
  columnMenu: "列操作",
  sortAsc: "升序",
  sortDesc: "降序",
  clearSorting: "清除排序",
  hideColumn: "隐藏列",
  pinStart: "固定在左侧",
  pinEnd: "固定在右侧",
  unpin: "取消固定",
  resizeColumn: "调整列宽",
};

const headerMenuLabels = computed<TableHeaderMenuLabels>(() => ({
  ...defaultHeaderMenuLabels,
  ...props.headerMenuLabels,
}));

const sizingEnabled = computed(
  () =>
    props.resizable ||
    props.pinnable ||
    Object.keys(columnSizing.value).length > 0 ||
    columnPinning.value.start.length > 0 ||
    columnPinning.value.end.length > 0
);

const virtualizeOptions = computed<TableVirtualizeOptions | null>(() => {
  if (!props.virtualize) {
    return null;
  }

  return props.virtualize === true ? {} : props.virtualize;
});

const virtualizeEnabled = computed(() => virtualizeOptions.value !== null);

const virtualizer = useVirtualizer(
  computed(() => ({
    count: rows.value.length,
    getScrollElement: () => rootRef.value,
    estimateSize: () => virtualizeOptions.value?.estimateSize ?? 45,
    overscan: virtualizeOptions.value?.overscan ?? 8,
    enabled: virtualizeEnabled.value,
  }))
);

const virtualRows = computed(() => virtualizer.value.getVirtualItems());

const virtualPaddingTop = computed(() =>
  virtualizeEnabled.value ? (virtualRows.value[0]?.start ?? 0) : 0
);

const virtualPaddingBottom = computed(() => {
  const items = virtualRows.value;

  if (!virtualizeEnabled.value || items.length === 0) {
    return 0;
  }

  return Math.max(
    0,
    virtualizer.value.getTotalSize() - items[items.length - 1].end
  );
});

const renderedRows = computed<Row<VTableFeatures, TData>[]>(() => {
  if (!virtualizeEnabled.value) {
    return rows.value;
  }

  return virtualRows.value
    .map((item) => rows.value[item.index])
    .filter((row): row is Row<VTableFeatures, TData> => row !== undefined);
});

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

function getMetaSizeStyle(meta?: TableColumnMeta): CSSProperties | undefined {
  const width = resolveSize(meta?.width);
  const minWidth = resolveSize(meta?.minWidth);

  if (width === undefined && minWidth === undefined) {
    return undefined;
  }

  return { width, minWidth };
}

function getColumnStyle(
  column: Column<VTableFeatures, TData>
): CSSProperties | undefined {
  const style: CSSProperties = {};

  if (sizingEnabled.value) {
    style.width = `${column.getSize()}px`;
  } else {
    Object.assign(style, getMetaSizeStyle(column.columnDef.meta));
  }

  const pinned = column.getIsPinned();

  if (pinned === "start") {
    style.position = "sticky";
    style.left = `${column.getStart("start")}px`;
  } else if (pinned === "end") {
    style.position = "sticky";
    style.right = `${column.getAfter("end")}px`;
  }

  return Object.keys(style).length > 0 ? style : undefined;
}

function getHeaderStyle(
  header: Header<VTableFeatures, TData, unknown>
): CSSProperties | undefined {
  const style: CSSProperties = {};

  if (shouldRenderHeaderMenu(header)) {
    // Reserve room for the absolutely positioned header menu button.
    style.paddingRight = "2.5rem";
  }

  if (sizingEnabled.value) {
    style.width = `${header.getSize()}px`;
  } else {
    Object.assign(style, getMetaSizeStyle(header.column.columnDef.meta));
  }

  const pinned = header.column.getIsPinned();

  if (pinned === "start") {
    style.position = "sticky";
    style.left = `${header.getStart("start")}px`;
  } else if (pinned === "end") {
    const leafHeaders = header.getLeafHeaders();
    const lastLeaf = leafHeaders[leafHeaders.length - 1];

    style.position = "sticky";
    style.right = `${lastLeaf?.column.getAfter("end") ?? 0}px`;
  }

  return Object.keys(style).length > 0 ? style : undefined;
}

function getPinBorderClass(column: Column<VTableFeatures, TData>) {
  const pinned = column.getIsPinned();

  if (pinned === "start") {
    return "border-r border-gray-100";
  }

  if (pinned === "end") {
    return "border-l border-gray-100";
  }

  return undefined;
}

function shouldRenderHeaderMenu(
  header: Header<VTableFeatures, TData, unknown>
) {
  if (!props.headerMenu || !shouldRenderHeaderContent(header)) {
    return false;
  }

  const column = header.column;

  return (
    column.getCanSort() ||
    column.getCanHide() ||
    (props.pinnable && column.getCanPin())
  );
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
  <div
    ref="rootRef"
    class="relative isolate w-full overflow-x-auto"
    :class="ui.root"
  >
    <table
      ref="tableRef"
      class="w-full border-spacing-0 text-sm"
      :class="[sizingEnabled ? 'table-fixed' : undefined, ui.table]"
      :aria-busy="loading ? 'true' : undefined"
    >
      <caption
        v-if="caption || $slots.caption"
        class="px-4 py-3 text-left text-sm text-gray-500"
      >
        <slot name="caption">{{ caption }}</slot>
      </caption>
      <colgroup v-if="sizingEnabled">
        <col
          v-for="column in visibleLeafColumns"
          :key="column.id"
          :style="{ width: `${column.getSize()}px` }"
        />
      </colgroup>
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
              :style="getHeaderStyle(header)"
              :class="[
                'relative whitespace-nowrap px-4 py-3 text-sm font-semibold text-gray-900',
                getAlignClass(header.column.columnDef.meta),
                header.column.columnDef.meta?.thClass,
                header.column.getIsPinned() ? 'z-20 bg-gray-50' : undefined,
                getPinBorderClass(header.column),
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

              <VDropdown
                v-if="shouldRenderHeaderMenu(header)"
                class="absolute right-2 top-1/2 z-10 -translate-y-1/2"
              >
                <button
                  type="button"
                  class="inline-flex cursor-pointer rounded p-1 text-gray-400 transition-colors hover:bg-gray-200 hover:text-gray-900"
                  :aria-label="headerMenuLabels.columnMenu"
                >
                  <IconMore class="h-4 w-4" />
                </button>
                <template #popper>
                  <VDropdownItem
                    v-if="header.column.getCanSort()"
                    :selected="header.column.getIsSorted() === 'asc'"
                    @click="header.column.toggleSorting(false)"
                  >
                    {{ headerMenuLabels.sortAsc }}
                  </VDropdownItem>
                  <VDropdownItem
                    v-if="header.column.getCanSort()"
                    :selected="header.column.getIsSorted() === 'desc'"
                    @click="header.column.toggleSorting(true)"
                  >
                    {{ headerMenuLabels.sortDesc }}
                  </VDropdownItem>
                  <VDropdownItem
                    v-if="
                      header.column.getCanSort() && header.column.getIsSorted()
                    "
                    @click="header.column.clearSorting()"
                  >
                    {{ headerMenuLabels.clearSorting }}
                  </VDropdownItem>
                  <VDropdownDivider
                    v-if="
                      header.column.getCanSort() &&
                      (header.column.getCanHide() ||
                        (pinnable && header.column.getCanPin()))
                    "
                  />
                  <VDropdownItem
                    v-if="header.column.getCanHide()"
                    @click="header.column.toggleVisibility(false)"
                  >
                    {{ headerMenuLabels.hideColumn }}
                  </VDropdownItem>
                  <VDropdownDivider
                    v-if="
                      pinnable &&
                      header.column.getCanPin() &&
                      (header.column.getCanSort() || header.column.getCanHide())
                    "
                  />
                  <template v-if="pinnable && header.column.getCanPin()">
                    <VDropdownItem
                      v-if="header.column.getIsPinned() !== 'start'"
                      @click="header.column.pin('start')"
                    >
                      {{ headerMenuLabels.pinStart }}
                    </VDropdownItem>
                    <VDropdownItem
                      v-if="header.column.getIsPinned() !== 'end'"
                      @click="header.column.pin('end')"
                    >
                      {{ headerMenuLabels.pinEnd }}
                    </VDropdownItem>
                    <VDropdownItem
                      v-if="header.column.getIsPinned()"
                      @click="header.column.pin(false)"
                    >
                      {{ headerMenuLabels.unpin }}
                    </VDropdownItem>
                  </template>
                </template>
              </VDropdown>

              <div
                v-if="resizable && header.column.getCanResize()"
                role="separator"
                aria-orientation="vertical"
                :aria-label="headerMenuLabels.resizeColumn"
                class="absolute right-0 top-0 z-10 h-full w-1 cursor-col-resize touch-none select-none"
                :class="
                  header.column.getIsResizing()
                    ? 'bg-primary'
                    : 'bg-transparent hover:bg-primary/30'
                "
                @mousedown="header.getResizeHandler()($event)"
                @touchstart="header.getResizeHandler()($event)"
              />
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
          <tr
            v-if="virtualizeEnabled && virtualPaddingTop > 0"
            aria-hidden="true"
          >
            <td
              :colspan="visibleColumnCount"
              class="p-0"
              :style="{ height: `${virtualPaddingTop}px` }"
            />
          </tr>
          <template v-for="row in renderedRows" :key="row.id">
            <tr
              class="group"
              :class="[
                row.getIsSelected() ? 'bg-gray-100' : 'hover:bg-gray-50',
                ui.tr,
              ]"
              @click="onRowClick($event, row.original)"
            >
              <td
                v-for="cell in row.getVisibleCells()"
                :key="cell.id"
                :style="getColumnStyle(cell.column)"
                :class="[
                  'whitespace-nowrap px-4 py-3 align-middle text-sm text-gray-500',
                  getAlignClass(cell.column.columnDef.meta),
                  cell.column.columnDef.meta?.tdClass,
                  cell.column.getIsPinned()
                    ? [
                        'sticky z-[1]',
                        row.getIsSelected()
                          ? 'bg-gray-100'
                          : 'bg-white group-hover:bg-gray-50',
                      ]
                    : undefined,
                  getPinBorderClass(cell.column),
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
          <tr
            v-if="virtualizeEnabled && virtualPaddingBottom > 0"
            aria-hidden="true"
          >
            <td
              :colspan="visibleColumnCount"
              class="p-0"
              :style="{ height: `${virtualPaddingBottom}px` }"
            />
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>
