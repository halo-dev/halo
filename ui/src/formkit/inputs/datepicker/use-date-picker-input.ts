import type { FormKitFrameworkContext } from "@formkit/core";
import type { DatePickerWeekStart } from "@halo-dev/components";
import { computed } from "vue";
import { useI18n } from "vue-i18n";

export function useDatePickerInput(
  context: FormKitFrameworkContext,
  mode: "date" | "datetime"
) {
  const { locale } = useI18n();

  const value = computed(
    () => (context._value as string | undefined) || undefined
  );

  const disabled = computed(() => !!context.disabled);

  const placeholder = computed(() => context.placeholder as string | undefined);

  const calendarLocale = computed(
    () => (context.calendarLocale as string | undefined) || locale.value
  );

  const weekStartsOn = computed(
    () => context.weekStartsOn as DatePickerWeekStart | undefined
  );

  const numberOfMonths = computed(
    () => context.numberOfMonths as number | undefined
  );

  const minValue = computed(() => context.minDate as string | undefined);

  const maxValue = computed(() => context.maxDate as string | undefined);

  const format = computed(
    () => context.format as ((value: string) => string) | undefined
  );

  const clearable = computed(() => context.clearable as boolean | undefined);

  const displayValue = computed(() => {
    const current = value.value;

    if (!current) {
      return undefined;
    }

    if (format.value) {
      return format.value(current);
    }

    return mode === "datetime" ? current.replace("T", " ") : current;
  });

  const defaultTime = computed(() => context.defaultTime as string | undefined);

  const hourCycle = computed(() => context.hourCycle as 12 | 24 | undefined);

  const triggerAttrs = computed<Record<string, unknown>>(() => ({
    id: context.id,
    "aria-describedby": context.describedBy as string | undefined,
    "aria-required": context.state.required || undefined,
  }));

  function onInput(next?: string) {
    context.node.input(next);
  }

  function onClear() {
    context.node.input(undefined);
  }

  function onOpenChange(open: boolean) {
    if (!open) {
      context.handlers.blur();
    }
  }

  return {
    value,
    disabled,
    placeholder,
    calendarLocale,
    weekStartsOn,
    numberOfMonths,
    minValue,
    maxValue,
    format,
    clearable,
    displayValue,
    defaultTime,
    hourCycle,
    triggerAttrs,
    onInput,
    onClear,
    onOpenChange,
  };
}
