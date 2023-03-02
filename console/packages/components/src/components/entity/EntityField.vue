<script lang="ts" setup>
import { computed } from "vue";
import type { RouteLocationRaw } from "vue-router";

const props = withDefaults(
  defineProps<{
    title?: string;
    description?: string;
    route?: RouteLocationRaw;
    width?: string | number;
  }>(),
  {
    title: undefined,
    description: undefined,
    route: undefined,
    width: undefined,
  }
);

const emit = defineEmits<{
  (event: "click"): void;
}>();

const wrapperStyles = computed(() => {
  if (props.width) {
    const width =
      typeof props.width === "string" ? props.width : `${props.width}px`;
    return {
      width,
      maxWidth: width,
    };
  }
  return {};
});
</script>

<template>
  <div class="entity-field-wrapper" :style="wrapperStyles">
    <div v-if="title || $slots.title" class="entity-field-title-body">
      <slot name="title">
        <div class="entity-field-title" @click="emit('click')">
          <RouterLink
            v-if="route"
            class="hover:text-gray-600"
            :to="route"
            :title="title"
          >
            {{ title }}
          </RouterLink>
          <span v-else :title="title">
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
        <span
          v-if="description"
          class="entity-field-description"
          :title="description"
        >
          {{ description }}
        </span>
      </slot>
    </div>
  </div>
</template>

<style lang="scss">
.entity-field-wrapper {
  @apply inline-flex flex-col gap-1 max-w-xs;

  .entity-field-title-body {
    @apply inline-flex items-center flex-row;

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
