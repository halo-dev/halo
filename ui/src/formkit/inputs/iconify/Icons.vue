<script lang="ts" setup>
import { Icon } from "@iconify/vue";
import { useVirtualizer } from "@tanstack/vue-virtual";
import { chunk } from "es-toolkit";
import { computed, useTemplateRef, watch } from "vue";

const COLUMNS = 12;
const ROW_HEIGHT = 36;

const props = defineProps<{
  icons: string[];
}>();

const emit = defineEmits<{
  (e: "select", icon: string): void;
}>();

const container = useTemplateRef<HTMLDivElement>("container");

const iconRows = computed(() => chunk(props.icons, COLUMNS));

const rowVirtualizerOptions = computed(() => ({
  count: iconRows.value.length,
  getScrollElement: () => container.value,
  estimateSize: () => ROW_HEIGHT,
  overscan: 5,
}));

const rowVirtualizer = useVirtualizer(rowVirtualizerOptions);

const virtualRows = computed(() => rowVirtualizer.value.getVirtualItems());
const totalSize = computed(() => rowVirtualizer.value.getTotalSize());

watch(
  () => props.icons,
  () => {
    rowVirtualizer.value.scrollToIndex(0);
  }
);
</script>
<template>
  <div ref="container" class="size-full flex-1 overflow-auto">
    <div
      :style="{
        height: `${totalSize}px`,
        width: '100%',
        position: 'relative',
      }"
    >
      <div
        v-for="virtualRow in virtualRows"
        :key="virtualRow.index"
        class="absolute left-0 grid w-full grid-cols-12 gap-1"
        :style="{
          height: `${virtualRow.size}px`,
          transform: `translateY(${virtualRow.start}px)`,
        }"
      >
        <div
          v-for="icon in iconRows[virtualRow.index]"
          :key="icon"
          class="aspect-square inline-flex cursor-pointer items-center justify-center rounded-lg hover:bg-gray-100 active:bg-gray-200"
          @click="emit('select', icon)"
        >
          <Icon :icon="icon" class="text-2xl" />
        </div>
      </div>
    </div>
  </div>
</template>
