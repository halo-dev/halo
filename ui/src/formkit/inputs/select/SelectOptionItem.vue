<script lang="ts" setup>
import type { SelectOption } from "./types";

defineProps<{
  option: SelectOption;
}>();

const handleIconLoadError = (event: Event) => {
  const target = event.target as HTMLImageElement;
  target.hidden = true;
};
</script>

<template>
  <div class="flex min-h-8 w-full items-center gap-3 rounded px-3 py-1.5">
    <img
      v-if="option.icon"
      :src="option.icon"
      alt=""
      aria-hidden="true"
      class="shrink-0 rounded object-contain"
      :class="option.description ? 'h-8 w-8' : 'h-5 w-5'"
      loading="lazy"
      referrerpolicy="no-referrer"
      @error="handleIconLoadError"
    />
    <span class="min-w-0 flex-1">
      <span class="block truncate text-sm leading-5 text-gray-900">
        {{ option.label }}
      </span>
      <span
        v-if="option.description"
        class="block truncate text-xs leading-4 text-gray-500"
      >
        {{ option.description }}
      </span>
    </span>
  </div>
</template>
