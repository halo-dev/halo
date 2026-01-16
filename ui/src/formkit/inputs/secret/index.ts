import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import { defineAsyncComponent } from "vue";
import secretFeature from "./feature";
import type { RequiredKey } from "./types";

export const secret = createInput(
  defineAsyncComponent(() => import("./SecretSelect.vue")),
  {
    type: "input",
    props: ["requiredKeys"],
    features: [initialValue, secretFeature],
  }
);

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    secret: {
      type: "secret";
      requiredKeys?: RequiredKey[];
    };
  }
}
