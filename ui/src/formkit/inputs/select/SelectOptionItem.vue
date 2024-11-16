<script lang="ts" setup>
defineProps<{
  option: Record<string, unknown> & { label: string; value: unknown };
}>();
</script>

<template>
  <div class="flex h-8 w-full items-center rounded px-3 py-1 text-base">
    <span class="flex-1 truncate text-sm"> {{ option.label }} </span>
  </div>
</template>
