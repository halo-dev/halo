<script lang="ts" setup>
import type { DatePickerPreset, DateRangeValue } from "./types";

const props = defineProps<{
  presets: DatePickerPreset<DateRangeValue>[];
}>();

const emit = defineEmits<{
  (event: "select", value: DateRangeValue): void;
}>();

function onSelect(preset: DatePickerPreset<DateRangeValue>) {
  const value =
    typeof preset.value === "function" ? preset.value() : preset.value;
  emit("select", value);
}
</script>

<template>
  <div class="flex w-28 shrink-0 flex-col gap-0.5 border-r border-gray-100 p-2">
    <button
      v-for="preset in props.presets"
      :key="preset.label"
      type="button"
      class="whitespace-nowrap rounded px-2 py-1.5 text-left text-sm text-gray-600 transition-colors hover:bg-gray-100 hover:text-gray-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
      @click="onSelect(preset)"
    >
      {{ preset.label }}
    </button>
  </div>
</template>
