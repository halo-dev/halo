import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import { defineAsyncComponent } from "vue";

export const iconify = createInput(
  defineAsyncComponent(() => import("./IconifyInput.vue")),
  {
    type: "input",
    props: ["format"],
    features: [initialValue],
  }
);

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    iconify: {
      type: "iconify";
      value?: string;
    };
  }
}
