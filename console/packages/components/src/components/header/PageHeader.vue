<script lang="ts" setup>
defineProps<{
  title?: string;
}>();
</script>
<template>
  <div class="flex items-center justify-between bg-white p-4 h-14">
    <div class="min-w-0 flex-1 self-center">
      <h2 class="flex items-center truncate text-xl font-bold text-gray-800">
        <slot name="icon" />
        <span>{{ title }}</span>
      </h2>
    </div>
    <div class="self-center">
      <slot name="actions" />
    </div>
  </div>
</template>
