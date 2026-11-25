<script lang="ts" setup>
import { AccordionRoot } from "reka-ui";
import { computed, provide, useSlots } from "vue";
import type {
  VAccordionContext,
  VAccordionHeadingOrder,
  VAccordionType,
  VAccordionVariant,
} from "./types";
import {
  accordionContextKey,
  accordionSlotsKey,
  defaultAccordionContext,
} from "./types";

const HEADING_BY_ORDER: Record<
  VAccordionHeadingOrder,
  VAccordionContext["headerAs"]
> = {
  2: "h2",
  3: "h3",
  4: "h4",
  5: "h5",
  6: "h6",
};

const props = withDefaults(
  defineProps<{
    /**
     * `single` keeps one item open at a time, `multiple` allows several.
     * When omitted, Reka UI infers it from the model value and falls back to
     * `single`.
     */
    type?: VAccordionType;
    /**
     * Whether clicking an open item in `single` mode closes it again. Set to
     * `false` to keep one item always open.
     */
    collapsible?: boolean;
    /**
     * Controlled open item value, usually bound with `v-model`.
     */
    modelValue?: string | string[];
    /**
     * Initial open item value when used uncontrolled.
     */
    defaultValue?: string | string[];
    /**
     * Disables the accordion and all of its items.
     */
    disabled?: boolean;
    /**
     * Whether to unmount content when an item is closed. Defaults to `false`
     * so forms inside collapsed items keep their state.
     */
    unmountOnHide?: boolean;
    /**
     * Visual layout of the accordion: `default` (divided list), `bordered`
     * (card), `filled`, `separated`, or `plain` (no container chrome).
     */
    variant?: VAccordionVariant;
    /**
     * Places the chevron before or after the label.
     */
    chevronPosition?: "left" | "right";
    /**
     * Keeps the chevron fixed instead of rotating it when an item opens.
     */
    disableChevronRotation?: boolean;
    /**
     * Duration of the open/close transition and chevron rotation in
     * milliseconds. Use `0` to disable the animation.
     */
    transitionDuration?: number;
    /**
     * Heading level used to wrap item triggers. Items can override this with
     * their own `headerAs` prop. Defaults to `h3`.
     */
    order?: VAccordionHeadingOrder;
  }>(),
  {
    type: undefined,
    collapsible: false,
    modelValue: undefined,
    defaultValue: undefined,
    disabled: false,
    unmountOnHide: false,
    variant: "default",
    chevronPosition: "right",
    disableChevronRotation: false,
    transitionDuration: 200,
    order: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string | string[] | undefined): void;
}>();

const context = computed<VAccordionContext>(() => ({
  variant: props.variant,
  chevronPosition: props.chevronPosition,
  disableChevronRotation: props.disableChevronRotation,
  transitionDuration: props.transitionDuration,
  headerAs: props.order
    ? HEADING_BY_ORDER[props.order]
    : defaultAccordionContext.headerAs,
}));

provide(accordionContextKey, context);
provide(accordionSlotsKey, useSlots());

const duration = computed(() => Math.max(0, props.transitionDuration));

const classes = computed(() => [
  "v-accordion",
  `v-accordion--${props.variant}`,
  { "v-accordion--no-transition": duration.value <= 0 },
]);

const style = computed(() => ({
  "--v-accordion-transition-duration": `${duration.value}ms`,
}));
</script>

<template>
  <AccordionRoot
    :type="type"
    :collapsible="collapsible"
    :model-value="modelValue"
    :default-value="defaultValue"
    :disabled="disabled"
    :unmount-on-hide="unmountOnHide"
    :class="classes"
    :style="style"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <slot />
  </AccordionRoot>
</template>

<style lang="scss">
.v-accordion {
  @apply w-full;

  &--bordered {
    @apply overflow-hidden rounded-base bg-white;
    border: 1px solid #eaecf0;
  }
}
</style>
