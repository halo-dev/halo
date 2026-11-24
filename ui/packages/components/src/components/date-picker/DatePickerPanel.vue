<script lang="ts" setup>
import type { CalendarDate } from "@internationalized/date";
import { computed } from "vue";
import CalendarView from "./CalendarView.vue";
import { formatDateString, parseDateString } from "./date-utils";
import type { DatePickerCommonProps } from "./types";

const props = withDefaults(
  defineProps<DatePickerCommonProps & { modelValue?: string }>(),
  {
    modelValue: undefined,
    locale: undefined,
    weekStartsOn: undefined,
    numberOfMonths: 1,
    fixedWeeks: true,
    minValue: undefined,
    maxValue: undefined,
    isDateDisabled: undefined,
    defaultPickerValue: undefined,
    disabled: false,
    readonly: false,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
}>();

const selectedDate = computed(() => parseDateString(props.modelValue));

function onUpdate(value?: CalendarDate) {
  emit("update:modelValue", formatDateString(value));
}
</script>

<template>
  <div class="p-3">
    <CalendarView
      :model-value="selectedDate"
      :locale="locale"
      :week-starts-on="weekStartsOn"
      :number-of-months="numberOfMonths"
      :fixed-weeks="fixedWeeks"
      :min-value="minValue"
      :max-value="maxValue"
      :is-date-disabled="isDateDisabled"
      :default-picker-value="defaultPickerValue"
      :calendar-label="calendarLabel"
      :disabled="disabled"
      :readonly="readonly"
      @update:model-value="onUpdate"
    />
  </div>
</template>
