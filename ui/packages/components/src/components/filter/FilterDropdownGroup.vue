<script lang="ts" setup>
import { computed, provide, shallowReactive } from "vue";
import { IconDeleteBin } from "../../icons/icons";
import { filterDropdownGroupKey, type FilterDropdownGroupEntry } from "./group";

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
  }>(),
  {
    clearLabel: "Reset filters",
    hasExternalFilters: false,
  }
);

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

defineOptions({
  inheritAttrs: false,
});

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
  <slot name="clear" :clear="clearAll" :has-filters="hasFilters">
    <button
      v-if="hasFilters"
      type="button"
      class="group inline-flex cursor-pointer items-center justify-center rounded-full bg-primary p-1 opacity-80 ring-1 ring-primary transition-all duration-300 hover:opacity-100 hover:shadow-sm hover:ring-opacity-50"
      :aria-label="clearLabel"
      @click="clearAll"
    >
      <IconDeleteBin
        class="h-4 w-4 text-gray-200 transition-all group-hover:text-white"
      />
    </button>
  </slot>
  <slot />
</template>
