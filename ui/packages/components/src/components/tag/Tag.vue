<script lang="ts" setup>
import type { CSSProperties } from "vue";
import { computed } from "vue";
import type { Theme } from "./interface";

const props = withDefaults(
  defineProps<{
    theme?: Theme;
    rounded?: boolean;
    styles?: CSSProperties;
  }>(),
  {
    theme: "default",
    rounded: false,
    styles: () => {
      return {};
    },
  }
);

const classes = computed(() => {
  return [`tag-${props.theme}`, { "tag-rounded": props.rounded }];
});
</script>
<template>
  <div :class="classes" :style="styles" class="tag-wrapper">
    <div v-if="$slots.leftIcon" class="tag-left-icon">
      <slot name="leftIcon" />
    </div>
    <span class="tag-content">
      <slot />
    </span>
    <div v-if="$slots.rightIcon" class="tag-right-icon">
      <slot name="rightIcon" />
    </div>
  </div>
</template>
<style lang="scss">
.tag-wrapper {
  @apply box-border inline-flex h-5 w-auto flex-shrink-0 cursor-pointer flex-wrap items-center justify-center rounded-base border border-solid px-1 text-center align-middle text-xs;

  &.tag-default {
    border: 1px solid #d9d9d9;
  }

  &.tag-primary {
    @apply border-primary bg-primary text-white;
  }

  &.tag-secondary {
    @apply border-secondary bg-secondary text-white;
  }

  &.tag-danger {
    background: #d71d1d;
    border: 1px solid #d71d1d;
    @apply text-white;
  }

  &.tag-rounded {
    @apply rounded-full;
  }

  .tag-content {
    @apply px-1;
  }
}
</style>
