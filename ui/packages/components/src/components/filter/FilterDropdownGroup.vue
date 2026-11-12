<script lang="ts" setup>
import { computed, provide, shallowReactive } from "vue";
import { IconClose } from "../../icons/icons";
import { filterDropdownGroupKey, type FilterDropdownGroupEntry } from "./group";
import type { FilterDropdownGroupSpacing } from "./types";

const spacingSize: Record<FilterDropdownGroupSpacing, number> = {
  xs: 2,
  sm: 4,
  md: 8,
  lg: 12,
};

const props = withDefaults(
  defineProps<{
    /**
     * Accessible label of the clear button.
     */
    clearLabel?: string;
    /**
     * Whether filters outside the group are active. Useful when a page has
     * filters that are not `VFilterDropdown` instances but should share the
     * same clear button.
     */
    hasExternalFilters?: boolean;
    /**
     * Spacing between the filters. The scale is smaller than `VSpace` because
     * filter triggers already have horizontal padding.
     */
    spacing?: FilterDropdownGroupSpacing;
  }>(),
  {
    clearLabel: "Reset filters",
    hasExternalFilters: false,
    spacing: "sm",
  }
);

const gap = computed(() => spacingSize[props.spacing]);

const emit = defineEmits<{
  /**
   * Emitted after all filters have been cleared, no matter whether they are
   * registered `VFilterDropdown` instances or external filters handled by the
   * caller.
   */
  (event: "clear"): void;
}>();

defineSlots<{
  default?: () => unknown;
  clear?: (props: { clear: () => void; hasFilters: boolean }) => unknown;
}>();

const entries = shallowReactive(new Map<symbol, FilterDropdownGroupEntry>());

provide(filterDropdownGroupKey, {
  register(id, entry) {
    entries.set(id, entry);
  },
  unregister(id) {
    entries.delete(id);
  },
});

const hasFilters = computed(
  () =>
    props.hasExternalFilters ||
    Array.from(entries.values()).some((entry) => entry.isActive())
);

function clearAll() {
  entries.forEach((entry) => entry.clear());
  emit("clear");
}

defineExpose({
  hasFilters,
  clearAll,
});
</script>

<template>
  <div
    class="filter-dropdown-group inline-flex flex-wrap items-center"
    :style="{ gap: `${gap}px` }"
  >
    <slot name="clear" :clear="clearAll" :has-filters="hasFilters">
      <button
        v-if="hasFilters"
        type="button"
        class="inline-flex h-7 w-7 cursor-pointer items-center justify-center rounded text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-primary"
        :aria-label="clearLabel"
        @click="clearAll"
      >
        <IconClose class="h-4 w-4" aria-hidden="true" />
      </button>
    </slot>
    <slot />
  </div>
</template>
