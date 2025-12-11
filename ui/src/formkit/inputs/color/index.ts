import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import { defineAsyncComponent } from "vue";

export const color = createInput(
  defineAsyncComponent(() => import("./ColorInput.vue")),
  {
    type: "input",
    props: ["format"],
    features: [initialValue],
  }
);

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    color: {
      type: "color";
      value?: string;
    };
  }
}
