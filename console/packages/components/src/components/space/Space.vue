<script lang="ts" setup>
import type { Align, Direction, Spacing } from "./interface";
import { SpacingSize } from "./interface";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    spacing?: Spacing;
    direction?: Direction;
    align?: Align;
  }>(),
  {
    spacing: "xs",
    direction: "row",
    align: "center",
  }
);

const wrapperClasses = computed(() => {
  const { direction, align } = props;
  return [`space-direction-${direction}`, `space-align-${align}`];
});
</script>
<template>
  <div
    :class="wrapperClasses"
    :style="`gap: ${SpacingSize[spacing]}px`"
    class="space-wrapper"
  >
    <slot />
  </div>
</template>
<style lang="scss">
.space-wrapper {
  @apply inline-flex
  box-border;

  &.space-direction-row {
    @apply flex-row;
  }

  &.space-direction-column {
    @apply flex-col;
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
}
</style>
