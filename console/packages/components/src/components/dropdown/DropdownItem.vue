<script lang="ts" setup>
import { VClosePopper } from "floating-vue";

withDefaults(
  defineProps<{
    selected?: boolean;
    type: "default" | "danger";
  }>(),
  {
    selected: false,
    type: "default",
  }
);
</script>

<template>
  <div
    v-close-popper
    class="dropdown-item-wrapper"
    :class="[`dropdown-item-wrapper--${type}${selected ? '--selected' : ''}`]"
    role="menuitem"
    tabindex="-1"
  >
    <div class="flex items-center gap-3">
      <slot name="prefix-icon" />

      <slot />
    </div>

    <slot name="suffix-icon"></slot>
  </div>
</template>

<style lang="scss">
.dropdown-item-wrapper {
  @apply flex w-full cursor-pointer justify-between items-center gap-1 rounded px-4 py-2 text-sm;

  &--default {
    @apply text-gray-700 hover:bg-gray-100 hover:text-gray-900;

    &--selected {
      @apply bg-gray-100 text-gray-900;
    }
  }

  &--danger {
    @apply text-red-500 hover:bg-red-50 hover:text-red-700;

    &--selected {
      @apply bg-red-50 text-red-700;
    }
  }
}
</style>
