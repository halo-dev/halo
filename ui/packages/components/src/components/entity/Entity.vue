<script lang="ts" setup>
import { ref } from "vue";
import { IconMore } from "../../icons/icons";
import { useComponentsI18n } from "../../locales";
import { VDropdown } from "../dropdown";

// The component can render a second row for the footer slot, so fallthrough
// attributes are forwarded to the main row explicitly.
defineOptions({ inheritAttrs: false });

withDefaults(
  defineProps<{
    /**
     * Whether the row is selected. Shows the selected background and the
     * leading accent bar.
     */
    isSelected?: boolean;
  }>(),
  {
    isSelected: false,
  }
);

const { t } = useComponentsI18n();

const dropdownOpen = ref(false);
</script>

<template>
  <tr
    :class="{ 'entity-selected': isSelected }"
    v-bind="$attrs"
    class="entity-wrapper group"
  >
    <td class="entity-start-wrapper">
      <slot name="prepend" />
      <div
        v-if="isSelected"
        aria-hidden="true"
        class="entity-selected-indicator"
      ></div>
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
          <!-- @vue-ignore -->
          <VDropdown v-model:shown="dropdownOpen" :dispose-timeout="null">
            <button
              type="button"
              class="entity-dropdown-trigger group-hover:bg-gray-200/60"
              :class="{ '!bg-gray-300/60': isSelected }"
              aria-haspopup="menu"
              :aria-expanded="dropdownOpen"
              :aria-label="t('entity.more_actions')"
              @click.stop
            >
              <IconMore />
            </button>
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
  @apply relative w-full transition-colors focus-within:bg-gray-50 hover:bg-gray-50;

  &.entity-selected {
    @apply bg-gray-100;
  }

  .entity-selected-indicator {
    @apply absolute inset-y-0 left-0 w-0.5 bg-primary;
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
    @apply inline-flex cursor-pointer items-center justify-center rounded p-1 transition-colors hover:text-blue-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary;
  }
}
</style>
