<script lang="ts" setup>
import type { CSSProperties } from "vue";
import { computed } from "vue";
import { vTooltip } from "../tooltip";
import type { StatusDotState } from "./types";

const props = withDefaults(
  defineProps<{
    state?: StatusDotState;
    animate?: boolean;
    text?: string;
    size?: "sm" | "md";
    color?: string;
    /**
     * Text used as the accessible name, and as the tooltip content.
     *
     * It is only rendered into the DOM when there is no visible text, to avoid
     * announcing the same thing twice.
     */
    label?: string;
    title?: string;
  }>(),
  {
    state: "success",
    animate: false,
    text: undefined,
    size: "md",
    color: undefined,
    label: undefined,
    title: undefined,
  }
);

const classes = computed(() => {
  return [
    `status-dot-${props.state}`,
    `status-dot-${props.size}`,
    { "status-dot-animate": props.animate || props.state === "processing" },
  ];
});

const style = computed((): CSSProperties | undefined => {
  if (!props.color) {
    return undefined;
  }

  return {
    "--status-dot-color": props.color,
  } as CSSProperties;
});
</script>

<template>
  <div
    v-tooltip="label"
    class="status-dot-wrapper"
    :class="classes"
    :style="style"
    :title="title"
  >
    <span class="status-dot-body" aria-hidden="true"></span>
    <span v-if="text || $slots.text" class="status-dot-text">
      <slot name="text">{{ text }}</slot>
    </span>
    <span v-else-if="label" class="status-dot-label">{{ label }}</span>
  </div>
</template>

<style lang="scss">
.status-dot-wrapper {
  --status-dot-color: theme("colors.gray.600");

  @apply inline-flex items-center gap-2;

  .status-dot-body {
    @apply relative inline-flex size-2 flex-none items-center justify-center rounded-full;

    background-color: var(--status-dot-color);
  }

  &.status-dot-sm .status-dot-body {
    @apply size-1.5;
  }

  .status-dot-text {
    @apply whitespace-nowrap text-xs text-gray-500;
  }

  .status-dot-label {
    position: absolute;
    width: 1px;
    height: 1px;
    padding: 0;
    margin: -1px;
    overflow: hidden;
    clip: rect(0, 0, 0, 0);
    white-space: nowrap;
    border-width: 0;
  }

  &.status-dot-animate .status-dot-body::after {
    content: "";

    @apply absolute inset-0 rounded-full;

    background-color: var(--status-dot-color);
    opacity: 0.75;
    animation: status-dot-ping 1s cubic-bezier(0, 0, 0.2, 1) infinite;
  }

  &.status-dot-default {
    --status-dot-color: theme("colors.gray.600");
  }

  &.status-dot-success {
    --status-dot-color: theme("colors.green.600");
  }

  &.status-dot-warning {
    --status-dot-color: theme("colors.yellow.700");
  }

  &.status-dot-error {
    --status-dot-color: theme("colors.red.600");
  }

  &.status-dot-info {
    --status-dot-color: theme("colors.blue.600");
  }

  &.status-dot-processing {
    --status-dot-color: theme("colors.blue.600");
  }
}

@keyframes status-dot-ping {
  75%,
  100% {
    transform: scale(2);
    opacity: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .status-dot-wrapper.status-dot-animate .status-dot-body::after {
    animation: none;
  }
}
</style>
