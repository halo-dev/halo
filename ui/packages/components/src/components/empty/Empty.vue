<script lang="ts" setup>
import Empty from "./Empty.svg";
import type { EmptySize } from "./types";

withDefaults(
  defineProps<{
    title?: string;
    message?: string;
    image?: string;
    /**
     * Accessible description of the image. Empty by default because the
     * built-in illustration is decorative.
     */
    imageAlt?: string;
    size?: EmptySize;
    /**
     * Whether the illustration is rendered. Turn it off for compact,
     * text-only empty states such as select dropdowns.
     */
    showImage?: boolean;
  }>(),
  {
    title: undefined,
    message: undefined,
    image: undefined,
    imageAlt: "",
    size: "md",
    showImage: true,
  }
);
</script>
<template>
  <div class="empty-wrapper" :class="`empty-size-${size}`" role="status">
    <div v-if="showImage" class="empty-image">
      <slot name="image">
        <img :src="image || Empty" :alt="imageAlt" />
      </slot>
    </div>
    <div v-if="title || $slots.title" class="empty-title">
      <slot name="title">
        {{ title }}
      </slot>
    </div>
    <div v-if="message || $slots.message" class="empty-message">
      <slot name="message">
        {{ message }}
      </slot>
    </div>
    <div v-if="$slots.actions" class="empty-actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>
<style lang="scss">
.empty-wrapper {
  --v-empty-image-size: 112px;
  --v-empty-spacing-y: 40px;
  --v-empty-padding-x: 40px;

  @apply flex flex-col items-center justify-center;

  margin-block: var(--v-empty-spacing-y);
  padding-inline: var(--v-empty-padding-x);

  &.empty-size-sm {
    --v-empty-image-size: 56px;
    --v-empty-spacing-y: 16px;
    --v-empty-padding-x: 16px;
  }

  .empty-image {
    @apply flex items-center justify-center;

    width: var(--v-empty-image-size);
    height: var(--v-empty-image-size);

    img {
      @apply block max-h-full max-w-full object-contain;
    }
  }

  .empty-title {
    @apply text-center text-sm font-medium text-gray-900;

    &:not(:first-child) {
      @apply mt-3;
    }
  }

  .empty-message {
    @apply mt-1.5 text-center text-xs text-gray-500;

    &:first-child {
      @apply mt-0;
    }
  }

  .empty-actions {
    @apply flex flex-row flex-wrap items-center justify-center;

    &:not(:first-child) {
      @apply mt-5;
    }
  }
}
</style>
