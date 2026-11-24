<script lang="ts" setup>
import { AccordionRoot } from "reka-ui";
import type { VAccordionType, VAccordionVariant } from "./types";

withDefaults(
  defineProps<{
    /**
     * `single` keeps one item open at a time, `multiple` allows several.
     * When omitted, Reka UI infers it from the model value and falls back to
     * `single`.
     */
    type?: VAccordionType;
    /**
     * Whether clicking an open item in `single` mode closes it again.
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
     * `bordered` renders a card-like list, `plain` removes the container
     * chrome for section headers inside an existing card or form.
     */
    variant?: VAccordionVariant;
  }>(),
  {
    type: undefined,
    collapsible: false,
    modelValue: undefined,
    defaultValue: undefined,
    disabled: false,
    unmountOnHide: false,
    variant: "bordered",
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string | string[] | undefined): void;
}>();
</script>

<template>
  <AccordionRoot
    :type="type"
    :collapsible="collapsible"
    :model-value="modelValue"
    :default-value="defaultValue"
    :disabled="disabled"
    :unmount-on-hide="unmountOnHide"
    :class="['v-accordion', `v-accordion--${variant}`]"
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
