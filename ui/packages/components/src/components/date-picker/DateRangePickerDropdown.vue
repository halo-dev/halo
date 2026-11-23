<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import { useComponentsI18n } from "../../locales";
import type { VDropdownPlacement } from "../dropdown";
import DateRangePickerPanel from "./DateRangePickerPanel.vue";
import DateTimeRangePickerPanel from "./DateTimeRangePickerPanel.vue";
import DropdownShell from "./DropdownShell.vue";
import type {
  DatePickerPreset,
  DatePickerWeekStart,
  DateRangePickerDropdownMode,
  DateRangeValue,
} from "./types";

const props = withDefaults(
  defineProps<{
    mode?: DateRangePickerDropdownMode;
    modelValue?: DateRangeValue;
    disabled?: boolean;
    placement?: VDropdownPlacement;
    panelWidth?: string;
    panelLabel?: string;
    open?: boolean;
    confirmLabel?: string;
    clearLabel?: string;
    locale?: string;
    weekStartsOn?: DatePickerWeekStart;
    numberOfMonths?: number;
    fixedWeeks?: boolean;
    minValue?: string;
    maxValue?: string;
    isDateDisabled?: (date: string) => boolean;
    defaultPickerValue?: string;
    calendarLabel?: string;
    presets?: DatePickerPreset<DateRangeValue>[];
    maximumDays?: number;
    allowNonContiguousRanges?: boolean;
    defaultStartTime?: string;
    defaultEndTime?: string;
    hourCycle?: 12 | 24;
    startTimeLabel?: string;
    endTimeLabel?: string;
  }>(),
  {
    mode: "date-range",
    modelValue: undefined,
    disabled: false,
    placement: "bottom-start",
    panelWidth: undefined,
    panelLabel: undefined,
    open: undefined,
    confirmLabel: undefined,
    clearLabel: undefined,
    locale: undefined,
    weekStartsOn: undefined,
    numberOfMonths: 2,
    fixedWeeks: true,
    minValue: undefined,
    maxValue: undefined,
    isDateDisabled: undefined,
    defaultPickerValue: undefined,
    calendarLabel: undefined,
    presets: undefined,
    maximumDays: undefined,
    allowNonContiguousRanges: undefined,
    defaultStartTime: "00:00",
    defaultEndTime: "23:59",
    hourCycle: 24,
    startTimeLabel: undefined,
    endTimeLabel: undefined,
  }
);

const { t } = useComponentsI18n();

const panelLabelText = computed(
  () => props.panelLabel ?? t("date_picker.range_panel")
);

const confirmLabelText = computed(
  () => props.confirmLabel ?? t("common.confirm")
);

const clearLabelText = computed(() => props.clearLabel ?? t("common.clear"));

const startTimeLabelText = computed(
  () => props.startTimeLabel ?? t("date_picker.start")
);

const endTimeLabelText = computed(
  () => props.endTimeLabel ?? t("date_picker.end")
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: DateRangeValue): void;
  (event: "update:open", value: boolean): void;
  (event: "select", value: DateRangeValue): void;
}>();

const shell = ref<InstanceType<typeof DropdownShell>>();

function cloneValue(value?: DateRangeValue): DateRangeValue | undefined {
  return value ? { ...value } : undefined;
}

const draft = ref<DateRangeValue | undefined>(cloneValue(props.modelValue));

watch(
  () => props.modelValue,
  (value) => {
    draft.value = cloneValue(value);
  }
);

const hasPartialDraft = computed(() => {
  const start = !!draft.value?.start;
  const end = !!draft.value?.end;
  return (start && !end) || (!start && end);
});

function onOpenChange(open: boolean) {
  if (!open) {
    draft.value = cloneValue(props.modelValue);
  }

  emit("update:open", open);
}

function commit(value?: DateRangeValue) {
  emit("update:modelValue", value);

  if (value?.start && value?.end) {
    emit("select", value);
  }
}

function onPanelUpdate(value?: DateRangeValue) {
  draft.value = value;

  if (props.mode !== "date-range") {
    return;
  }

  if (value?.start && value?.end) {
    commit(value);
    shell.value?.hide();
  }
}

function onConfirm() {
  commit(draft.value);
  shell.value?.hide();
}

function onDraftClear() {
  draft.value = undefined;
}

const footerButtonClasses =
  "rounded py-1 text-sm text-gray-500 transition-colors hover:text-gray-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary";
</script>

<template>
  <DropdownShell
    ref="shell"
    :open="open"
    :disabled="disabled"
    :placement="placement"
    :panel-width="panelWidth"
    :panel-label="panelLabelText"
    @update:open="onOpenChange"
  >
    <template #default="{ open: isOpen, show, hide }">
      <slot name="trigger" :open="isOpen" :show="show" :hide="hide" />
    </template>
    <template #panel>
      <DateRangePickerPanel
        v-if="mode === 'date-range'"
        :model-value="draft"
        :locale="locale"
        :week-starts-on="weekStartsOn"
        :number-of-months="numberOfMonths"
        :fixed-weeks="fixedWeeks"
        :min-value="minValue"
        :max-value="maxValue"
        :is-date-disabled="isDateDisabled"
        :default-picker-value="defaultPickerValue"
        :calendar-label="calendarLabel"
        :presets="presets"
        :maximum-days="maximumDays"
        :allow-non-contiguous-ranges="allowNonContiguousRanges"
        :disabled="disabled"
        @update:model-value="onPanelUpdate"
      />
      <DateTimeRangePickerPanel
        v-else
        :model-value="draft"
        :locale="locale"
        :week-starts-on="weekStartsOn"
        :number-of-months="numberOfMonths"
        :fixed-weeks="fixedWeeks"
        :min-value="minValue"
        :max-value="maxValue"
        :is-date-disabled="isDateDisabled"
        :default-picker-value="defaultPickerValue"
        :calendar-label="calendarLabel"
        :presets="presets"
        :maximum-days="maximumDays"
        :allow-non-contiguous-ranges="allowNonContiguousRanges"
        :default-start-time="defaultStartTime"
        :default-end-time="defaultEndTime"
        :hour-cycle="hourCycle"
        :start-time-label="startTimeLabelText"
        :end-time-label="endTimeLabelText"
        :disabled="disabled"
        @update:model-value="onPanelUpdate"
      />
      <div
        v-if="mode === 'datetime-range'"
        class="flex items-center justify-between gap-2 border-t border-gray-100 px-3 py-2"
      >
        <button
          type="button"
          :class="footerButtonClasses"
          @click="onDraftClear"
        >
          {{ clearLabelText }}
        </button>
        <button
          type="button"
          class="rounded bg-primary px-3 py-1.5 text-sm font-medium text-white transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary disabled:cursor-not-allowed disabled:opacity-60"
          :disabled="hasPartialDraft"
          @click="onConfirm"
        >
          {{ confirmLabelText }}
        </button>
      </div>
    </template>
  </DropdownShell>
</template>
