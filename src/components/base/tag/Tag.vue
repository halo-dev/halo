<script lang="ts" setup>
import type { PropType } from "vue";
import { computed } from "vue";
import type { Theme } from "@/components/base/tag/interface";

const props = defineProps({
  theme: {
    type: String as PropType<Theme>,
    default: "default",
  },
  rounded: {
    type: Boolean,
    default: false,
  },
});

const classes = computed(() => {
  return [`tag-${props.theme}`, { "tag-rounded": props.rounded }];
});
</script>
<template>
  <div :class="classes" class="tag-wrapper">
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
  border-radius: 4px;
  @apply inline-flex;
  @apply flex-shrink-0;
  @apply flex-wrap;
  @apply box-border;
  @apply cursor-pointer;
  @apply text-center;
  @apply items-center;
  @apply justify-center;
  @apply w-auto;
  @apply align-middle;
  @apply h-5;
  @apply text-xs;

  &.tag-default {
    border: 1px solid #d9d9d9;
  }

  &.tag-primary {
    background: #4ccba0;
    border: 1px solid #4ccba0;
    @apply text-white;
  }

  &.tag-secondary {
    background: #0e1731;
    border: 1px solid #0e1731;
    @apply text-white;
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
