<script setup lang="ts">
import { computed, shallowRef, useId, useTemplateRef } from "vue";
import { i18n } from "@/locales";

const props = withDefaults(
  defineProps<{
    rows?: number;
    columns?: number;
  }>(),
  {
    rows: 8,
    columns: 8,
  }
);

const emit = defineEmits<{
  select: [dimensions: { rows: number; columns: number }];
  cancel: [];
}>();

const selectedRow = shallowRef(1);
const selectedColumn = shallowRef(1);
const grid = useTemplateRef<HTMLElement>("grid");
const gridId = useId();

const cells = computed(() => {
  return Array.from({ length: props.rows * props.columns }, (_, index) => ({
    row: Math.floor(index / props.columns) + 1,
    column: (index % props.columns) + 1,
  }));
});

const selectionLabel = computed(() => {
  return i18n.global.t("editor.menus.table.insert_dimensions", {
    rows: selectedRow.value,
    columns: selectedColumn.value,
  });
});

const activeCellId = computed(
  () => `${gridId}-cell-${selectedRow.value}-${selectedColumn.value}`
);

function setSelection(row: number, column: number) {
  selectedRow.value = Math.min(props.rows, Math.max(1, row));
  selectedColumn.value = Math.min(props.columns, Math.max(1, column));
}

function select(row = selectedRow.value, column = selectedColumn.value) {
  emit("select", { rows: row, columns: column });
}

function handleKeydown(event: KeyboardEvent) {
  const movements: Partial<Record<string, [number, number]>> = {
    ArrowUp: [-1, 0],
    ArrowDown: [1, 0],
    ArrowLeft: [0, -1],
    ArrowRight: [0, 1],
  };
  const movement = movements[event.key];
  if (movement) {
    event.preventDefault();
    event.stopPropagation();
    setSelection(
      selectedRow.value + movement[0],
      selectedColumn.value + movement[1]
    );
    return;
  }

  if (event.key === "Enter" || event.key === " ") {
    event.preventDefault();
    event.stopPropagation();
    select();
  } else if (event.key === "Escape") {
    event.preventDefault();
    event.stopPropagation();
    emit("cancel");
  }
}

function focus() {
  grid.value?.focus();
}

defineExpose({ focus });
</script>

<template>
  <div
    ref="grid"
    class="table-insert-grid w-60 rounded-lg p-3 focus-visible:outline focus-visible:outline-2 focus-visible:-outline-offset-2"
    role="grid"
    :aria-label="selectionLabel"
    :aria-activedescendant="activeCellId"
    tabindex="0"
    @keydown="handleKeydown"
  >
    <div
      class="grid gap-1"
      :style="{
        gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))`,
      }"
    >
      <button
        v-for="cell in cells"
        :id="`${gridId}-cell-${cell.row}-${cell.column}`"
        :key="`${cell.row}-${cell.column}`"
        type="button"
        role="gridcell"
        tabindex="-1"
        class="table-insert-grid-cell min-w-[1.15rem] rounded-[0.2rem] border [aspect-ratio:1] focus-visible:outline focus-visible:outline-1"
        :class="{
          'table-insert-grid-cell-selected':
            cell.row <= selectedRow && cell.column <= selectedColumn,
        }"
        :aria-label="
          i18n.global.t('editor.menus.table.insert_dimensions', {
            rows: cell.row,
            columns: cell.column,
          })
        "
        :aria-selected="
          cell.row <= selectedRow && cell.column <= selectedColumn
        "
        @mouseenter="setSelection(cell.row, cell.column)"
        @focus="setSelection(cell.row, cell.column)"
        @click="select(cell.row, cell.column)"
      />
    </div>
    <div
      class="table-insert-grid-status mt-2.5 text-center text-[0.8125rem]"
      aria-live="polite"
    >
      {{ selectionLabel }}
    </div>
  </div>
</template>

<style scoped>
.table-insert-grid {
  background: var(--halo-table-menu-background, white);
}

.table-insert-grid:focus-visible {
  outline-color: var(--halo-table-control-active, rgb(37 132 255));
}

.table-insert-grid-cell {
  border-color: var(--halo-table-menu-border, rgb(209 213 219));
  background: var(--halo-table-menu-background, white);
}

.table-insert-grid-cell:hover,
.table-insert-grid-cell:focus-visible,
.table-insert-grid-cell-selected {
  border-color: var(--halo-table-control-active, rgb(37 132 255));
  background: var(--halo-table-selection-soft, rgb(219 234 254));
}

.table-insert-grid-cell:focus-visible {
  outline-color: var(--halo-table-control-active, rgb(37 132 255));
}

.table-insert-grid-status {
  color: var(--halo-table-menu-text, rgb(75 85 99));
}
</style>
