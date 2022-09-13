<script lang="ts" setup>
import type { RouteLocationRaw } from "vue-router";

withDefaults(
  defineProps<{
    title?: string;
    description?: string;
    route?: RouteLocationRaw;
  }>(),
  {
    title: undefined,
    description: undefined,
    route: undefined,
  }
);

const emit = defineEmits<{
  (event: "click"): void;
}>();
</script>

<template>
  <div class="inline-flex flex-col gap-1">
    <div
      v-if="title || $slots.title"
      class="inline-flex flex-col items-center sm:flex-row"
    >
      <slot name="title">
        <div
          class="mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2"
          @click="emit('click')"
        >
          <RouterLink v-if="route" :to="route">
            {{ title }}
          </RouterLink>
          <span v-else>
            {{ title }}
          </span>
        </div>
        <slot name="extra" />
      </slot>
    </div>
    <div
      v-if="description || $slots.description"
      class="inline-flex items-center"
    >
      <slot name="description">
        <span v-if="description" class="text-xs text-gray-500">
          {{ description }}
        </span>
      </slot>
    </div>
  </div>
</template>
