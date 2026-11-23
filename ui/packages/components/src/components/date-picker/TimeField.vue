<script lang="ts" setup>
import { TimeFieldInput, TimeFieldRoot, type TimeValue } from "reka-ui";
import { computed } from "vue";
import { useComponentsI18n } from "../../locales";
import { formatTimeString, resolveTime } from "./date-utils";

const props = withDefaults(
  defineProps<{
    modelValue?: string;
    locale?: string;
    hourCycle?: 12 | 24;
    disabled?: boolean;
    readonly?: boolean;
  }>(),
  {
    modelValue: undefined,
    locale: undefined,
    hourCycle: 24,
    disabled: false,
    readonly: false,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
}>();

const { locale: componentsLocale } = useComponentsI18n();

const resolvedLocale = computed(() => props.locale ?? componentsLocale.value);

const timeValue = computed(() => resolveTime(props.modelValue));

function onUpdate(value?: TimeValue) {
  emit("update:modelValue", formatTimeString(value));
}

const segmentClasses =
  "inline-flex min-w-5 items-center justify-center rounded px-0.5 text-center outline-none transition-colors focus:bg-primary/10 data-[disabled]:text-gray-400";
</script>

<template>
  <div
    class="inline-flex h-8 items-center rounded-md border border-gray-300 bg-white px-2 text-sm transition-colors focus-within:border-primary"
    :class="disabled ? 'cursor-not-allowed bg-gray-50 opacity-60' : ''"
  >
    <TimeFieldRoot
      v-slot="{ segments }"
      :model-value="timeValue"
      :locale="resolvedLocale"
      :hour-cycle="hourCycle"
      granularity="minute"
      :disabled="disabled"
      :readonly="readonly"
      class="inline-flex items-center"
      @update:model-value="onUpdate"
    >
      <TimeFieldInput
        v-for="(segment, index) in segments"
        :key="`${segment.part}-${index}`"
        :part="segment.part"
        :class="
          segment.part === 'literal' ? 'px-0.5 text-gray-400' : segmentClasses
        "
      >
        {{ segment.value }}
      </TimeFieldInput>
    </TimeFieldRoot>
  </div>
</template>
