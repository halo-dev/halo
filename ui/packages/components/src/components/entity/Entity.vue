<script lang="ts" setup>
import { computed } from "vue";
import { IconMore } from "../../icons/icons";
import { VDropdown } from "../dropdown";

const props = withDefaults(
  defineProps<{
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const classes = computed(() => {
  const result = [];
  if (props.isSelected) {
    result.push("entity-selected");
  }
  return result;
});
</script>

<template>
  <tr :class="classes" v-bind="$attrs" class="entity-wrapper group">
    <td class="entity-start-wrapper">
      <slot name="prepend" />
      <div v-show="isSelected" class="entity-selected-indicator"></div>
      <div class="entity-start">
        <div v-if="$slots.checkbox" class="entity-checkbox">
          <slot name="checkbox" />
        </div>
        <slot name="start" />
      </div>
    </td>
    <td class="entity-end-wrapper">
      <div class="entity-end">
        <slot name="end" />
        <div v-if="$slots.dropdownItems" class="entity-dropdown">
          <VDropdown>
            <div
              class="entity-dropdown-trigger group-hover:bg-gray-200/60"
              :class="{ '!bg-gray-300/60': isSelected }"
              @click.stop
            >
              <IconMore />
            </div>
            <template #popper>
              <slot name="dropdownItems"></slot>
            </template>
          </VDropdown>
        </div>
      </div>
    </td>
  </tr>
  <tr v-if="$slots.footer">
    <td colspan="2">
      <slot name="footer" />
    </td>
  </tr>
</template>
<style lang="scss">
.entity-wrapper {
  @apply relative w-full transition-all hover:bg-gray-50;

  &.entity-selected {
    @apply bg-gray-100;
  }

  .entity-selected-indicator {
    @apply absolute inset-y-0 left-0 w-0.5 bg-primary;
  }

  .entity-body {
    @apply relative flex w-full flex-row items-center;
  }

  .entity-checkbox {
    @apply hidden sm:inline-flex;
  }

  .entity-start-wrapper,
  .entity-end-wrapper {
    @apply w-auto px-4 py-3 align-middle;
  }

  .entity-start {
    @apply flex items-center gap-4;
  }

  .entity-end {
    @apply flex items-center justify-end gap-6;
  }

  .entity-dropdown {
    @apply inline-flex items-center;
  }

  .entity-dropdown-trigger {
    @apply cursor-pointer rounded p-1 transition-all hover:text-blue-600;
  }
}
</style>
