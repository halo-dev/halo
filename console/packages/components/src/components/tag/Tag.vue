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
  @apply rounded-base
  inline-flex
  flex-shrink-0
  flex-wrap
  box-border
  cursor-pointer
  text-center
  items-center
  justify-center
  w-auto
  align-middle
  h-5
  text-xs
  border
  border-solid;

  &.tag-default {
    border: 1px solid #d9d9d9;
  }

  &.tag-primary {
    @apply text-white
    bg-primary
    border-primary;
  }

  &.tag-secondary {
    @apply text-white
    bg-secondary
    border-secondary;
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

  .tag-left-icon {
    @apply pl-1;
  }

  .tag-right-icon {
    @apply pr-1;
  }
}
</style>
