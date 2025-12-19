<script lang="ts" setup>
defineProps<{
  isActive: boolean;
}>();
</script>
<template>
  <button
    type="button"
    class="rounded p-0.5 transition-all"
    :class="{
      'text-gray-600 hover:bg-gray-100 hover:text-gray-900 active:bg-gray-200':
        !isActive,
      'bg-primary/20 text-gray-900 outline outline-1 outline-primary/50 hover:bg-primary/30':
        isActive,
    }"
  >
    <slot />
  </button>
</template>
