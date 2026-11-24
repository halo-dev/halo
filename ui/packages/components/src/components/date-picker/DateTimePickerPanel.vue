<script lang="ts" setup>
import type { CalendarDate } from "@internationalized/date";
import { computed, ref, shallowRef, watch } from "vue";
import { useComponentsI18n } from "../../locales";
import CalendarView from "./CalendarView.vue";
import {
  combineDateTime,
  formatDateTimeString,
  normalizeTimeString,
  splitDateTime,
} from "./date-utils";
import TimeField from "./TimeField.vue";
import type { DatePickerCommonProps, DateTimePanelProps } from "./types";

const props = withDefaults(
  defineProps<
    DatePickerCommonProps & DateTimePanelProps & { modelValue?: string }
  >(),
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
    defaultTime: "00:00",
    hourCycle: 24,
  }
);

const { t } = useComponentsI18n();

const timeLabelText = computed(() => props.timeLabel ?? t("date_picker.time"));

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
}>();

const initial = splitDateTime(props.modelValue, props.defaultTime);

const selectedDate = shallowRef<CalendarDate | undefined>(initial?.date);
const selectedTime = ref(
  initial?.time ?? normalizeTimeString(props.defaultTime)
);

watch(
  () => props.modelValue,
  (value) => {
    const parts = splitDateTime(value, props.defaultTime);
    selectedDate.value = parts?.date;
    selectedTime.value = parts?.time ?? normalizeTimeString(props.defaultTime);
  }
);

function emitValue() {
  if (!selectedDate.value) {
    emit("update:modelValue", undefined);
    return;
  }

  emit(
    "update:modelValue",
    formatDateTimeString(
      combineDateTime(selectedDate.value, selectedTime.value)
    )
  );
}

function onDateUpdate(value?: CalendarDate) {
  selectedDate.value = value;
  emitValue();
}

function onTimeUpdate(value?: string) {
  if (!value) {
    return;
  }

  selectedTime.value = value;

  if (selectedDate.value) {
    emitValue();
  }
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
      @update:model-value="onDateUpdate"
    />
    <div
      class="mt-2 flex items-center justify-between gap-4 border-t border-gray-100 pt-3"
    >
      <span class="text-sm text-gray-500">{{ timeLabelText }}</span>
      <TimeField
        :model-value="selectedTime"
        :locale="locale"
        :hour-cycle="hourCycle"
        :disabled="disabled"
        :readonly="readonly"
        @update:model-value="onTimeUpdate"
      />
    </div>
  </div>
</template>
