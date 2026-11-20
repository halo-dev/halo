<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { VDatePickerDropdown } from "@halo-dev/components";
import type { PropType } from "vue";
import DatePickerTrigger from "../datepicker/DatePickerTrigger.vue";
import { useDatePickerInput } from "../datepicker/use-date-picker-input";

const props = defineProps({
  context: {
    type: Object as PropType<FormKitFrameworkContext>,
    required: true,
  },
});

const {
  value,
  disabled,
  placeholder,
  calendarLocale,
  weekStartsOn,
  numberOfMonths,
  minValue,
  maxValue,
  clearable,
  displayValue,
  defaultTime,
  hourCycle,
  triggerAttrs,
  onInput,
  onClear,
  onOpenChange,
} = useDatePickerInput(props.context, "datetime");
</script>

<template>
  <div class="w-full">
    <VDatePickerDropdown
      :model-value="value"
      mode="datetime"
      :locale="calendarLocale"
      :week-starts-on="weekStartsOn"
      :number-of-months="numberOfMonths"
      :min-value="minValue"
      :max-value="maxValue"
      :default-time="defaultTime"
      :hour-cycle="hourCycle"
      :time-label="$t('core.formkit.datetimepicker.time')"
      :calendar-label="$t('core.formkit.datepicker.calendar_label')"
      :panel-label="$t('core.formkit.datepicker.calendar_label')"
      :clear-label="$t('core.common.buttons.clear')"
      :confirm-label="$t('core.common.buttons.confirm')"
      :disabled="disabled"
      @update:model-value="onInput"
      @update:open="onOpenChange"
    >
      <template #trigger="{ open }">
        <DatePickerTrigger
          :value="value"
          :display-value="displayValue"
          :placeholder="
            placeholder || $t('core.formkit.datetimepicker.placeholder')
          "
          :clearable="clearable"
          :clear-label="$t('core.common.buttons.clear')"
          :disabled="disabled"
          :open="open"
          :trigger-attrs="triggerAttrs"
          :input-class="context.classes.input"
          @clear="onClear"
        />
      </template>
    </VDatePickerDropdown>
  </div>
</template>
