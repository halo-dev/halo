import { initialValue, type FormKitInputs } from "@formkit/inputs";
import { createInput } from "@formkit/vue";
import { defineAsyncComponent } from "vue";
import attachmentFeature from "./feature";

export const attachment = createInput(
  defineAsyncComponent(() => import("./AttachmentInput.vue")),
  {
    type: "input",
    props: ["multiple", "width", "aspectRatio", "accepts"],
    features: [initialValue, attachmentFeature],
  }
);

declare module "@formkit/inputs" {
  export interface FormKitInputProps<Props extends FormKitInputs<Props>> {
    attachment: {
      type: "attachment";
      value?: string | string[];
    };
  }
}
