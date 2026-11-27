<script lang="ts" setup>
import type { Component, CSSProperties } from "vue";
import { computed } from "vue";
import type { SkeletonAnimation, SkeletonShape, SkeletonSize } from "./types";
import { toCssSize } from "./utils";

const props = withDefaults(
  defineProps<{
    /**
     * Width of the placeholder. Numbers are treated as pixels, strings are
     * passed through as CSS values.
     */
    width?: SkeletonSize;
    /**
     * Height of the placeholder. Numbers are treated as pixels, strings are
     * passed through as CSS values.
     */
    height?: SkeletonSize;
    /**
     * Geometry preset. Use `text` for a single line of text, `circle` for
     * avatars and icons, and `rect` for everything else.
     */
    shape?: SkeletonShape;
    animation?: SkeletonAnimation;
    /**
     * Element or component to render as.
     */
    as?: string | Component;
  }>(),
  {
    width: undefined,
    height: undefined,
    shape: "rect",
    animation: "shimmer",
    as: "div",
  }
);

const classes = computed(() => [
  "skeleton",
  `skeleton--${props.shape}`,
  `skeleton--${props.animation}`,
]);

const styles = computed<CSSProperties>(() => {
  const result: CSSProperties = {};
  const width = toCssSize(props.width);
  const height = toCssSize(props.height);

  if (width !== undefined) {
    result.width = width;
  }

  if (height !== undefined) {
    result.height = height;
  }

  if (props.shape === "circle") {
    if (width !== undefined && height === undefined) {
      result.height = width;
    }

    if (height !== undefined && width === undefined) {
      result.width = height;
    }
  }

  return result;
});
</script>

<template>
  <component :is="as" :class="classes" :style="styles" aria-hidden="true" />
</template>

<style lang="scss">
:where(.skeleton) {
  --v-skeleton-bg: theme("colors.gray.100");
  --v-skeleton-highlight: theme("colors.gray.200");
  --v-skeleton-duration: 1.25s;

  display: block;
  width: 100%;
  height: 1rem;
  overflow: hidden;
  border-radius: theme("borderRadius.base");
  background-color: var(--v-skeleton-bg);
}

:where(.skeleton--text) {
  height: 1em;
}

:where(.skeleton--circle) {
  flex: none;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 9999px;
}

:where(.skeleton--shimmer) {
  background: linear-gradient(
    90deg,
    var(--v-skeleton-bg) 25%,
    var(--v-skeleton-highlight) 37%,
    var(--v-skeleton-bg) 63%
  );
  background-size: 400% 100%;
  animation: v-skeleton-shimmer var(--v-skeleton-duration) ease infinite;
}

:where(.skeleton--pulse) {
  background-color: var(--v-skeleton-bg);
  animation: v-skeleton-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

:where(.skeleton--none) {
  background-color: var(--v-skeleton-bg);
}

@keyframes v-skeleton-shimmer {
  0% {
    background-position: 100% 50%;
  }

  100% {
    background-position: 0 50%;
  }
}

@keyframes v-skeleton-pulse {
  50% {
    opacity: 0.5;
  }
}

@media (prefers-reduced-motion: reduce) {
  :where(.skeleton--shimmer),
  :where(.skeleton--pulse) {
    animation: none;
    background: var(--v-skeleton-bg);
  }
}
</style>
