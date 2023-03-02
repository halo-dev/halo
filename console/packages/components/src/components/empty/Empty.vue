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
  @apply flex
  flex-col
  items-center
  justify-center
  my-10
  px-10;

  .empty-title {
    @apply text-sm
    text-gray-900
    text-center
    font-medium;
  }

  .empty-message {
    @apply text-gray-500
    text-xs
    text-center
    mt-1.5;
  }

  .empty-actions {
    @apply flex
    flex-row
    mt-5;
  }
}
</style>
