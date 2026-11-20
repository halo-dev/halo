<script lang="ts" setup>
import { computed } from "vue";
import {
  parseDateRange,
  serializeDateRange,
  type CalendarDateRange,
} from "./date-utils";
import PresetList from "./PresetList.vue";
import RangeCalendarView from "./RangeCalendarView.vue";
import type {
  DatePickerCommonProps,
  DateRangePanelProps,
  DateRangeValue,
} from "./types";

const props = withDefaults(
  defineProps<
    DatePickerCommonProps &
      DateRangePanelProps & { modelValue?: DateRangeValue }
  >(),
  {
    modelValue: undefined,
    locale: "en",
    weekStartsOn: undefined,
    numberOfMonths: 2,
    fixedWeeks: true,
    minValue: undefined,
    maxValue: undefined,
    isDateDisabled: undefined,
    defaultPickerValue: undefined,
    calendarLabel: "Calendar",
    disabled: false,
    readonly: false,
    maximumDays: undefined,
    allowNonContiguousRanges: undefined,
    presets: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: DateRangeValue): void;
}>();

const range = computed(() => parseDateRange(props.modelValue));

function onUpdate(value: CalendarDateRange) {
  emit("update:modelValue", serializeDateRange(value.start, value.end));
}

function onPreset(value: DateRangeValue) {
  const parsed = parseDateRange(value);
  emit("update:modelValue", serializeDateRange(parsed.start, parsed.end));
}
</script>

<template>
  <div class="flex">
    <PresetList v-if="presets?.length" :presets="presets" @select="onPreset" />
    <div class="p-3">
      <RangeCalendarView
        :model-value="range"
        :locale="locale"
        :week-starts-on="weekStartsOn"
        :number-of-months="numberOfMonths"
        :fixed-weeks="fixedWeeks"
        :min-value="minValue"
        :max-value="maxValue"
        :is-date-disabled="isDateDisabled"
        :default-picker-value="defaultPickerValue"
        :calendar-label="calendarLabel"
        :maximum-days="maximumDays"
        :allow-non-contiguous-ranges="allowNonContiguousRanges"
        :disabled="disabled"
        :readonly="readonly"
        @update:model-value="onUpdate"
      />
    </div>
  </div>
</template>
