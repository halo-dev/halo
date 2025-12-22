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
</script>
<template>
  <div
    class="size-4 rounded-full border border-gray-200"
    :style="{
      backgroundColor: colorValue,
    }"
  ></div>
</template>
