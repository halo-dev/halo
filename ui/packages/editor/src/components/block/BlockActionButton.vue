<script lang="ts" setup>
import { vTooltip } from "floating-vue";

withDefaults(
  defineProps<{
    tooltip?: string;
    selected?: boolean;
  }>(),
  {
    tooltip: undefined,
    selected: false,
  }
);
</script>

<template>
  <div
    v-tooltip="tooltip"
    class="editor-block__actions-button"
    :class="{
      'editor-block__actions-button--selected': selected,
    }"
  >
    <slot name="icon" />
  </div>
</template>

<style lang="scss">
.editor-block__actions-button {
  @apply p-1.5 bg-gray-50 rounded-md cursor-pointer hover:bg-gray-200;

  &--selected {
    @apply bg-gray-200;
  }
}
</style>
