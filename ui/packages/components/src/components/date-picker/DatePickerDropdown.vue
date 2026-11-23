<script lang="ts" setup>
import { computed, ref, watch } from "vue";
import { useComponentsI18n } from "../../locales";
import type { VDropdownPlacement } from "../dropdown";
import DatePickerPanel from "./DatePickerPanel.vue";
import DateTimePickerPanel from "./DateTimePickerPanel.vue";
import DropdownShell from "./DropdownShell.vue";
import type { DatePickerDropdownMode, DatePickerWeekStart } from "./types";

const props = withDefaults(
  defineProps<{
    /**
     * Panel rendered in the dropdown.
     */
    mode?: DatePickerDropdownMode;
    /**
     * Selected value: `YYYY-MM-DD` in date mode, `YYYY-MM-DDTHH:mm` in
     * datetime mode.
     */
    modelValue?: string;
    disabled?: boolean;
    placement?: VDropdownPlacement;
    panelWidth?: string;
    panelLabel?: string;
    /**
     * Controlled open state, bindable with `v-model:open`.
     */
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
    defaultTime?: string;
    hourCycle?: 12 | 24;
    timeLabel?: string;
  }>(),
  {
    mode: "date",
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
    numberOfMonths: 1,
    fixedWeeks: true,
    minValue: undefined,
    maxValue: undefined,
    isDateDisabled: undefined,
    defaultPickerValue: undefined,
    calendarLabel: undefined,
    defaultTime: "00:00",
    hourCycle: 24,
    timeLabel: undefined,
  }
);

const { t } = useComponentsI18n();

const panelLabelText = computed(
  () => props.panelLabel ?? t("date_picker.panel")
);

const confirmLabelText = computed(
  () => props.confirmLabel ?? t("common.confirm")
);

const clearLabelText = computed(() => props.clearLabel ?? t("common.clear"));

const emit = defineEmits<{
  (event: "update:modelValue", value?: string): void;
  (event: "update:open", value: boolean): void;
  (event: "select", value: string): void;
}>();

const shell = ref<InstanceType<typeof DropdownShell>>();
const draft = ref<string | undefined>(props.modelValue);

watch(
  () => props.modelValue,
  (value) => {
    draft.value = value;
  }
);

function onOpenChange(open: boolean) {
  if (!open) {
    draft.value = props.modelValue;
  }

  emit("update:open", open);
}

function onPanelUpdate(value?: string) {
  draft.value = value;

  if (props.mode !== "date") {
    return;
  }

  emit("update:modelValue", value);

  if (value) {
    emit("select", value);
  }

  shell.value?.hide();
}

function onConfirm() {
  emit("update:modelValue", draft.value);

  if (draft.value) {
    emit("select", draft.value);
  }

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
      <DatePickerPanel
        v-if="mode === 'date'"
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
        :disabled="disabled"
        @update:model-value="onPanelUpdate"
      />
      <DateTimePickerPanel
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
        :default-time="defaultTime"
        :hour-cycle="hourCycle"
        :time-label="timeLabel"
        :disabled="disabled"
        @update:model-value="onPanelUpdate"
      />
      <div
        v-if="mode === 'datetime'"
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
          class="rounded bg-primary px-3 py-1.5 text-sm font-medium text-white transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary"
          @click="onConfirm"
        >
          {{ confirmLabelText }}
        </button>
      </div>
    </template>
  </DropdownShell>
</template>
