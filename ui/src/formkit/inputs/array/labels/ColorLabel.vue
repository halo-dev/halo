<script lang="ts" setup>
import { computed } from "vue";
import type { ArrayItemLabelType } from "..";

const props = defineProps<{
  itemLabel: {
    type: ArrayItemLabelType;
    value: string;
  };
}>();

const colorValue = computed(() => {
  return props.itemLabel.value || "#000000";
});
</script>
<template>
  <div class="inline-flex items-center gap-1.5">
    <div
      class="size-4 rounded border border-gray-200"
      :style="{
        backgroundColor: colorValue,
      }"
    ></div>
    <span class="text-xs text-gray-500">{{ colorValue }}</span>
  </div>
</template>
