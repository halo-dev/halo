import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import { defineAsyncComponent } from "vue";
import imageFeature from "./feature";

export const image = createInput(
  defineAsyncComponent(() => import("./ImageInput.vue")),
  {
    type: "input",
    props: ["multiple", "max", "width", "aspectRatio"],
    features: [initialValue, imageFeature],
  }
);

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    image: {
      type: "image";
      value?: string | string[];
    };
  }
}
