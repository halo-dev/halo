<script lang="ts" setup>
import { computed } from "vue";
import type { Size } from "./interface";
import { useImage } from "@vueuse/core";
import { IconUserLine } from "@/icons/icons";

const props = withDefaults(
  defineProps<{
    src?: string;
    alt?: string;
    size?: Size;
    width?: string;
    height?: string;
    circle?: boolean;
  }>(),
  {
    src: undefined,
    alt: undefined,
    size: undefined,
    width: undefined,
    height: undefined,
    circle: false,
  }
);

const classes = computed(() => {
  const result = [`avatar-${props.circle ? "circle" : "square"}`];
  if (props.size) {
    result.push(`avatar-${props.size}`);
  }
  return result;
});

const styles = computed(() => {
  const result: Record<string, string> = {};
  if (props.width) {
    result.width = props.width;
  }
  if (props.height) {
    result.height = props.height;
  }
  return result;
});

const { isLoading, error } = useImage({ src: props.src });
</script>

<template>
  <div class="avatar-wrapper" :class="classes" :style="styles">
    <div v-if="isLoading || error" class="w-full h-full">
      <IconUserLine class="w-full h-full" />
    </div>
    <img v-else :src="src" :alt="alt" />
  </div>
</template>

<style lang="scss">
.avatar-wrapper {
  @apply inline-flex items-center justify-center overflow-hidden bg-white;

  img {
    @apply w-full h-full object-cover;
  }

  &.avatar-circle {
    @apply rounded-full;
  }

  &.avatar-square {
    @apply rounded-base;
  }

  &.avatar-xs {
    @apply w-6 h-6;
  }

  &.avatar-sm {
    @apply w-8 h-8;
  }

  &.avatar-md {
    @apply w-10 h-10;
  }

  &.avatar-lg {
    @apply w-12 h-12;
  }
}
</style>
