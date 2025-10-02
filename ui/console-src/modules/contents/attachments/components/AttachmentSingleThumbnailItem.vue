<script lang="ts" setup>
import { THUMBNAIL_WIDTH_MAP } from "@/utils/thumbnail";

const { size, permalink } = defineProps<{
  size: string;
  permalink: string;
}>();
</script>
<template>
  <li>
    <div
      class="flex w-full cursor-pointer items-center justify-between space-x-3 rounded border p-3 hover:border-primary"
    >
      <a :href="permalink" target="_blank" class="block flex-none">
        <img
          :src="permalink"
          class="h-10 w-10 rounded-md object-cover"
          loading="lazy"
        />
      </a>
      <div class="flex min-w-0 flex-1 flex-col space-y-2 text-xs text-gray-900">
        <span class="font-semibold"> {{ THUMBNAIL_WIDTH_MAP[size] }}w </span>
        <a
          :href="permalink"
          target="_blank"
          class="line-clamp-3 break-all hover:text-gray-600"
        >
          {{ permalink }}
        </a>
      </div>
    </div>
  </li>
</template>
