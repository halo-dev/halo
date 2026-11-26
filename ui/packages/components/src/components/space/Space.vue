<script lang="ts" setup>
import { Primitive } from "reka-ui";
import type { Component, CSSProperties } from "vue";
import { computed } from "vue";
import type {
  SpaceAlign,
  SpaceDirection,
  SpaceJustify,
  SpaceSpacingValue,
} from "./types";
import { SpaceSpacingSize } from "./types";

const props = withDefaults(
  defineProps<{
    spacing?: SpaceSpacingValue;
    direction?: SpaceDirection;
    align?: SpaceAlign;
    justify?: SpaceJustify;
    wrap?: boolean;
    block?: boolean;
    fill?: boolean;
    as?: string | Component;
  }>(),
  {
    spacing: "xs",
    direction: "row",
    align: "center",
    justify: undefined,
    wrap: false,
    block: false,
    fill: false,
    as: "div",
  }
);

/**
 * Resolved gap in pixels, as `[horizontal, vertical]`.
 */
const gaps = computed<[number, number]>(() => {
  const { spacing } = props;

  if (Array.isArray(spacing)) {
    return [spacing[0], spacing[1]];
  }

  if (typeof spacing === "number") {
    return [spacing, spacing];
  }

  // Falls back to `xs` for unknown runtime values, so a typo never results in
  // an invalid `undefinedpx` declaration.
  const size = SpaceSpacingSize[spacing] ?? SpaceSpacingSize.xs;

  return [size, size];
});

const classes = computed(() => {
  const classNames = [
    "space-wrapper",
    `space-direction-${props.direction}`,
    `space-align-${props.align}`,
  ];

  if (props.justify) {
    classNames.push(`space-justify-${props.justify}`);
  }

  if (props.wrap) {
    classNames.push("space-wrap");
  }

  if (props.block) {
    classNames.push("space-block");
  }

  if (props.fill) {
    classNames.push("space-fill");
  }

  return classNames;
});

const styles = computed<CSSProperties>(() => {
  const [horizontal, vertical] = gaps.value;

  return {
    "--v-space-gap-x": `${horizontal}px`,
    "--v-space-gap-y": `${vertical}px`,
  } as CSSProperties;
});
</script>

<template>
  <Primitive :as="as" :class="classes" :style="styles">
    <slot />
  </Primitive>
</template>

<style lang="scss">
.space-wrapper {
  @apply inline-flex;

  column-gap: var(--v-space-gap-x);
  row-gap: var(--v-space-gap-y);

  &.space-direction-row {
    @apply flex-row;
  }

  &.space-direction-column {
    @apply flex-col;
  }

  &.space-block {
    @apply flex;
  }

  &.space-align-center {
    @apply items-center;
  }

  &.space-align-start {
    @apply items-start;
  }

  &.space-align-end {
    @apply items-end;
  }

  &.space-align-stretch {
    @apply items-stretch;
  }

  &.space-align-baseline {
    @apply items-baseline;
  }

  &.space-justify-start {
    @apply justify-start;
  }

  &.space-justify-end {
    @apply justify-end;
  }

  &.space-justify-center {
    @apply justify-center;
  }

  &.space-justify-between {
    @apply justify-between;
  }

  &.space-justify-around {
    @apply justify-around;
  }

  &.space-justify-evenly {
    @apply justify-evenly;
  }

  &.space-wrap {
    @apply flex-wrap;
  }

  &.space-fill > * {
    flex: 1 1 0%;
    min-width: 0;
  }
}
</style>
