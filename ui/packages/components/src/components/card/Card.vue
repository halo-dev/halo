<script lang="ts" setup>
import type { CSSProperties } from "vue";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    title?: string;
    bodyClass?: string | string[];
    bodyStyle?: CSSProperties;
    flush?: boolean;
    fullHeight?: boolean;
    scrollable?: boolean;
    overflow?: "hidden" | "visible";
    hoverable?: boolean;
    shadow?: "always" | "hover" | "never";
    as?: string;
  }>(),
  {
    title: undefined,
    bodyClass: undefined,
    bodyStyle: undefined,
    flush: false,
    fullHeight: false,
    scrollable: false,
    overflow: "hidden",
    hoverable: false,
    shadow: "always",
    as: "div",
  }
);

const wrapperClasses = computed(() => ({
  "card-full-height": props.fullHeight,
  "card-overflow-visible": props.overflow === "visible",
  "card-hoverable": props.hoverable && props.shadow !== "never",
  [`card-shadow-${props.shadow}`]: true,
}));

const bodyClasses = computed(() => [
  props.bodyClass,
  {
    "card-body-flush": props.flush,
    "card-body-scrollable": props.scrollable,
  },
]);
</script>

<template>
  <component :is="as" :class="wrapperClasses" class="card-wrapper">
    <div
      v-if="title || $slots.title || $slots.header || $slots.actions"
      class="card-header"
    >
      <slot name="header">
        <h3 v-if="title || $slots.title" class="card-header-title">
          <slot name="title">{{ title }}</slot>
        </h3>
        <div v-if="$slots.actions" class="card-header-actions">
          <slot name="actions" />
        </div>
      </slot>
    </div>
    <div v-if="$slots.toolbar" class="card-toolbar">
      <slot name="toolbar" />
    </div>
    <div :class="bodyClasses" :style="bodyStyle" class="card-body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="card-footer">
      <slot name="footer" />
    </div>
  </component>
</template>
<style lang="scss">
.card-wrapper {
  --v-card-border-color: #eaecf0;
  --v-card-border-color-hover: #d0d5dd;
  --v-card-radius: 4px;
  --v-card-padding-x: 16px;
  --v-card-padding-y: 12px;

  @apply box-border flex flex-col bg-white shadow-sm ring-1 ring-[color:var(--v-card-border-color)];
  border-radius: var(--v-card-radius);

  &.card-shadow-hover {
    @apply shadow-none transition-shadow;

    &:hover {
      @apply shadow-md;
    }
  }

  &.card-shadow-never {
    @apply shadow-none;
  }

  &.card-hoverable {
    transition: box-shadow 0.15s ease;

    &:hover {
      @apply shadow-md;
      --tw-ring-color: var(--v-card-border-color-hover);
    }
  }

  &.card-full-height {
    height: 100%;

    .card-body {
      flex: 1 1 auto;
      min-height: 0;
    }
  }

  &.card-overflow-visible .card-body {
    overflow: visible;
  }

  .card-header {
    @apply flex justify-between overflow-hidden;
    border-bottom: 1px solid var(--v-card-border-color);
    border-radius: var(--v-card-radius) var(--v-card-radius) 0 0;

    .card-header-title {
      @apply min-w-0 flex-1 self-center truncate text-base font-medium text-gray-900;
      padding: var(--v-card-padding-y) var(--v-card-padding-x);
    }

    .card-header-actions {
      @apply ml-auto flex flex-none items-center gap-2 self-center;
      padding-right: var(--v-card-padding-x);
    }
  }

  .card-toolbar {
    @apply block w-full overflow-hidden bg-gray-50 px-4 py-3;
    border-bottom: 1px solid var(--v-card-border-color);
    border-radius: var(--v-card-radius) var(--v-card-radius) 0 0;
  }

  .card-header + .card-toolbar {
    border-radius: 0;
  }

  .card-body {
    @apply overflow-hidden;
    padding: var(--v-card-padding-y) var(--v-card-padding-x);
    border-radius: var(--v-card-radius);

    &.card-body-flush {
      padding: 0;
    }

    &.card-body-scrollable {
      overflow: auto;
    }
  }

  .card-footer {
    @apply overflow-hidden;
    padding: var(--v-card-padding-y) var(--v-card-padding-x);
    border-top: 1px solid var(--v-card-border-color);
    border-radius: 0 0 var(--v-card-radius) var(--v-card-radius);
  }
}
</style>
