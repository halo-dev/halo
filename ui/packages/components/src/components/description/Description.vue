<script lang="ts" setup>
import { computed, provide } from "vue";
import type {
  DescriptionAlign,
  DescriptionContext,
  DescriptionLayout,
  DescriptionSize,
} from "./types";
import { descriptionContextKey } from "./types";

const MAX_COLUMNS = 12;

const props = withDefaults(
  defineProps<{
    columns?: number;
    layout?: DescriptionLayout;
    size?: DescriptionSize;
    bordered?: boolean;
    divider?: boolean;
    hover?: boolean;
    title?: string;
    separator?: string;
    emptyText?: string;
    labelWidth?: string | number;
    align?: DescriptionAlign;
  }>(),
  {
    columns: 1,
    layout: "horizontal",
    size: "md",
    bordered: false,
    divider: true,
    hover: true,
    title: undefined,
    separator: ":",
    emptyText: undefined,
    labelWidth: undefined,
    align: undefined,
  }
);

const columns = computed(() =>
  Math.min(Math.max(Math.round(props.columns), 1), MAX_COLUMNS)
);

const context = computed<DescriptionContext>(() => ({
  size: props.size,
  layout: props.layout,
  bordered: props.bordered,
  hover: props.hover,
  divider: props.divider,
  columns: columns.value,
  separator: props.separator,
  emptyText: props.emptyText,
  labelWidth: props.labelWidth,
  align: props.align,
}));

provide(descriptionContextKey, context);

const wrapperClasses = computed(() => ({
  "description-wrapper--grid": columns.value > 1,
  [`description-wrapper--columns-${columns.value}`]: columns.value > 1,
  "description-wrapper--divided":
    props.divider && !props.bordered && columns.value === 1,
  "description-wrapper--bordered": props.bordered,
}));
</script>

<template>
  <div class="description">
    <div
      v-if="title || $slots.title || $slots.extra"
      class="description-header"
    >
      <div v-if="title || $slots.title" class="description-title">
        <slot name="title">{{ title }}</slot>
      </div>
      <div v-if="$slots.extra" class="description-extra">
        <slot name="extra" />
      </div>
    </div>
    <dl class="description-wrapper" :class="wrapperClasses">
      <slot />
    </dl>
  </div>
</template>

<style lang="scss">
.description {
  .description-header {
    @apply flex items-center justify-between gap-4 px-4 py-3 sm:px-6;

    .description-title {
      @apply text-base font-medium text-gray-900;
    }

    .description-extra {
      @apply text-sm;
    }
  }

  .description-wrapper {
    &.description-wrapper--divided {
      @apply divide-y divide-gray-100;
    }

    &.description-wrapper--grid {
      @apply sm:grid;
    }

    @for $i from 1 through 12 {
      &.description-wrapper--columns-#{$i} {
        grid-template-columns: repeat(#{$i}, minmax(0, 1fr));
      }
    }

    &.description-wrapper--bordered {
      @apply overflow-hidden rounded-base border border-gray-100;

      > .description-item-wrapper:last-child {
        @apply border-b-0;
      }
    }
  }
}
</style>
