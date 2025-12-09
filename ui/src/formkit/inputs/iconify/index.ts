import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import IconifyInput from "./IconifyInput.vue";

export const iconify = createInput(IconifyInput, {
  type: "input",
  props: ["format"],
  features: [initialValue],
});

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    iconify: {
      type: "iconify";
      value?: string;
    };
  }
}
