<script lang="ts" setup>
import { TinyColor } from "@ctrl/tinycolor";
import { computed } from "vue";
import type { ArrayItemLabelType } from "..";

const props = defineProps<{
  itemLabel: {
    type: ArrayItemLabelType;
    value: string;
  };
}>();

const colorValue = computed(() => {
  const value = props.itemLabel.value;
  if (!value) {
    return "#000000";
  }

  // Validate and sanitize color value using TinyColor
  const color = new TinyColor(value);
  if (!color.isValid) {
    return "#000000";
  }

  // Return the validated color in hex format to prevent CSS injection
  return color.toHexString();
});

const displayValue = computed(() => {
  return props.itemLabel.value || colorValue.value;
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
    <span class="text-xs text-gray-500">{{ displayValue }}</span>
  </div>
</template>
