<script lang="ts" setup>
withDefaults(
  defineProps<{
    label: string;
    content?: string;
    verticalCenter?: boolean;
  }>(),
  {
    content: undefined,
    verticalCenter: false,
  }
);
</script>

<template>
  <div
    class="description-item-wrapper"
    :class="{ 'items-center': verticalCenter }"
  >
    <dt class="description-item__label">{{ label }}</dt>
    <dd class="description-item__content">
      <slot v-if="$slots.default" />
      <template v-else>
        {{ content }}
      </template>
    </dd>
  </div>
</template>

<style lang="scss">
.description-item-wrapper {
  @apply bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6;

  .description-item__label {
    @apply text-sm font-medium text-gray-900;
  }

  .description-item__content {
    @apply mt-1 text-sm text-gray-900 sm:col-span-6 md:col-span-5 lg:col-span-3 sm:mt-0;
  }
}
</style>
