import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import { defineAsyncComponent } from "vue";
import iconifyFeature from "./feature";
import type { IconifyFormat, IconifyValue } from "./types";

export const iconify = createInput(
  defineAsyncComponent(() => import("./IconifyInput.vue")),
  {
    type: "input",
    props: ["format", "popperPlacement"],
    features: [initialValue, iconifyFeature],
  }
);

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    iconify: {
      type: "iconify";
      value?: IconifyValue;
      format: IconifyFormat;
    };
  }
}
