<script lang="ts" setup>
defineProps<{
  disabled?: boolean;
}>();
</script>
<template>
  <button
    type="button"
    class="rounded p-1"
    :class="{
      'text-gray-600 opacity-100 hover:bg-gray-100 hover:text-gray-900 active:bg-gray-200':
        !disabled,
      'opacity-50': disabled,
    }"
    :disabled="disabled"
  >
    <slot />
  </button>
</template>
