<script lang="ts" setup>
import { IconCalendar, IconClose } from "@halo-dev/components";
import { computed } from "vue";

const props = withDefaults(
  defineProps<{
    value?: string;
    displayValue?: string;
    placeholder?: string;
    clearLabel?: string;
    clearable?: boolean;
    disabled?: boolean;
    open?: boolean;
    triggerAttrs?: Record<string, unknown>;
    inputClass?: string;
  }>(),
  {
    value: undefined,
    displayValue: undefined,
    placeholder: undefined,
    clearLabel: "Clear",
    clearable: true,
    disabled: false,
    open: false,
    triggerAttrs: undefined,
    inputClass: undefined,
  }
);

const emit = defineEmits<{
  (event: "clear"): void;
}>();

const hasValue = computed(() => !!props.value);

const canClear = computed(
  () => hasValue.value && props.clearable && !props.disabled
);
</script>

<template>
  <div class="relative w-full">
    <button
      type="button"
      v-bind="triggerAttrs"
      :class="[
        inputClass,
        'flex h-full w-full items-center justify-between gap-2 bg-transparent pr-3 text-left text-sm text-gray-900 transition-colors focus:outline-none disabled:cursor-not-allowed disabled:text-gray-400',
      ]"
      :disabled="disabled"
      aria-haspopup="dialog"
      :aria-expanded="open"
    >
      <span
        class="truncate"
        :class="{ 'text-gray-400': !hasValue, 'pr-5': canClear }"
      >
        {{ displayValue ?? placeholder }}
      </span>
      <span
        class="pointer-events-none flex h-4 w-4 shrink-0 items-center justify-center text-gray-400"
      >
        <IconCalendar />
      </span>
    </button>
    <button
      v-if="canClear"
      type="button"
      class="absolute inset-y-0 right-8 my-auto flex h-4 w-4 items-center justify-center rounded text-gray-400 transition-colors hover:text-gray-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
      :aria-label="clearLabel"
      @click.stop="emit('clear')"
    >
      <IconClose />
    </button>
  </div>
</template>
