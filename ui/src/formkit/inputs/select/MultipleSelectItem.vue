<script lang="ts" setup>
import { IconClose } from "@halo-dev/components";

const emit = defineEmits<{
  (event: "deleteSelectItem"): void;
}>();
</script>
<template>
  <div
    class="inline-flex select-none items-center self-center rounded bg-gray-200 px-2 py-1 text-sm font-medium text-gray-800"
  >
    <slot></slot>
    <span @click.stop="emit('deleteSelectItem')">
      <IconClose
        class="h-4 w-4 cursor-pointer text-gray-400 hover:text-gray-700"
      />
    </span>
  </div>
</template>
