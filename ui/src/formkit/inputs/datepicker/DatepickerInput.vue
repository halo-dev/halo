<script lang="ts" setup>
import type { FormKitFrameworkContext } from "@formkit/core";
import { VDatePickerDropdown } from "@halo-dev/components";
import type { PropType } from "vue";
import DatePickerTrigger from "./DatePickerTrigger.vue";
import { useDatePickerInput } from "./use-date-picker-input";

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
  triggerAttrs,
  onInput,
  onClear,
  onOpenChange,
} = useDatePickerInput(props.context, "date");
</script>

<template>
  <div class="w-full">
    <VDatePickerDropdown
      :model-value="value"
      mode="date"
      :locale="calendarLocale"
      :week-starts-on="weekStartsOn"
      :number-of-months="numberOfMonths"
      :min-value="minValue"
      :max-value="maxValue"
      :disabled="disabled"
      :calendar-label="$t('core.formkit.datepicker.calendar_label')"
      :panel-label="$t('core.formkit.datepicker.calendar_label')"
      @update:model-value="onInput"
      @update:open="onOpenChange"
    >
      <template #trigger="{ open }">
        <DatePickerTrigger
          :value="value"
          :display-value="displayValue"
          :placeholder="
            placeholder || $t('core.formkit.datepicker.placeholder')
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
