<script lang="ts" setup>
withDefaults(
  defineProps<{
    label: string;
    description?: string;
  }>(),
  {
    description: undefined,
  }
);
</script>

<template>
  <div
    class="bg-white px-4 py-5 hover:bg-gray-50 sm:grid sm:grid-cols-6 sm:gap-4 sm:px-6"
  >
    <dt class="text-sm font-medium text-gray-900">{{ label }}</dt>
    <dd class="mt-1 text-sm text-gray-900 sm:col-span-3 sm:mt-0">
      <slot v-if="$slots.default" />
      <template v-else>
        {{ description }}
      </template>
    </dd>
  </div>
</template>
