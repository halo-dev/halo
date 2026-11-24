<script lang="ts" setup>
import type { CSSProperties } from "vue";
import { computed, inject } from "vue";
import type { DescriptionAlign } from "./types";
import { defaultDescriptionContext, descriptionContextKey } from "./types";

const props = withDefaults(
  defineProps<{
    label?: string;
    content?: string | number;
    verticalCenter?: boolean;
    span?: number;
    labelWidth?: string | number;
    align?: DescriptionAlign;
    emptyText?: string;
  }>(),
  {
    label: "",
    content: undefined,
    verticalCenter: false,
    span: 1,
    labelWidth: undefined,
    align: undefined,
    emptyText: undefined,
  }
);

const parentContext = inject(descriptionContextKey, null);

const context = computed(
  () => parentContext?.value ?? defaultDescriptionContext
);

const resolvedLabelWidth = computed(
  () => props.labelWidth ?? context.value.labelWidth
);

const hasLabelWidth = computed(
  () =>
    resolvedLabelWidth.value !== undefined && resolvedLabelWidth.value !== ""
);

const resolvedAlign = computed(() => props.align ?? context.value.align);

const resolvedEmptyText = computed(
  () => props.emptyText ?? context.value.emptyText
);

const clampedSpan = computed(() =>
  Math.min(Math.max(props.span, 1), context.value.columns)
);

const hasContent = computed(
  () =>
    props.content !== undefined &&
    props.content !== null &&
    props.content !== ""
);

const showSeparator = computed(
  () => context.value.layout === "inline" && context.value.separator !== ""
);

const itemClasses = computed(() => {
  const ctx = context.value;
  return [
    "description-item-wrapper",
    `description-item--${ctx.size}`,
    `description-layout-${ctx.layout}`,
    {
      "description-item--grid": ctx.columns > 1 && ctx.layout === "horizontal",
      "description-item--hoverable": ctx.hover && !ctx.bordered,
      "description-item--divided":
        !ctx.bordered && ctx.divider && ctx.columns > 1,
      "description-item--bordered": ctx.bordered,
      "description-item--vertical-center": props.verticalCenter,
      [`description-item--span-${clampedSpan.value}`]: ctx.columns > 1,
    },
  ];
});

const itemStyles = computed<CSSProperties>(() => {
  const styles: CSSProperties = {};
  const ctx = context.value;

  if (ctx.layout === "horizontal" && hasLabelWidth.value) {
    const width = resolvedLabelWidth.value;
    styles.gridTemplateColumns = `${
      typeof width === "number" ? `${width}px` : width
    } minmax(0, 1fr)`;
  }

  return styles;
});

const labelStyles = computed<CSSProperties>(() =>
  resolvedAlign.value ? { textAlign: resolvedAlign.value } : {}
);

const contentStyles = computed<CSSProperties>(() => {
  const styles: CSSProperties = {};

  if (resolvedAlign.value) {
    styles.textAlign = resolvedAlign.value;
  }

  if (context.value.layout === "horizontal" && hasLabelWidth.value) {
    styles.gridColumn = "2";
  }

  return styles;
});
</script>

<template>
  <div
    class="description-item-wrapper"
    :class="itemClasses"
    :style="itemStyles"
  >
    <dt
      class="description-item__label"
      :style="labelStyles"
      :data-separator="showSeparator ? context.separator : undefined"
    >
      <slot name="label">{{ label }}</slot>
    </dt>
    <dd class="description-item__content" :style="contentStyles">
      <slot v-if="$slots.default" />
      <template v-else-if="hasContent">
        {{ content }}
      </template>
      <template v-else-if="resolvedEmptyText">
        {{ resolvedEmptyText }}
      </template>
    </dd>
  </div>
</template>

<style lang="scss">
.description-item-wrapper {
  @apply bg-white;

  .description-item__label {
    @apply text-sm font-medium text-gray-900;
  }

  .description-item__content {
    @apply mt-1 min-w-0 break-words text-sm text-gray-900;
  }

  &.description-layout-horizontal {
    @apply sm:grid sm:grid-cols-6 sm:gap-4;

    .description-item__content {
      @apply sm:col-span-6 sm:mt-0 md:col-span-5 lg:col-span-5;
    }

    &.description-item--grid {
      grid-template-columns: minmax(0, 40%) minmax(0, 1fr);

      .description-item__content {
        grid-column: 2;
      }
    }
  }

  &.description-layout-vertical {
    @apply flex flex-col gap-1;

    .description-item__content {
      @apply mt-0;
    }
  }

  &.description-layout-inline {
    @apply flex flex-wrap items-baseline gap-x-2 gap-y-1;

    .description-item__label {
      @apply shrink-0;
    }

    .description-item__content {
      @apply mt-0;
    }

    .description-item__label[data-separator]::after {
      content: attr(data-separator);
    }
  }

  &.description-item--hoverable {
    @apply hover:bg-gray-50;
  }

  &.description-item--divided {
    @apply border-b border-gray-100;
  }

  &.description-item--vertical-center {
    @apply items-center;
  }

  @for $i from 1 through 12 {
    &.description-item--span-#{$i} {
      grid-column: span #{$i};
    }
  }

  &.description-item--xs {
    @apply px-3 py-2 sm:px-4;

    .description-item__label,
    .description-item__content {
      @apply text-xs;
    }
  }

  &.description-item--sm {
    @apply px-4 py-3 sm:px-5;
  }

  &.description-item--md {
    @apply px-4 py-5 sm:px-6;
  }

  &.description-item--lg {
    @apply px-4 py-6 sm:px-8;

    .description-item__label,
    .description-item__content {
      @apply text-base;
    }
  }

  &.description-item--bordered {
    @apply border-b border-r border-gray-100;
    padding: 0;

    .description-item__label {
      @apply bg-gray-50 px-4 py-3;
    }

    .description-item__content {
      @apply px-4 py-3;
    }

    &.description-layout-horizontal .description-item__label {
      @apply border-r border-gray-100;
    }

    &.description-layout-vertical .description-item__label {
      @apply border-b border-gray-100;
    }

    &.description-item--xs {
      .description-item__label,
      .description-item__content {
        @apply px-3 py-2;
      }
    }

    &.description-item--lg {
      .description-item__label,
      .description-item__content {
        @apply px-6 py-4;
      }
    }
  }
}
</style>
