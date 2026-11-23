<script lang="ts" setup>
import type { CalendarDate } from "@internationalized/date";
import { computed, ref, shallowRef, watch } from "vue";
import { useComponentsI18n } from "../../locales";
import {
  normalizeTimeString,
  parseDateTimeRange,
  serializeDateTimeRange,
  type CalendarDateRange,
} from "./date-utils";
import PresetList from "./PresetList.vue";
import RangeCalendarView from "./RangeCalendarView.vue";
import TimeField from "./TimeField.vue";
import type {
  DatePickerCommonProps,
  DateRangePanelProps,
  DateRangeValue,
  DateTimeRangePanelProps,
} from "./types";

const props = withDefaults(
  defineProps<
    DatePickerCommonProps &
      DateRangePanelProps &
      DateTimeRangePanelProps & { modelValue?: DateRangeValue }
  >(),
  {
    modelValue: undefined,
    locale: undefined,
    weekStartsOn: undefined,
    numberOfMonths: 2,
    fixedWeeks: true,
    minValue: undefined,
    maxValue: undefined,
    isDateDisabled: undefined,
    defaultPickerValue: undefined,
    disabled: false,
    readonly: false,
    maximumDays: undefined,
    allowNonContiguousRanges: undefined,
    presets: undefined,
    defaultStartTime: "00:00",
    defaultEndTime: "23:59",
    hourCycle: 24,
  }
);

const { t } = useComponentsI18n();

const startTimeLabelText = computed(
  () => props.startTimeLabel ?? t("date_picker.start")
);

const endTimeLabelText = computed(
  () => props.endTimeLabel ?? t("date_picker.end")
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: DateRangeValue): void;
}>();

const initial = parseDateTimeRange(
  props.modelValue,
  props.defaultStartTime,
  props.defaultEndTime
);

const startDate = shallowRef<CalendarDate | undefined>(initial.start?.date);
const endDate = shallowRef<CalendarDate | undefined>(initial.end?.date);
const startTime = ref(
  initial.start?.time ?? normalizeTimeString(props.defaultStartTime)
);
const endTime = ref(
  initial.end?.time ?? normalizeTimeString(props.defaultEndTime)
);

watch(
  () => props.modelValue,
  (value) => {
    const parsed = parseDateTimeRange(
      value,
      props.defaultStartTime,
      props.defaultEndTime
    );

    startDate.value = parsed.start?.date;
    endDate.value = parsed.end?.date;

    if (parsed.start) {
      startTime.value = parsed.start.time;
    }

    if (parsed.end) {
      endTime.value = parsed.end.time;
    }

    if (!value) {
      startTime.value = normalizeTimeString(props.defaultStartTime);
      endTime.value = normalizeTimeString(props.defaultEndTime);
    }
  }
);

const calendarRange = computed<CalendarDateRange>(() => ({
  start: startDate.value,
  end: endDate.value,
}));

function emitValue() {
  emit(
    "update:modelValue",
    serializeDateTimeRange(
      startDate.value
        ? { date: startDate.value, time: startTime.value }
        : undefined,
      endDate.value ? { date: endDate.value, time: endTime.value } : undefined
    )
  );
}

function onRangeUpdate(value: CalendarDateRange) {
  startDate.value = value.start;
  endDate.value = value.end;

  if (!value.start) {
    startTime.value = normalizeTimeString(props.defaultStartTime);
  }

  if (!value.end) {
    endTime.value = normalizeTimeString(props.defaultEndTime);
  }

  emitValue();
}

function onStartTimeUpdate(value?: string) {
  if (!value) {
    return;
  }

  startTime.value = value;

  if (startDate.value) {
    emitValue();
  }
}

function onEndTimeUpdate(value?: string) {
  if (!value) {
    return;
  }

  endTime.value = value;

  if (endDate.value) {
    emitValue();
  }
}

function onPreset(value: DateRangeValue) {
  const parsed = parseDateTimeRange(
    value,
    props.defaultStartTime,
    props.defaultEndTime
  );

  startDate.value = parsed.start?.date;
  endDate.value = parsed.end?.date;
  startTime.value =
    parsed.start?.time ?? normalizeTimeString(props.defaultStartTime);
  endTime.value = parsed.end?.time ?? normalizeTimeString(props.defaultEndTime);

  emitValue();
}
</script>

<template>
  <div class="flex">
    <PresetList v-if="presets?.length" :presets="presets" @select="onPreset" />
    <div class="p-3">
      <RangeCalendarView
        :model-value="calendarRange"
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
        @update:model-value="onRangeUpdate"
      />
      <div class="mt-2 space-y-2 border-t border-gray-100 pt-3">
        <div class="flex items-center justify-between gap-4">
          <span class="text-sm text-gray-500">{{ startTimeLabelText }}</span>
          <TimeField
            :model-value="startTime"
            :locale="locale"
            :hour-cycle="hourCycle"
            :disabled="disabled || !startDate"
            :readonly="readonly"
            @update:model-value="onStartTimeUpdate"
          />
        </div>
        <div class="flex items-center justify-between gap-4">
          <span class="text-sm text-gray-500">{{ endTimeLabelText }}</span>
          <TimeField
            :model-value="endTime"
            :locale="locale"
            :hour-cycle="hourCycle"
            :disabled="disabled || !endDate"
            :readonly="readonly"
            @update:model-value="onEndTimeUpdate"
          />
        </div>
      </div>
    </div>
  </div>
</template>
