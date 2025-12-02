<script lang="ts" setup>
import type { ToolboxItemComponentProps } from "@/types";

defineProps<ToolboxItemComponentProps>();
</script>

<template>
  <div
    class="group flex cursor-pointer flex-row items-center gap-3 rounded px-1.5 py-1 hover:bg-gray-100"
    @click="action?.()"
  >
    <div
      class="size-7 flex-none rounded bg-gray-100 p-1.5 group-hover:bg-white"
    >
      <component :is="icon" class="size-full" />
    </div>

    <div class="flex min-w-0 flex-1 shrink flex-col gap-0.5">
      <span
        class="line-clamp-1 text-sm text-gray-600 group-hover:font-medium group-hover:text-gray-900"
        :title="title"
      >
        {{ title }}
      </span>
      <span
        v-if="description"
        class="line-clamp-2 text-xs text-gray-500"
        :title="description"
      >
        {{ description }}
      </span>
    </div>
  </div>
</template>
