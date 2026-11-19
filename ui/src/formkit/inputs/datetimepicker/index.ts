import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import DatetimepickerInput from "./DatetimepickerInput.vue";

export const datetimepicker = createInput(DatetimepickerInput, {
  type: "input",
  props: [
    "placeholder",
    "format",
    "clearable",
    "weekStartsOn",
    "numberOfMonths",
    "minDate",
    "maxDate",
    "calendarLocale",
    "defaultTime",
    "hourCycle",
  ],
  features: [initialValue],
});

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    datetimepicker: {
      type: "datetimepicker";
      value?: string;
    };
  }
}
