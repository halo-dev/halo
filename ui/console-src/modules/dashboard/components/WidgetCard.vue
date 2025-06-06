<script setup lang="ts">
defineProps<{
  title?: string;
  bodyClass?: string[];
}>();
</script>
<template>
  <div
    class="flex flex-col rounded-lg overflow-hidden ring-1 ring-[#eaecf0] shadow-sm bg-white w-full h-full"
  >
    <div
      v-if="title || $slots.title || $slots.actions"
      class="flex flex-none justify-between h-10 items-center px-4 border-b border-[#eaecf0]"
    >
      <slot name="title">
        <div class="text-base font-medium flex-1 shrink">
          {{ title }}
        </div>
      </slot>
      <div class="text-base font-medium flex-none">
        <slot name="actions" />
      </div>
    </div>
    <div :class="bodyClass" class="flex-1 min-h-0 shrink w-full">
      <slot />
    </div>
  </div>
</template>
