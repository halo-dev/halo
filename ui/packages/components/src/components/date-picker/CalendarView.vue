<script lang="ts" setup>
import type { CalendarDate, DateValue } from "@internationalized/date";
import {
  CalendarCell,
  CalendarCellTrigger,
  CalendarGrid,
  CalendarGridBody,
  CalendarGridHead,
  CalendarGridRow,
  CalendarHeadCell,
  CalendarHeader,
  CalendarHeading,
  CalendarNext,
  CalendarPrev,
  CalendarRoot,
} from "reka-ui";
import { computed, shallowRef, watch } from "vue";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";
import {
  parseDateString,
  resolveDefaultPickerDate,
  toCalendarDateValue,
  toDateKey,
} from "./date-utils";
import type { DatePickerCommonProps } from "./types";

const props = withDefaults(
  defineProps<DatePickerCommonProps & { modelValue?: CalendarDate }>(),
  {
    modelValue: undefined,
    locale: "en",
    weekStartsOn: undefined,
    numberOfMonths: 1,
    fixedWeeks: true,
    minValue: undefined,
    maxValue: undefined,
    isDateDisabled: undefined,
    defaultPickerValue: undefined,
    calendarLabel: "Calendar",
    disabled: false,
    readonly: false,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: CalendarDate): void;
}>();

const minDate = computed(() => parseDateString(props.minValue));
const maxDate = computed(() => parseDateString(props.maxValue));

const placeholder = shallowRef<CalendarDate>(
  props.modelValue ??
    resolveDefaultPickerDate(undefined, props.defaultPickerValue)
);

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      placeholder.value = value;
    }
  }
);

function onPlaceholderUpdate(value: DateValue) {
  placeholder.value = toCalendarDateValue(value) as CalendarDate;
}

const dateDisabled = computed(() => {
  if (!props.isDateDisabled) {
    return undefined;
  }

  return (date: DateValue) => props.isDateDisabled?.(toDateKey(date)) ?? false;
});

function onUpdate(value: DateValue | undefined) {
  emit("update:modelValue", toCalendarDateValue(value));
}

const navButtonClasses =
  "inline-flex h-7 w-7 items-center justify-center rounded text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary disabled:cursor-not-allowed disabled:opacity-40";
const dayClasses =
  "inline-flex h-8 w-8 items-center justify-center whitespace-nowrap rounded-md text-sm font-normal text-gray-700 transition-colors hover:bg-gray-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary data-[today]:font-semibold data-[outside-view]:text-gray-300 data-[disabled]:pointer-events-none data-[disabled]:text-gray-300 data-[unavailable]:text-gray-300 data-[selected]:bg-primary data-[selected]:text-white data-[selected]:hover:bg-primary";
</script>

<template>
  <CalendarRoot
    v-slot="{ grid, weekDays }"
    :model-value="modelValue"
    :placeholder="placeholder"
    :locale="locale"
    :week-starts-on="weekStartsOn"
    :number-of-months="numberOfMonths"
    :fixed-weeks="fixedWeeks"
    :min-value="minDate"
    :max-value="maxDate"
    :is-date-disabled="dateDisabled"
    :disabled="disabled"
    :readonly="readonly"
    :prevent-deselect="true"
    :initial-focus="true"
    :calendar-label="calendarLabel"
    class="select-none"
    @update:model-value="onUpdate"
    @update:placeholder="onPlaceholderUpdate"
  >
    <CalendarHeader class="mb-2 flex items-center justify-between">
      <CalendarPrev :class="navButtonClasses">
        <IconArrowLeft />
      </CalendarPrev>
      <CalendarHeading class="text-sm font-medium text-gray-900" />
      <CalendarNext :class="navButtonClasses">
        <IconArrowRight />
      </CalendarNext>
    </CalendarHeader>
    <div class="flex gap-4">
      <div
        v-for="(month, monthIndex) in grid"
        :key="month.value.toString()"
        :class="monthIndex > 0 ? 'hidden sm:block' : ''"
      >
        <CalendarGrid class="w-full border-collapse">
          <CalendarGridHead>
            <CalendarGridRow>
              <CalendarHeadCell
                v-for="day in weekDays"
                :key="day"
                class="h-8 w-8 text-center text-xs font-normal text-gray-400"
              >
                {{ day }}
              </CalendarHeadCell>
            </CalendarGridRow>
          </CalendarGridHead>
          <CalendarGridBody>
            <CalendarGridRow
              v-for="week in month.rows"
              :key="week[0].toString()"
            >
              <CalendarCell
                v-for="day in week"
                :key="day.toString()"
                :date="day"
                class="p-0 text-center text-sm"
              >
                <CalendarCellTrigger
                  :day="day"
                  :month="month.value"
                  :class="dayClasses"
                />
              </CalendarCell>
            </CalendarGridRow>
          </CalendarGridBody>
        </CalendarGrid>
      </div>
    </div>
  </CalendarRoot>
</template>
