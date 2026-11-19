import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import DatepickerInput from "./DatepickerInput.vue";

export const datepicker = createInput(DatepickerInput, {
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
  ],
  features: [initialValue],
});

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    datepicker: {
      type: "datepicker";
      value?: string;
    };
  }
}
