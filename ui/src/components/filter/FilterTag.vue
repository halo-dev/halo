<script lang="ts" setup>
import { IconCloseCircle } from "@halo-dev/components";

const emit = defineEmits<{
  (event: "close"): void;
}>();
</script>

<template>
  <div
    class="group inline-flex cursor-pointer items-center justify-center gap-1.5 rounded-full bg-primary px-2 py-1 ring-1 ring-primary transition-all duration-300 hover:shadow-sm hover:ring-opacity-50"
  >
    <span v-if="$slots.default" class="text-xs text-white transition-all">
      <slot />
    </span>
    <div
      class="h-4 w-4 rounded-full ring-white transition-all group-hover:ring-1"
      @click="emit('close')"
    >
      <IconCloseCircle
        class="h-4 w-4 text-gray-200 transition-all group-hover:text-white"
      />
    </div>
  </div>
</template>
