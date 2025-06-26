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
  @apply cursor-pointer rounded-md bg-gray-50 p-1.5 hover:bg-gray-200;

  &--selected {
    @apply bg-gray-200;
  }
}
</style>
