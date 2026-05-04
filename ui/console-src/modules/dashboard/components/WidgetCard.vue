<script setup lang="ts">
defineProps<{
  title?: string;
  bodyClass?: string[];
}>();
</script>
<template>
  <div
    class="flex h-full w-full flex-col overflow-hidden rounded-lg bg-white shadow-sm ring-1 ring-gray-200"
  >
    <div
      v-if="title || $slots.title || $slots.actions"
      class="flex h-10 flex-none items-center justify-between border-b border-gray-200 px-4"
    >
      <slot name="title">
        <div class="flex-1 shrink text-base font-medium">
          {{ title }}
        </div>
      </slot>
      <div class="flex-none text-base font-medium">
        <slot name="actions" />
      </div>
    </div>
    <div :class="bodyClass" class="min-h-0 w-full flex-1 shrink">
      <slot />
    </div>
  </div>
</template>
