<script lang="ts" setup>
import { computed, onMounted, ref } from "vue";
import { IconErrorWarning } from "../../icons/icons";
import type { Size } from "./interface";

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
    size: "md",
    width: undefined,
    height: undefined,
    circle: false,
  }
);

const isLoading = ref(false);
const error = ref(false);

const loadImage = async () => {
  if (!props.src) {
    return Promise.reject();
  }

  const image = new Image();
  image.src = props.src;
  return new Promise((resolve, reject) => {
    image.onload = () => resolve(image);
    image.onerror = (err) => reject(err);
  });
};

onMounted(async () => {
  if (!props.src) {
    error.value = true;
    return;
  }

  isLoading.value = true;
  try {
    await loadImage();
  } catch (e) {
    error.value = true;
  } finally {
    isLoading.value = false;
  }
});

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

const placeholderText = computed(() => {
  if (!props.alt) {
    return undefined;
  }
  const words = props.alt.split(" ");
  if (words.length === 1) {
    return words[0].charAt(0).toUpperCase();
  }
  if (words.length > 1) {
    return words[0].charAt(0).toUpperCase() + words[1].charAt(0).toUpperCase();
  }
  return undefined;
});
</script>

<template>
  <div class="avatar-wrapper" :class="classes" :style="styles">
    <div v-if="isLoading || error" class="avatar-fallback">
      <svg
        v-if="isLoading"
        class="avatar-loading"
        fill="none"
        viewBox="0 0 24 24"
        xmlns="http://www.w3.org/2000/svg"
      >
        <circle
          class="opacity-25"
          cx="12"
          cy="12"
          r="10"
          stroke="currentColor"
          stroke-width="4"
        ></circle>
        <path
          class="opacity-75"
          d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
          fill="currentColor"
        ></path>
      </svg>
      <span v-else-if="placeholderText" class="avatar-placeholder">
        {{ placeholderText }}
      </span>
      <IconErrorWarning v-else class="avatar-error" />
    </div>
    <img v-else :src="src" :alt="alt" />
  </div>
</template>

<style lang="scss">
.avatar-wrapper {
  @apply inline-flex items-center justify-center overflow-hidden bg-gray-100;

  img {
    @apply w-full h-full object-cover;
  }

  .avatar-fallback {
    @apply w-full h-full flex items-center justify-center;
  }

  .avatar-loading {
    @apply animate-spin w-5 h-5;
  }

  .avatar-placeholder {
    @apply text-sm text-gray-800 font-medium;
  }

  .avatar-error {
    @apply w-5 h-5 text-red-500;
  }

  &.avatar-circle {
    @apply rounded-full;
  }

  &.avatar-square {
    @apply rounded-base;
  }

  &.avatar-xs {
    @apply w-6 h-6;

    .avatar-placeholder {
      @apply text-xs;
    }
  }

  &.avatar-sm {
    @apply w-8 h-8;

    .avatar-placeholder {
      @apply text-xs;
    }
  }

  &.avatar-md {
    @apply w-10 h-10;
  }

  &.avatar-lg {
    @apply w-12 h-12;
  }
}
</style>
