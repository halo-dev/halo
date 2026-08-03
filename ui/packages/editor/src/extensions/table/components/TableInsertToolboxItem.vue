<script setup lang="ts">
import { VDropdown } from "@halo-dev/components";
import { nextTick, onBeforeUnmount, shallowRef, useTemplateRef } from "vue";
import type { ToolboxItemComponentProps } from "@/types";
import TableInsertGrid from "./TableInsertGrid.vue";

const props = defineProps<ToolboxItemComponentProps>();
const dropdownShown = shallowRef(false);
const trigger = useTemplateRef<HTMLButtonElement>("trigger");
const grid = useTemplateRef<InstanceType<typeof TableInsertGrid>>("grid");
let focusFrame: number | undefined;
let restoreEditorOnUnmount = false;

function insertTable({ rows, columns }: { rows: number; columns: number }) {
  restoreEditorOnUnmount = false;
  dropdownShown.value = false;
  props.editor
    .chain()
    .focus()
    .insertTable({ rows, cols: columns, withHeaderRow: true })
    .fitTableToWidth()
    .run();
}

function focusGrid() {
  if (focusFrame !== undefined) {
    cancelAnimationFrame(focusFrame);
  }
  focusFrame = requestAnimationFrame(() => {
    focusFrame = undefined;
    grid.value?.focus();
  });
}

async function cancel() {
  restoreEditorOnUnmount = true;
  dropdownShown.value = false;
  await nextTick();
  trigger.value?.focus();
}

onBeforeUnmount(() => {
  if (focusFrame !== undefined) {
    cancelAnimationFrame(focusFrame);
  }
  if (restoreEditorOnUnmount) {
    props.editor.commands.focus();
  }
});
</script>

<template>
  <VDropdown
    v-model:shown="dropdownShown"
    :triggers="['click']"
    :distance="10"
    :no-auto-focus="true"
    placement="right-start"
    @show="focusGrid"
    @apply-show="focusGrid"
  >
    <button
      ref="trigger"
      type="button"
      role="menuitem"
      class="flex w-full cursor-pointer items-center gap-3 rounded p-1.5 text-left hover:bg-gray-100 focus-visible:bg-gray-100 focus-visible:outline-none"
      :aria-label="title"
      :title="title"
    >
      <span class="size-7 flex-none rounded bg-gray-100 p-1.5">
        <component :is="icon" class="size-full" />
      </span>
      <span class="flex min-w-0 flex-1 flex-col gap-0.5">
        <span class="truncate text-sm text-gray-600">{{ title }}</span>
        <span v-if="description" class="text-xs text-gray-500">
          {{ description }}
        </span>
      </span>
    </button>

    <template #popper>
      <TableInsertGrid ref="grid" @select="insertTable" @cancel="cancel" />
    </template>
  </VDropdown>
</template>
