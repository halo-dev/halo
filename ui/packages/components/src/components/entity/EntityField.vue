<script lang="ts" setup>
import { computed, getCurrentInstance, type CSSProperties } from "vue";
import type { RouteLocationRaw } from "vue-router";

const props = withDefaults(
  defineProps<{
    /** Main line of the field; ignored when the `title` slot is used. */
    title?: string;
    /** Secondary line below the title; ignored when the `description` slot is used. */
    description?: string;
    /** Renders the title as a link to this route. */
    route?: RouteLocationRaw;
    /** Fixed width of the field. Numbers are treated as pixels. */
    width?: string | number;
    /** Maximum width of the field. Numbers are treated as pixels. */
    maxWidth?: string | number;
  }>(),
  {
    title: undefined,
    description: undefined,
    route: undefined,
    width: undefined,
    maxWidth: undefined,
  }
);

const emit = defineEmits<{
  (event: "click"): void;
}>();

// `click` is a declared emit, so a parent's listener is not in `attrs`.
// Read it from the raw vnode props to make the title keyboard operable only
// when the consumer actually made it clickable.
const hasClickListener = Boolean(getCurrentInstance()?.vnode.props?.onClick);
// A routed title already contains a focusable link; adding button semantics
// around it would nest interactive controls.
const clickable = computed(() => hasClickListener && !props.route);

const wrapperStyles = computed(() => {
  const styles: CSSProperties = {};
  if (props.width !== undefined) {
    styles.width = toCssLength(props.width);
  }
  if (props.maxWidth !== undefined) {
    styles.maxWidth = toCssLength(props.maxWidth);
  }
  return styles;
});

function toCssLength(value: string | number) {
  return typeof value === "number" ? `${value}px` : value;
}

function handleTitleKeydown(event: KeyboardEvent) {
  if (!clickable.value) {
    return;
  }
  if (event.key !== "Enter" && event.key !== " ") {
    return;
  }
  event.preventDefault();
  emit("click");
}
</script>

<template>
  <div class="entity-field-wrapper" :style="wrapperStyles">
    <div v-if="title || $slots.title" class="entity-field-title-body">
      <slot name="title">
        <div
          class="entity-field-title"
          :class="{ 'entity-field-title-clickable': clickable }"
          :role="clickable ? 'button' : undefined"
          :tabindex="clickable ? 0 : undefined"
          @click="emit('click')"
          @keydown="handleTitleKeydown"
        >
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
      </slot>
      <slot name="extra" />
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
  @apply inline-flex max-w-xs flex-col gap-1;

  .entity-field-title-body {
    @apply inline-flex flex-row items-center gap-2 whitespace-nowrap;

    .entity-field-title {
      @apply truncate text-sm font-medium text-gray-900;

      &.entity-field-title-clickable {
        @apply cursor-pointer rounded-sm;
      }

      &:focus-visible,
      &:has(:focus-visible) {
        @apply rounded-sm outline-none ring-2 ring-primary;
      }
    }
  }

  .entity-field-description-body {
    @apply inline-flex items-center whitespace-nowrap;

    .entity-field-description {
      @apply truncate text-xs text-gray-500;
    }
  }
}
</style>
