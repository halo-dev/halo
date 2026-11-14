<script setup lang="ts">
import { VDropdown } from "@halo-dev/components";
import { computed, shallowRef } from "vue";
import MdiArrowDown from "~icons/mdi/arrow-down";
import MdiArrowExpandHorizontal from "~icons/mdi/arrow-expand-horizontal";
import MdiArrowLeft from "~icons/mdi/arrow-left";
import MdiArrowRight from "~icons/mdi/arrow-right";
import MdiArrowUp from "~icons/mdi/arrow-up";
import MdiContentCopy from "~icons/mdi/content-copy";
import MdiContentDuplicate from "~icons/mdi/content-duplicate";
import MdiEraser from "~icons/mdi/eraser";
import MdiFormatAlignCenter from "~icons/mdi/format-align-center";
import MdiFormatAlignLeft from "~icons/mdi/format-align-left";
import MdiFormatAlignRight from "~icons/mdi/format-align-right";
import MdiFormatColorFill from "~icons/mdi/format-color-fill";
import MdiFormatHeaderPound from "~icons/mdi/format-header-pound";
import MdiFormatVerticalAlignBottom from "~icons/mdi/format-vertical-align-bottom";
import MdiFormatVerticalAlignCenter from "~icons/mdi/format-vertical-align-center";
import MdiFormatVerticalAlignTop from "~icons/mdi/format-vertical-align-top";
import MdiTableColumn from "~icons/mdi/table-column";
import MdiTableColumnPlusAfter from "~icons/mdi/table-column-plus-after";
import MdiTableColumnPlusBefore from "~icons/mdi/table-column-plus-before";
import MdiTableMergeCells from "~icons/mdi/table-merge-cells";
import MdiTableRow from "~icons/mdi/table-row";
import MdiTableRowPlusAfter from "~icons/mdi/table-row-plus-after";
import MdiTableRowPlusBefore from "~icons/mdi/table-row-plus-before";
import MdiTableSplitCell from "~icons/mdi/table-split-cell";
import MingcuteDelete2Line from "@/components/icon/MingcuteDelete2Line.vue";
import { i18n } from "@/locales";
import type { Editor } from "@/tiptap";
import TableMenuButton from "./TableMenuButton.vue";
import TableMenuSegmentedControl from "./TableMenuSegmentedControl.vue";
import { useTableCommands } from "./useTableCommands";

const props = defineProps<{
  editor: Editor;
}>();

const {
  layoutMode,
  horizontalAlign,
  verticalAlign,
  backgroundColor,
  rowHeight,
  isHeaderRow,
  isHeaderColumn,
  hasCellFormatting,
  can,
} = useTableCommands(props.editor);
type MenuKey = "width" | "row" | "column" | "format";
const openMenu = shallowRef<MenuKey | null>(null);
const widthModeLabel = computed(() =>
  i18n.global.t(
    layoutMode.value === "auto"
      ? "editor.menus.table.fit_to_width"
      : "editor.menus.table.fixed_layout"
  )
);
const headerRowLabel = computed(() =>
  i18n.global.t(
    isHeaderRow.value
      ? "editor.menus.table.unset_header_row"
      : "editor.menus.table.set_header_row"
  )
);
const headerColumnLabel = computed(() =>
  i18n.global.t(
    isHeaderColumn.value
      ? "editor.menus.table.unset_header_column"
      : "editor.menus.table.set_header_column"
  )
);
const widthMenuShown = menuVisibility("width");
const rowMenuShown = menuVisibility("row");
const columnMenuShown = menuVisibility("column");
const formatMenuShown = menuVisibility("format");
const tableMenuPopperClass = "table-menu-popper";

const backgroundColors = [
  { label: "transparent", value: null },
  { label: "gray", value: "#f3f4f6" },
  { label: "red", value: "#fee2e2" },
  { label: "orange", value: "#ffedd5" },
  { label: "yellow", value: "#fef9c3" },
  { label: "green", value: "#dcfce7" },
  { label: "blue", value: "#dbeafe" },
  { label: "purple", value: "#f3e8ff" },
] as const;

const verticalAlignOptions = [
  { value: "top", icon: MdiFormatVerticalAlignTop },
  { value: "middle", icon: MdiFormatVerticalAlignCenter },
  { value: "bottom", icon: MdiFormatVerticalAlignBottom },
] as const;

const rowHeightOptions = [null, 40, 60, 80, 120] as const;

function rowHeightLabel(height: (typeof rowHeightOptions)[number]) {
  return height === null
    ? i18n.global.t("editor.menus.table.row_height_auto")
    : `${height}px`;
}

function command(action: () => boolean) {
  action();
  openMenu.value = null;
  props.editor.commands.focus();
}

function setCellAlign(alignment: "left" | "center" | "right" | null) {
  props.editor.chain().focus().setCellAttribute("align", alignment).run();
  openMenu.value = null;
}

function menuVisibility(menu: MenuKey) {
  return computed({
    get: () => openMenu.value === menu,
    set: (shown: boolean) => {
      if (shown) {
        openMenu.value = menu;
      } else if (openMenu.value === menu) {
        openMenu.value = null;
      }
    },
  });
}

function verticalAlignLabel(alignment: "top" | "middle" | "bottom") {
  return i18n.global.t(`editor.menus.table.vertical_${alignment}`);
}

function colorLabel(color: (typeof backgroundColors)[number]) {
  return i18n.global.t(`editor.menus.table.color_${color.label}`);
}
</script>

<template>
  <div
    class="table-bubble-menu flex items-center gap-0 whitespace-nowrap"
    role="toolbar"
    :aria-label="i18n.global.t('editor.menus.table.title')"
  >
    <VDropdown
      v-model:shown="widthMenuShown"
      class="table-toolbar-dropdown inline-flex flex-none"
      :triggers="['click']"
      :distance="10"
      placement="bottom-start"
      :popper-class="tableMenuPopperClass"
    >
      <TableMenuButton
        variant="toolbar-label"
        :label="widthModeLabel"
        :icon="MdiArrowExpandHorizontal"
        has-popup
        :expanded="widthMenuShown"
      />
      <template #popper>
        <div
          class="table-menu-panel flex w-[12.5rem] max-w-[calc(100vw-2rem)] flex-col gap-0.5 p-1.5"
          role="group"
          :aria-label="i18n.global.t('editor.menus.table.fit_to_width')"
        >
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.fit_to_width')"
            :active="layoutMode === 'auto'"
            :icon="MdiArrowExpandHorizontal"
            @activate="command(() => editor.commands.fitTableToWidth())"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.fixed_layout')"
            :active="layoutMode === 'fixed'"
            :icon="MdiTableColumn"
            @activate="command(() => editor.commands.setTableLayout('fixed'))"
          />
        </div>
      </template>
    </VDropdown>

    <span
      class="table-toolbar-divider mx-[0.1875rem] h-5 w-px flex-none max-[480px]:mx-[0.0625rem]"
      aria-hidden="true"
    />

    <VDropdown
      v-model:shown="rowMenuShown"
      class="table-toolbar-compact table-toolbar-dropdown inline-flex flex-none"
      :triggers="['click']"
      :distance="10"
      placement="bottom-start"
      :popper-class="tableMenuPopperClass"
    >
      <TableMenuButton
        variant="toolbar-label"
        hide-label-on-mobile
        :label="i18n.global.t('editor.menus.table.row')"
        :icon="MdiTableRow"
        has-popup
        :expanded="rowMenuShown"
      />
      <template #popper>
        <div
          class="table-menu-panel flex w-72 max-w-[calc(100vw-2rem)] flex-col gap-0.5 p-1.5"
          role="group"
          :aria-label="i18n.global.t('editor.menus.table.row')"
        >
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.add_row_before')"
            :icon="MdiTableRowPlusBefore"
            :disabled="!can.addRowBefore.value"
            @activate="command(() => editor.commands.addRowBefore())"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.add_row_after')"
            :icon="MdiTableRowPlusAfter"
            :disabled="!can.addRowAfter.value"
            @activate="command(() => editor.commands.addRowAfter())"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.duplicate_row')"
            :icon="MdiContentDuplicate"
            :disabled="!can.duplicateRow.value"
            @activate="command(() => editor.commands.duplicateTableRow())"
          />
          <div class="table-menu-divider mx-1.5 my-1 h-px border-b" />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.move_row_up')"
            :icon="MdiArrowUp"
            :disabled="!can.moveRowUp.value"
            @activate="command(() => editor.commands.moveTableRow(-1))"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.move_row_down')"
            :icon="MdiArrowDown"
            :disabled="!can.moveRowDown.value"
            @activate="command(() => editor.commands.moveTableRow(1))"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.clear_row')"
            :icon="MdiEraser"
            :disabled="!can.clearRow.value"
            @activate="command(() => editor.commands.clearSelectedTableRow())"
          />
          <TableMenuButton
            :label="headerRowLabel"
            :icon="MdiFormatHeaderPound"
            :active="isHeaderRow"
            :disabled="!can.toggleHeaderRow.value"
            @activate="command(() => editor.commands.toggleHeaderRow())"
          />
          <TableMenuSegmentedControl
            :label="i18n.global.t('editor.menus.table.row_height')"
          >
            <TableMenuButton
              v-for="height in rowHeightOptions"
              :key="String(height)"
              variant="segment"
              :label="rowHeightLabel(height)"
              :active="rowHeight === height"
              @activate="
                command(() => editor.commands.setTableRowHeight(height))
              "
            />
          </TableMenuSegmentedControl>
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.delete_row')"
            :icon="MingcuteDelete2Line"
            :disabled="!can.deleteRow.value"
            @activate="command(() => editor.commands.deleteRow())"
          />
        </div>
      </template>
    </VDropdown>

    <VDropdown
      v-model:shown="columnMenuShown"
      class="table-toolbar-compact table-toolbar-dropdown inline-flex flex-none"
      :triggers="['click']"
      :distance="10"
      placement="bottom-start"
      :popper-class="tableMenuPopperClass"
    >
      <TableMenuButton
        variant="toolbar-label"
        hide-label-on-mobile
        :label="i18n.global.t('editor.menus.table.column')"
        :icon="MdiTableColumn"
        has-popup
        :expanded="columnMenuShown"
      />
      <template #popper>
        <div
          class="table-menu-panel flex w-[14.5rem] max-w-[calc(100vw-2rem)] flex-col gap-0.5 p-1.5"
          role="group"
          :aria-label="i18n.global.t('editor.menus.table.column')"
        >
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.add_column_before')"
            :icon="MdiTableColumnPlusBefore"
            :disabled="!can.addColumnBefore.value"
            @activate="command(() => editor.commands.addColumnBefore())"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.add_column_after')"
            :icon="MdiTableColumnPlusAfter"
            :disabled="!can.addColumnAfter.value"
            @activate="command(() => editor.commands.addColumnAfter())"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.duplicate_column')"
            :icon="MdiContentDuplicate"
            :disabled="!can.duplicateColumn.value"
            @activate="command(() => editor.commands.duplicateTableColumn())"
          />
          <div class="table-menu-divider mx-1.5 my-1 h-px border-b" />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.move_column_left')"
            :icon="MdiArrowLeft"
            :disabled="!can.moveColumnLeft.value"
            @activate="command(() => editor.commands.moveTableColumn(-1))"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.move_column_right')"
            :icon="MdiArrowRight"
            :disabled="!can.moveColumnRight.value"
            @activate="command(() => editor.commands.moveTableColumn(1))"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.clear_column')"
            :icon="MdiEraser"
            :disabled="!can.clearColumn.value"
            @activate="
              command(() => editor.commands.clearSelectedTableColumn())
            "
          />
          <TableMenuButton
            :label="headerColumnLabel"
            :icon="MdiFormatHeaderPound"
            :active="isHeaderColumn"
            :disabled="!can.toggleHeaderColumn.value"
            @activate="command(() => editor.commands.toggleHeaderColumn())"
          />
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.delete_column')"
            :icon="MingcuteDelete2Line"
            :disabled="!can.deleteColumn.value"
            @activate="command(() => editor.commands.deleteColumn())"
          />
        </div>
      </template>
    </VDropdown>

    <TableMenuButton
      v-if="can.mergeCells.value"
      variant="toolbar-label"
      :label="i18n.global.t('editor.menus.table.merge_cells')"
      :icon="MdiTableMergeCells"
      @activate="command(() => editor.commands.mergeCells())"
    />
    <TableMenuButton
      v-if="can.splitCell.value"
      variant="toolbar-label"
      :label="i18n.global.t('editor.menus.table.split_cell')"
      :icon="MdiTableSplitCell"
      @activate="command(() => editor.commands.splitCell())"
    />

    <VDropdown
      v-model:shown="formatMenuShown"
      class="table-toolbar-compact table-toolbar-dropdown inline-flex flex-none"
      :triggers="['click']"
      :distance="10"
      placement="bottom-start"
      :popper-class="tableMenuPopperClass"
    >
      <TableMenuButton
        variant="toolbar-label"
        hide-label-on-mobile
        :label="i18n.global.t('editor.menus.table.format')"
        :icon="MdiFormatColorFill"
        :active="hasCellFormatting"
        has-popup
        :expanded="formatMenuShown"
      />
      <template #popper>
        <div
          class="table-menu-panel flex w-[16.25rem] max-w-[calc(100vw-2rem)] flex-col gap-0.5 p-1.5"
          role="group"
          :aria-label="i18n.global.t('editor.menus.table.format')"
        >
          <TableMenuSegmentedControl
            :label="i18n.global.t('editor.menus.table.horizontal_align')"
          >
            <TableMenuButton
              variant="segment"
              icon-only
              :label="i18n.global.t('editor.menus.table.align_left')"
              :icon="MdiFormatAlignLeft"
              :active="(horizontalAlign ?? 'left') === 'left'"
              @activate="setCellAlign('left')"
            />
            <TableMenuButton
              variant="segment"
              icon-only
              :label="i18n.global.t('editor.menus.table.align_center')"
              :icon="MdiFormatAlignCenter"
              :active="horizontalAlign === 'center'"
              @activate="setCellAlign('center')"
            />
            <TableMenuButton
              variant="segment"
              icon-only
              :label="i18n.global.t('editor.menus.table.align_right')"
              :icon="MdiFormatAlignRight"
              :active="horizontalAlign === 'right'"
              @activate="setCellAlign('right')"
            />
          </TableMenuSegmentedControl>
          <TableMenuSegmentedControl
            :label="i18n.global.t('editor.menus.table.vertical_align')"
          >
            <TableMenuButton
              v-for="option in verticalAlignOptions"
              :key="option.value"
              variant="segment"
              icon-only
              :label="verticalAlignLabel(option.value)"
              :icon="option.icon"
              :active="(verticalAlign ?? 'top') === option.value"
              @activate="
                command(() =>
                  editor.commands.setTableCellVerticalAlign(option.value)
                )
              "
            />
          </TableMenuSegmentedControl>
          <div
            class="table-menu-section flex flex-col gap-2 border-b px-1.5 pb-2.5 pt-2"
          >
            <span
              class="table-menu-section-label text-[0.6875rem] font-semibold leading-4 tracking-[0.02em]"
            >
              {{ i18n.global.t("editor.menus.table.background") }}
            </span>
            <div class="table-color-grid grid grid-cols-8 gap-2">
              <button
                v-for="color in backgroundColors"
                :key="color.label"
                type="button"
                class="table-color-swatch size-6 rounded-md border transition-[box-shadow,transform] duration-[120ms,120ms] hover:-translate-y-px focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 motion-reduce:transition-none"
                :class="{
                  'table-color-swatch-active': backgroundColor === color.value,
                }"
                :style="{ backgroundColor: color.value ?? 'transparent' }"
                :aria-label="colorLabel(color)"
                :aria-pressed="backgroundColor === color.value"
                :title="colorLabel(color)"
                @click="
                  command(() =>
                    editor.commands.setTableCellBackground(color.value)
                  )
                "
              />
            </div>
          </div>
          <TableMenuButton
            :label="i18n.global.t('editor.menus.table.clear_formatting')"
            :icon="MdiEraser"
            :disabled="!hasCellFormatting || !can.clearFormatting.value"
            @activate="
              command(() => editor.commands.clearTableCellFormatting())
            "
          />
        </div>
      </template>
    </VDropdown>

    <span
      class="table-toolbar-divider mx-[0.1875rem] h-5 w-px flex-none max-[480px]:mx-[0.0625rem]"
      aria-hidden="true"
    />

    <TableMenuButton
      variant="toolbar"
      :label="i18n.global.t('editor.menus.table.copy_table')"
      :icon="MdiContentCopy"
      @activate="command(() => editor.commands.copyTable())"
    />
    <TableMenuButton
      variant="toolbar"
      :label="i18n.global.t('editor.menus.table.delete_table')"
      :icon="MingcuteDelete2Line"
      @activate="command(() => editor.commands.deleteTable())"
    />
  </div>
</template>

<style scoped>
.table-toolbar-divider {
  background: var(--halo-table-menu-border, rgb(229 231 235));
}

.table-menu-divider,
.table-menu-section {
  border-color: var(--halo-table-menu-border, rgb(229 231 235));
}

.table-menu-section-label {
  color: var(--halo-table-menu-muted, rgb(107 114 128));
}

.table-color-swatch {
  border-color: var(--halo-table-menu-border, rgb(209 213 219));
}

.table-color-swatch-active {
  box-shadow:
    0 0 0 2px var(--halo-table-menu-background, #fff),
    0 0 0 4px var(--halo-table-control-active, rgb(37 132 255));
}

.table-color-swatch:first-child {
  background-image: linear-gradient(
    135deg,
    transparent 45%,
    rgb(239 68 68) 46%,
    rgb(239 68 68) 54%,
    transparent 55%
  );
}

.table-color-swatch:focus-visible {
  outline-color: var(--halo-table-control-active, rgb(37 132 255));
}
</style>
