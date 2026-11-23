<script lang="ts" setup>
import type { CalendarDate, DateValue } from "@internationalized/date";
import {
  RangeCalendarCell,
  RangeCalendarCellTrigger,
  RangeCalendarGrid,
  RangeCalendarGridBody,
  RangeCalendarGridHead,
  RangeCalendarGridRow,
  RangeCalendarHeadCell,
  RangeCalendarHeader,
  RangeCalendarHeading,
  RangeCalendarNext,
  RangeCalendarPrev,
  RangeCalendarRoot,
  type DateRange,
} from "reka-ui";
import { computed, shallowRef, watch } from "vue";
import { IconArrowLeft, IconArrowRight } from "../../icons/icons";
import { useComponentsI18n } from "../../locales";
import {
  parseDateString,
  resolveDefaultPickerDate,
  toCalendarDateValue,
  toDateKey,
  type CalendarDateRange,
} from "./date-utils";
import type { DatePickerCommonProps, DateRangePanelProps } from "./types";

const props = withDefaults(
  defineProps<
    DatePickerCommonProps &
      DateRangePanelProps & { modelValue?: CalendarDateRange }
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
  }
);

const { locale: componentsLocale, t } = useComponentsI18n();

const resolvedLocale = computed(() => props.locale ?? componentsLocale.value);

const calendarLabelText = computed(
  () => props.calendarLabel ?? t("date_picker.calendar")
);

const emit = defineEmits<{
  (event: "update:modelValue", value: CalendarDateRange): void;
}>();

const minDate = computed(() => parseDateString(props.minValue));
const maxDate = computed(() => parseDateString(props.maxValue));

const calendarRange = computed(() => ({
  start: props.modelValue?.start,
  end: props.modelValue?.end,
}));

const placeholder = shallowRef<CalendarDate>(
  props.modelValue?.start ??
    props.modelValue?.end ??
    resolveDefaultPickerDate(undefined, props.defaultPickerValue)
);

watch(
  () => [props.modelValue?.start, props.modelValue?.end] as const,
  ([start, end]) => {
    const focus = start ?? end;

    if (focus) {
      placeholder.value = focus;
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

function onUpdate(value: DateRange) {
  emit("update:modelValue", {
    start: toCalendarDateValue(value.start),
    end: toCalendarDateValue(value.end),
  });
}

const navButtonClasses =
  "inline-flex h-7 w-7 items-center justify-center rounded text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary disabled:cursor-not-allowed disabled:opacity-40";
const dayClasses =
  "inline-flex h-8 w-8 items-center justify-center whitespace-nowrap text-sm font-normal text-gray-700 transition-colors hover:bg-gray-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary data-[today]:font-semibold data-[outside-view]:text-gray-300 data-[disabled]:pointer-events-none data-[disabled]:text-gray-300 data-[unavailable]:text-gray-300 data-[highlighted]:rounded-none data-[highlighted]:bg-primary/10 data-[highlighted]:text-primary data-[highlighted]:hover:bg-primary/10 data-[selected]:rounded-none data-[selected]:bg-primary/10 data-[selected]:text-primary data-[selected]:hover:bg-primary/10 data-[selection-end]:rounded-r-md data-[selection-end]:bg-primary data-[selection-end]:text-white data-[selection-end]:hover:bg-primary data-[selection-start]:rounded-l-md data-[selection-start]:bg-primary data-[selection-start]:text-white data-[selection-start]:hover:bg-primary";
</script>

<template>
  <RangeCalendarRoot
    v-slot="{ grid, weekDays }"
    :model-value="calendarRange"
    :placeholder="placeholder"
    :locale="resolvedLocale"
    :week-starts-on="weekStartsOn"
    :number-of-months="numberOfMonths"
    :fixed-weeks="fixedWeeks"
    :min-value="minDate"
    :max-value="maxDate"
    :is-date-disabled="dateDisabled"
    :is-date-unavailable="dateDisabled"
    :maximum-days="maximumDays"
    :allow-non-contiguous-ranges="allowNonContiguousRanges"
    :disabled="disabled"
    :readonly="readonly"
    :initial-focus="true"
    :calendar-label="calendarLabelText"
    weekday-format="narrow"
    class="select-none"
    @update:model-value="onUpdate"
    @update:placeholder="onPlaceholderUpdate"
  >
    <RangeCalendarHeader class="mb-2 flex items-center justify-between">
      <RangeCalendarPrev
        :aria-label="t('common.previous')"
        :class="navButtonClasses"
      >
        <IconArrowLeft />
      </RangeCalendarPrev>
      <RangeCalendarHeading class="text-sm font-medium text-gray-900" />
      <RangeCalendarNext
        :aria-label="t('common.next')"
        :class="navButtonClasses"
      >
        <IconArrowRight />
      </RangeCalendarNext>
    </RangeCalendarHeader>
    <div class="flex gap-4">
      <div
        v-for="(month, monthIndex) in grid"
        :key="month.value.toString()"
        :class="monthIndex > 0 ? 'hidden sm:block' : ''"
      >
        <RangeCalendarGrid class="w-full border-collapse">
          <RangeCalendarGridHead>
            <RangeCalendarGridRow>
              <RangeCalendarHeadCell
                v-for="day in weekDays"
                :key="day"
                class="h-8 w-8 text-center text-xs font-normal text-gray-400"
              >
                {{ day }}
              </RangeCalendarHeadCell>
            </RangeCalendarGridRow>
          </RangeCalendarGridHead>
          <RangeCalendarGridBody>
            <RangeCalendarGridRow
              v-for="week in month.rows"
              :key="week[0].toString()"
            >
              <RangeCalendarCell
                v-for="day in week"
                :key="day.toString()"
                :date="day"
                class="p-0 text-center text-sm"
              >
                <RangeCalendarCellTrigger
                  :day="day"
                  :month="month.value"
                  :class="dayClasses"
                />
              </RangeCalendarCell>
            </RangeCalendarGridRow>
          </RangeCalendarGridBody>
        </RangeCalendarGrid>
      </div>
    </div>
  </RangeCalendarRoot>
</template>
