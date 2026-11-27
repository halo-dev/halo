<script lang="ts" setup>
import type { Component, CSSProperties } from "vue";
import { computed } from "vue";
import Skeleton from "./Skeleton.vue";
import type { SkeletonAnimation, SkeletonSize } from "./types";
import { toCssSize } from "./utils";

const props = withDefaults(
  defineProps<{
    /**
     * Number of placeholder lines.
     */
    lines?: number;
    /**
     * Width of individual lines. Missing entries fall back to the full width,
     * except for the last line, which uses `lastLineWidth`.
     */
    widths?: SkeletonSize[];
    /**
     * Width of the last line when `widths` does not provide one.
     */
    lastLineWidth?: SkeletonSize;
    lineHeight?: SkeletonSize;
    spacing?: SkeletonSize;
    animation?: SkeletonAnimation;
    /**
     * Element or component to render as.
     */
    as?: string | Component;
  }>(),
  {
    lines: 3,
    widths: () => [],
    lastLineWidth: "60%",
    lineHeight: "0.875rem",
    spacing: "0.5rem",
    animation: "shimmer",
    as: "div",
  }
);

const lineCount = computed(() => Math.max(0, Math.floor(props.lines)));

const lineWidths = computed<SkeletonSize[]>(() =>
  Array.from({ length: lineCount.value }, (_, index) => {
    const width = props.widths?.[index];

    if (width !== undefined) {
      return width;
    }

    return index === lineCount.value - 1 ? props.lastLineWidth : "100%";
  })
);

const styles = computed<CSSProperties>(() => ({
  gap: toCssSize(props.spacing),
}));
</script>

<template>
  <component :is="as" class="skeleton-text" :style="styles" aria-hidden="true">
    <Skeleton
      v-for="(width, index) in lineWidths"
      :key="index"
      :width="width"
      :height="lineHeight"
      :animation="animation"
      shape="text"
    />
  </component>
</template>

<style lang="scss">
:where(.skeleton-text) {
  display: flex;
  flex-direction: column;
  width: 100%;
}
</style>
