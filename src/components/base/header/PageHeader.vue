<script lang="ts" setup>
defineProps({
  title: {
    type: String,
  },
});
</script>
<template>
  <div class="flex items-center justify-between p-4 bg-white">
    <div class="flex-1 self-center min-w-0">
      <h2 class="flex text-xl font-bold text-gray-800 truncate">
        <slot name="icon" />
        <span>{{ title }}</span>
      </h2>
    </div>
    <div class="self-center">
      <slot name="actions" />
    </div>
  </div>
</template>
