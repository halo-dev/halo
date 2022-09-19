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
  <div class="entity-field-wrapper">
    <div v-if="title || $slots.title" class="entity-field-title-body">
      <slot name="title">
        <div class="entity-field-title" @click="emit('click')">
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
      class="entity-field-description-body"
    >
      <slot name="description">
        <span v-if="description" class="entity-field-description">
          {{ description }}
        </span>
      </slot>
    </div>
  </div>
</template>

<style lang="scss">
.entity-field-wrapper {
  @apply inline-flex flex-col gap-1;

  .entity-field-title-body {
    @apply inline-flex flex-col items-center sm:flex-row;

    .entity-field-title {
      @apply mr-0 truncate text-sm font-medium text-gray-900 sm:mr-2;
    }
  }

  .entity-field-description-body {
    @apply inline-flex items-center;

    .entity-field-description {
      @apply text-xs text-gray-500 truncate;
    }
  }
}
</style>
