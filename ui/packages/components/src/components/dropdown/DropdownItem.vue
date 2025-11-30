<script lang="ts" setup>
import { inject } from "vue";
const props = withDefaults(
  defineProps<{
    selected?: boolean;
    disabled?: boolean;
    type?: "default" | "danger";
  }>(),
  {
    selected: false,
    disabled: false,
    type: "default",
  }
);

const emit = defineEmits<{
  (event: "click", e: MouseEvent): void;
}>();

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const dropdown = inject<{ parentPopper: any }>("__floating-vue__popper");

function onClick(e: MouseEvent) {
  if (props.disabled) {
    return;
  }

  dropdown?.parentPopper?.hide?.();

  emit("click", e);
}
</script>

<template>
  <div
    class="dropdown-item-wrapper"
    :class="[
      `dropdown-item-wrapper--${type}${selected ? '--selected' : ''}`,
      { 'dropdown-item-wrapper--disabled': disabled },
    ]"
    role="menuitem"
    tabindex="-1"
    @click="onClick"
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
  @apply flex w-full min-w-52 cursor-pointer items-center justify-between gap-1 rounded px-4 py-2 text-sm;

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

  &--disabled {
    @apply cursor-not-allowed opacity-70;
  }
}
</style>
