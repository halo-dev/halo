<script lang="ts" setup>
import type { GlobalSearchResult } from "./types";

withDefaults(
  defineProps<{
    item: GlobalSearchResult;
    selected?: boolean;
  }>(),
  {
    selected: false,
  }
);
</script>

<template>
  <div
    class="flex cursor-pointer items-center rounded-md px-2 py-2.5 hover:bg-gray-100"
    :class="{ 'bg-gray-100': selected }"
  >
    <div class="inline-flex min-w-0 flex-1 items-center gap-3">
      <div class="h-5 w-5 flex-none rounded border p-0.5">
        <component
          :is="item.icon.component"
          v-if="'component' in item.icon"
          class="h-full w-full"
        />
        <img
          v-if="'src' in item.icon"
          :src="item.icon.src"
          class="h-full w-full object-cover"
        />
      </div>
      <span class="truncate text-sm font-medium">{{ item.title }}</span>
      <span v-if="item.context" class="truncate text-xs text-gray-400">
        {{ item.context }}
      </span>
    </div>
    <div class="flex-none flex-shrink-0 text-xs text-gray-500">
      {{ item.group }}
    </div>
  </div>
</template>
