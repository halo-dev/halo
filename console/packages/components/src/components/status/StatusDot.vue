<script lang="ts" setup>
import { computed } from "vue";
import type { State } from "./interface";

const props = withDefaults(
  defineProps<{
    state?: State;
    animate?: boolean;
    text?: string;
  }>(),
  { state: "success", animate: false, text: undefined }
);

const classes = computed(() => {
  return [`status-dot-${props.state}`, { "status-dot-animate": props.animate }];
});
</script>

<template>
  <div class="status-dot-wrapper" :class="classes">
    <div class="status-dot-body">
      <span class="status-dot-inner"></span>
    </div>
    <slot v-if="$slots.text || text" name="text">
      <span class="status-dot-text">{{ text }}</span>
    </slot>
  </div>
</template>

<style lang="scss">
.status-dot-wrapper {
  @apply flex items-center gap-2;

  .status-dot-body {
    @apply inline-flex h-2 w-2 items-center justify-center rounded-full;
  }

  .status-dot-inner {
    @apply inline-block h-1.5 w-1.5 rounded-full;
  }

  .status-dot-text {
    @apply text-gray-500 text-xs;
  }

  &.status-dot-animate {
    .status-dot-inner {
      @apply animate-ping;
    }
  }

  &.status-dot-default {
    .status-dot-body {
      @apply bg-gray-300;
    }

    .status-dot-inner {
      @apply bg-gray-300;
    }
  }

  &.status-dot-success {
    .status-dot-body {
      @apply bg-green-600;
    }

    .status-dot-inner {
      @apply bg-green-600;
    }
  }

  &.status-dot-warning {
    .status-dot-body {
      @apply bg-yellow-600;
    }

    .status-dot-inner {
      @apply bg-yellow-600;
    }
  }

  &.status-dot-error {
    .status-dot-body {
      @apply bg-red-600;
    }

    .status-dot-inner {
      @apply bg-red-600;
    }
  }
}
</style>
