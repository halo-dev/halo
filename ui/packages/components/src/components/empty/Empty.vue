<script lang="ts" setup>
import Empty from "./Empty.svg";

defineProps<{
  title?: string;
  message?: string;
  image?: string;
}>();
</script>
<template>
  <div class="empty-wrapper">
    <div class="empty-image h-32 w-32">
      <slot name="image">
        <img :src="image || Empty" alt="Empty" />
      </slot>
    </div>
    <div class="empty-title">{{ title }}</div>
    <div class="empty-message">
      <slot name="message">
        {{ message }}
      </slot>
    </div>
    <div class="empty-actions">
      <slot name="actions"></slot>
    </div>
  </div>
</template>
<style lang="scss">
.empty-wrapper {
  @apply my-10 flex flex-col items-center justify-center px-10;

  .empty-title {
    @apply text-center text-sm font-medium text-gray-900;
  }

  .empty-message {
    @apply mt-1.5 text-center text-xs text-gray-500;
  }

  .empty-actions {
    @apply mt-5 flex flex-row;
  }
}
</style>
