<script lang="ts" setup>
import { IconMore } from "../../icons/icons";
import { computed } from "vue";
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
  <div :class="classes" class="entity-wrapper group">
    <div v-show="isSelected" class="entity-selected-indicator"></div>
    <slot name="prepend" />
    <div class="entity-body">
      <div v-if="$slots.checkbox" class="entity-checkbox">
        <slot name="checkbox" />
      </div>
      <div class="entity-start">
        <slot name="start" />
      </div>
      <div class="entity-end">
        <slot name="end" />
      </div>
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
    <div v-if="$slots.footer">
      <slot name="footer" />
    </div>
  </div>
</template>
<style lang="scss">
.entity-wrapper {
  @apply relative block cursor-pointer px-4 py-3 transition-all hover:bg-gray-50 w-full;

  &.entity-selected {
    @apply bg-gray-100;
  }

  .entity-selected-indicator {
    @apply absolute inset-y-0 left-0 w-0.5 bg-primary;
  }

  .entity-body {
    @apply relative flex flex-row items-center w-full;
  }

  .entity-checkbox {
    @apply mr-4 hidden items-center sm:flex;
  }

  .entity-start {
    @apply flex flex-1 items-center gap-4;
  }

  .entity-end {
    @apply flex flex-col items-end gap-4 sm:flex-row sm:items-center sm:gap-6;
  }

  .entity-dropdown {
    @apply ml-4 inline-flex items-center;
  }

  .entity-dropdown-trigger {
    @apply cursor-pointer rounded p-1 transition-all hover:text-blue-600;
  }
}
</style>
