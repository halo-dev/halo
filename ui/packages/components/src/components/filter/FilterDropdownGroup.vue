<script lang="ts" setup>
import { computed, provide, shallowReactive } from "vue";
import { IconClose } from "../../icons/icons";
import { useComponentsI18n } from "../../locales";
import { VButton } from "../button";
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
    clearLabel: undefined,
    hasExternalFilters: false,
    spacing: "sm",
  }
);

const { t } = useComponentsI18n();

const clearLabelText = computed(() => props.clearLabel ?? t("filter.reset"));

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
      <VButton
        v-if="hasFilters"
        :label="clearLabelText"
        variant="ghost"
        shape="circle"
        size="sm"
        @click="clearAll"
      >
        <template #icon>
          <IconClose aria-hidden="true" />
        </template>
      </VButton>
    </slot>
    <slot />
  </div>
</template>
