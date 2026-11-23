<script lang="ts" setup>
import { computed, inject, onBeforeUnmount, onMounted } from "vue";
import { IconArrowDown } from "../../icons/icons";
import DateRangePickerDropdown from "../date-picker/DateRangePickerDropdown.vue";
import type {
  DatePickerPreset,
  DateRangePickerDropdownMode,
  DateRangeValue,
} from "../date-picker/types";
import type { VDropdownPlacement } from "../dropdown";
import { filterDropdownGroupKey } from "./group";
import { getFilterTriggerClasses } from "./trigger";
import type { FilterDropdownVariant } from "./types";

const props = withDefaults(
  defineProps<{
    /**
     * Text displayed on the trigger, for example "Created at".
     */
    label: string;
    modelValue?: DateRangeValue;
    /**
     * `date-range` closes automatically after a complete range is selected.
     * `datetime-range` commits with the confirm button.
     */
    mode?: DateRangePickerDropdownMode;
    variant?: FilterDropdownVariant;
    presets?: DatePickerPreset<DateRangeValue>[];
    disabled?: boolean;
    placement?: VDropdownPlacement;
    panelWidth?: string;
    labelSeparator?: string;
    rangeSeparator?: string;
    confirmLabel?: string;
    clearLabel?: string;
    hourCycle?: 12 | 24;
    startTimeLabel?: string;
    endTimeLabel?: string;
    locale?: string;
  }>(),
  {
    modelValue: undefined,
    mode: "date-range",
    variant: "default",
    presets: undefined,
    disabled: false,
    placement: "bottom-start",
    panelWidth: undefined,
    labelSeparator: ": ",
    rangeSeparator: " ~ ",
    confirmLabel: undefined,
    clearLabel: undefined,
    hourCycle: 24,
    startTimeLabel: undefined,
    endTimeLabel: undefined,
    locale: undefined,
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value?: DateRangeValue): void;
}>();

const group = inject(filterDropdownGroupKey);
const groupEntryId = Symbol("filter-date-range-dropdown");

const isActive = computed(
  () => !!props.modelValue?.start || !!props.modelValue?.end
);

const displayRange = computed(() => {
  const value = props.modelValue;

  if (!value?.start && !value?.end) {
    return "";
  }

  const formatValue = (part?: string) => {
    if (!part) {
      return "…";
    }

    return props.mode === "datetime-range" ? part.replace("T", " ") : part;
  };

  return `${formatValue(value.start)}${props.rangeSeparator}${formatValue(
    value.end
  )}`;
});

function clear() {
  emit("update:modelValue", undefined);
}

function onUpdate(value?: DateRangeValue) {
  emit("update:modelValue", value);
}

onMounted(() => {
  group?.register(groupEntryId, {
    isActive: () => isActive.value,
    clear,
  });
});

onBeforeUnmount(() => {
  group?.unregister(groupEntryId);
});

defineExpose({
  clear,
});
</script>

<template>
  <DateRangePickerDropdown
    :model-value="modelValue"
    :mode="mode"
    :disabled="disabled"
    :placement="placement"
    :panel-width="panelWidth"
    :panel-label="label"
    :presets="presets"
    :confirm-label="confirmLabel"
    :clear-label="clearLabel"
    :hour-cycle="hourCycle"
    :start-time-label="startTimeLabel"
    :end-time-label="endTimeLabel"
    :locale="locale"
    @update:model-value="onUpdate"
  >
    <template #trigger="{ open }">
      <button
        type="button"
        :class="
          getFilterTriggerClasses({
            selected: isActive,
            open,
            variant,
          })
        "
        :disabled="disabled"
        aria-haspopup="dialog"
        :aria-expanded="open"
      >
        <span v-if="!isActive" class="mr-0.5">
          {{ label }}
        </span>
        <span v-else class="mr-0.5">
          {{ label }}{{ labelSeparator }}{{ displayRange }}
        </span>
        <span>
          <IconArrowDown />
        </span>
      </button>
    </template>
  </DateRangePickerDropdown>
</template>
